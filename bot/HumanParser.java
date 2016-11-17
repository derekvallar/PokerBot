/**
 * www.TheAIGames.com 
 * Heads Up Omaha pokerbot
 *
 * Last update: May 07, 2014
 *
 * @author Jim van Eeden, Starapple
 * @version 1.0
 * @License MIT License (http://opensource.org/Licenses/MIT)
 */


package bot;

import java.util.Scanner;

import poker.PokerMove;

/**
 * Class that reads the engine's input and asks the bot Class to calculate the next move.
 * Then returns that move to the engine.
 */
public class HumanParser {
	
	final Scanner scan;
	final HumanStarter human;

	public HumanParser(HumanStarter human) {
		this.scan = new Scanner(System.in);
		this.human = human;
	}

	public void run() {
		BotState currentState = new BotState();
		while (scan.hasNextLine()) {
			String line = scan.nextLine().trim();
			if (line.length() == 0) {
				continue;
			}

			String[] parts = line.split("\\s+");

			if (parts.length == 3 && parts[0].equals("Action") ) {
				// Do nothing
			}
			else if (parts.length == 3 && parts[0].equals("Settings") ) { 	// Update the state with settings info
				currentState.updateSetting(parts[1], parts[2]);
			}
			else if (parts.length == 3 && parts[0].equals("Match") ) { 		// Update the state with match info
				currentState.updateMatch(parts[1], parts[2]);
			}
			else if (parts.length == 3 && parts[0].startsWith("player")) { 	// Update the state with info about the moves
				currentState.updateMove(parts[0], parts[1], parts[2]);
			}
			else if (parts[0].equals("User")) {
				PokerMove move = human.getMove(currentState, line);
				System.out.println(move.toString());
				System.out.flush();
			}
			else {
				System.err.printf("Unable to parse line ``%s''\n", line);
			}
		}
	}
}
