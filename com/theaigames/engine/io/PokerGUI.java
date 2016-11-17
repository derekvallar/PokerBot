package com.theaigames.engine.io;

public class PokerGUI {
	
	String name;
	String dealer;
	String[] tableCards;

	int round;
	int potSize;
	int amountToCall;
	int[] chips;
	int blindPaid;

	public PokerGUI() {
		tableCards = new String[5];
		chips = new int[2];
	}

	public void update(String line) {
		System.out.println(line);

		String[] parts = line.split("\\s+");

		if (parts.length == 3) {

			if (parts[0].equals("Settings") ) { 	// Update the state with settings info
				name = parts[2];
			}
			else if (parts[0].equals("Match") ) { 		// Update the state with match info
				if (parts[1].equals("round")) {
					round = Integer.parseInt(parts[2]);
				}
				else if (parts[1].equals("on_button")) {
					dealer = parts[2];
				}
			}
			else if (parts[0].startsWith("player")) { 	// Update the state with info about the moves

			}
		}
	}

	private void clearScreen() {
		System.out.print("\033[H\033[2J");  
    	System.out.flush(); 
	}
}