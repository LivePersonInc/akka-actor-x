package com.liveperson.infra.common.akka.actorx.demo;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.JavaTestKit;
import com.liveperson.infra.akka.actorx.ActorXBackStage;
import com.liveperson.infra.akka.actorx.ActorXManuscript;
import com.liveperson.infra.akka.actorx.extension.ActorXExtension;
import com.liveperson.infra.akka.actorx.extension.ActorXExtensionProvider;
import com.liveperson.infra.akka.actorx.header.CorrelationHeader;
import com.liveperson.infra.akka.actorx.staff.CastTraceAssistant;
import org.junit.Assert;
import scala.concurrent.duration.FiniteDuration;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Amit Tal
 * @since 9/22/2014
 */
public class BlackJack {

    /*public static void main(String[] args) {

        int numPlayers = 3;
        FiniteDuration FINITE_DURATION = JavaTestKit.duration("3000 second");
        ActorSystem actorSystem = ActorSystem.create("test-akka-system");
        JavaTestKit probe = new JavaTestKit(actorSystem);

        ActorRef dealer = actorSystem.actorOf(DealerAbstractFsm.props(numPlayers), "dealer");
        dealer.tell(DealerAbstractFsm.START_GAME.INSTANCE, probe.getRef());
        probe.expectMsgAnyClassOf(FINITE_DURATION, String.class);

        actorSystem.shutdown();
        actorSystem.awaitTermination();
    }*/

    public static void main(String[] args) {

        int numGames = 3;
        FiniteDuration FINITE_DURATION = JavaTestKit.duration("3000 second");
        ActorSystem actorSystem = ActorSystem.create("test-akka-system");

        List<JavaTestKit> probes = new ArrayList<>(numGames);
        for (int i=1; i<=numGames; i++) {

            // Probe
            JavaTestKit probe = new JavaTestKit(actorSystem);
            probes.add(probe);

            // Construct
            ActorRef dealer = actorSystem.actorOf(DealerAbstractFsm.props(3), "dealer" + i);
            ActorXManuscript actorXManuscript = new ActorXManuscript(DealerAbstractFsm.START_GAME.INSTANCE);

            // Run
            dealer.tell(actorXManuscript, probe.getRef());
        }

        // Wait for all games to finish
        probes.stream().forEach( probe -> probe.expectMsgAnyClassOf(FINITE_DURATION, String.class) );

        // Log cast
        FiniteDuration SHORT_FINITE_DURATION = JavaTestKit.duration("3 second");
        JavaTestKit probe = new JavaTestKit(actorSystem);
        ActorXExtension actorXExtension = ActorXExtensionProvider.actorXExtensionProvider.get(actorSystem);
        ActorRef castTraceActor = actorXExtension.getCastTraceActor();
        Assert.assertNotNull("Trace actor assistant should not be null, check if cast-trace is enabled", castTraceActor);
        castTraceActor.tell(CastTraceAssistant.LogCast.INSTANCE, probe.getRef());
        probe.expectNoMsg(SHORT_FINITE_DURATION);

        actorSystem.shutdown();
        actorSystem.awaitTermination();
    }
}
