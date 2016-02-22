package org.b10h4z4rd.game;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Random;

/**
 * Created by Mathias on 05.02.16.
 */
public class CardManager {

    private LinkedList<Card> cardArrayList = new LinkedList<Card>();
    private Random random = new Random();

    public CardManager() {
        for (int sign = 0; sign <= 3; sign++) {
            for (int num = 1; num <= 13; num++) {
                cardArrayList.add(new Card(num, sign));
            }
        }
    }

    public Card getRandomCard() {
        int pos = random.nextInt(42);
        return cardArrayList.remove(pos);
    }

    public void putCardBack(Card card) {
        cardArrayList.add(card);
    }

    public void shuffle() {
        Collections.shuffle(cardArrayList, random);
    }

}
