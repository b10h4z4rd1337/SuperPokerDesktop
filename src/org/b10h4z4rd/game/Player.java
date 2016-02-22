package org.b10h4z4rd.game;

import org.b10h4z4rd.net.TableInformation;

import java.io.Serializable;

/**
 * Created by Mathias on 05.02.16.
 */
public class Player implements Serializable {

    private String name;
    private int money;
    private int currentBet = 0;
    private int pass = -1;
    private boolean fold = false;
    transient private Card[] cards;


    public Player(String name, int money) {
        this.money = money;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    public int getPass() {
        if (pass == -1) {
            for (char c : name.toCharArray())
                pass += (int) c;
            pass += Math.random() * 1337;
        }

        return pass;
    }

    public void setCards(Card... cards) {
        this.cards = cards;
    }

    public Card[] getCards() {
        if (TableInformation.getSingleton().isFinished)
            return cards;
        else
            return null;
    }

    public Card[] getCardsWithoutGuard() {
        return cards;
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj instanceof Player && name.equals(((Player) obj).getName());
    }

    public int getCurrentBet() {
        return currentBet;
    }

    public void setCurrentBet(int bet) {
        this.currentBet = bet;
    }

    public boolean isFold() {
        return fold;
    }

    public void setFold(boolean fold) {
        this.fold = fold;
    }
}
