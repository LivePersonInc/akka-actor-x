package com.liveperson.infra.common.akka.actorx.demo;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Amit Tal
 * @since 9/22/2014
 */
public class PlayerAbstractActor extends AbstractActor {

    private Logger logger = LoggerFactory.getLogger(PlayerAbstractActor.class);

    public static enum PLAY {
        INSTANCE
    }

    public static class CARD {
        Integer value;
        public CARD(Integer value) {
            this.value = value;
        }
    }

    public static enum WIN {
        INSTANCE
    }

    public static enum LOOSE {
        INSTANCE
    }

    public static class LUCK {
        boolean haveLuck;
        public LUCK(boolean haveLuck) {
            this.haveLuck = haveLuck;
        }
    }

    public static Props props(Integer card1, Integer card2) {
        return Props.create(PlayerAbstractActor.class, card1, card2);
    }

    private ActorRef originalSender;
    private int totalHand;
    private int coinIndex;

    public PlayerAbstractActor(Integer card1, Integer card2) {
        this.totalHand = card1 + card2;
        logger.info("Starting with hand {}", totalHand);
        receive(ReceiveBuilder.match(PLAY.class, this::play).
                match(CARD.class, this::card).
                match(WIN.class, this::win).
                match(LOOSE.class, this::loose).
                match(LUCK.class, this::luck)
                .build());
    }

    private void loose(LOOSE loose) {
        logger.info("Bummer, I lost...");
        self().tell(PoisonPill.getInstance(), ActorRef.noSender());
    }

    private void win(WIN win) {
        logger.info("YESSSSSSSSS!!!!!");
        self().tell(PoisonPill.getInstance(), ActorRef.noSender());
    }

    private void card(CARD card) {
        logger.info("Received card {}", card.value);
        this.totalHand += card.value;
    }

    private void play(PLAY play) {
        if (this.totalHand > 21) {
            sender().tell(DealerAbstractFsm.BUST.INSTANCE, self());
        }
        else if (this.totalHand > 17) {
            sender().tell(new DealerAbstractFsm.STAND(this.totalHand), self());
        }
        else if (this.totalHand >= 13 && this.totalHand <= 17) {
            GameUtil.cheatEnabled(); // Just making a point
            this.originalSender = sender();
            ActorRef luckCoin = context().actorOf(LuckCoinUntypedActor.props(), "coin-"+self().path().name()+(coinIndex++));
            luckCoin.tell(LuckCoinUntypedActor.FLIP.INSTANCE, self());
        }
        else {
            sender().tell(DealerAbstractFsm.HIT.INSTANCE, self());
        }
    }

    private void luck(LUCK luck) {
        logger.info("I am {}feeling lucky today", (luck.haveLuck ? "" : "NOT "));
        if (luck.haveLuck) {
            this.originalSender.tell(DealerAbstractFsm.HIT.INSTANCE, self());
        }
        else {
            this.originalSender.tell(new DealerAbstractFsm.STAND(this.totalHand), self());
        }
    }
}
