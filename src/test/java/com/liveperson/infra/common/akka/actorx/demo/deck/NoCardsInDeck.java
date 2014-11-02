package com.liveperson.infra.common.akka.actorx.demo.deck;

/**
 * @author Amit Tal
 * @since 10/30/2014
 */
public class NoCardsInDeck extends RuntimeException {

    public NoCardsInDeck() {
        super("No more cards in deck");
    }
}
