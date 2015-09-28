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
/**
 * Creates a frame that displays time lines. Add tracks and values then update. To store a configuration in 
 * the history take a snapshot which allows to use the slider to go and review it. Also supports zooming with
 * CTRL + mouse wheel and dumping the time lines into a JPEG. 
 * 
 * Missing features:
 * - Online settings for colors, fonts, etc. 
 * 
 * @author Uwe Köckemann
 */

package org.spiderplan.tools.visulization.timeLineViewer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class TimeLineViewer extends JFrame implements ChangeListener, ItemListener, MouseWheelListener, ActionListener {
	private static final long serialVersionUID = 3766475783823087486L;
	
	private State curState = new State();
	private State viewState;
	private ArrayList<State> history = new ArrayList<State>();
	
	protected JSlider historySlider;
	protected int historyIndex = 0;
	
	TimeLinePanel drawTracks;
	
	Container c;
	private JScrollPane jScrollPane;
	private JCheckBox followCB;
	private JButton dumpJPEG;
	
	private boolean follow = true;
	private boolean firstUpdate = true;

	public TimeLineViewer() {
		this("TimeLines");
	}
	
	public TimeLineViewer( String name ) {
		this.setTitle(name);
		this.setSize(300, 200);
		this.addWindowListener(new WindowAdapter() {
	      @Override
		public void windowClosing(WindowEvent e) {
	        System.exit(0);
	      }
	    });
		
		/**
		 * This draws the time lines
		 */
		drawTracks = new TimeLinePanel(curState);
	    Container contentPane = this.getContentPane();
	    drawTracks.addMouseWheelListener(this);
	    drawTracks.setVisible(true);

	    /**
	     * ScrollPane around drawTracks
	     */
	    jScrollPane = new JScrollPane(drawTracks);
	    jScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED );
	    jScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED );
	    jScrollPane.setWheelScrollingEnabled(true);
	    contentPane.add(jScrollPane);
	    
	    /**
		 * Setup history slider
		 */		
		historySlider = new JSlider( 0, 1, 1 ); 
		historySlider.addChangeListener(this);
		historySlider.setMajorTickSpacing(10);
		historySlider.setMinorTickSpacing(1);
		historySlider.setPaintTicks(true);
		historySlider.setPaintLabels(true);
		historySlider.setBorder(
				BorderFactory.createEmptyBorder(0,0,10,0));
        Font font = new Font("Serif", Font.ITALIC, 15);
        historySlider.setFont(font);
		
		getContentPane().add(historySlider, BorderLayout.NORTH);
	
		
		/**
		 * Create simple container for stuff on SOUTH border of this JFrame
		 */
		c = new Container();
		c.setLayout(new FlowLayout());
		c.setBackground(java.awt.Color.lightGray);
		c.setFont(new Font("Serif", Font.PLAIN, 10));
		
		/**
		 * Button to dump jpeg
		 */
		dumpJPEG = new JButton("Dump");
		dumpJPEG.addActionListener(this);
		dumpJPEG.setName("Dump");
		c.add(dumpJPEG);
		
		/**
		 * CheckBox for following updates or watching history
		 */
		followCB = new JCheckBox("Follow");
		followCB.setName("Follow");
		followCB.setSelected(true);
	    followCB.addItemListener(this);
	    c.add(followCB);    
	    
	    /**
		 * Add container to layout
		 */
		c.setVisible(true);
		getContentPane().add(c, BorderLayout.SOUTH);
		
	    
	    this.setVisible(true);
	}
	
	/**
	 * Check if a {@link Track} exists
	 * @param name Name of the Track
	 */
	public boolean hasTrack( String name ) {
		for ( Track t : curState.tracks ) {
			if ( t.name.equals(name) ) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Create a {@link Track}
	 * @param name Name of the Track
	 */
	public void createTrack( String name ) {
		Track t = new Track(name);
		curState.tracks.add(t);
	}
	
	/**
	 * Check if a value with given id exists
	 * @param id ID of the Value
	 */
	public boolean hasValue( String id ) {
		for ( Track t : curState.tracks ) {
			for ( Value v : t.values ) {
				if ( v.id.equals(id)) {
					return true;
				}
			}
		}
		return false;
	}	
	
	/**
	 * Create a value for a track
	 * @param tName Name of track
	 * @param vName Name of value
	 * @param id ID of value
	 * @param start Start time
	 * @param end End time
	 */
	public void createValue( String tName, String vName, String id, int start, int end ) {
		for ( Track t : curState.tracks ) {
			if ( t.name.equals(tName) ) {
				Value v = new Value(vName, id, start, end);
				t.values.add(v);
			}
		}
	}
	
	/**
	 * Update value with id
	 * @param id ID to be updated
	 * @param start New start time
	 * @param end New end time
	 */
	public void updateValue( String id, int start, int end ) {
		for ( Track t : curState.tracks ) {
			t.update(id, start, end);
		}
	}
	
	/**
	 * Redraw this frame.
	 */
	public void update() {
		if ( follow ) {
			viewState = curState;
		} 
		
		drawTracks.setState(viewState);
		drawTracks.repaint();
		
		if ( firstUpdate ) {
			firstUpdate = false;
			this.setSize(1000,1000);//drawTracks.getWidth() + 20, historySlider.getHeight() + 50 + drawTracks.getHeight() + c.getHeight());
		}
	}
	
	/**
	 * Copy current {@link State} into history and update history slider.
	 */
	public void snapshot() {
		if ( history == null ) {
			history = new ArrayList<State>();
		}
		history.add(curState.copy());
		historySlider.setMaximum(history.size()-1);
	}

	@Override
	public void stateChanged(ChangeEvent arg0) {
		if ( arg0.getSource() instanceof JSlider ) {
			JSlider source = (JSlider)arg0.getSource();

			int newHistoryIndex = source.getValue();
			if ( newHistoryIndex == historyIndex ) {
				return;
			}
			historyIndex = newHistoryIndex;			
			
			viewState = this.history.get(historyIndex);
			
			this.followCB.setSelected(false);
			this.follow = false;
		}
		this.update();
	}

	@Override
	public void itemStateChanged(ItemEvent arg0) {
		if ( arg0.getSource() instanceof JCheckBox ) {
			JCheckBox cb = (JCheckBox)arg0.getSource();
			if ( cb.getName().equals("Follow") ) {
				this.follow = !this.follow;
			}
		}
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		if (e.isControlDown()) {
			drawTracks.zoom(e.getWheelRotation());
			drawTracks.updateSize();
			drawTracks.repaint();
			jScrollPane.setViewportView(drawTracks);
			this.repaint();
		} else {
			

		}
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		Component source = (Component)arg0.getSource();
		
		if ( source.getName().equals("Dump") ) {
			this.writeJPEGImage("screenshot.png", "png");
		}
	}
	
	/**
     * copy the visible part of the graph to a file as a jpeg image
     * @param file
     */
    private void writeJPEGImage(String name, String type) {
    	
    	File file = new File(name);
    	
        int width = drawTracks.getWidth();
        int height = drawTracks.getHeight();

        BufferedImage bi = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = bi.createGraphics();
        drawTracks.paint(graphics);
        graphics.dispose();

        try {
            ImageIO.write(bi, type, file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
