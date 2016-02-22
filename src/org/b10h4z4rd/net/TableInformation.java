package org.b10h4z4rd.net;

import org.b10h4z4rd.game.Player;
import org.b10h4z4rd.ui.PokerView;

import javax.swing.*;

/**
 * Created by Mathias on 07.02.16.
 */
public class TableInformation {
    private static TableInformation singleton;
    public static TableInformation getSingleton() {
        return singleton;
    }
    public static void setSingleton(TableInformation tableInformation) {
        TableInformation.singleton = tableInformation;
    }

    public TableInformation(PokerView view) {
        this.view = view;
    }

    public void notifyView() {
        view.setPlayersToDraw(getPlayers());
    }

    private int pot = 0;

    public boolean isFinished = false;
    public PokerView view;
    public int getPlayerCount() { return -1; }
    public Player[] getPlayers() { return null; }
    public int getPot() { return pot; }
    public void setPot(Integer pot) {
        this.pot = pot;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                TableInformation.this.view.repaint();
            }
        });
    }
    public void incrementPot(int num) {
        this.pot += num;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                TableInformation.this.view.repaint();
            }
        });
    }
}
