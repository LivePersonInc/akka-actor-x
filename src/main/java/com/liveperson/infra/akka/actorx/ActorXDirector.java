package com.liveperson.infra.akka.actorx;

import akka.actor.*;
import akka.pattern.PromiseActorRef;
import com.liveperson.infra.akka.actorx.extension.ActorXConfig;
import com.liveperson.infra.akka.actorx.header.MessageTrailHeader;
import com.liveperson.infra.akka.actorx.role.*;
import com.liveperson.infra.akka.actorx.header.Manuscript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Amit Tal
 * @since 9/29/2014
 */
public class ActorXDirector {

    private Logger logger = LoggerFactory.getLogger(ActorXDirector.class);

    // This class can keep trace of all in/out actor refs
    // It can build an internal actor web that can send internal messages
    // TODO Should director keep track of this or should a proprietary mask do this?
    private Set<ActorRef> in;
    private Set<ActorRef> out;

    private Actor extendedActor;
    private RoleList maskList;

    public ActorXDirector(Actor extendedActor) {
        this.in = new HashSet<>();
        this.out = new HashSet<>();
        this.extendedActor = extendedActor;
        setup();
    }

    public void setup() {
        List<Role> roles = new ArrayList<>();
        if (ActorXConfig.isRoleAkkaSourceMdcActive()) {
            roles.add(new AkkaSourceMdcRole());
        }
        if (ActorXConfig.isRoleCorrelationActive()) {
            roles.add(new CorrelationRole());
        }
        if (ActorXConfig.isRoleMessageTrailActive()) {
            roles.add(new MessageTrailRole(extendedActor.getClass().getName()));
        }
        this.maskList = new RoleList(roles);
    }

    public void clean() {
        this.maskList = new RoleList(Collections.emptyList());
    }



    public Object beforeReceive(Object msg) {
        recordIn(extendedActor.sender());
        this.maskList.beforeReceive(extendedActor.sender(), msg, extendedActor);

        // Strip actor x manuscript wrapper and return original body
        if (msg instanceof ActorXManuscript) {
            return ((ActorXManuscript)msg).getMessage();
        }
        else {
            return msg;
        }
    }

    public void afterReceive(Object msg) {
        this.maskList.afterReceive(extendedActor.sender(), msg, extendedActor);
    }

    public Object beforeSend(ActorRef recipient, Object msg, ActorRef sender) {
        recordOut(recipient);

        // Wrap message in manuscript if needed
        Object wrappedMessage = msg;
        if (shouldWrapWithManuscript(recipient, msg)) {
            wrappedMessage = new ActorXManuscript(msg);
        }

        this.maskList.beforeSend(recipient, wrappedMessage, sender);
        return wrappedMessage;
    }

    public void afterSend(ActorRef recipient, Object msg, ActorRef sender) {
        this.maskList.afterSend(recipient, msg, sender);
    }


    public void beforeActorOf(Props props, String name) {
        // TODO Do we need this?
    }

    public void afterActorOf(ActorRef actorRef) {
        recordOut(actorRef);
        this.maskList.afterActorOf(actorRef);
    }

    private void recordIn(ActorRef actorRef) {
        if (actorRef != extendedActor.context().system().deadLetters()) {
            in.add(actorRef);
        }
    }

    private void recordOut(ActorRef actorRef) {
        out.add(actorRef);
    }

    private boolean shouldWrapWithManuscript(ActorRef recipient, Object msg) {
        return      !(msg instanceof AutoReceivedMessage)
                &&  !(msg instanceof Manuscript)
                &&  !(recipient instanceof PromiseActorRef); // akka ask pattern
    }

    // TODO CHANGE HOW THIS WORKS
    // TODO CHANGE WHERE API IS DEFINED / IMPLEMENTED
    public List<MessageTrailHeader.Trail> getMessageTrail() {
        List<Role> messageTrailRoles =
                maskList.getRoles().stream().filter(mask -> MessageTrailRole.class.isInstance(mask)).collect(Collectors.toList());
        if (messageTrailRoles != null) {
            LinkedList<MessageTrailHeader.Trail> messageTrail = ((MessageTrailRole) messageTrailRoles.get(0)).getMessageTrail();
            return messageTrail;
        }
        else {
            return null;
        }
    }



}
