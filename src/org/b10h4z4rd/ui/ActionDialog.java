package org.b10h4z4rd.ui;

import org.b10h4z4rd.net.Packet;
import org.b10h4z4rd.net.PokerHost;
import org.b10h4z4rd.net.TableInformation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Mathias on 06.02.16.
 */
public class ActionDialog extends JDialog {

    private static ActionDialog singleton;
    public static void openActionDialog(Window owner) {
        singleton = new ActionDialog(owner);
    }

    public static ActionDialog getSingleton() {
        return singleton;
    }


    private Packet value;
    private final Object monitor = new Object();
    private Container savedState;
    private int minimumToSet;
    private JTextField field;
    private JButton checkButton, callButton;
    private JLabel minBetLabel;

    private ActionDialog(Window owner){
        super(owner, "Choose Action");
        setLayout(null);
        setResizable(false);
        setSize(new Dimension(300, 150));

        setupMinBetLabel();
        setupField();
        setupRaiseButton();
        setupCallButton();
        setupCheckButton();
        setupFoldButton();
    }

    private void setupMinBetLabel() {
        minBetLabel = new JLabel();
        Dimension size = minBetLabel.getPreferredSize();
        minBetLabel.setBounds(10, 75, 100, (int) size.getHeight());
        add(minBetLabel);
        minBetLabel.setVisible(false);
    }

    private void setupField() {
        field = new JTextField();
        Dimension size = field.getPreferredSize();
        field.setBounds(10, (int) (75 - size.getHeight()), 50, (int) size.getHeight());
        add(field);
    }

    private void setupRaiseButton() {
        JButton button = new JButton("Raise");
        Dimension size = button.getPreferredSize();
        button.setBounds(200, 0, size.width, size.height);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int toRaise = Integer.parseInt(field.getText());
                if (toRaise > 0) {
                    value = new Packet(Packet.RAISE, toRaise);
                    synchronized (monitor) {
                        monitor.notify();
                    }
                    minBetLabel.setVisible(false);
                    ActionDialog.this.setVisible(false);
                }
            }
        });
        add(button);
    }

    private void setupCallButton() {
        callButton = new JButton("Call");
        Dimension size = callButton.getPreferredSize();
        callButton.setBounds(200, size.height, size.width, size.height);
        callButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                value = new Packet(Packet.CALL, minimumToSet);
                synchronized (monitor) {
                    monitor.notify();
                }
                minBetLabel.setVisible(false);
                ActionDialog.this.setVisible(false);
            }
        });
        add(callButton);
    }

    private void setupCheckButton() {
        checkButton = new JButton("Check");
        Dimension size = checkButton.getPreferredSize();
        checkButton.setBounds(200, size.height * 2, size.width, size.height);
        checkButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                value = new Packet(Packet.CHECK, 0);
                synchronized (monitor) {
                    monitor.notify();
                }
                ActionDialog.this.setVisible(false);
            }
        });
        add(checkButton);
    }

    private void setupFoldButton() {
        JButton button = new JButton("Fold");
        Dimension size = button.getPreferredSize();
        button.setBounds(200, size.height * 3, size.width, size.height);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                value = new Packet(Packet.FOLD, 0);
                synchronized (monitor) {
                    monitor.notify();
                }
                ActionDialog.this.setVisible(false);
            }
        });
        add(button);
    }

    public void setMinimumToSet(int minimumToSet) {
        this.minimumToSet = minimumToSet;
        if (minimumToSet > 0) {
            checkButton.setEnabled(false);
            callButton.setEnabled(true);
            minBetLabel.setText("Minimum to bet: " + minimumToSet);
            Dimension size = minBetLabel.getPreferredSize();
            minBetLabel.setBounds(10, 100, (int) size.getWidth(), (int) size.getHeight());
            minBetLabel.setVisible(true);
        } else {
            checkButton.setEnabled(true);
            callButton.setEnabled(false);
            minBetLabel.setVisible(false);
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                revalidate();
                repaint();
            }
        });
    }

    public void changeToHostMode() {
        savedState = getContentPane();

        JPanel panel = new JPanel();
        panel.setLayout(null);

        JButton button = new JButton("Start Round");
        Dimension size = button.getPreferredSize();
        button.setBounds(150 - size.width / 2, 50 - size.height / 2, size.width, size.height);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PokerHost.getHost().startRound();
                TableInformation.getSingleton().view.started = true;
                ActionDialog.this.changeToActionMode();
            }
        });
        panel.add(button);

        setContentPane(panel);
        setSize(new Dimension(300, 100));
    }

    public void changeToActionMode() {
        Container temp = savedState;
        savedState = getContentPane();
        setContentPane(temp);
        setSize(new Dimension(300, 150));
        setVisible(false);
    }

    public Packet getValue() {
        synchronized (monitor) {
            try {
                monitor.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return value;
    }
}
