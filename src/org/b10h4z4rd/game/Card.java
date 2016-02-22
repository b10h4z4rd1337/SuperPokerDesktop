package org.b10h4z4rd.game;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.Serializable;
import java.net.URL;
import java.text.DecimalFormat;

/**
 * Created by Mathias on 05.02.16.
 */
public class Card implements Serializable {

    public static final int KARO = 0, HERZ = 1, PIK = 2, KREUZ = 3;

    private int num, sign;

    public Card(int num, int sign) {
        this.num = num;
        this.sign = sign;
    }

    public int getSign() {
        return sign;
    }

    public int getNum() {
        return num;
    }

    public static Image getImageByName(String name) throws Exception {
        URL url = Card.class.getResource("../img/" + name);
        return ImageIO.read(url);
    }

    public String getImageName() {
        String result = "";
        switch (sign) {
            case KARO:
                result += "d";
                break;
            case HERZ:
                result += "h";
                break;
            case PIK:
                result += "s";
                break;
            default:
                result += "c";
                break;
        }

        result += new DecimalFormat("00").format(num);
        result += ".png";
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null) {
            if (obj instanceof Card) {
                Card card = (Card) obj;
                return this.num == card.num && this.sign == card.sign;
            }
        }
        return false;
    }
}
