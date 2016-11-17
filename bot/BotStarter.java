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

import poker.HandHoldem;
import poker.PokerMove;

import bot.HandEval;

/**
 * This class is the brains of your bot. Make your calculations here and return the best move with GetMove
 */
public class BotStarter extends Bot {

	/**
	 * Implement this method to return the best move you can. Currently it will return a raise the ordinal value
	 * of one of our cards is higher than 9, a call when one of the cards has a higher ordinal value than 5 and
	 * a check otherwise.
	 * @param state : The current state of your bot, with all the (parsed) information given by the engine
	 * @param timeOut : The time you have to return a move
	 * @return PokerMove : The move you will be doing
	 */
	public PokerMove getMove(BotState state, Long timeOut) {
		HandHoldem hand = state.getHand();
		String handCategory = getHandCategory(hand, state.getTable()).toString();
		System.err.printf("my hand is %s, opponent action is %s, pot: %d\n", handCategory, state.getOpponentAction(), state.getPot());

		// Get the ordinal values of the cards in your hand
		int height1 = hand.getCard(0).getHeight().ordinal();
		int height2 = hand.getCard(1).getHeight().ordinal();
		
		// Return the appropriate move according to our amazing strategy
		if( height1 > 9 || height2 > 9 ) {
			return new PokerMove(state.getMyName(), "raise", 2*state.getBigBlind());
		} else if( height1 > 5 && height2 > 5 ) {
			return new PokerMove(state.getMyName(), "call", state.getAmountToCall());
		} else {
			return new PokerMove(state.getMyName(), "check", 0);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		BotParser parser = new BotParser(new BotStarter());
		parser.run();
	}

}
