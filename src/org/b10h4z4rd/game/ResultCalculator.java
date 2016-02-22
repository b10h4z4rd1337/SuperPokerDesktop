package org.b10h4z4rd.game;

import java.util.List;

/**
 * Created by Mathias on 08.02.16.
 */
public class ResultCalculator {

    public static final int PAIR = 1, TWO_PAIRS = 2, TRIPLET = 3, STREET = 4, FLUSH = 5, FULL_HOUSE = 6, QUARTET = 7, STRAIGHT_FLUSH = 8, ROYAL_FLUSH = 9;

    private Card[] table;
    private int code;
    private Player player;

    private ResultCalculator(Player player, Card[] table, int code) {
        this.player = player;
        this.table = table;
        this.code = code;
    }

    private Card[] cardsWithoutResult(Card[] origin, Card[] cards){
        Card[] result = new Card[origin.length - cards.length];
        int counter = 0;

        for (Card anOrigin : origin) {
            boolean found = false;
            for (Card aToRemove : cards) {
                if (anOrigin.equals(aToRemove)) {
                    found = true;
                    break;
                }
            }
            if (found) {
                result[counter] = anOrigin;
                counter++;
            }
        }

        return result;
    }

    private Card[] combineCards() {
        Card[] cards = new Card[7];
        System.arraycopy(table, 0, cards, 0, 5);
        System.arraycopy(player.getCardsWithoutGuard(), 0, cards, 5, 2);
        return cards;
    }

    private void checkForWinning() {

        Card[] origin = combineCards();
        Card[] result;

        if ((result = findPair(origin)) != null) {
            if ((result = findTriplet(cardsWithoutResult(origin, result))) != null) {
                //Full House
                code = FULL_HOUSE;
            } else if ((result = findPair(cardsWithoutResult(origin, result))) != null) {
                //Two Pairs
                code = TWO_PAIRS;
            } else {
                //Pair
                code = PAIR;
            }
        }

        if ((result = findFlush(origin, 5, 0, 0)) != null) {
            if ((result = findStreet(cardsWithoutResult(origin, result), 5, 0)) != null) {
                if (isRoyal(result)) {
                    //Royal Flush
                    code = ROYAL_FLUSH;
                } else {
                    //Straight Flush
                    code = STRAIGHT_FLUSH;
                }
            } else {
                code = FLUSH;
            }
        }

        if ((result = findStreet(origin, 5, 0)) != null) {
            //Straight
            code = STREET;
        }

        if ((result = findQuartet(origin)) != null) {
            //Quartet
            code = QUARTET;
        }
    }

    private static Card[] findQuartet(Card[] cards) {
        Card[] result = new Card[4];

        int counter = 1;
        for (Card c : cards) {
            result[0] = c;
            Card[] temp = new Card[cards.length - counter];
            System.arraycopy(cards, counter, temp, 0, cards.length - counter);
            Card[] possibleTriplet = findTriplet(temp);
            if (possibleTriplet != null) {
                if (result[0].getNum() == possibleTriplet[0].getNum()) {
                    result[1] = possibleTriplet[0];
                    result[2] = possibleTriplet[1];
                    result[4] = possibleTriplet[2];
                    return result;
                }
            }
            counter++;
        }

        return null;
    }

    private static Card findCardOfValueAndColor(Card[] cards, int value, int sign) {
        if (value == 14)
            value = 1;

        for (Card c : cards)
            if (c.getNum() == value && c.getSign() == sign)
                return c;

        return null;
    }

    private static Card[] findFlush(Card[] cards, int length, int startVal, int sign) {
        Card[] result = new Card[length];

        if (length == 1)
            return new Card[]{findCardOfValueAndColor(cards, startVal, sign)};

        for (Card c : cards) {
            result[0] = findCardOfValueAndColor(cards, c.getNum(), c.getSign());
            if (result[0] != null)
                findStreet(cards, length - 1, startVal + 1);
            else
                return null;
        }

        return result;
    }

    private static boolean isRoyal(Card[] cards) {
        return cards[0].getNum() == 1 || cards[4].getNum() == 1;
    }

    private static Card findCardOfValue(Card[] cards, int value) {
        if (value == 14)
            value = 1;

        for (Card c : cards)
            if (c.getNum() == value)
                return c;

        return null;
    }

    private static Card[] findStreet(Card[] cards, int length, int startVal) {
        Card[] result = new Card[length];

        if (length == 1)
            return new Card[]{findCardOfValue(cards, startVal)};

        for (Card c : cards) {
            result[0] = findCardOfValue(cards, c.getNum());
            if (result[0] != null)
                findStreet(cards, length - 1, startVal + 1);
            else
                return null;
        }

        return result;
    }

    private static Card[] findTriplet(Card[] cards) {
        Card[] result = new Card[3];

        int counter = 1;
        for (Card c : cards) {
            result[0] = c;
            Card[] temp = new Card[cards.length - counter];
            System.arraycopy(cards, counter, temp, 0, cards.length - counter);
            Card[] possiblePair = findPair(temp);
            if (possiblePair != null) {
                if (result[0].getNum() == possiblePair[0].getNum()) {
                    result[1] = possiblePair[0];
                    result[2] = possiblePair[1];
                    return result;
                }
            }
            counter++;
        }

        return null;
    }

    private static Card[] findPair(Card[] cards) {
        Card[] result = new Card[2];

        int counter = 1;
        for (Card c : cards) {
            result[0] = c;
            for (int i = counter; i < cards.length; i++) {
                if (result[0].getNum() == cards[i].getNum()) {
                    result[1] = cards[i];
                    return result;
                }
            }
            counter++;
        }

        return null;
    }

    public static int[] searchCollisions(List<ResultCalculator> corresopondingResults, int code) {
        int[] result = new int[0];
        int counter = 0;
        for (ResultCalculator wc : corresopondingResults) {
            if (wc.code == code)
                result = addIndexToArray(result, counter);
            counter++;
        }
        return result;
    }

    public static int[] addIndexToArray(int[] dest, int toPut) {
        int[] result = new int[dest.length + 1];
        System.arraycopy(result, 0, dest, 0, dest.length);
        result[result.length - 1] = toPut;
        return result;
    }
}
