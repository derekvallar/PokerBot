// Copyright 2015 theaigames.com (developers@theaigames.com)

//    Licensed under the Apache License, Version 2.0 (the "License");
//    you may not use this file except in compliance with the License.
//    You may obtain a copy of the License at

//        http://www.apache.org/licenses/LICENSE-2.0

//    Unless required by applicable law or agreed to in writing, software
//    distributed under the License is distributed on an "AS IS" BASIS,
//    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//    See the License for the specific language governing permissions and
//    limitations under the License.
//	
//    For the full copyright and license information, please view the LICENSE
//    file that was distributed with this source code.

package com.theaigames.engine.io;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Scanner;

import poker.Card;
import poker.HandHoldem;
import bot.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import bot.HandEval;
import bot.HandEval.HandCategory;

/**
 * IOPlayer class
 * 
 * Does the communication between the bot process and the engine
 * 
 * @author Jackie Xu <jackie@starapple.nl>, Jim van Eeden <jim@starapple.nl>
 */
public class HumanIOPlayer extends IOPlayer {
    
    PokerGUI gui;

    public HumanIOPlayer(Process process) {
        super(process);
        gui = new PokerGUI();
    }

    public void process(String line, String type) throws IOException {
        if (!this.finished) {
            switch (type) {
            case "input":
                try {
                    this.inputStream.write(line + "\n");
                    this.inputStream.flush();
                    gui.update(line);

                }
                catch(IOException e) {
                    System.err.println("Writing to bot failed");
                }
                catch (InterruptedException e) {
                    System.err.println("printDelay() error: " + e);
                }
                addToDump(line + "\n");


                break;
            case "output":
    //          System.out.println("out: " + line);
                break;
            case "error":
    //          System.out.println("error: " + line);
                break;
            }
        }
    }

    // waits for a response from the bot
    public String getResponse(long timeOut) {
        String savedResponse;
        Scanner scan = new Scanner(System.in);
    	long timeStart = System.currentTimeMillis();

    	if (this.errorCounter > this.maxErrors) {
    		addToDump("Maximum number (" + this.maxErrors + ") of time-outs reached: skipping all moves.\n");
    		return "";
    	}

        if (gui.tableCards.size() == 0) { 
            double conf = ((double)getConfidenceLevel(gui.c_hand[0].toString(),gui.c_hand[1].toString()) / 55 ) * 100;

            if (isPair(gui.s_hand[0], gui.s_hand[1]))
                System.out.println("You have a pair!");

            System.out.printf("Tip: This hand has %.2f percent chance of winning!\n",conf);
        }
        else if (gui.tableCards.size() == 3) {
            System.out.println("You currently have " + getScoreMessage());
            System.out.println("Tip: The next card has a " + getProbability() + "% probability of improving your hand.");
        }
        else if (gui.tableCards.size() == 4) {
            System.out.println("You currently have " + getScoreMessage());
            System.out.println("Tip: The next card has a " + getProbability() + "% probability of improving your hand.");
        }
        else
            System.out.println();

        System.out.println("What would you like to do? ");
		String line = scan.nextLine().toLowerCase();

        while (!line.contains("raise") && !line.contains("call")
         && !line.contains("check") && !line.contains("fold")) {
            System.out.println("Unknown response.");
            System.out.println("What would you like to do? ");
            line = scan.nextLine().toLowerCase();
        }

        try {
            inputStream.write("User " + line + "\n");
            inputStream.flush();
        }
        catch(IOException e) {
            System.err.println("Writing to human failed");
        }

        while (this.response == null) {
            try {
                Thread.sleep(50);
            }
            catch (InterruptedException e) {}
        }

		if(this.response.equalsIgnoreCase("No moves")) {
			this.response = null;
            addToDump("Output from your bot: \"No moves\"\n");
			return "";
		}

		savedResponse = this.response;
		this.response = null;

		addToDump("Output from your bot: \"" + savedResponse + "\"\n");
		return savedResponse;
    }

