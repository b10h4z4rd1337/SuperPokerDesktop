package org.b10h4z4rd.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Mathias on 06.02.16.
 */
public class MenuView extends JPanel {

    public MenuView() {
        super();
        setPreferredSize(new Dimension(300, 100));
        setLayout(null);

        JButton button = new JButton("Client");
        Dimension size = button.getPreferredSize();
        button.setBounds(150 - size.width / 2, 45 - size.height, size.width, size.height);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(MenuView.this);
                frame.setContentPane(new PokerView(false));
                frame.invalidate();
                frame.pack();
            }
        });
        add(button);

        button = new JButton("Host");
        button.setBounds(150 - size.width / 2, 55, size.width, size.height);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(MenuView.this);
                frame.setContentPane(new PokerView(true));
                frame.invalidate();
                frame.pack();
            }
        });
        add(button);
    }

}
