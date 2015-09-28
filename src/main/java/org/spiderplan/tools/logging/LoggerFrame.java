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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.spiderplan.tools.stopWatch.StopWatch;

/**
 * Frame for the Logger class. Create using one of the Logger.draw() methods.
 * 
 * @author Uwe Köckemann
 *
 */
public class LoggerFrame extends JFrame implements ListSelectionListener, ComponentListener, ItemListener, ChangeListener, ActionListener{

	private static final long serialVersionUID = -3706638429190971291L;
	private String title;
	
	boolean autoUpdate = true;
	
	ArrayList<String> selectedSources, availableSources;
	
	JTextArea mainTextArea;
	TextAreaPrintStream mainPrintStream;
	
	@SuppressWarnings("rawtypes")
	DefaultListModel listModel;
	@SuppressWarnings("rawtypes")
	JList sourceList;
	
	@SuppressWarnings("rawtypes")
	JComboBox sourceffComboBox;

	ArrayList<JCheckBox> selectionBoxes = new ArrayList<JCheckBox>();
	
	JCheckBox autoscroll, autoUpdateBox, sourceSelected;
	
	JButton updateButton, newFrameButton;
	
	JLabel landmarkFromLabel, landmarkToLabel, msgLevelLabel, filterLabel;
	JTextField landmarkFrom, landmarkTo, msgLevelField, filter;
	
	Container cC, cLandmarkFromTo;

	private boolean listChangeLock = false;;
	
	int min_w = 800;
	int w = 700;
	int h = 600;
	int controls_h = 30;
	int left_w = 200;
	
	public LoggerFrame( String title, ArrayList<String> sources ) {
		this( title, sources, false, 0, 0, 0, "" );
	}
	
