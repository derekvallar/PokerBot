package com.theaigames.engine.io;

import java.lang.Integer;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import poker.HandHoldem;
import poker.Card;

public class PokerGUI {
    
    TimeUnit unit = TimeUnit.MILLISECONDS;

    int DRAWSPEED = 3;
    int FAST = 20;
    int MEDIUM = 70;
    int SLOW = 180;

    int PAUSE = 1700;
    int QUICKPAUSE = 500;

    String name;
    String dealer;
    ArrayList<DisplayCard> tableCards;
    ArrayList<String> s_tableCards;
    ArrayList<Card> c_tableCards;
    DisplayCard[] hand;
	DisplayCard[] opponentsHand;
    public Card[] c_hand;
    public String[] s_hand;
    String[] lastAction;
    String[] lastActionValue;

    int round;
    boolean roundChange;
    boolean gameStart;
    boolean preflop;

    int potSize;
    int[] chips;
    int[] post;
    int[] bets;

    int amountToCall;
    boolean betToMatch;
    boolean drawOpponentsHand;

    // Changed the name from Card to DisplayCard
    // Cause i needed the actual object Card
    class DisplayCard {
        char rank;
        char suit;

        public DisplayCard(String value) {
            rank = value.charAt(0);
            char temp = value.charAt(1);

            if (temp == 'd') {
                suit = '♦';
            }
            else if (temp == 'c') {
                suit = '♣';
            }
            else if (temp == 'h') {
                suit = '♥';
            }
            else {
                suit = '♠';
            }
        }
    }

    public PokerGUI() {
        hand = new DisplayCard[2];
		opponentsHand = new DisplayCard[2];
        c_hand = new Card[2];
        s_hand = new String[2];
        tableCards = new ArrayList<DisplayCard>();
        s_tableCards = new ArrayList<String>();
        c_tableCards = new ArrayList<Card>();

        lastAction = new String[2];
        lastActionValue = new String[2];

        chips = new int[2];
        post = new int[2];
        bets = new int[2];

        roundChange = false;
        preflop = true;
        betToMatch = false;
        drawOpponentsHand = false;
        gameStart = true;
    }

