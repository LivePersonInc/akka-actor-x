package com.liveperson.infra.akka.actorx;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.liveperson.infra.akka.actorx.extension.ActorXExtension;
import com.liveperson.infra.akka.actorx.extension.ActorXExtensionProvider;
import com.liveperson.infra.akka.actorx.header.MessageTrailHeader;
import com.liveperson.infra.akka.actorx.staff.CastTraceAssistant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Amit Tal
 * @since 10/6/2014
 */
public class ActorXBackStage {

    private static Logger logger = LoggerFactory.getLogger(ActorXBackStage.class);

    public static String getMessageTrailString() {
        List<MessageTrailHeader.Trail> messageTrail = getMessageTrail();
        if (messageTrail != null) {
            return MessageTrailHeader.getMessageTrailString(messageTrail);
        }
        return null;
    }

    public static List<MessageTrailHeader.Trail> getMessageTrail() {
        ActorXDirector actorXDirector = ActorXDirectorOffice.getActorXDirector();
        if (actorXDirector != null) {
            return actorXDirector.getMessageTrail();
        }
        return null;
    }

    public static void logCastNetwork(ActorSystem actorSystem) {
        ActorXExtension actorXExtension = ActorXExtensionProvider.actorXExtensionProvider.get(actorSystem);
        ActorRef castTraceActor = actorXExtension.getCastTraceActor();
        if (castTraceActor != null) {
            castTraceActor.tell(CastTraceAssistant.LogCast.INSTANCE, ActorRef.noSender());
        }
        else {
            logger.info("Cannot log cast network, check if feature is enabled");
        }
    }
}
