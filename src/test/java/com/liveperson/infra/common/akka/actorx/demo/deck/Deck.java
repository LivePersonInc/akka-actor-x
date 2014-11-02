package com.liveperson.infra.common.akka.actorx.demo.deck;

/**
 * @author Amit Tal
 * @since 10/30/2014
 */
public interface Deck {

    int nextCard() throws NoCardsInDeck;
}
