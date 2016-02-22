package org.b10h4z4rd;

import org.b10h4z4rd.ui.MenuView;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        JFrame frame = new JFrame("SuperPoker");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setContentPane(new MenuView());
        frame.pack();
        frame.setVisible(true);
    }

}
