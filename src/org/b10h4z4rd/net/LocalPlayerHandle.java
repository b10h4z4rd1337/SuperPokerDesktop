package org.b10h4z4rd.net;

import org.b10h4z4rd.game.Card;
import org.b10h4z4rd.game.Player;
import org.b10h4z4rd.ui.ActionDialog;
import org.b10h4z4rd.ui.PokerView;

import javax.swing.*;
import java.io.IOException;

/**
 * Created by Mathias on 07.02.16.
 */
public class LocalPlayerHandle extends PlayerHandle {

    private PokerView view;

    public LocalPlayerHandle(PokerView view, Player player) {
        this.view = view;
        this.player = player;
    }

    @Override
    public void sendCards(final Card... cards) throws IOException {
        player.setCards(cards);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                view.repaint();
            }
        });
    }

    @Override
    public void sendRoundCards(final Card... cards) {
        view.setRoundCards(cards);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                view.repaint();
            }
        });
    }

    @Override
    public Player getPlayerInformation() throws IOException, ClassNotFoundException {
        return player;
    }

    @Override
    public void roundStarted(Player... players) throws IOException {
        TableInformation.getSingleton().isFinished = false;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                view.repaint();
            }
        });
    }

    @Override
    public void tellResult(Player[] players, Card[][] cards, Player winner) throws IOException {
        TableInformation.getSingleton().isFinished = true;
        view.markWinner(winner);
        view.repaint();
    }

    @Override
    public Packet askForAction(final int min) throws Exception {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    ActionDialog.getSingleton().setMinimumToSet(min);
                    ActionDialog.getSingleton().setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        lastCommand = ActionDialog.getSingleton().getValue();
        return lastCommand;
    }

    @Override
    public void sync(Sync sync) throws IOException {
        view.setPlayersToDraw(sync.players);
        TableInformation.getSingleton().notifyView();
    }
}
