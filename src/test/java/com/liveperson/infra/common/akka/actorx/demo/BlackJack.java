package com.liveperson.infra.common.akka.actorx.demo;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.JavaTestKit;
import com.liveperson.infra.akka.actorx.ActorXBackStage;
import com.liveperson.infra.akka.actorx.ActorXManuscript;
import com.liveperson.infra.akka.actorx.header.CorrelationHeader;
import com.liveperson.infra.common.akka.actorx.demo.deck.Deck;
import com.liveperson.infra.common.akka.actorx.demo.deck.RandomDeck;
import scala.concurrent.duration.FiniteDuration;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Amit Tal
 * @since 9/22/2014
 */
public class BlackJack {

    public static void main(String[] args) {

        // Arguments
        int numGames = 1;
        int numPlayersPerGame = 3;
        Deck[] randomDecks = new Deck[numPlayersPerGame];
        for (int d=0; d<numPlayersPerGame; d++) {
            randomDecks[d] = new RandomDeck();
        }

        // Init akka
        FiniteDuration FINITE_DURATION = JavaTestKit.duration("3000 second");
        ActorSystem actorSystem = ActorSystem.create("test-akka-system");

        // Execute games
        List<JavaTestKit> probes = new ArrayList<>(numGames);
        for (int i=1; i<=numGames; i++) {

            // Probe
            JavaTestKit probe = new JavaTestKit(actorSystem);
            probes.add(probe);

            // Construct dealer

            ActorRef dealer = actorSystem.actorOf(DealerAbstractFsm.props(randomDecks, numPlayersPerGame), "dealer" + i);

            // Run
            dealer.tell(DealerAbstractFsm.START_GAME.INSTANCE, probe.getRef());

            // Alternative Run
            /*ActorXManuscript actorXManuscript = new ActorXManuscript(DealerAbstractFsm.START_GAME.INSTANCE);
            CorrelationHeader.setCorrelation(actorXManuscript, "GAME", "Game"+i);
            dealer.tell(actorXManuscript, probe.getRef());*/
        }

        // Wait for all games to finish
        probes.stream().forEach( probe -> probe.expectMsgAnyClassOf(FINITE_DURATION, DealerAbstractFsm.GAME_ENDED.class) );

        // Log cast
        /*ActorXBackStage.logCastNetwork(actorSystem);
        JavaTestKit probe = new JavaTestKit(actorSystem);
        probe.expectNoMsg(JavaTestKit.duration("2 second"));*/

        actorSystem.shutdown();
        actorSystem.awaitTermination();
    }
}
