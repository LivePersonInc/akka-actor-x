package com.liveperson.infra.common.akka.actorx;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.JavaTestKit;
import com.liveperson.infra.common.akka.actorx.demo.DealerAbstractFsm;
import com.liveperson.infra.common.akka.actorx.demo.deck.Deck;
import com.liveperson.infra.common.akka.actorx.demo.deck.PreloadedDeck;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import scala.concurrent.duration.FiniteDuration;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Amit Tal
 * @since 10/30/2014
 */
public class SimplePreloadedDecksMultiGame {

    private final static FiniteDuration FINITE_DURATION = JavaTestKit.duration("3 second");

    private ActorSystem actorSystem;

    @Before
    public void setup() {
        actorSystem = ActorSystem.create("test-akka-system");
    }

    @After
    public void destroy() {
        actorSystem.shutdown();
        actorSystem.awaitTermination();
    }

    @Test
    public void testMultiGame () {

        // Setup
        int numGames = 3;
        int numPlayersPerGame = 3;

        // Game decks
        ArrayList<Deck[]> allDecks = new ArrayList<>(3);
        allDecks.add(createDecksForGame1());
        allDecks.add(createDecksForGame2());
        allDecks.add(createDecksForGame3());

        // Execute all games
        List<JavaTestKit> probes = new ArrayList<>(numGames);
        for (int i=1; i<=numGames; i++) {

            // Probe
            JavaTestKit probe = new JavaTestKit(actorSystem);
            probes.add(probe);

            // Construct dealer

            ActorRef dealer = actorSystem.actorOf(DealerAbstractFsm.props(allDecks.get(i-1), numPlayersPerGame), "dealer" + i);

            // Run
            dealer.tell(DealerAbstractFsm.START_GAME.INSTANCE, probe.getRef());

            // Alternative Run
            /*ActorXManuscript actorXManuscript = new ActorXManuscript(DealerAbstractFsm.START_GAME.INSTANCE);
            CorrelationHeader.setCorrelation(actorXManuscript, "GAME", "Game"+i);
            dealer.tell(actorXManuscript, probe.getRef());*/
        }

        // Wait for all games to finish
        DealerAbstractFsm.GAME_ENDED[] results = new DealerAbstractFsm.GAME_ENDED[numGames];
        probes.stream().forEach( probe -> {
            DealerAbstractFsm.GAME_ENDED gameEnded =
                    (DealerAbstractFsm.GAME_ENDED)probe.expectMsgAnyClassOf(FINITE_DURATION, DealerAbstractFsm.GAME_ENDED.class);
            String dealerName = probe.getLastSender().path().name();
            String dealerNumber = dealerName.substring("dealer".length());
            int gameNumber = Integer.parseInt(dealerNumber);
            results[gameNumber-1] = gameEnded;
        });

        // Validate results
        validateWinningHand(results[0], "player-2", 20);
        validateWinningHand(results[1], "player-1", 19);
        validateWinningHand(results[2], "player-3", 17);

    }

    private Deck[] createDecksForGame1() {

        // Create deck for only player
        Deck deck1 = new PreloadedDeck(new int[]{
                5, // Card 1    (5)
                7, // Card 2    (12)
                3, // Card 3    (15) -> not luck (stand)
        });
        Deck deck2 = new PreloadedDeck(new int[]{
                3, // Card 1    (3)
                4, // Card 2    (7)
                2, // Card 3    (9)
                5, // Card 4    (14) -> luck (hit)
                2, // Card 5    (16) -> luck (hit)
                4  // Card 6    (20)
        });
        Deck deck3 = new PreloadedDeck(new int[]{
                8, // Card 1    (8)
                6, // Card 2    (14) -> luck (hit)
                9, // Card 3    (23)
        });
        Deck[] decks = new Deck[]{deck1, deck2, deck3};
        return decks;
    }

    private Deck[] createDecksForGame2() {

        // Create deck for only player
        Deck deck1 = new PreloadedDeck(new int[]{
                3, // Card 1    (3)
                4, // Card 2    (7)
                2, // Card 3    (9)
                5, // Card 4    (14) -> luck (hit)
                2, // Card 5    (16) -> luck (hit)
                3  // Card 6    (19)
        });
        Deck deck2 = new PreloadedDeck(new int[]{
                8, // Card 1    (8)
                6, // Card 2    (14) -> luck (hit)
                9, // Card 3    (23)
        });
        Deck deck3 = new PreloadedDeck(new int[]{
                5, // Card 1    (5)
                7, // Card 2    (12)
                3, // Card 3    (15) -> not luck (stand)
        });
        Deck[] decks = new Deck[]{deck1, deck2, deck3};
        return decks;
    }

    private Deck[] createDecksForGame3() {

        // Create deck for only player
        Deck deck1 = new PreloadedDeck(new int[]{
                3, // Card 1    (3)
                4, // Card 2    (7)
                3, // Card 3    (10)
                6, // Card 4    (16) -> luck (hit)
                7  // Card 5    (23)
        });
        Deck deck2 = new PreloadedDeck(new int[]{
                8, // Card 1    (8)
                7, // Card 2    (15) -> no luck (stand
        });
        Deck deck3 = new PreloadedDeck(new int[]{
                5, // Card 1    (5)
                7, // Card 2    (12)
                5, // Card 3    (17) -> not luck (stand)
        });
        Deck[] decks = new Deck[]{deck1, deck2, deck3};
        return decks;
    }

    private void validateWinningHand(DealerAbstractFsm.GAME_ENDED gameEnded, String expectedWinner, Integer expectedHand) {
        Assert.assertEquals("Expected player-1 to win", expectedWinner, gameEnded.getWinner());
        Assert.assertEquals("Expected winning hand of 15",expectedHand, gameEnded.getHand());
    }
}