    public void update(String line) throws InterruptedException {
        int amount;

        // System.out.print("PokerGUI - Processing: " + line);
        // System.out.println(" (end)***");

        String[] parts = line.split("\\s+");

        if (parts.length % 3 != 0) {
            return;
        }

        for (int i = 0; i < parts.length / 3; i++) {

            String type = parts[3*i];
            String setting = parts[3*i + 1];
            String value = parts[3*i + 2];

            if (type.equals("Settings") && setting.equals("your_bot")) {        // Update the state with settings info
                name = value;
            }
            else if (type.equals("Match")) {    // Update the state with match info
                switch (setting) {
                    case "round":
                        round = Integer.parseInt(value);
                        roundChange = true;
                        preflop = true;
                        post[0] = 0;
                        post[1] = 0;
                        bets[0] = 0;
                        bets[1] = 0;
                        potSize = 0;
                        betToMatch = true;

                        break;

                    case "on_button":
                        dealer = value;

                        break;

                    case "table":
                        preflop = false;
                        String[] cards = value.substring(1, value.length() - 1).split(",");
                        tableCards.clear();
                        s_tableCards.clear();
                        for (String s : cards) {
                            tableCards.add(new DisplayCard(s));
                            s_tableCards.add(s);
                        }
                        betToMatch = false;

                        drawUI();
                        break;

                    case "max_win_pot":
                        potSize = Integer.parseInt(value);
                        printDelay(String.format("\nPot: %-7d Your current bet: %-7d Opponent's current bet: %-7d\n",
                         potSize, bets[0], bets[1]), FAST);
                        printDelay(String.format("Your stack: %-7d Opponent stack: %-7d\n",
                         chips[0], chips[1]), FAST);

                        break;

                    case "amount_to_call":
                        amountToCall = Integer.parseInt(value);
                        break;

                    default:
                }
            }
            else if (type.startsWith("player")) {   // Update the state with info about the moves
                int player = type.equals("player1") ? 0 : 1;
                switch (setting) {
                    case "stack":
                        chips[player] = Integer.parseInt(value);
                        break;

                    case "post":
                        post[player] = Integer.parseInt(value);
                        chips[player] -= post[player];
                        bets[player] += post[player];
                        potSize += post[player];

                        break;

                    case "hand":
                        String[] handarray = value.substring(1, value.length() - 1).split(",");
                        
                        if (player == 0) {
                            hand[0] = new DisplayCard(handarray[0]);
                            hand[1] = new DisplayCard(handarray[1]);

                            c_hand[0] = Card.getCard(handarray[0]);
                            c_hand[1] = Card.getCard(handarray[1]);
                            s_hand[0] = handarray[0];
                            s_hand[1] = handarray[1];
                        }
                        else {
                            opponentsHand[0] = new DisplayCard(handarray[0]);
                            opponentsHand[1] = new DisplayCard(handarray[1]);
                            drawOpponentsHand = true;
                        }

                        if (roundChange || drawOpponentsHand) {
                            drawUI();
                        }
                        
                        break;

                    case "wins":
                        if (player == 0) {
                            printDelay("You won this round! + "+ value + "\n", MEDIUM);
                        }
                        else {
                            printDelay("Your opponent has won this round! + "+ value + "\n", MEDIUM);
                        }
                        s_tableCards = new ArrayList<String>();
                        s_hand = new String[2];

                        unit.sleep(PAUSE);
                        break;

                    case "fold":
                        if (player == 0) {
                            printDelay("You have folded.\n", MEDIUM);
                        }
                        else {
                            printDelay("Your opponent has folded.\n", MEDIUM);
                        }

                        unit.sleep(PAUSE);
                        break;

                    case "check":
                        if (player == 0) {
                            printDelay("You have checked.\n", MEDIUM);
                        }
                        else {
                            printDelay("Your opponent has checked.\n", MEDIUM);
                        }

                        unit.sleep(PAUSE);
                        break;

                    case "call":
                        amount = Integer.parseInt(value);
                        if (player == 0) {
                            printDelay("You have called (" + amount + ").\n", MEDIUM);
                        }
                        else {
                            printDelay("Your opponent has called (" + amount + ").\n", MEDIUM);
                        }

                        chips[player] -= amount;
                        bets[player] += amount;
                        betToMatch = false;
                        unit.sleep(PAUSE);
                        break;

                    case "raise":
                        int diff = 0;
                        amount = Integer.parseInt(value);

                        if (player == 0) {
                            printDelay("You have ", MEDIUM);
                            if (betToMatch) {
                                diff = bets[1] - bets[0];
                                printDelay("matched your opponent's bet (" + bets[1] + ") and ", MEDIUM);
                            }
                        }
                        else {
                            printDelay("Your opponent has ", MEDIUM);
                            if (betToMatch) {
                                diff = bets[0] - bets[1];
                                printDelay("matched your bet (" + bets[0] + ") and ", MEDIUM);
                            }
                        }
                        printDelay("raised by: " + value + "\n", MEDIUM);

                        bets[player] += amount + diff;
                        chips[player] -= amount + diff;
                        betToMatch = true;
                        unit.sleep(PAUSE);

                        break;

                    default:
                        lastAction[player] = setting;
                        lastActionValue[player] = value;
                }
            }
        }
    }

    private void drawUI() throws InterruptedException {
        clearScreen();
        
        if (roundChange) {
            printDelay("Round: " + round + "\n\n", SLOW);
            roundChange = false;
            unit.sleep(PAUSE);
        }
        else {
            System.out.print("Round: " + round + "\n\n");
        }

        drawTable();
        if (gameStart) {
            printDelay(String.format("\nPot: %-7d Your current bet: %-7d Opponent's current bet: %-7d\n",
             potSize, bets[0], bets[1]), FAST);
            printDelay(String.format("Your stack: %-7d Opponent stack: %-7d\n",
             chips[0], chips[1]), FAST);

            gameStart = false;
        }
        unit.sleep(QUICKPAUSE);
    }

