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
package org.spiderplan.tools.logging;

import java.io.PrintStream;

import javax.swing.JTextArea;
import javax.swing.text.Document;

/**
 * Forwards {@link PrintStream} to a {@link JTextArea}.
 * Has autoscroll option which can be switched on/off in the GUI.
 * 
 * @author Uwe Köckemann
 *
 */
public class TextAreaPrintStream extends PrintStream {
	
	public boolean autoscroll = true;
	
	private JTextArea textArea;
	
	public TextAreaPrintStream( JTextArea textArea, PrintStream ps ) {
		super(ps);
		this.textArea = textArea;
	}
	
	@Override
	public void write( byte[]buf, int off, int len ) {
		textArea.append(new String(buf, off, len));
		if ( autoscroll ) {
			Document d = textArea.getDocument();
			textArea.setCaretPosition(d.getLength());
		}
	}
}
