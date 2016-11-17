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

import poker.HandHoldem;
import poker.PokerMove;

import bot.HandEval;

/**
 * This class is the brains of your bot. Make your calculations here and return the best move with GetMove
 */
public class HumanStarter extends Bot {

	public PokerMove getMove(BotState state, String line) {

		if (line.contains("raise")) {
			int amount = Integer.parseInt(line.replaceAll("[^0-9]+", ""));
			return new PokerMove(state.getMyName(), "raise", amount);
		}
		else if (line.contains("call")) {
			return new PokerMove(state.getMyName(), "call", state.getAmountToCall());
		}
		else if (line.contains("check")) {
			return new PokerMove(state.getMyName(), "check", 0);
		}
		else {
			return new PokerMove(state.getMyName(), "fold", 0);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		HumanParser parser = new HumanParser(new HumanStarter());
		parser.run();
	}
}