    private void drawTable() throws InterruptedException {
        
        if (preflop) {
            printDelay(dealer + " is the dealer.\n", FAST);
            unit.sleep(QUICKPAUSE);
            
            printDelay("You post " + post[0] + " chips.\n", FAST);
            unit.sleep(QUICKPAUSE);

            printDelay("Your opponent posts " + post[1] + " chips.\n", FAST);
            unit.sleep(QUICKPAUSE);

            for (int i = 0; i < 6; i++) {
                System.out.println();
            }
        }
        else {
            int cards = tableCards.size();
            for (int i = 0; i < cards; i++) {
                printDelay(" ┌─────────┐", DRAWSPEED);
            }
            System.out.println();
            
            for (int i = 0; i < cards; i++) {
                printDelay(" │ "+tableCards.get(i).rank+tableCards.get(i).suit+"      │", DRAWSPEED);
            }
            System.out.println();

            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < cards; j++) {
                    printDelay(" │         │", DRAWSPEED);
                }
                System.out.println();
            }

            for (int i = 0; i < cards; i++) {
                printDelay(" │      "+tableCards.get(i).rank+tableCards.get(i).suit+" │", DRAWSPEED);
            }
            System.out.println();

            for (int i = 0; i < cards; i++) {
                printDelay(" └─────────┘", DRAWSPEED);
            }
            System.out.print("\n\n");
        }
    
        // printDelay("     ┌────┌─────────┐\n", DRAWSPEED);
        // printDelay("     │ "+hand[0].rank+hand[0].suit+" │ "+hand[1].rank+hand[1].suit+"      │\n", DRAWSPEED);
        // printDelay("     │    │         │\n", DRAWSPEED);
        // printDelay("     │    │         │\n", DRAWSPEED);

        String[] opponentsHandDrawn = new String[10];
        for (int i = 0; i < 10; i++) {
            opponentsHandDrawn[i] = "\n";
        }

        if (drawOpponentsHand) {
            opponentsHandDrawn[0] = "           Opponent's Hand:\n";
            opponentsHandDrawn[1] = "           ┌─────────┐\n";
            opponentsHandDrawn[2] = "           │ "+opponentsHand[0].rank+opponentsHand[0].suit+" ┌─────────┐\n";
            opponentsHandDrawn[3] = "           │    │ "+opponentsHand[1].rank+opponentsHand[1].suit+"      │\n";
            opponentsHandDrawn[4] = "           │    │         │\n";
            opponentsHandDrawn[5] = "           │    │         │\n";
            opponentsHandDrawn[6] = "           │    │         │\n";
            opponentsHandDrawn[7] = "           │    │         │\n";
            opponentsHandDrawn[8] = "           └────│      "+opponentsHand[1].rank+opponentsHand[1].suit+" │\n";
            opponentsHandDrawn[9] = "                └─────────┘\n";
            drawOpponentsHand = false;
        }

        printDelay("     Your Hand:      " + opponentsHandDrawn[0], DRAWSPEED);
        printDelay("     ┌─────────┐     " + opponentsHandDrawn[1], DRAWSPEED);
        printDelay("     │ "+hand[0].rank+hand[0].suit+" ┌─────────┐" + opponentsHandDrawn[2], DRAWSPEED);
        printDelay("     │    │ "+hand[1].rank+hand[1].suit+"      │" + opponentsHandDrawn[3], DRAWSPEED);
        printDelay("     │    │         │" + opponentsHandDrawn[4], DRAWSPEED);
        printDelay("     │    │         │" + opponentsHandDrawn[5], DRAWSPEED);
        printDelay("     │    │         │" + opponentsHandDrawn[6], DRAWSPEED);
        printDelay("     │    │         │" + opponentsHandDrawn[7], DRAWSPEED);
        printDelay("     └────│      "+hand[1].rank+hand[1].suit+" │" + opponentsHandDrawn[8], DRAWSPEED);
        printDelay("          └─────────┘" + opponentsHandDrawn[9], DRAWSPEED);
    }

    private void clearScreen() {
        System.out.print("\033[H\033[2J");  
        System.out.flush(); 
    }

    private void printDelay(String data, long delay) throws InterruptedException {
        for (char c : data.toCharArray()) {
             System.out.print(c);
             unit.sleep(delay);
        }
   }
}