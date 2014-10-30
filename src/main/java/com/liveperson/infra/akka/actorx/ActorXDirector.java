package com.liveperson.infra.akka.actorx;

import akka.actor.*;
import akka.pattern.PromiseActorRef;
import com.liveperson.infra.akka.actorx.extension.ActorXConfig;
import com.liveperson.infra.akka.actorx.extension.ActorXExtension;
import com.liveperson.infra.akka.actorx.extension.ActorXExtensionProvider;
import com.liveperson.infra.akka.actorx.header.MessageTrailHeader;
import com.liveperson.infra.akka.actorx.role.*;
import com.liveperson.infra.akka.actorx.header.Manuscript;
import com.liveperson.infra.akka.actorx.staff.StaffMessage;
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

    private Actor enhancedActor;
    private Roles roles;
    private Colleagues colleagues;
    private ActorXExtension actorXExtension;

    public ActorXDirector(Actor enhancedActor) {
        this.enhancedActor = enhancedActor;
        this.colleagues = new Colleagues(enhancedActor);
        this.actorXExtension = ActorXExtensionProvider.actorXExtensionProvider.get(enhancedActor.context().system());
        setup();
    }

    public void setup() {
        String enhancedActorClassName = enhancedActor.getClass().getName();
        List<Role> roles = new ArrayList<>();
        if (ActorXConfig.isRoleAkkaSourceMdcActive()) {
            roles.add(new AkkaSourceMdcRole());
        }
        if (ActorXConfig.isRoleCorrelationActive()) {
            roles.add(new CorrelationRole());
        }
        if (ActorXConfig.isRoleMessageTrailActive()) {
            roles.add(new MessageTrailRole(enhancedActorClassName));
        }
        if (ActorXConfig.isCastTraceActive()) {
            roles.add(new CastTraceRole(enhancedActor.self(), enhancedActorClassName, this.actorXExtension.getCastTraceActor()));
        }
        this.roles = new Roles(roles);
    }

    public void clean() {
        this.roles = new Roles(Collections.emptyList());
    }


    public Object beforeReceive(Object msg) {
        this.colleagues.recordIn(enhancedActor.sender());
        this.roles.beforeReceive(enhancedActor.sender(), msg, enhancedActor);

        // Strip actor x manuscript wrapper and return original body
        if (msg instanceof ActorXManuscript) {
            return ((ActorXManuscript)msg).getMessage();
        }
        else {
            return msg;
        }
    }

    public void afterReceive(Object msg) {
        this.roles.afterReceive(enhancedActor.sender(), msg, enhancedActor);
    }

    public Object beforeSend(ActorRef recipient, Object msg, ActorRef sender) {
        this.colleagues.recordOut(recipient);

        // Wrap message in manuscript if needed
        Object wrappedMessage = msg;
        if (shouldWrapWithManuscript(recipient, msg)) {
            wrappedMessage = new ActorXManuscript(msg);
        }

        this.roles.beforeSend(recipient, wrappedMessage, sender);
        return wrappedMessage;
    }

    public void afterSend(ActorRef recipient, Object msg, ActorRef sender) {
        this.roles.afterSend(recipient, msg, sender);
    }


    public void beforeActorOf(Props props, String name) {
        // TODO Do we need this?
    }

    public void afterActorOf(ActorRef actorRef) {
        this.colleagues.recordOut(actorRef);
        this.roles.afterActorOf(actorRef);
    }

    private boolean shouldWrapWithManuscript(ActorRef recipient, Object msg) {
        return      !(msg instanceof AutoReceivedMessage)   // Internal akka messages
                &&  !(msg instanceof Manuscript)            // Message already wrapped
                &&  !(recipient instanceof PromiseActorRef) // Akka ask pattern
                &&  !(msg instanceof StaffMessage);         // Internal Staff Messages
    }

    // TODO CHANGE HOW THIS WORKS
    // TODO CHANGE WHERE API IS DEFINED / IMPLEMENTED
    public List<MessageTrailHeader.Trail> getMessageTrail() {
        List<Role> messageTrailRoles =
                roles.getRoles().stream().filter(role -> MessageTrailRole.class.isInstance(role)).collect(Collectors.toList());
        if (messageTrailRoles != null) {
            LinkedList<MessageTrailHeader.Trail> messageTrail = ((MessageTrailRole) messageTrailRoles.get(0)).getMessageTrail();
            return messageTrail;
        }
        else {
            return null;
        }
    }


    // This class can keep trace of all in/out actor refs
    // It can build an internal actor web that can send internal messages
    public static class Colleagues {

        // TODO Should director keep track of this or should a proprietary role do this?
        private Set<ActorRef> in;
        private Set<ActorRef> out;
        private Actor extendedActor;

        public Colleagues(Actor extendedActor) {
            this.extendedActor = extendedActor;
            this.in = new HashSet<>();
            this.out = new HashSet<>();
        }

        public void recordIn(ActorRef actorRef) {
            if (actorRef != extendedActor.context().system().deadLetters()) {
                in.add(actorRef);
            }
        }

        public void recordOut(ActorRef actorRef) {
            out.add(actorRef);
        }
    }
}
