package org.b10h4z4rd.net;

import org.b10h4z4rd.game.Card;
import org.b10h4z4rd.game.Player;
import org.b10h4z4rd.game.Round;
import org.b10h4z4rd.game.ResultCalculator;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by Mathias on 06.02.16.
 */
public class PokerHost implements Runnable {

    private static PokerHost host;
    public static PokerHost getHost() {
        return host;
    }
    public static void launchHost() throws Exception {
        host = new PokerHost();
    }

    private ServerSocket serverSocket;
    private Player[] players;
    private HashMap<Player, PlayerHandle> playersToHandleMap;

    private HashMap<Player, PlayerHandle> queue;

    public PokerHost() throws Exception {
        serverSocket = new ServerSocket(1337);
        players = new Player[0];
        playersToHandleMap = new HashMap<Player, PlayerHandle>();
        queue = new HashMap<Player, PlayerHandle>();
        new Thread(this).start();
    }

    public Player[] getPlayers() {
        return players;
    }

    public PlayerHandle getHandleForPlayer(Player p) {
        return playersToHandleMap.get(p);
    }

    @Override
    public void run() {
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                PlayerHandle playerHandle = new PlayerHandle(socket);
                Player player = playerHandle.getPlayerInformation();

                queue.put(player, playerHandle);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void syncCards(Card[] cards) throws IOException {
        for (Player player : players) {
            PlayerHandle playerHandle = PokerHost.getHost().getHandleForPlayer(player);

            playerHandle.sendRoundCards(cards);
        }
    }

    public void roundHasStarted() throws IOException {
        Player[] copy = Arrays.copyOf(players, players.length);
        for (Player player : players) {
            PlayerHandle playerHandle = PokerHost.getHost().getHandleForPlayer(player);

            playerHandle.roundStarted(copy);
        }
        TableInformation.getSingleton().notifyView();
    }

    public void tellResult(ResultCalculator winner) throws IOException {
        Player[] playerCopy = Arrays.copyOf(players, players.length);
        Card[][] cards = new Card[2][players.length];

        int i = 0;
        for (Player p : players) {
            playerCopy[i] = p;
            cards[i] = p.getCardsWithoutGuard();
            i++;
        }

        for (Player p : players) {
            PlayerHandle playerHandle = getHandleForPlayer(p);

            playerHandle.tellResult(playerCopy, cards, winner.getPlayer());
        }
    }

    public int getPlayerCount() {
        return players.length;
    }

    public void startRound() {
        new Thread(new Round()).start();
    }

    public void addToQueue(Player player, PlayerHandle playerHandle) {
        queue.put(player, playerHandle);
    }

    public void addQueueToActive() {
        Set<Player> playerSet = queue.keySet();
        players = playerSet.toArray(new Player[playerSet.size()]);

        for (Player p : players) {
            PlayerHandle playerHandle = queue.get(p);
            playersToHandleMap.put(p, playerHandle);
        }
    }

    public void sync() throws IOException {
        Sync sync = new Sync(players, TableInformation.getSingleton().getPot());
        for (Player p : players) {
            PlayerHandle playerHandle = getHandleForPlayer(p);

            playerHandle.sync(sync);
        }
    }
}
