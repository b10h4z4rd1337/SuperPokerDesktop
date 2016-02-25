package org.b10h4z4rd.game;

import org.b10h4z4rd.net.*;

import java.io.IOException;
import java.util.LinkedList;

/**
 * Created by Mathias on 06.02.16.
 */
public class Round implements Runnable {

    private Card[] cards;
    private CardManager cardManager = new CardManager();
    private LinkedList<Card> cardsTakenOut = new LinkedList<Card>();

    public void informAll() throws IOException {
        PokerHost.getHost().roundHasStarted();
    }

    @Override
    public void run() {
        try {
            PokerHost.getHost().addQueueToActive();
            TableInformation.getSingleton().setPot(0);
            informAll();

            cards = new Card[5];

            //Prepare new Round
            for (int i = 0; i < 3; i++) {
                cards[i] = cardManager.getRandomCard();
            }

            for (Player player : PokerHost.getHost().getPlayers()) {
                PlayerHandle playerHandle = PokerHost.getHost().getHandleForPlayer(player);
                Card c1 = cardManager.getRandomCard();
                Card c2 = cardManager.getRandomCard();
                Card[] cards = new Card[]{c1, c2};
                player.setCards(cards);
                playerHandle.sendCards(cards);
                cardsTakenOut.add(c1);
                cardsTakenOut.add(c2);
            }

            PokerHost.getHost().syncCards(cards);

            askAround();

            Card c = cardManager.getRandomCard();
            cardsTakenOut.add(c);
            cards[3] = c;
            PokerHost.getHost().syncCards(cards);

            askAround();

            c = cardManager.getRandomCard();
            cardsTakenOut.add(c);
            cards[4] = c;
            PokerHost.getHost().syncCards(cards);

            askAround();

            //Sync Ergebnis, Karten, Geld
            ResultCalculator resultCalculator = calculateWinner();
            Player player = resultCalculator.getPlayer();
            player.setMoney(player.getMoney() + TableInformation.getSingleton().getPot());

            PokerHost.getHost().tellResult(resultCalculator);

            cleanUp();

        }catch (Exception e) {
            e.printStackTrace();
            System.exit(42);
        }
    }

    private ResultCalculator calculateWinner() {
        LinkedList<ResultCalculator> results = new LinkedList<ResultCalculator>();

        for (Player player : PokerHost.getHost().getPlayers()) {
            ResultCalculator result = new ResultCalculator(player, cards);
            if (result.getCode() > 0) {
                results.add(result);
            }
        }

        if (results.size() > 0) {
            int max = 0;
            int index = 0;
            int c = 0;
            for (ResultCalculator resultCalculator : results) {
                if (resultCalculator.getCode() > max) {
                    max = resultCalculator.getCode();
                    index = c;
                }
                c++;
            }

            int[] collisions = ResultCalculator.searchCollisions(results, max);
            if (collisions.length > 0) {
                // TODO: Find better player
            } else {
                ResultCalculator wc = results.get(index);
                return wc;
            }
        }

        return null;
    }

    private void cleanUp() throws IOException {
        for (Player player : PokerHost.getHost().getPlayers()) {
            PlayerHandle playerHandle = PokerHost.getHost().getHandleForPlayer(player);
            playerHandle.cards = new Card[2];
        }

        for (Card c : cardsTakenOut) {
            cardManager.putCardBack(c);
        }

        cardsTakenOut.clear();

        cardManager.shuffle();
    }

    private void askAround() throws Exception {
        while (!areAllChecked()) {
            for (Player player : PokerHost.getHost().getPlayers()) {
                if (!areAllChecked() && !player.isFold()) {
                    PlayerHandle playerHandle = PokerHost.getHost().getHandleForPlayer(player);
                    Packet action = playerHandle.askForAction(findMinBet());

                    switch (action.code) {
                        case Packet.RAISE:
                            TableInformation.getSingleton().incrementPot((Integer) action.object);
                            playerHandle.player.setCurrentBet(playerHandle.player.getCurrentBet() + (Integer) action.object);
                            playerHandle.player.setMoney(playerHandle.player.getMoney() - (Integer) action.object);
                            break;
                        case Packet.CALL:
                            TableInformation.getSingleton().incrementPot((Integer) action.object);
                            playerHandle.player.setCurrentBet(playerHandle.player.getCurrentBet() + (Integer) action.object);
                            playerHandle.player.setMoney(playerHandle.player.getMoney() - (Integer) action.object);
                            break;
                        case Packet.FOLD:
                            playerHandle.player.setFold(true);
                            break;
                    }

                    PokerHost.getHost().sync();
                }
            }
        }

        for (Player player : PokerHost.getHost().getPlayers()) {
            PlayerHandle playerHandle = PokerHost.getHost().getHandleForPlayer(player);
            playerHandle.lastCommand = null;
        }
    }

    private int findMinBet() {
        int max = Integer.MIN_VALUE;
        int min = Integer.MAX_VALUE;
        for (Player player : PokerHost.getHost().getPlayers()) {
            PlayerHandle playerHandle = PokerHost.getHost().getHandleForPlayer(player);

            max = Math.max(max, playerHandle.player.getCurrentBet());
            min = Math.min(min, playerHandle.player.getCurrentBet());
        }

        return max - min;
    }

    public boolean areAllChecked() {
        for (Player player : PokerHost.getHost().getPlayers()) {
            PlayerHandle playerHandle = PokerHost.getHost().getHandleForPlayer(player);
            if (playerHandle.lastCommand != null) {
                if (playerHandle.lastCommand.code != Packet.CHECK)
                    return false;
            } else
                return false;
        }
        return true;
    }

}
