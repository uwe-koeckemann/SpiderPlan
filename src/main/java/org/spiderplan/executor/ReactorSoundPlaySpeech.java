/*******************************************************************************
 * Copyright (c) 2015 Uwe Köckemann <uwe.kockemann@oru.se>
 *  
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *******************************************************************************/
package org.spiderplan.executor;

import org.spiderplan.representation.expressions.Statement;
import org.spiderplan.tools.ExecuteSystemCommand;

/**
 * Execute ROS sound-play text-to-speech to say something.
 * 
 * @author Uwe Koeckemann
 *
 */
public class ReactorSoundPlaySpeech extends Reactor {
	
	TalkThread execThread;
	
	/**
	 * Constructor using target statement and text that will
	 * be said.
	 * 
	 * @param target statement to be executed
	 * @param text text to be converted to speech
	 */
	public ReactorSoundPlaySpeech( Statement target, String text ) {
		super(target);
		this.execThread = new TalkThread(text);
	}
	
	@Override
	public void initStart( ) {
		execThread.start();
	}
	
	
	@Override
	public boolean hasStarted( long EST, long LST ) {
		return execThread.started;
	}
	
	@Override
	public boolean hasEnded( long EET, long LET ) {	
		return execThread.ended;
	}
	
	private class TalkThread extends Thread {

		public String string;
		public boolean started = false;
		public boolean ended = false;
		
		public TalkThread( String string ) {
			this.string = string.replace("-", " ");
		}
		
	    @Override
		public void run() {
	    	started = true;
	    	System.out.println(string);
	    	ExecuteSystemCommand.call("/tmp/", "rosrun sound_play say.py '"+string+"'");
	    	ended = true;
	    }
	  }
}
