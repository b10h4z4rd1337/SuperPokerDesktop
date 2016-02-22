package org.b10h4z4rd.net;

import org.b10h4z4rd.game.Player;

import java.io.Serializable;

/**
 * Created by Mathias on 08.02.16.
 */
public class Sync implements Serializable {

    public Player[] players;
    public int pot;

    public Sync(Player[] players, int pot) {
        this.players = players;
        this.pot = pot;
    }
}
