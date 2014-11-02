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
public class SimplePreloadedDecksMultiPlayer {

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
        int numPlayers = 3;
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

        // Construct dealer
        JavaTestKit probe = new JavaTestKit(actorSystem);
        ActorRef dealer = actorSystem.actorOf(DealerAbstractFsm.props(decks, numPlayers), "dealer");

        // Run
        dealer.tell(DealerAbstractFsm.START_GAME.INSTANCE, probe.getRef());

        // Wait for game to end
        DealerAbstractFsm.GAME_ENDED gameEnded =
                (DealerAbstractFsm.GAME_ENDED)probe.expectMsgAnyClassOf(FINITE_DURATION, DealerAbstractFsm.GAME_ENDED.class);

        // Validate
        validateWinningHand(gameEnded, "player-2", 20);
    }

    private void validateWinningHand(DealerAbstractFsm.GAME_ENDED gameEnded, String expectedWinner, Integer expectedHand) {
        Assert.assertEquals("Expected player-1 to win", expectedWinner, gameEnded.getWinner());
        Assert.assertEquals("Expected winning hand of 15",expectedHand, gameEnded.getHand());
    }
}