	public LoggerFrame( ArrayList<String> sources ) {
		this( "Logger", sources, false, 0, 0, 0, "" );
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public LoggerFrame( String title, ArrayList<String> sources, boolean staticFrame, int from, int to, int maxLevel, String filterStr ) {
		super(title);
		this.title = title;
		
		selectedSources = sources;
		
		
		JRootPane rp = this.getRootPane();
		rp.putClientProperty("defeatSystemEventQueueCheck", Boolean.TRUE);

		getContentPane().setLayout(new BorderLayout());
		getContentPane().setBackground(java.awt.Color.lightGray);
		getContentPane().setFont(new Font("Serif", Font.PLAIN, 10));

		/**
		 * Main textArea
		 */
		 mainTextArea = new JTextArea();
		 mainTextArea.setAutoscrolls(true);
		 mainTextArea.setSize(200, 200);
		 mainTextArea.setTabSize(2);
		 
		 mainPrintStream = new TextAreaPrintStream(mainTextArea, System.out);
		 
		 JScrollPane mainTextScrollPane = new JScrollPane(mainTextArea);
		 mainTextScrollPane.setWheelScrollingEnabled(true);
		 
		 if ( !staticFrame ) {
			 for ( String source : sources ) {
				 Logger.addPrintStream(source, mainPrintStream);
			 }
		 } else {
			 Logger.streamLandmarks(selectedSources, from, to, mainPrintStream, maxLevel, filterStr);
		 }
		 getContentPane().add(mainTextScrollPane, BorderLayout.CENTER);
	
		/**
		 * Create simple container for stuff on SOUTH border of this JFrame
		 */
		cC = new Container();
		cC.setLayout(new BoxLayout(cC,  BoxLayout.PAGE_AXIS ) );

		cC.setBackground(java.awt.Color.lightGray);
		cC.setFont(new Font("Serif", Font.PLAIN, 10));
		
		cC.setSize( new Dimension (left_w, h));
		cC.setMaximumSize(new Dimension(left_w, 10000));
		cC.setMinimumSize(new Dimension(left_w, 0));
				 
		/**
		 * ComboBox for Source selection:
		 */
		availableSources = Logger.getAllSources();
	
		listModel = new DefaultListModel();
		sourceList = new JList<DefaultListModel>(listModel);
		sourceList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		sourceList.addListSelectionListener(this);
		
		
		JScrollPane sourceListScrollPane = new JScrollPane(sourceList);
		sourceListScrollPane.setWheelScrollingEnabled(true);
		sourceListScrollPane.setMinimumSize(new Dimension(200, 150));
		sourceListScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		updateListModel();
		
		cC.add(sourceListScrollPane);

		/**
		 * Autoscroll checkbox
		 */
		autoscroll = new JCheckBox("Autoscroll");
		autoscroll.setName("Autoscroll");
		autoscroll.setSelected(true);
		autoscroll.addItemListener(this);
		autoscroll.setEnabled(true);
		autoscroll.setBackground(java.awt.Color.lightGray);
		autoscroll.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		cC.add(autoscroll);
		
		/**
		 * Autoupdate checkbox
		 */
		autoUpdateBox = new JCheckBox("Auto. update");
		autoUpdateBox.setName("AutoUpdate");
		autoUpdateBox.setSelected(true);
		autoUpdateBox.addItemListener(this);
		autoUpdateBox.setEnabled(true);
		autoUpdateBox.setBackground(java.awt.Color.lightGray);
		
		cC.add(autoUpdateBox);
		autoUpdateBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		/**
		 * Update Button
		 */
		updateButton = new JButton("Update");
		updateButton.addActionListener(this);
		updateButton.setName("Update");
		updateButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		cC.add(updateButton);
		
		
		landmarkFromLabel = new JLabel("Landmarks from ");
		cC.add(landmarkFromLabel);
		landmarkFrom = new JTextField("0", 3);
		landmarkFrom.setHorizontalAlignment(SwingConstants.CENTER);
		landmarkFrom.setToolTipText("First landmark that is added.");
		landmarkFrom.setMaximumSize(new Dimension(left_w, 25));
		landmarkFrom.setMinimumSize(new Dimension(left_w, 25));
		landmarkFrom.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		cC.add(landmarkFrom);
		
		landmarkToLabel = new JLabel(" until ");
		cC.add(landmarkToLabel);
		landmarkTo = new JTextField("inf", 3);
		landmarkTo.setHorizontalAlignment(SwingConstants.CENTER);
		landmarkTo.setToolTipText("Last landmark that is added.");
		landmarkTo.setMaximumSize(new Dimension(left_w, 25));
		landmarkTo.setMinimumSize(new Dimension(left_w, 25));
		landmarkTo.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		cC.add(landmarkTo);
		
		msgLevelLabel = new JLabel("Level: ");
		cC.add(msgLevelLabel);
		msgLevelField = new JTextField("inf", 3);
		msgLevelField.setHorizontalAlignment(SwingConstants.CENTER);
		msgLevelField.setToolTipText("Level of detail that will be shown.");
		msgLevelField.setMaximumSize(new Dimension(left_w, 25));
		msgLevelField.setMinimumSize(new Dimension(left_w, 25));
		msgLevelField.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		cC.add(msgLevelField);
		
		filterLabel = new JLabel("Filter: ");
		cC.add(filterLabel);
		filter = new JTextField("", 10);
		filter.setHorizontalAlignment(SwingConstants.CENTER);
		filter.setToolTipText("Show only lines that contain this string.");
		filter.setMaximumSize(new Dimension(left_w, 25));
		filter.setMinimumSize(new Dimension(left_w, 25));
		filter.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		cC.add(filter);
		
		/**
		 * New Button
		 */

		newFrameButton = new JButton("New");
		newFrameButton.addActionListener(this);
		newFrameButton.setName("New");
		updateButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		cC.add(newFrameButton);

					
		/**
		 * Add container to layout
		 */
		cC.setBackground(java.awt.Color.lightGray);
		cC.setVisible(true);
		cC.setSize( new Dimension (left_w, h));
		cC.setMaximumSize(new Dimension(left_w, 10000));
		cC.setMinimumSize(new Dimension(left_w, 0));
		
		getContentPane().add(cC, BorderLayout.WEST);
		
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.addComponentListener(this);
		getContentPane().setPreferredSize( new Dimension(w, 600) );
		this.setPreferredSize(new Dimension(w, 600));
		this.setSize(new Dimension(w, 600));
		this.setMinimumSize(new Dimension(min_w, 200));
		this.pack();
		this.setVisible(true);
	}
	
	@SuppressWarnings("unchecked")
	private void updateListModel() {
		listChangeLock = true;		// locks change listener, avoiding some side effects (e.g. loosing selection)
		
		sourceList.removeListSelectionListener(this);
		listModel.removeAllElements();		// for some reason this clears selected sources
		sourceList.addListSelectionListener(this);
		
		Collections.sort(selectedSources);
		
		int[] selectedIndecies = new int[selectedSources.size()];
		int cur = 0;		
		
		for ( int i = 0 ; i < availableSources.size() ; i++ ) {
			listModel.addElement(availableSources.get(i));
			if ( selectedSources.contains( availableSources.get(i) ) ) {
				selectedIndecies[cur++] = i;
			}
		}
		sourceList.setSelectedIndices(selectedIndecies);
		
		listChangeLock = false;
	}
	
	private void update() {
		StopWatch.start("Logger");
		availableSources = Logger.getAllSources();
		updateListModel();		

		for ( String source : availableSources ) {
			Logger.outStreamMap.get(source).remove(mainPrintStream);
		}
		
		for ( String source : selectedSources ) {
			Logger.addPrintStream(source, mainPrintStream);
		}
		StopWatch.stop("Logger");
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Component source = (Component)e.getSource();
		updateListModel();	
		if  (source.getName().equals("SelectSource") ) {
			@SuppressWarnings("rawtypes")
			JComboBox box = (JComboBox)source;
			
			if ( selectedSources.contains(box.getSelectedItem()) ) {
				sourceSelected.setSelected(true);
			} else {
				sourceSelected.setSelected(false);
			}
		} else if ( source.getName().equals("Update") ) {
			update();			
		} else if ( source.getName().equals("New") ) {
			int from = Integer.valueOf(this.landmarkFrom.getText()).intValue();
			int to;
			if ( !this.landmarkTo.getText().equals("inf") ) {
				try {
					to = Integer.valueOf(this.landmarkTo.getText()).intValue();
				} catch ( NumberFormatException nfe ) {
					to = Integer.MAX_VALUE;
				}
			} else {
				to = Integer.MAX_VALUE;
			}
			int maxLevel;
			if ( !this.msgLevelField.getText().equals("inf") ) {
				try {
					maxLevel = Integer.valueOf(this.msgLevelField.getText()).intValue();
				} catch ( NumberFormatException nfe ) {
					maxLevel = Integer.MAX_VALUE;
				}
			} else {
				maxLevel = Integer.MAX_VALUE;
			}
			
			String filterStr = filter.getText();

			new LoggerFrame(title, selectedSources, true, from, to, maxLevel, filterStr );			
		}
	}

	@Override
	public void stateChanged(ChangeEvent arg0) {
		
	}

	@Override
	public void itemStateChanged(ItemEvent arg0) {
		if ( arg0.getSource() instanceof JCheckBox ) {
			JCheckBox source = (JCheckBox)arg0.getSource();
			
			if ( source.getName().equals("Autoscroll") ) {
				mainPrintStream.autoscroll = source.isSelected();
			} else if ( source.getName().equals("AutoUpdate") ) {
				this.autoUpdate = source.isSelected();
			} 
		}
	}

	@Override
	public void componentHidden(ComponentEvent arg0) {

	}

	@Override
	public void componentMoved(ComponentEvent arg0) {

	}

	@Override
	public void componentResized(ComponentEvent arg0) {
	}

	@Override
	public void componentShown(ComponentEvent arg0) {
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting() == false && !listChangeLock ) {
			selectedSources.clear();
			for ( int i : sourceList.getSelectedIndices() ) {
				selectedSources.add(availableSources.get(i));		
			}
			if ( autoUpdate ) {
				update();
			}
        }
	}
}
