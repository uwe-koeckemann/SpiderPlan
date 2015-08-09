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
package org.spiderplan.runnable;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JRootPane;
import javax.swing.WindowConstants;

/**
 * Simple top-level GUI for file selection and problem solving.
 * @author Uwe Köckemann
 *
 */
public class MainFrame extends JFrame implements ActionListener {
	private static final long serialVersionUID = 3896624751126451811L;
		
	protected JButton runButton, checkButton;
	protected JButton selectDomainButton, selectProblemButton, selectPlannerButton;
	
	protected JComboBox selectDomainBox, selectProblemBox, selectPlannerBox;
	
	protected Container c;
	
	HashMap<String,String> plannerFileLookUp = new HashMap<String,String>();
	HashMap<String,String> domainFileLookUp = new HashMap<String,String>();
	HashMap<String,String> problemFileLookUp = new HashMap<String,String>();
	
	ArrayList<String> planners = new ArrayList<String>();
	ArrayList<String> domains = new ArrayList<String>();
	ArrayList<String> problems = new ArrayList<String>();
	
	String selectedDomain;
	public String selectedDomainFileName;
	String selectedProblem;
	public String selectedProblemFileName;
	String selectedPlanner;
	public String selectedPlannerFileName;
	
	private boolean allowActions = true;
	
	public boolean run = false;
	public boolean check  = false;

	public MainFrame( String baseDirectory, String defaultPlanner, String defaultDomain, String defaultProblem ) {
		super("Planner Main Frame");
		
		File plannersBaseDir = new File(baseDirectory+"/"+"planners");
		File domainsBaseDir = new File(baseDirectory+"/"+"domains");
		
		for ( File f : plannersBaseDir.listFiles() ) {
			if ( f.isFile() && f.getName().contains(".uddl") && !f.getName().contains("~")) {
				planners.add( f.getName() );
				plannerFileLookUp.put(f.getName(), f.getAbsolutePath());
			}
		}
		
		Collections.sort(planners);
		
		if ( defaultPlanner != null ) {
			selectedPlanner = defaultPlanner;
		} else {
			selectedPlanner = planners.get(0);
		}
		selectedPlannerFileName = plannerFileLookUp.get(selectedPlanner);
		
		for ( File f : domainsBaseDir.listFiles() ) {
			if ( f.isDirectory() ) {
				domains.add( f.getName() );
				domainFileLookUp.put(f.getName(), f.getAbsolutePath());
			}
		}
		Collections.sort(domains);
		
		if ( defaultDomain != null ) {
			selectedDomain = defaultDomain;
		} else {
			selectedDomain = domains.get(0);
		}
		selectedDomainFileName = domainFileLookUp.get(selectedDomain) + "/domain.uddl"; 
		
		
		JRootPane rp = this.getRootPane();
		rp.putClientProperty("defeatSystemEventQueueCheck", Boolean.TRUE);

		getContentPane().setLayout(new BorderLayout());
		getContentPane().setBackground(java.awt.Color.lightGray);
		getContentPane().setFont(new Font("Serif", Font.PLAIN, 10));

		c = new Container( );
		c.setLayout(new FlowLayout());
		
		selectPlannerBox = new JComboBox();
		
		selectPlannerBox.setName("Select Planner");
		
		for ( String plannerStr : planners ) {
			selectPlannerBox.addItem(plannerStr);
		}
		selectPlannerBox.setSelectedItem(selectedPlanner);
		selectPlannerBox.setVisible(true);

		selectDomainBox = new JComboBox();
		selectDomainBox.setName("Select Domain");
		
		for ( String domainStr : domains ) {
			selectDomainBox.addItem(domainStr);
		}
		selectDomainBox.setSelectedItem(selectedDomain);
		selectDomainBox.setVisible(true);
		
		selectProblemBox = new JComboBox();
		selectProblemBox.setName("Select Problem");
		updateProblemCheckBox( selectedDomain );		
		
		if ( defaultProblem != null ) {
			selectedProblem = defaultProblem;
			selectedProblemFileName = problemFileLookUp.get(selectedProblem);
		}
		selectProblemBox.setSelectedItem(selectedProblem);
		selectProblemBox.setVisible(true);
		
		runButton = new JButton();
		runButton.setName("Run");
		runButton.setText("Run");
		
		checkButton = new JButton();
		checkButton.setName("Check");
		checkButton.setText("Check");
		
		c.setVisible(true);
		c.add(selectPlannerBox);
		c.add(selectDomainBox);
		c.add(selectProblemBox);
		c.add(runButton);
		c.add(checkButton);
		
		/**
		 * Add all listeners
		 */
		selectPlannerBox.addActionListener(this);
		selectDomainBox.addActionListener(this);
		selectProblemBox.addActionListener(this);
		runButton.addActionListener(this);
		checkButton.addActionListener(this);
		
		getContentPane().add(c);
		
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.pack();
		this.setPreferredSize(new Dimension(100,100));
		this.setSize(new Dimension(500, 500));
		this.setMinimumSize(new Dimension(100,100));
		this.setVisible(true);
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		if ( allowActions ) {
			Component source = (Component)e.getSource();
	
//			System.out.println("Selected: " + selectedPlanner + "(" + plannerFileLookUp.get(selectedPlanner)+ ")");
//			System.out.println("Selected: " + selectedDomain + "(" + domainFileLookUp.get(selectedDomain)+ ")");
//			System.out.println("Selected: " + selectedProblem + "(" + problemFileLookUp.get(selectedProblem)+ ")");
//			
//			System.out.println("============================================================================");
			
			if  ( source.getName().equals("Select Planner") ) {
				selectedPlanner = (String)selectPlannerBox.getSelectedItem();
				selectedPlannerFileName = plannerFileLookUp.get(selectedPlanner);
				
				
			} else if ( source.getName().equals("Select Domain")  ) {
				selectedDomain = (String)selectDomainBox.getSelectedItem();
				selectedDomainFileName = domainFileLookUp.get(selectedDomain) + "/domain.uddl";
				updateProblemCheckBox( selectedDomain );
				
			} else if ( source.getName().equals("Select Problem")  ) {
				selectedProblem = (String)selectProblemBox.getSelectedItem();
				selectedProblemFileName = problemFileLookUp.get(selectedProblem);
			} else if ( source.getName().equals("Run") ) {
				run = true;
			} else if ( source.getName().equals("Check") ) {
				check = true;
			}
			
//			System.out.println("Selected: " + selectedPlanner + "(" + selectedPlannerFileName+ ")");
//			System.out.println("Selected: " + selectedDomain + "(" + selectedDomainFileName + ")");
//			System.out.println("Selected: " + selectedProblem + "(" + selectedProblemFileName + ")");
		}
	}
	
	private void updateProblemCheckBox( String selectedDomain ) {
		allowActions = false;
		selectProblemBox.removeAllItems();
		problems.clear();
		
		File problemBaseDir = new File(domainFileLookUp.get(selectedDomain));
				
		for ( File f : problemBaseDir.listFiles() ) {
			if ( f.isFile() && f.getName().contains(".uddl") && !f.getName().equals("domain.uddl") && !f.getName().contains("~")) {
				problems.add( f.getName() );
				problemFileLookUp.put(f.getName(), f.getAbsolutePath());
			}
		}
		Collections.sort(problems);
		
		for ( String problemStr : problems ) {
			selectProblemBox.addItem(problemStr);
		}
		
		selectedProblem = problems.get(0);
		selectedProblemFileName = problemFileLookUp.get(selectedProblem);
		allowActions = true;
	}
	
}
