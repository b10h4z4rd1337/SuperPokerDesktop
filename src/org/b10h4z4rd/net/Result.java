package org.b10h4z4rd.net;

import org.b10h4z4rd.game.Card;
import org.b10h4z4rd.game.Player;

import java.io.Serializable;

/**
 * Created by Mathias on 07.02.16.
 */
public class Result implements Serializable {

    public Player[] players;
    public Card[][] cards;
    public Player winner;

    public Result(Player[] players, Card[][] cards, Player winner) {
        this.players = players;
        this.cards = cards;
        this.winner = winner;
    }
}
