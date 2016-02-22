package org.b10h4z4rd.net;

import org.b10h4z4rd.game.Card;
import org.b10h4z4rd.game.Player;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyAgreement;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
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
public class PlayerHandle {

    public Player player;
    public Card[] cards;
    public Socket socket;
    public ObjectInputStream inputStream;
    public ObjectOutputStream outputStream;
    public Packet lastCommand;

    public PlayerHandle() { }

    public PlayerHandle(Socket socket) throws Exception {
        this.socket = socket;
        ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream is = new ObjectInputStream(socket.getInputStream());

        KeyPairGenerator kpg = KeyPairGenerator.getInstance("DH");
        kpg.initialize(1024);
        KeyPair kp = kpg.generateKeyPair();

        Class.forName("javax.crypto.spec.DHParameterSpec");
        DHParameterSpec dhSpec = ((DHPublicKey) kp.getPublic()).getParams();
        BigInteger aliceG = dhSpec.getG();
        BigInteger aliceP = dhSpec.getP();
        int aliceL = dhSpec.getL();
        byte[] alice = kp.getPublic().getEncoded();

        os.writeObject(alice);
        os.writeObject(aliceG);
        os.writeObject(aliceP);
        os.writeInt(aliceL);
        os.flush();

        KeyAgreement ka = KeyAgreement.getInstance("DH");
        ka.init(kp.getPrivate());

        byte[] bob = (byte[]) is.readObject();


        KeyFactory kf = KeyFactory.getInstance("DH");
        X509EncodedKeySpec x509Spec = new X509EncodedKeySpec(bob);
        PublicKey pk = kf.generatePublic(x509Spec);
        ka.doPhase(pk, true);

        byte secret[] = ka.generateSecret();
        byte[] key = new byte[16];
        System.arraycopy(secret, 0, key, 0, 16);

        Key aesKey = new SecretKeySpec(key, "AES");

        Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, aesKey, new IvParameterSpec(generateIV(secret)));

        inputStream = new ObjectInputStream(new CipherInputStream(socket.getInputStream(), cipher));
        outputStream = new ObjectOutputStream(new CipherOutputStream(socket.getOutputStream(), cipher));
    }


    private static byte[] generateIV(byte[] key) throws NoSuchAlgorithmException {
        byte[] result = new byte[16];
        byte[] tmp = MessageDigest.getInstance("SHA-1").digest(key);
        System.arraycopy(tmp, 0, result, 0, 16);
        return result;
    }

    public Packet askForAction(int min) throws Exception {
        outputStream.reset();
        outputStream.writeObject(new Packet(Packet.ASK_FOR_ACTION, min));
        lastCommand = (Packet) inputStream.readObject();
        return lastCommand;
    }

    public void sendRoundCards(Card[] cards) throws IOException {
        outputStream.reset();
        outputStream.writeObject(new Packet(Packet.SYNC_ROUND_CARD, cards));
    }

    public void sendCards(Card[] cards) throws IOException {
        outputStream.reset();
        outputStream.writeObject(new Packet(Packet.SYNC_CARD, cards));
    }

    public Player getPlayerInformation() throws IOException, ClassNotFoundException {
        player = (Player) inputStream.readObject();
        return player;
    }

    public void roundStarted(Player[] players) throws IOException {
        outputStream.reset();
        outputStream.writeObject(new Packet(Packet.SYNC_START, players));
    }

    public void tellResult(Player[] players, Card[][] cards, Player winner) throws IOException {
        outputStream.reset();
        outputStream.writeObject(new Packet(Packet.SYNC_END, new Result(players, cards, winner)));
    }

    public void sync(Sync sync) throws IOException {
        outputStream.reset();
        outputStream.writeObject(new Packet(Packet.SYNC, sync));
    }
}
