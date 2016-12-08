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
import java.util.*;

import poker.HandHoldem;
import poker.PokerMove;


import poker.Card;
import com.theaigames.game.texasHoldem.table.BetRound;


import bot.HandEval;
import bot.HandEval.HandCategory;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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
	public PokerMove getMoveNew(BotState state, Long timeOut) {
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

	public PokerMove getMove(BotState state, Long timeOut) {
		HandHoldem hand = state.getHand();
		HandEval.HandCategory category = getHandCategory(hand, state.getTable());
		Card[] cards = state.getHand().getCards();
		int conf = getConfidenceLevel(cards[0].toString(), cards[1].toString());
		boolean goodKicker = isGoodKicker();
		boolean topPair = isTopPair();

		double multiplier = 0;
		// raising 1.5  1.7  2  2.5 3
		switch(state.getHistory().getPlayStyle()) {
			case "tight passive": multiplier = 3; break;
			case "loose passive": multiplier = 2.5; break;
			case "tight aggresive": multiplier = 1.7; break;
			case "loose aggresive": multiplier = 1.5; break;
			case "unsure": multiplier = 2; break;
			default: multiplier = 2; break;
		}


		// The preflop state
		if (state.getBetRound() == BetRound.PREFLOP) {
			
			// If starting round, and confidence is not good enough, flop
			// the number 26 may contain a pair, or A#, K# combination.
			if (conf < 15)
				return new PokerMove(state.getMyName(), "fold", 0);

			// Bot gets to decide first on the hand, doesnt know opponent decision
			// At this point, the cands are decent to continue playing
			if (state.onButton() == true) {

				// if somewhat confident, just call it
				// if really confident, bet twice amount
				if (conf < 40)
					return new PokerMove(state.getMyName(), "call", 0);
				else
					return new PokerMove(state.getMyName(), "raise", (int)(state.getCurrentBet() * multiplier));
			}
			else if (state.getOpponentAction().getAction().equals("raise")) {
				// amount to call = whatever the opponent raise
				// current bet = the previous agreed bet

				// too much of a raise by opponent, leavee!!
				if ((state.getAmountToCall() / state.getCurrentBet()) > 4)
					return new PokerMove(state.getMyName(), "fold", 0);
				else { // Opp's Raise is good enough, see if re-raise, or call

					if (conf > 40) {
						return new PokerMove(state.getMyName(), "raise", (int)(state.getAmountToCall() * multiplier));
					}
					else
						return new PokerMove(state.getMyName(), "call", 0);

				}

			}
			else if (state.getOpponentAction().getAction().equals("check")) {
				return new PokerMove(state.getMyName(), "check", 0);
			}
		}
		else if (state.getBetRound() == BetRound.FLOP) {
			HandEval.HandCategory score = getHandCategory(hand, state.getTable());
			double probToImprove = getProbability(state, score);
			int curPot = state.getPot();

			if (state.onButton() == true) { // bot goes first: FIX THIS LATER
				int totalPot = (2 * state.getAmountToCall()) + state.getPot();
				double xxxx = (double)state.getAmountToCall() / totalPot; // not sure what to name this LOL

				if (score == HandCategory.NO_PAIR) { // not worth to go with it
					return new PokerMove(state.getMyName(), "fold", 0);
				}
				else if (score == HandCategory.PAIR) {
					if (topPair && goodKicker) {
						return new PokerMove(state.getMyName(), "raise", (int)(state.getCurrentBet() * multiplier));
					}
					else if (topPair && !goodKicker) {
						return new PokerMove(state.getMyName(), "check", 0);
					}
					else if (isPair(state.getHand().getCards()[0].toString(), 
						state.getHand().getCards()[1].toString())) { // POCKET PAIR
						return new PokerMove(state.getMyName(), "raise", (int)(state.getCurrentBet() * multiplier));
					}
					else
						return new PokerMove(state.getMyName(), "check", 0);
				}
				else if (score == HandCategory.TWO_PAIR) {
					return new PokerMove(state.getMyName(), "raise", (int)(state.getCurrentBet() * multiplier));
				}
				else if (score == HandCategory.THREE_OF_A_KIND) {
					return new PokerMove(state.getMyName(), "raise", (int)(state.getCurrentBet() * multiplier));
				}
				else { // just raise high
					return new PokerMove(state.getMyName(), "raise", (int)(state.getCurrentBet() * multiplier));
				}
					
			}
			else if (state.getOpponentAction().getAction().equals("raise")) {
				int totalPot = (2 * state.getAmountToCall()) + state.getPot();
				double xxxx = (double)state.getAmountToCall() / totalPot; // not sure what to name this LOL

				if (score == HandCategory.NO_PAIR) { // not worth to go with it
					return new PokerMove(state.getMyName(), "fold", 0);
				}
				else if (score == HandCategory.PAIR) {
					if (topPair && goodKicker) {
						if (probToImprove - xxxx <= .10)
							return new PokerMove(state.getMyName(), "call", 0);
						else
							return new PokerMove(state.getMyName(), "raise", (int)(state.getAmountToCall() * multiplier));
					}
					else if (topPair && !goodKicker) {
						return new PokerMove(state.getMyName(), "call", 0);
					}
					else if (isPair(state.getHand().getCards()[0].toString(), 
						state.getHand().getCards()[1].toString())) { // POCKET PAIR

						if (probToImprove - xxxx <= .10)
							return new PokerMove(state.getMyName(), "call", 0);
						else
							return new PokerMove(state.getMyName(), "raise", (int)(state.getAmountToCall() * multiplier));
					}
					else
						return new PokerMove(state.getMyName(), "call", 0);
				}
				else if (score == HandCategory.TWO_PAIR) {
					return new PokerMove(state.getMyName(), "raise", (int)(state.getCurrentBet() * multiplier));
				}
				else if (score == HandCategory.THREE_OF_A_KIND) {

					if (probToImprove - xxxx <= .10)
						return new PokerMove(state.getMyName(), "raise", (int)(state.getAmountToCall() * multiplier));
					else
						return new PokerMove(state.getMyName(), "raise", (int)(state.getCurrentBet() * multiplier)); // probs increase later
				}
				else { // just raise high
					return new PokerMove(state.getMyName(), "raise", (int)(state.getCurrentBet() * multiplier));
				}
			}
			else if (state.getOpponentAction().getAction().equals("check")) {
				int totalPot = (2 * state.getAmountToCall()) + state.getPot();
				double xxxx = (double)state.getAmountToCall() / totalPot;

				if (probToImprove - xxxx <= .10)
					return new PokerMove(state.getMyName(), "check", 0); // decides to check as well
				else // raises by twice amount
					return new PokerMove(state.getMyName(), "raise", (int)(state.getCurrentBet() * multiplier));
			}
			else 
				return new PokerMove(state.getMyName(), "check", 0); // default 

		}
		else if (state.getBetRound() == BetRound.TURN) {
			HandEval.HandCategory score = getHandCategory(hand, state.getTable());
			double probToImprove = getProbability(state, score);
			int curPot = state.getPot();
			int totalPot = (2 * state.getAmountToCall()) + state.getPot();
			double xxxx = (double)state.getAmountToCall() / totalPot; // 
			/*public static enum HandCategory { NO_PAIR, PAIR, TWO_PAIR, THREE_OF_A_KIND, STRAIGHT,
							FLUSH, FULL_HOUSE, FOUR_OF_A_KIND, STRAIGHT_FLUSH; } */

			if (state.onButton() == true) {
				if (score == HandCategory.PAIR) {
					if (xxxx < probToImprove) // not worth it to go on
						return new PokerMove(state.getMyName(), "fold", 0);
					else if (probToImprove - xxxx <= .10)
						return new PokerMove(state.getMyName(), "check", 0);
					else
						return new PokerMove(state.getMyName(), "raise", (int)(state.getCurrentBet() * multiplier)); // probs increase later
				}
				else
					return new PokerMove(state.getMyName(), "raise", (int)(state.getCurrentBet() * multiplier));
			}
			else if (state.getOpponentAction().getAction().equals("raise")) {
				if (score == HandCategory.PAIR) {
					if (xxxx < probToImprove) // not worth it to go on
						return new PokerMove(state.getMyName(), "fold", 0);
					else if (probToImprove - xxxx <= .10)
						return new PokerMove(state.getMyName(), "check", 0);
					else
						return new PokerMove(state.getMyName(), "raise", (int)(state.getAmountToCall() * multiplier));
				}
				else
					return new PokerMove(state.getMyName(), "call", 0);
			}
			else if (state.getOpponentAction().getAction().equals("check")) {
				return new PokerMove(state.getMyName(), "check", 0);
			}
			else 
				return new PokerMove(state.getMyName(), "check", 0); // default

		}
		else if (state.getBetRound() == BetRound.RIVER) {
			HandEval.HandCategory score = getHandCategory(hand, state.getTable());
			if (state.onButton() == true) {
				if (score == HandCategory.PAIR || score == HandCategory.TWO_PAIR || 
					score == HandCategory.THREE_OF_A_KIND)
					return new PokerMove(state.getMyName(), "check", 0);
				else
					return new PokerMove(state.getMyName(), "raise", (int)(state.getCurrentBet() * multiplier));
			}
			else
				return new PokerMove(state.getMyName(), "check", 0); 
		}
		else
			return new PokerMove(state.getMyName(), "check", 0);


		return new PokerMove(state.getMyName(), "check", 0); // to please compiler
	}


	private boolean isGoodKicker(BotState state) {
		HandHoldem hand = state.getHand();
		Card[] table = state.getTable();
		
		Card[] totalCard = new Card[table.length + 2];
		
		totalCard[0] = hand.getCard(0);
		totalCard[1] = hand.getCard(1);
		
		for (int i = 0; i < table.length; i++) {
			totalCard[i + 2] = table[i];
		}
		
		Set<Card> cardSet = new HashSet<>();
        for (Card card : totalCard) {
            if (cardSet.contains(card)) {
                cardSet.remove(card);
            }
            else {
                cardSet.add(card);
            }
        }

        Card maxCard = Collections.max(cardSet);
		return maxCard.getHeight().ordinal() >= 9;
	}

	private boolean isTopPair(BotState state) {
		HandHoldem hand = state.getHand();
		Card[] table = state.getTable();
		Arrays.sort(table);
		Card maxCard = table[table.length - 1];
		return hand.getCard(0).equals(maxCard) || hand.getCard(1).equals(maxCard);
	}

	// This returns a probability percentage of improving hand
    // Uses the number of outs (cards to improve hand) to develop calculation
    // On  the flop stage, multiply the outs times 4, on not the flop stage, multiply times 2
    public int getProbability(BotState state, HandEval.HandCategory curScore) {
    	int odds = calculateOdds(state, curScore);

    	return state.getBetRound() == BetRound.FLOP ? odds * 4 : odds * 2;
    }

    public HashMap<String, Card> fullDeck() {
    	HashMap<String, Card> deck = new HashMap<String, Card>();

    	for (int i = 0; i < 52; i++) {
    		Card card = new Card(i);
    		deck.put(card.toString(), card);
    	}

    	return deck;
    }

    public int calculateOdds(BotState state, HandEval.HandCategory curScore) {
    	//int currentMax = curScore;
    	int singleOuts = 0;


    	// Mocking a deck guesses
    	HashMap<String,Card> deck = fullDeck();
    	ArrayList<String> improves = new ArrayList<String>(); // cards that already improved hand

		// cards shown on table + 2 hand cards + 1 possible extra
    	Card temp[] = new Card[state.getTable().length + 1]; 

    	// removing the cards appearing hand, and in table from possible solutions
    	// storing them to build a 'solution' hand
    	deck.remove(state.getHand().getCards()[0].toString());
    	deck.remove(state.getHand().getCards()[1].toString());

    	for (int i = 0; i < state.getTable().length; i++) {
    		deck.remove(state.getTable()[i].toString());
    		temp[i] = state.getTable()[i]; // building base for possible new table combination
    	}

    	

    	// This helps determin how many cards can actually improve score
    	// ON THE FLOP: what will the 6th card do to help
    	// ON THE RIVER: what will the 7th card to help
    	for (String key : deck.keySet()) {
    		temp[temp.length - 1] = deck.get(key); // filling a next possible card

    		// if card has not already been recorded as making improvement
    		// and if that improvement is better than current score
    		if (!improves.contains(deck.get(key)) && getHandCategory(state.getHand(), temp).ordinal() > curScore.ordinal())
    			singleOuts++;
    	}

    	return singleOuts;
    }

	public boolean isGoodKicker() {
		return true;
	}

	public boolean isTopPair() {
		return true;
	}

	// FORMAT OF CARD: <rank><suit> : i.e. 3s, Ah
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

	public boolean isAceOrKing(String card1, String card2) {

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
            default:
                num = -1;
                break;

        }

        return num;

    }

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		BotParser parser = new BotParser(new BotStarter());
		parser.run();
	}

}
