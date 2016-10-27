
package com.theaigames.engine.io;

import bot.PlayerState;
import com.theaigames.game.texasHoldem.Player;
import com.theaigames.game.texasHoldem.move.PokerMove;

import java.io.IOException;
import java.util.Scanner;

public class HumanPlayer extends Player {

    private int errorCounter;
    private final int maxErrors = 2;
    private PlayerState state;

    public HumanPlayer() {
    	super();
    	state = new PlayerState();
    }

    public PokerMove requestMove() 
    {        
        System.out.println("Action " + getName() + " " + getTimePerMove());
        String response = getResponse(state.getTimePerMove());
        
        if(response == "") {
            addToDump("Error, action set to 'check'");
        } else {
            String[] parts = response.split("\\s");
            if(parts.length != 2)
                addToDump("Bot input '" + response + "' does not split into two parts. Action set to \"check\"");
            else
                return new PokerMove(parts[0], (int) Double.parseDouble(parts[1]));
        }
        
        return new PokerMove("check", 0);
    }

    public void sendInfo(String info) 
    {
		String line = info.trim();
		if( line.length() != 0 ) {
			String[] parts = line.split("\\s+");
			if( parts.length == 3 && parts[0].equals("Action") ) {
				// we need to move
				// PokerMove move = bot.getMove(state, Long.valueOf(parts[2]));
				// System.out.println(move.toString());
				// System.out.flush();
			} else if( parts.length == 3 && parts[0].equals("Settings") ) { 	// Update the state with settings info
				state.updateSetting(parts[1], parts[2]);
			} else if( parts.length == 3 && parts[0].equals("Match") ) { 		// Update the state with match info
				state.updateMatch(parts[1], parts[2]);
			} else if( parts.length == 3 && parts[0].startsWith("player")) { 	// Update the state with info about the moves
				state.updateMove(parts[0], parts[1], parts[2]);
			} else {
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

			if(timeElapsed >= timeOut) {
				addToDump("Response timed out (" + timeOut + "ms), let your bot return 'No moves' instead of nothing or make it faster.\n");
				this.errorCounter++;
                if (this.errorCounter > this.maxErrors) {
                    finish();
                }
                addToDump("Output from your bot: null");
				return "";
			} else {
				if (input.hasNext()) {
					response = input.nextLine();
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


