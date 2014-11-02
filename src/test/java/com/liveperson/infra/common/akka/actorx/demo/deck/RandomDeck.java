package com.liveperson.infra.common.akka.actorx.demo.deck;

import java.util.Random;

/**
 * @author Amit Tal
 * @since 10/30/2014
 */
public class RandomDeck implements Deck {

    private Random random;

    public RandomDeck() {
        this.random = new Random();
    }

    @Override
    public int nextCard() throws NoCardsInDeck {
        return this.random.nextInt(10) + 1;
    }
}
