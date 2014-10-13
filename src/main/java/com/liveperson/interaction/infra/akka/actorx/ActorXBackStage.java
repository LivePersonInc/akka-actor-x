package com.liveperson.interaction.infra.akka.actorx;

import com.liveperson.interaction.infra.akka.actorx.header.MessageTrailHeader;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Amit Tal
 * @since 10/6/2014
 */
public class ActorXBackStage {

    public static String getMessageTrailString() {
        List<MessageTrailHeader.Trail> messageTrail = getMessageTrail();
        if (messageTrail != null) {
            return MessageTrailHeader.getMessageTrailString(messageTrail);
        }
        return null;
    }

    public static List<MessageTrailHeader.Trail> getMessageTrail() {
        ActorXDirector actorXDirector = ActorXDressingRoom.getActorXDirector();
        if (actorXDirector != null) {
            return actorXDirector.getMessageTrail();
        }
        return null;
    }


}
