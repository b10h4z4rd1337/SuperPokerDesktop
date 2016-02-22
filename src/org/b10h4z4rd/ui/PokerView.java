package org.b10h4z4rd.ui;

import org.b10h4z4rd.game.Card;
import org.b10h4z4rd.game.Player;
import org.b10h4z4rd.net.LocalPlayerHandle;
import org.b10h4z4rd.net.PokerClient;
import org.b10h4z4rd.net.PokerHost;
import org.b10h4z4rd.net.TableInformation;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

/**
 * Created by Mathias on 05.02.16.
 */
public class PokerView extends JPanel {

    private static Image back;

    private Player self, winner;
    private Player[] playersToDraw = new Player[0];
    public boolean started = false;
    private Card[] cards;

    public PokerView(final boolean host) {
        super();

        try {
            back = ImageIO.read(PokerView.class.getResource("../img/back.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        setPreferredSize(new Dimension(600, 400));

        final JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Enter your Nickname:");
        dialog.setLayout(null);

        final JTextField field = new JTextField();
        Dimension size = field.getPreferredSize();
        field.setBounds(10, 10, 280, (int) size.getHeight());
        dialog.add(field);

        JButton button = new JButton("Ok");
        size = button.getPreferredSize();
        button.setBounds(150 - size.width / 2, 75 - size.height, size.width, size.height);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = field.getText();
                try {
                    self = new Player(name, 1000);
                    if (!host) {
                        PokerClient.startClient(PokerView.this, self);
                        TableInformation.setSingleton(new TableInformation(PokerView.this) {
                            @Override
                            public int getPlayerCount() {
                                return PokerClient.getClient().getPlayerCount();
                            }

                            @Override
                            public Player[] getPlayers() {
                                return PokerClient.getClient().getPlayers();
                            }
                        });
                        ActionDialog.openActionDialog(SwingUtilities.getWindowAncestor(PokerView.this));
                        repaint();
                    } else {
                        PokerHost.launchHost();
                        TableInformation.setSingleton(new TableInformation(PokerView.this) {
                            @Override
                            public int getPlayerCount() {
                                return PokerHost.getHost().getPlayerCount();
                            }

                            @Override
                            public Player[] getPlayers() {
                                return PokerHost.getHost().getPlayers();
                            }
                        });
                        PokerHost.getHost().addToQueue(self, new LocalPlayerHandle(PokerView.this, self));
                        ActionDialog.openActionDialog(SwingUtilities.getWindowAncestor(PokerView.this));
                        ActionDialog.getSingleton().changeToHostMode();
                        ActionDialog.getSingleton().setVisible(true);
                        repaint();
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                dialog.dispose();
            }
        });
        dialog.add(button);

        dialog.setSize(new Dimension(300, 100));
        dialog.setVisible(true);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        g.clearRect(0, 0, getWidth(), getHeight());

        int fontHeight = g.getFontMetrics().getHeight();

        if (started) {
            g.setColor(new Color(0, 101, 15));
            g.fillOval(10, 10, getWidth() - 20, getHeight() - 20);

            if (cards != null) {
                int x = getWidth() / 2 - 198;
                for (Card c : cards) {
                    if (c != null)
                        try {
                            g.drawImage(Card.getImageByName(c.getImageName()), x, getHeight() / 2 - 52, 75, 104, null);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    x += 80;
                }
            }

            g.setColor(Color.BLACK);
            String pot = String.valueOf(TableInformation.getSingleton().getPot());
            g.drawString(pot, getWidth() / 2 + 208 + 25 - g.getFontMetrics().stringWidth(pot) / 2, getHeight() / 2 + fontHeight / 2);
            ((Graphics2D) g).setStroke(new BasicStroke(5));
            g.drawArc(getWidth() / 2 + 203, getHeight() / 2 - 30, 60, 60, 0, 360);

            double deltaAngle = 360.0 / (TableInformation.getSingleton().getPlayerCount());
            double angle = 270.0;

            for (Player p : playersToDraw) {
                if (p.equals(self)) {
                    g.drawString(p.getName(), 0, fontHeight);
                    g.drawString(String.valueOf(p.getPass()), 0, fontHeight * 2);
                    g.drawString(String.valueOf(p.getMoney()), 0, fontHeight * 3);
                }

                double sin = Math.sin(Math.toRadians(angle));
                double cos = Math.cos(Math.toRadians(angle));

                int x = getWidth() / 2 + (int) (((getWidth() - 20) / 2) * cos);
                int y = getHeight() / 2 - (int) (((getHeight() - 20) / 2) * sin);

                if (sin < 0)
                    y += (104 + fontHeight) * sin;

                if (cos > 0)
                    x -= 155 * cos;

                y -= (104 + fontHeight) / 2 * Math.abs(cos);
                x -= 155 / 2 * Math.abs(sin);

                try {
                    if (p.equals(self)) {
                        Card[] cards = self.getCardsWithoutGuard();
                        g.drawImage(Card.getImageByName(cards[0].getImageName()), x, y, 75, 104, null);
                        g.drawImage(Card.getImageByName(cards[1].getImageName()), x + 80, y, 75, 104, null);
                    } else if (p.getCards() != null) {
                        Card[] cards = p.getCards();
                        g.drawImage(Card.getImageByName(cards[0].getImageName()), x, y, 75, 104, null);
                        g.drawImage(Card.getImageByName(cards[1].getImageName()), x + 80, y, 75, 104, null);
                    } else {
                        g.drawImage(back, x, y, 75, 104, null);
                        g.drawImage(back, x + 80, y, 75, 104, null);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                g.setColor(Color.BLACK);
                g.drawString(String.valueOf(p.getCurrentBet()), x, y + 104 + fontHeight);

                if (p.equals(winner)) {
                    g.setColor(Color.BLACK);
                    g.drawArc(x, y, 100, 100, 0, 360);
                }

                angle -= deltaAngle;
            }
        }
    }

    public void setPlayersToDraw(Player[] newPlayers) {
        Player[] result = new Player[newPlayers.length];

        int i = 0;
        for (; i < newPlayers.length; i++) {
            if (newPlayers[i].equals(self)) {
                newPlayers[i].setCards(self.getCardsWithoutGuard());
                self = newPlayers[i];
                break;
            }
        }

        int toCopy = newPlayers.length - i;
        System.arraycopy(newPlayers, i, result, 0, toCopy);
        System.arraycopy(newPlayers, 0, result, toCopy, newPlayers.length - toCopy);

        this.playersToDraw = result;

        this.repaint();
    }

    public void setRoundCards(Card[] cards) {
        this.cards = cards;
    }

    public void markWinner(Player winner) {
        this.winner = winner;
    }
}
