package com.liveperson.infra.common.akka.actorx.demo.deck;

/**
 * @author Amit Tal
 * @since 10/30/2014
 */
public class PreloadedDeck implements Deck {

    private int cardIndex;
    private int[] cards;

    public PreloadedDeck(int[] cards) {
        this.cards = cards;
        this.cardIndex = 0;
    }

    @Override
    public int nextCard() throws NoCardsInDeck {

        // Validate
        if (cards == null || cardIndex >= cards.length) {
            throw new NoCardsInDeck();
        }

        // Return next card
        return cards[cardIndex++];
    }
}
