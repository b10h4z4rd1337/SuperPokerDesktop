package org.b10h4z4rd.net;

import java.io.Serializable;

/**
 * Created by Mathias on 06.02.16.
 */
public class Packet implements Serializable {

    public static final int SYNC_START = 1, SYNC_CARD = 2, SYNC = 3, ASK_FOR_ACTION = 4, RAISE = 5, CALL = 6, FOLD = 7, CHECK = 8, SYNC_END = 9, SYNC_ROUND_CARD = 10;

    public int code;
    public Object object;

    public Packet(int code, Object object) {
        this.code = code;
        this.object = object;
    }
}