    public String getScoreMessage() {
        HandHoldem tempHand = new HandHoldem(Card.getCard(gui.s_hand[0]), Card.getCard(gui.s_hand[1]));
        Card[] tempTable = new Card[gui.s_tableCards.size()];

        for (int i = 0; i < gui.s_tableCards.size(); i++)
            tempTable[i] = Card.getCard(gui.s_tableCards.get(i));

        HandEval.HandCategory score = getHandCategory(tempHand, tempTable);


        String str = "";
        //str += "[" + Card.getCard(gui.s_hand[0]).toString() + "] " + "[" + Card.getCard(gui.s_hand[1]).toString() + "] " + "[" + 
        //  Card.getCard(gui.s_tableCards.get(0)).toString() + "] " + "[" + Card.getCard(gui.s_tableCards.get(1)).toString() + "] " + "[" + Card.getCard(gui.s_tableCards.get(2)).toString() + "] ";

        switch (score) {
            case NO_PAIR: str += "nothing, not even pair.";break;
            case PAIR: str +="a pair!"; break;
            case TWO_PAIR: str +="two pairs!"; break;
            case THREE_OF_A_KIND: str +="a three of a kind!"; break;
            case STRAIGHT: str +="a straight!"; break;
            case FLUSH: str +="a flush!"; break;
            case FULL_HOUSE: str +="a full house!"; break;
            case FOUR_OF_A_KIND: str +="a four of a kind!"; break;
            case STRAIGHT_FLUSH: str +="a straight flush!"; break;
            default: str +="AN ERROR!"; break;
        }

        return str;
    }


    public int getConfidenceLevel(String card1, String card2) {
        int confidence = 0;
        boolean temp = false;


        confidence = getRank(card1) + getRank(card2);
        if(isPair(card1,card2))
            confidence += 27; // Bonus cause its a pair
        else { // No pair, but the cards might be good for a flush, or hold A or K
            if (isAceOrKing(card1, card2))
                confidence += 15;
            else
                confidence += (11 - Math.abs((getRank(card1) - getRank(card2))));
        
        }
        return confidence;
    }

    private boolean isAceOrKing(String card1, String card2) {

        if (getRank(card1) > 12 || getRank(card2) > 12)
            return true;
        else
            return false;
    }

    public boolean isPair(String card1, String card2) {
        return getRank(card1) == getRank(card2); 
    }

    public int getRank(String val) {
        int num = 0; 
            
        try {
           num = Integer.parseInt(val.substring(0, val.length()-1));
           return num;
        } catch(NumberFormatException e) {val = val.substring(0, val.length()-1);}
       switch(val) {
            case "A":
                num = 14;
                break;
            case "K":
                num = 13;
                break;
            case "Q":
                num = 12;
                break;
            case "J":
                num = 11;
                break;
            case "T":
                num = 10;
                break;
            default:
                num = -1;
                break;

        }

        return num;
    }

    public int getProbability() {
        HandHoldem tempHand = new HandHoldem(Card.getCard(gui.s_hand[0]), Card.getCard(gui.s_hand[0]));
        Card[] tempTable = new Card[gui.s_tableCards.size()];
        for (int i = 0; i < gui.s_tableCards.size(); i++)
            tempTable[i] = Card.getCard(gui.s_tableCards.get(i));

        HandEval.HandCategory score = getHandCategory(tempHand, tempTable);
        int odds = calculateOdds(tempHand, score);

        return gui.s_tableCards.size() == 3 ? odds * 4 : odds * 2;
    }

    public HashMap<String, Card> fullDeck() {
        HashMap<String, Card> deck = new HashMap<String, Card>();

        for (int i = 0; i < 52; i++) {
            Card card = new Card(i);
            deck.put(card.toString(), card);
        }

        return deck;
    }

    public int calculateOdds(HandHoldem hand, HandEval.HandCategory score) {
        int singleOuts = 0;
        HashMap<String, Card> deck = fullDeck();
        ArrayList<String> improves = new ArrayList<String>();

        // cards shown on table + 2 hand cards + 1 possible extra
        Card temp[] = new Card[gui.s_tableCards.size() + 1];

        // removing the cards appearing hand, and in table from possible solutions
        // storing them to build a 'solution' hand
        deck.remove(gui.s_hand[0]);
        deck.remove(gui.s_hand[1]);

        for (int i = 0; i < gui.s_tableCards.size(); i++) {
            deck.remove(gui.s_tableCards.get(i));
            temp[i] = Card.getCard(gui.s_tableCards.get(i));
        }
        
        // This helps determin how many cards can actually improve score
        // ON THE FLOP: what will the 6th card do to help
        // ON THE RIVER: what will the 7th card to help
        for (String key : deck.keySet()) {
            temp[temp.length - 1] = deck.get(key); // filling a next possible card

            // if card has not already been recorded as making improvement
            // and if that improvement is better than current score
            if (!improves.contains(key) && getHandCategory(hand, temp).ordinal() > score.ordinal()) {
                singleOuts++;
                improves.add(key);
            }
        }

        return singleOuts;
    }
}
