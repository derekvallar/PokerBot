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

                } catch(IOException e) {
                    System.err.println("Writing to bot failed");
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
}
