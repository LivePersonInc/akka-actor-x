package com.liveperson.infra.common.akka.actorx.demo;

import akka.actor.*;
import com.liveperson.infra.common.akka.actorx.demo.deck.Deck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author Amit Tal
 * @since 9/18/2014
 */
public class DealerAbstractFsm extends AbstractFSM<DealerAbstractFsm.State, String> {

    private Logger logger = LoggerFactory.getLogger(DealerAbstractFsm.class);

    public static enum START_GAME {
        INSTANCE
    }

    public static enum HIT {
        INSTANCE
    }

    public static class STAND {
        Integer hand;
        public STAND(Integer hand) {
            this.hand = hand;
        }
    }

    public static enum BUST {
        INSTANCE
    }

    public static class GAME_ENDED {

        String winner;
        Integer hand;

        public GAME_ENDED(String winner, Integer hand) {
            this.winner = winner;
            this.hand = hand;
        }

        public String getWinner() {
            return winner;
        }

        public Integer getHand() {
            return hand;
        }
    }

    enum State {
        START,
        DEAL_CARDS,
        END
    }



    public static Props props(Deck[] decks, int numPlayers) {
        return Props.create(DealerAbstractFsm.class, decks, numPlayers);
    }

    private Deck[] decks;
    private Integer numPlayers;
    private int numOfPlayersToPlay;
    private Set<ActorRef> players;
    private Map<ActorRef, Integer> stoppedPlayers;
    private ActorRef originalSender;
    private GAME_ENDED gameEndedMessage;

    public DealerAbstractFsm(Deck[] decks, Integer numPlayers) {

        this.decks = decks;
        this.numPlayers = numPlayers;
        this.numOfPlayersToPlay = numPlayers;
        this.players = new HashSet<>(numPlayers);
        this.stoppedPlayers = new HashMap<>(numPlayers);
        this.gameEndedMessage = new GAME_ENDED("none", -1);

        // Validate
        if (this.decks == null || this.numPlayers == null || this.numPlayers == 0 || this.decks.length != this.numPlayers) {
            logger.error("Illegal dealer argument: numPlayers={}, decks={}", this.numPlayers, this.decks);
            throw new IllegalArgumentException("Illegal dealer arguments");
        }

        logger.info("Creating blackjack game for {} players", numPlayers);
        startWith(State.START, null);
        when(State.START, matchEvent(START_GAME.class, this::startGame));
        when(State.DEAL_CARDS, matchEvent(HIT.class, this::hit));
        when(State.DEAL_CARDS, matchEvent(STAND.class, this::stand));
        when(State.DEAL_CARDS, matchEvent(BUST.class, this::bust));
        initialize();
    }


    private FSM.State<State, String> startGame(START_GAME startGame, String data) {
        logger.info("Staring players");
        this.originalSender = sender();
        for (int i=0; i<numPlayers; i++) {
            ActorRef actorRef = context().actorOf(PlayerAbstractActor.props(decks[i].nextCard(), decks[i].nextCard()), "player-" + (i + 1));
            this.players.add(actorRef);
            actorRef.tell(PlayerAbstractActor.PLAY.INSTANCE, self());
        }
        return goTo(State.DEAL_CARDS);
    }

    private FSM.State<State, String> hit(HIT hit, String data) {
        final String actorName = sender().path().name();
        logger.info("Player {} requested a card", actorName);
        String actorNumber = actorName.substring("player-".length());
        int playerIndex = Integer.parseInt(actorNumber);
        sender().tell(new PlayerAbstractActor.CARD(decks[playerIndex-1].nextCard()), self());
        numOfPlayersToPlay--;
        return playRound();
    }

    private FSM.State<State, String> stand(STAND stand, String data) {
        logger.info("Player {} stands", sender().path().name());
        numOfPlayersToPlay--;
        players.remove(sender());
        stoppedPlayers.put(sender(), stand.hand);
        return playRound();
    }

    private FSM.State<State, String> bust(BUST bust, String data) {
        logger.info("Player {} is bust", sender().path().name());
        numOfPlayersToPlay--;
        players.remove(sender());
        sender().tell(PlayerAbstractActor.LOOSE.INSTANCE, self());
        return playRound();
    }

    private FSM.State<State, String> playRound() {

        if (numOfPlayersToPlay == 0) {

            if (!this.players.isEmpty()) {
                GameUtil.cheatEnabled(); // Just making a point
                logger.info("Next round");
                this.numOfPlayersToPlay = players.size();
                for (ActorRef actorRef : players) {
                    actorRef.tell(PlayerAbstractActor.PLAY.INSTANCE, self());
                }
                return stay();
            }
            else if (stoppedPlayers.isEmpty()) {
                logger.info("Everyone lost!!!");
                return end();
            }
            else {
                int winningHand = -1;
                ActorRef winningActor = null;
                for (Map.Entry<ActorRef, Integer> entry : stoppedPlayers.entrySet()) {
                    if (entry.getValue() > winningHand) {
                        winningHand = entry.getValue();
                        winningActor = entry.getKey();
                    }
                }

                logger.info("The winner is {} with a hand of {}", winningActor.path().name(), winningHand);
                gameEndedMessage = new GAME_ENDED(winningActor.path().name(), winningHand);
                winningActor.tell(PlayerAbstractActor.WIN.INSTANCE, self());
                for (ActorRef looser : stoppedPlayers.keySet()) {
                    if (looser != winningActor) {
                        looser.tell(PlayerAbstractActor.LOOSE.INSTANCE, self());
                    }
                }

                return end();
            }
        }
        else {
            return stay();
        }
    }

    private FSM.State<State, String> end() {
        originalSender.tell(gameEndedMessage, self());
        self().tell(PoisonPill.getInstance(), ActorRef.noSender());
        return goTo(State.END);
    }
}

