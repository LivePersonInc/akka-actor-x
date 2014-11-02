package com.liveperson.infra.common.akka.actorx.demo;

import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.actor.UntypedActor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Amit Tal
 * @since 9/22/2014
 */
public class LuckCoinUntypedActor extends UntypedActor {

    private Logger logger = LoggerFactory.getLogger(LuckCoinUntypedActor.class);

    public static class FLIP {
        int currentHand;
        public FLIP(int currentHand) {
            this.currentHand = currentHand;
        }
    }

    public static Props props() {
        return Props.create(LuckCoinUntypedActor.class);
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof FLIP) {
            flipCoin((FLIP)message);
        } else {
            unhandled(message);
        }
    }

    private void flipCoin(FLIP message) {

        GameUtil.cheatEnabled(); // Just making a point
        boolean luck = message.currentHand % 2 == 0;
        sender().tell(new PlayerAbstractActor.LUCK(luck), ActorRef.noSender());
        self().tell(PoisonPill.getInstance(), ActorRef.noSender());
    }
}
