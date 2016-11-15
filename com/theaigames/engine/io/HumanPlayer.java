
package com.theaigames.engine.io;

import com.theaigames.engine.io.PlayerState;
import com.theaigames.game.texasHoldem.Player;
import com.theaigames.game.texasHoldem.move.PokerMove;

import java.io.IOException;
import java.util.Scanner;

public class HumanPlayer extends Player {

    private int errorCounter;
    private final int maxErrors = 2;

    public HumanPlayer(String name) {
    	super(name);
    	state = new PlayerState();
    }

    public PokerMove requestMove()
    {
        String response = getResponse(state.getTimePerMove());
        
        if(response == "") {
            addToDump("Error, action set to 'check'");
        } 
        else {
            String[] parts = response.split("\\s");
            if(parts.length != 2)
                addToDump("Bot input '" + response + "' does not split into two parts. Action set to \"check\"");
            else
                return new PokerMove(parts[0], (int) Double.parseDouble(parts[1]));
        }

        return new PokerMove("check", 0);
    }

    public void sendInfo(String info) {
		String line = info.trim();
		if (line.length() == 0) {
            return;
        }

		String[] parts = line.split("\\s+");

        if (parts.length % 3 != 0) {
            System.err.printf("Unable to parse line ``%s''\n", line);
            return;
        }

        for (int i = 0; i < parts.length / 3; i++) {
            int index = i * 3;
            if(parts[index].equals("Action") ) {
                // we need to move
                // PokerMove move = bot.getMove(state, Long.valueOf(parts[2]));
                // System.out.println(move.toString());
                // System.out.flush();
            } 
            else if(parts[index].equals("Settings") ) {     // Update the state with settings info
                state.updateSetting(parts[index + 1], parts[index + 2]);
            }
            else if(parts[index].equals("Match") ) {        // Update the state with match info
                state.updateMatch(parts[index + 1], parts[index + 2]);
            }
            else if(parts[index].startsWith("player")) {    // Update the state with info about the moves
                state.updateMove(parts[index], parts[index + 1], parts[index + 2]);
            }
            else {
                System.err.printf("Unable to parse line ``%s''\n", line);
            }
        }
    }

    public String getResponse(long timeOut) {
    	long timeStart = System.currentTimeMillis();
    	String response = null;
    	Scanner input = new Scanner(System.in);

    	if (this.errorCounter > this.maxErrors) {
    		addToDump("Maximum number (" + this.maxErrors + ") of time-outs reached: skipping all moves.\n");
    		return "";
    	}

    	System.out.println("What would you like to do? You have: " + timeOut);

    	while(response == null) {
    		long timeNow = System.currentTimeMillis();
			long timeElapsed = timeNow - timeStart;

System.err.println("Time Elapsed: " + timeElapsed);

			if(timeElapsed >= timeOut) {

System.err.println("Timed out.");

				addToDump("Response timed out (" + timeOut + "ms), let your bot return 'No moves' instead of nothing or make it faster.\n");
				this.errorCounter++;
                if (this.errorCounter > this.maxErrors) {
                    finish();
                }
                addToDump("Output from your bot: null");
				return "";
			} else {

System.err.println("Still time.");

				if (input.hasNext()) {

System.err.println("input hasNext().");

					response = input.nextLine();
				}
                else {
System.err.println("input does not hasNext().");
                }
			}

			try { Thread.sleep(100); } catch (InterruptedException e) {}
    	}
		if(response.equalsIgnoreCase("No moves")) {
			response = null;
            addToDump("Output from your bot: \"No moves\"\n");
			return "";
		}

		addToDump("Output from your bot: \"" + response + "\"\n");
		return response;
    }

    public void finish() {};

}


