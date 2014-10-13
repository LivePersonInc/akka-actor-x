package com.liveperson.interaction.infra.common.akka.actorx.demo;

import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.actor.UntypedActor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

/**
 * @author Amit Tal
 * @since 9/22/2014
 */
public class LuckCoinUntypedActor extends UntypedActor {

    private Logger logger = LoggerFactory.getLogger(LuckCoinUntypedActor.class);

    public static enum FLIP {
        INSTANCE
    }

    public static Props props() {
        return Props.create(LuckCoinUntypedActor.class);
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof FLIP) {
            flipCoin();
        } else {
            unhandled(message);
        }
    }

    private void flipCoin() {

        GameUtil.cheatEnabled(); // Just making a point
        Random random = new Random();
        boolean luck = random.nextBoolean();
        sender().tell(new PlayerAbstractActor.LUCK(luck), ActorRef.noSender());
        self().tell(PoisonPill.getInstance(), ActorRef.noSender());
    }
}
