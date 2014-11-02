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

/**
 * @author Amit Tal
 * @since 10/30/2014
 */
public class SimplePreloadedDecksOnePlayer {

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
    public void testOnePlayerWinNoLuck() {

        // Create deck for only player
        int numPlayers = 1;
        Deck deck = new PreloadedDeck(new int[]{
                5, // Card 1    (5)
                7, // Card 2    (12)
                3, // Card 3    (15) -> not luck (stand)
        });
        Deck[] decks = new Deck[]{deck};

        // Construct dealer
        JavaTestKit probe = new JavaTestKit(actorSystem);
        ActorRef dealer = actorSystem.actorOf(DealerAbstractFsm.props(decks, numPlayers), "dealer");

        // Run
        dealer.tell(DealerAbstractFsm.START_GAME.INSTANCE, probe.getRef());

        // Wait for game to end
        DealerAbstractFsm.GAME_ENDED gameEnded =
                (DealerAbstractFsm.GAME_ENDED)probe.expectMsgAnyClassOf(FINITE_DURATION, DealerAbstractFsm.GAME_ENDED.class);

        // Validate
        validateWinningHand(gameEnded, "player-1", 15);
    }

    @Test
    public void testOnePlayerWinWithLuck() {

        // Create deck for only player
        int numPlayers = 1;
        Deck deck = new PreloadedDeck(new int[]{
                5, // Card 1    (5)
                7, // Card 2    (12)
                4, // Card 3    (16) -> luck (hit)
                5  // Card 4    (21)
        });
        Deck[] decks = new Deck[]{deck};

        // Construct dealer
        JavaTestKit probe = new JavaTestKit(actorSystem);
        ActorRef dealer = actorSystem.actorOf(DealerAbstractFsm.props(decks, numPlayers), "dealer");

        // Run
        dealer.tell(DealerAbstractFsm.START_GAME.INSTANCE, probe.getRef());

        // Wait for game to end
        DealerAbstractFsm.GAME_ENDED gameEnded =
                (DealerAbstractFsm.GAME_ENDED)probe.expectMsgAnyClassOf(FINITE_DURATION, DealerAbstractFsm.GAME_ENDED.class);

        // Validate
        validateWinningHand(gameEnded, "player-1", 21);
    }

    @Test
    public void testOnePlayerBust() {

        // Create deck for only player
        int numPlayers = 1;
        Deck deck = new PreloadedDeck(new int[]{
                5, // Card 1    (5)
                7, // Card 2    (12)
                4, // Card 3    (16) -> luck (hit)
                8  // Card 4    (24) -> bust
        });
        Deck[] decks = new Deck[]{deck};

        // Construct dealer
        JavaTestKit probe = new JavaTestKit(actorSystem);
        ActorRef dealer = actorSystem.actorOf(DealerAbstractFsm.props(decks, numPlayers), "dealer");

        // Run
        dealer.tell(DealerAbstractFsm.START_GAME.INSTANCE, probe.getRef());

        // Wait for game to end
        DealerAbstractFsm.GAME_ENDED gameEnded =
                (DealerAbstractFsm.GAME_ENDED)probe.expectMsgAnyClassOf(FINITE_DURATION, DealerAbstractFsm.GAME_ENDED.class);

        // Validate
        validateWinningHand(gameEnded, "none", -1);
    }

    private void validateWinningHand(DealerAbstractFsm.GAME_ENDED gameEnded, String expectedWinner, Integer expectedHand) {
        Assert.assertEquals("Expected player-1 to win", expectedWinner, gameEnded.getWinner());
        Assert.assertEquals("Expected winning hand of 15",expectedHand, gameEnded.getHand());
    }
}
