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
package org.spiderplan.tools;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 * Simple class to make system calls on a server. 
 * @author Uwe Koeckemann
 *
 */
public class ExecuteSystemCommand {
	
	/**
	 * Execute command <code>cmd</code> in directory <code>dir</code> 
	 * @param dir Directory in which command will be executed.
	 * @param cmd Command to be executed.
	 * @return Array of type String where first element is STDOUT and second element STERR
	 */
	public static String[] callExternal( String dir, String cmd ) {
		try {
			Socket clientSocket;
			clientSocket = new Socket("localhost", 6789);
			DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
			BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
	
			outToServer.writeBytes(dir + "<CMD>" + cmd + '\n');

			String answer = "";			
			answer = inFromServer.readLine().replace("<LINEBREAK>", "\n");

			clientSocket.close(); 
			String[] ret = answer.split("<ERR>");
			ret[1] = ret[1].replace("<EOF>", "");

			return ret;
		} catch (UnknownHostException e) {
			e.printStackTrace();
			Loop.start();
		} catch (IOException e) {
			e.printStackTrace();
			Loop.start();
		}
		return null;
	}
	
	/**
	 * Execute command <code>cmd</code> in directory <code>dir</code> 
	 * @param dir Directory in which command will be executed.
	 * @param cmd Command to be executed.
	 * @return Array of type String where first element is STDOUT and second element STERR
	 */
	public static String[] call( String dir, String cmd ) {

		String s = null;
		
		try {
			ProcessBuilder builder = new ProcessBuilder( cmd.split(" ") );
			builder.directory( new File( dir ).getAbsoluteFile() );
			builder.redirectErrorStream(true);
			Process process =  builder.start();

			BufferedReader stdoutReader = new BufferedReader(new
                 InputStreamReader(process.getInputStream()));
 
            BufferedReader stderrReader = new BufferedReader(new
                 InputStreamReader(process.getErrorStream()));
 
            StringBuilder stdout = new StringBuilder();
            StringBuilder stderr = new StringBuilder();
	
            while ((s = stdoutReader.readLine()) != null) {
	        	stdout.append(s);
	        	stdout.append("\n");
	        }
	        while ((s = stderrReader.readLine()) != null) {
	        	stderr.append(s);
	        	stderr.append("\n");
	        }
	             
			process.waitFor();
			
			String[] ret = new String[2];
			ret[0] = stdout.toString();
			ret[1] = stderr.toString();
			return ret;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Execute command <code>cmd</code> in directory <code>dir</code> 
	 * @param dir Directory in which command will be executed.
	 * @param cmd Command to be executed.
	 * @return Array of type String where first element is STDOUT and second element STERR
	 */
	public static boolean testIfCommandExists( String cmd ) {

		String s = null;
		
		try {
			ProcessBuilder builder = new ProcessBuilder( cmd );
			
//			builder.redirectErrorStream(true);
			Process process =  builder.start();
	             
			process.waitFor();
			return true;
		} catch (IOException e) {
		} catch (InterruptedException e) {
		}
		return false;
	}
}
