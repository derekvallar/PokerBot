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

package com.theaigames.game.texasHoldem;

import java.io.IOException;

import com.theaigames.engine.io.IOPlayer;
import com.theaigames.game.texasHoldem.move.PokerMove;

/**
 * Class that represents one Robot object and stores additional information such as the name that the bot receives and
 * which person is the author.
 */
public abstract class Player
{
	private String name;
	private long timePerMove;

    private StringBuilder dump;

    public Player() {
    	this.dump = new StringBuilder();
    }

	// public Player(String name, IOPlayer bot, long timePerMove)
	// {
	// 	this.bot = bot;
	// 	this.name = name;
	// 	this.timePerMove = timePerMove;
	// }

	// public Player(String name, HumanPlayer human, long timePerMove)
	// {
	// 	this.human = human;
	// 	this.name = name;
	// 	this.timePerMove = timePerMove;
	// }

	/**
	 * @return The String name of this Player
	 */
	public String getName() {
		return name;
	}

	public void setTimePerMove(long time) {
		this.timePerMove = time;
	}

	public long getTimePerMove() {
		return timePerMove;
	}

	public abstract void sendInfo(String info);
	
	public abstract PokerMove requestMove();

    public abstract void finish();

	public void addToDump(String dumpy){
		dump.append(dumpy);
	}
    
    public String getDump() {
    	return dump.toString();
    }
}
