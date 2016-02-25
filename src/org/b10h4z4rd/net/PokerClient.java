package org.b10h4z4rd.net;

import org.b10h4z4rd.game.Card;
import org.b10h4z4rd.game.Player;
import org.b10h4z4rd.ui.ActionDialog;
import org.b10h4z4rd.ui.PokerView;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyAgreement;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;

/**
 * Created by Mathias on 06.02.16.
 */
public class PokerClient implements Runnable {

    protected static PokerClient client;

    public static PokerClient getClient() {
        return client;
    }


    private Player self;
    private final PokerView view;
    private Socket socket;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    protected Player[] players = new Player[1];

    public static void startClient(PokerView view, Player player) throws Exception {
        client = new PokerClient(view, player);
    }

    private PokerClient(PokerView view, Player player) throws Exception {
        this.self = player;
        this.view = view;
        socket = new Socket(getIpAddress(), 1337);
        establishConnection();
        outputStream.writeObject(self);
        new Thread(this).start();
    }

    public String getIpAddress() {
        return "127.0.0.1";
    }

    public int getPlayerCount() {
        return players.length;
    }

    public Player[] getPlayers() {
        return players;
    }

    public void establishConnection() throws Exception {
        ObjectInputStream is = new ObjectInputStream(socket.getInputStream());
        ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());

        byte[] alice = (byte[]) is.readObject();
        BigInteger aliceG = (BigInteger) is.readObject();
        BigInteger aliceP = (BigInteger) is.readObject();
        int aliceL = is.readInt();

        KeyPairGenerator kpg = KeyPairGenerator.getInstance("DH");
        DHParameterSpec dhSpec = new DHParameterSpec(aliceP, aliceG, aliceL);
        kpg.initialize(dhSpec);
        KeyPair kp = kpg.generateKeyPair();
        byte[] bob = kp.getPublic().getEncoded();

        os.writeObject(bob);
        os.flush();

        KeyAgreement ka = KeyAgreement.getInstance("DH");
        ka.init(kp.getPrivate());

        KeyFactory kf = KeyFactory.getInstance("DH");
        X509EncodedKeySpec x509Spec =  new X509EncodedKeySpec(alice);
        PublicKey pk = kf.generatePublic(x509Spec);
        ka.doPhase(pk, true);

        byte[] secret = ka.generateSecret();
        byte[] key = new byte[16];
        System.arraycopy(secret, 0, key, 0, 16);

        Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
        byte[] iv = generateIV(secret);
        IvParameterSpec ivspec = new IvParameterSpec(iv);
        Key aesKey = new SecretKeySpec(key, "AES");
        cipher.init(Cipher.ENCRYPT_MODE, aesKey, ivspec);


        outputStream = new ObjectOutputStream(new CipherOutputStream(socket.getOutputStream(), cipher));
        inputStream = new ObjectInputStream(new CipherInputStream(socket.getInputStream(), cipher));
    }

    static byte[] generateIV(byte[] key) throws NoSuchAlgorithmException {
        byte[] result = new byte[16];
        byte[] tmp = MessageDigest.getInstance("SHA-1").digest(key);
        System.arraycopy(tmp, 0, result, 0, 16);
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void run() {
        try {
            while (true) {
                Packet packet = (Packet) inputStream.readObject();

                switch (packet.code) {
                    case Packet.ASK_FOR_ACTION:
                        Packet resp = askAction((Integer)packet.object);
                        outputStream.writeObject(resp);
                        break;
                    case Packet.SYNC_START:
                        TableInformation.getSingleton().isFinished = false;
                        setPlayers((Player[]) packet.object);
                        TableInformation.getSingleton().notifyView();
                        TableInformation.getSingleton().view.started = true;
                        break;
                    case Packet.SYNC_CARD:
                        self.setCards((Card[]) packet.object);
                        break;
                    case Packet.SYNC_ROUND_CARD:
                        view.setRoundCards((Card[]) packet.object);
                        repaint();
                        break;
                    case Packet.SYNC:
                        Sync sync = (Sync) packet.object;
                        TableInformation.getSingleton().setPot(sync.pot);
                        setPlayers(sync.players);
                        TableInformation.getSingleton().notifyView();
                        repaint();
                        break;
                    case Packet.SYNC_END:
                        final Result result = (Result) packet.object;
                        view.markWinner(result.winner);

                        setPlayers(result.players);

                        int i = 0;
                        for (Player player : players) {
                            player.setCards(result.cards[i]);
                            i++;
                        }

                        TableInformation.getSingleton().isFinished = true;
                        TableInformation.getSingleton().notifyView();

                        repaint();
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void setPlayers(Player[] players) {
        this.players = players;
        for (int i = 0; i < players.length; i++) {
            if (players[i] != null && players[i].equals(self))
                self = players[i];
        }
    }

    private void repaint() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                view.repaint();
            }
        });
    }

    private Packet askAction(final int minimum) throws InterruptedException, IOException {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                ActionDialog.getSingleton().setMinimumToSet(minimum);
                ActionDialog.getSingleton().setVisible(true);
            }
        });
        return ActionDialog.getSingleton().getValue();
    }
}
