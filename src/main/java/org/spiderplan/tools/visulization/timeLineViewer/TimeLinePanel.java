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
 * Panel that actually draws the time lines.
 */

package org.spiderplan.tools.visulization.timeLineViewer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import javax.swing.JPanel;

public class TimeLinePanel extends JPanel {
	private static final long serialVersionUID = -268214621165273296L;
	
	private State viewState;
	
	/**
	 * Changeable
	 */
	private int w_per_t = 2;
	private int gridSteps_t_minor = 10;
	private int gridSteps_t_major = 50;
	private Stroke dottedLine = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{1}, 0);
	private Stroke normalLine = new BasicStroke();
	private Font defaultFont = new Font("TimesRoman", Font.PLAIN, 15);
	private Font trackFont = new Font("TimesRoman", Font.BOLD, 15);
	
	private Color fontColor = Color.black;
	private Color fillColor = Color.yellow;
	
	/**
	 * Auto adjusted
	 */
	private int w = 400;
	private int h = 200;
	private int wTrackNameBox = 190;
	
	
	/**
	 * Fixed
	 */
	private int h_track = 30;
	private int y_offset_bottom = 10;
	private int rect_line_size = 1;
	
	/**
	 * Allows to get sizes of strings with certain Font
	 */
	private FontMetrics defaultMetric;
	private FontMetrics trackMetric;
	
	public TimeLinePanel( State s ) {
		this.viewState = s;
	}
	
	public void setState( State s ) {
		this.viewState = s;
	}
	
	@Override
	public void paintComponent(Graphics g) {
	    super.paintComponent(g);
	    Graphics2D g2d = (Graphics2D) g;
	    
	    defaultMetric = g.getFontMetrics(defaultFont);
	    trackMetric = g.getFontMetrics(trackFont);

	    g.setFont(defaultFont);
	    updateSize();
	    g.setColor(fontColor);
	    	    
	    /**
	     * Draw vertical lines and put numbers on major steps
	     */
	    int x_next_gridline = wTrackNameBox;
	    int tStep = gridSteps_t_minor;
	    while ( x_next_gridline <= w ) {
	    	x_next_gridline += gridSteps_t_minor * w_per_t;
	    	
	    	int h_line = viewState.tracks.size()*h_track + y_offset_bottom;

	    	if ( tStep % gridSteps_t_major == 0 ) {
	    		g2d.setStroke(normalLine);
	    		Line2D line = new Line2D.Double(x_next_gridline, 0.0, x_next_gridline, h_line);
	    		g2d.draw(line);
	    	} else {
	    		g2d.setStroke(dottedLine);
	    		Line2D line = new Line2D.Double(x_next_gridline, 0.0, x_next_gridline, h_line-2.5f);
	    		g2d.draw(line);
	    	}
	    	
	    	
	    	if ( tStep % gridSteps_t_major == 0 ) {
		    	String tStepStr = tStep+"";
		    	int w_s = defaultMetric.stringWidth(tStepStr);
		    	g.drawString(""+tStep, x_next_gridline - w_s/2, h_line + 15);
	    	}
	    	tStep += gridSteps_t_minor;
	    	
	    	g2d.setStroke(normalLine);
	    }
	    
	   
	    for ( int i = 0 ; i < viewState.tracks.size() ; i++ ) {
	    	/**
	    	 * Draw track name box:
	    	 */
	    	Track t = viewState.tracks.get(i);
	    	int y = i*h_track;
	    	
//	    	g.drawRect(0, y, wTrackNameBox, h_track);
//	    	g.setColor(fillColor);
//	    	g.fillRect(0+rect_line_size, y+rect_line_size, wTrackNameBox-rect_line_size, h_track-rect_line_size);
//	    	g.setColor(fontColor);
	    	
	    	drawFilledBox(g, 0, y, wTrackNameBox, h_track);

	 	    g.drawString(t.name, 10, y + h_track/2+h_track/4);
	 	    
	    	/**
	    	 * Draw boxes for all values
	    	 */
		    for ( Value v : t.values ) {
		    	int x = (wTrackNameBox + v.start*w_per_t);
		    	
		    	int w = ((v.end-v.start)*w_per_t);
	
//		    	g.drawRect(x, y, w, h_track);
//		    	g.setColor(fillColor);
//		    	g.fillRect(x+rect_line_size, y+rect_line_size, w-rect_line_size, h_track-rect_line_size);
//		    	g.setColor(fontColor);
		    	
		    	drawFilledBox(g,x, y, w, h_track);

			    g.drawString(v.name, x + 10, y + h_track/2+h_track/4);
		    }
	    }
	}
	
	private void drawFilledBox( Graphics g, int x, int y, int w, int h ) {
		g.drawRect(x, y, w, h);
    	g.setColor(fillColor);
    	g.fillRect(x+rect_line_size, y+rect_line_size, w-rect_line_size, h_track-rect_line_size);
    	g.setColor(fontColor);
	}
	
	/**
	 * Change zooming factor by adjusting width per time step
	 * @param change Change of width per time step
	 */
	public void zoom( int change ) {
		w_per_t += change;
		if ( w_per_t <= 0 ) 
			w_per_t = 1;
	}
	
	/**
	 * Calculate width and height based on width per time step and set preferred size 
	 * (important for jScrollPane)
	 */
	protected void updateSize() {
		w = 0;

		wTrackNameBox = 0;
		for ( int i = 0 ; i < viewState.tracks.size(); i++ ) {
			Track t = viewState.tracks.get(i);
			int w_neededForTrack = 10 + trackMetric.stringWidth(t.name);
			if ( w_neededForTrack > wTrackNameBox ) {
				wTrackNameBox = w_neededForTrack;
			}
			
			for ( Value v : t.values ) {
				if ( this.wTrackNameBox  + v.end * this.w_per_t > w ) {
					w = this.wTrackNameBox + v.end * this.w_per_t;
				}
			}
		}
		w += (this.gridSteps_t_minor*this.w_per_t) - (w % (this.gridSteps_t_minor*this.w_per_t)) + 20;
		h = this.y_offset_bottom + viewState.tracks.size() * this.h_track + 20; 
		
		
		this.setPreferredSize(new Dimension(w, h));
	}	
	
	@Override
	public int getWidth() {
		return w;
	}
	
	@Override
	public int getHeight() {
		return h;
	}
}
