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
package org.spiderplan.tools.visulization;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Map;
import java.util.Vector;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JRootPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.spiderplan.tools.GraphTools;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.DAGLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout2;
import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout2;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.algorithms.layout.util.Relaxer;
import edu.uci.ics.jung.algorithms.layout.util.VisRunner;
import edu.uci.ics.jung.algorithms.util.IterativeContext;
import edu.uci.ics.jung.graph.AbstractTypedGraph;
import edu.uci.ics.jung.graph.Forest;
import edu.uci.ics.jung.graph.ObservableGraph;
import edu.uci.ics.jung.graph.event.GraphEvent;
import edu.uci.ics.jung.graph.event.GraphEventListener;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.EditingModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.PickableEdgePaintTransformer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.layout.LayoutTransition;
import edu.uci.ics.jung.visualization.picking.PickedState;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import edu.uci.ics.jung.visualization.util.Animator;

/**
 * A variation of AddNodeDemo that animates transitions between graph states.
 *
 * @author Tom Nelson
 * @author Uwe Köckemann
 */


public class GraphFrame<V,E> extends JFrame implements Transformer<E,String>,ChangeListener, ActionListener, GraphEventListener<V,E> {

	private static final long serialVersionUID = 3896624751126451811L;
	
	public enum LayoutClass { Static, Circle, DAG, Spring, Spring2, FR, FR2, ISOM, KK, PolarPoint }; 
	private LayoutClass layoutClass;
	
	private enum Mode { Picking, Transformation };
	private Mode mode = Mode.Picking;
	
	final EditingModalGraphMouse<V, E> graphMouse;
	
	private ObservableGraph<V,E> g = null;
	private VisualizationViewer<V,E> vv = null;
	private Layout<V,E> layout = null;
	
	private Vector<AbstractTypedGraph<V,E>> history;

	private EdgeType defaultEdgeType = EdgeType.UNDIRECTED;
	
	private Map<E,String> edgeLabels;
	
	protected JButton switchMode, dumpJPEG, subGraphButton;
	protected JSlider historySlider;
	protected JLabel subGraphDepthLabel;
	protected JTextField subGraphDepth;

	protected int historyIndex = 0; 
	
	protected Animator animator;
	
	private int subGraphCounter = 0;
	
	private int w = 600;
	private int h = 600;
	
	public GraphFrame( AbstractTypedGraph<V,E> graph, Vector<AbstractTypedGraph<V,E>> history, String title, LayoutClass lC , Map<E,String> edgeLabels ) {
		this(graph, history, title, lC, edgeLabels, 600, 600);
	}
	
	public GraphFrame( AbstractTypedGraph<V,E> graph, Vector<AbstractTypedGraph<V,E>> history, String title, LayoutClass lC  ) {
		this(graph, history, title, lC, null, 600, 600);
	}

	public GraphFrame( AbstractTypedGraph<V,E> graph, Vector<AbstractTypedGraph<V,E>> history, String title, LayoutClass lC , Map<E,String> edgeLabels, int w, int h ) {
		super(title);
		this.edgeLabels = edgeLabels;
		this.g = new ObservableGraph<V,E>(graph);
		this.g.addGraphEventListener(this);
	
		this.defaultEdgeType =  this.g.getDefaultEdgeType();
		
		this.layoutClass = lC;
				
		this.history = history;
		
		this.setLayout(lC);
	
		layout.setSize(new Dimension(w,h));

		try {
			Relaxer relaxer = new VisRunner((IterativeContext)layout);
			relaxer.stop();
			relaxer.prerelax();
		} catch (java.lang.ClassCastException e) {}

//		Layout<V,E> staticLayout = new StaticLayout<V,E>(g, layout);
//		Layout<V,E> staticLayout = new SpringLayout<V, E>(g);

		vv = new VisualizationViewer<V,E>(layout, new Dimension(w,h));

		JRootPane rp = this.getRootPane();
		rp.putClientProperty("defeatSystemEventQueueCheck", Boolean.TRUE);

		getContentPane().setLayout(new BorderLayout());
		getContentPane().setBackground(java.awt.Color.lightGray);
		getContentPane().setFont(new Font("Serif", Font.PLAIN, 10));


		vv.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.S);
		vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<V>());
		vv.setForeground(Color.black);
		
		graphMouse = new EditingModalGraphMouse<V,E>( vv.getRenderContext(), null, null );
		graphMouse.setMode(ModalGraphMouse.Mode.PICKING);
		vv.setGraphMouse(graphMouse);
        vv.addKeyListener(graphMouse.getModeKeyListener());

//        vv.getRenderContext().setEd
		vv.getRenderContext().setEdgeLabelTransformer(this);
		vv.getRenderContext().setEdgeDrawPaintTransformer(new PickableEdgePaintTransformer<E>(vv.getPickedEdgeState(), Color.black, Color.cyan));

		vv.addComponentListener(new ComponentAdapter() {

			/**
			 * @see java.awt.event.ComponentAdapter#componentResized(java.awt.event.ComponentEvent)
			 */
			@Override
			public void componentResized(ComponentEvent arg0) {
				super.componentResized(arg0);
				layout.setSize(arg0.getComponent().getSize());
			}});

		getContentPane().add(vv);
		
		/**
		 * Create simple container for stuff on SOUTH border of this JFrame
		 */
		Container c = new Container();
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
		 * Button that creates offspring frame for selected vertices
		 */
		subGraphButton = new JButton("Subgraph");
		subGraphButton.addActionListener(this);
		subGraphButton.setName("Subgraph");
		c.add(subGraphButton);
		
		subGraphDepthLabel = new JLabel("Depth");
		c.add(subGraphDepthLabel);
		subGraphDepth = new JTextField("0", 2);
		subGraphDepth.setHorizontalAlignment(SwingConstants.CENTER);
		subGraphDepth.setToolTipText("Depth of sub-graph created from selected nodes.");
		c.add(subGraphDepth);
		
		/**
		 * Button that switches mouse mode
		 */
		switchMode = new JButton("Transformation");
		switchMode.addActionListener(this);
		switchMode.setName("SwitchMode");
		c.add(switchMode);
		
		/**
		 * ComboBox for Layout selection:
		 */
		JComboBox layoutList;
		if ( graph instanceof Forest ) {
			String[] layoutStrings = { "Static", "Circle", "DAG", "Spring", "Spring2", "FR", "FR2", "Baloon", "ISOM", "KK", "PolarPoint", "RadialTree", "Tree" };
			layoutList = new JComboBox(layoutStrings);
		} else {
			String[] layoutStrings = { "Static", "Circle", "DAG", "Spring", "Spring2", "FR", "FR2", "ISOM", "KK", "PolarPoint" };
			layoutList = new JComboBox(layoutStrings);
		}
		
		layoutList.setSelectedIndex(5);
		layoutList.addActionListener(this);
		layoutList.setName("SelectLayout");
		c.add(layoutList);
		
		/**
		 * Add container to layout
		 */
		c.setVisible(true);
		getContentPane().add(c, BorderLayout.SOUTH);
		
		/**
		 * Setup history scroll bar
		 */
		if ( history != null ) {
			historySlider = new JSlider( 0, history.size()-1, history.size()-1 ); 
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

		}
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.pack();
		this.setVisible(true);
	}

	@Override
	public void stateChanged(ChangeEvent arg0) {
		if ( arg0.getSource() instanceof JSlider ) {
			JSlider source = (JSlider)arg0.getSource();
//			if ( !source.getValueIsAdjusting() ) {
				int newHistoryIndex = source.getValue();
				
				if ( newHistoryIndex == historyIndex ) {
					return;
				}
				historyIndex = newHistoryIndex;
				AbstractTypedGraph<V,E> H = this.history.get(historyIndex);
	
				Vector<E> removeList = new Vector<E>();
				for ( E e : this.g.getEdges() ) {
					if ( !H.containsEdge(e) ) {
						removeList.add(e);
					}
				}
				for ( E e : removeList ) {
					this.g.removeEdge(e);
				}
				Vector<V> removeVertexList = new Vector<V>();
				for ( V v : this.g.getVertices() ) {
					if ( !H.containsVertex(v) ) {
						removeVertexList.add(v);
					}
				}
				for ( V v : removeVertexList ) {
					this.g.removeVertex(v);
				}
				
				for ( E e : H.getEdges() ) {
					if ( !this.g.containsEdge(e) ) {
						this.g.addEdge(e, H.getEndpoints(e).getFirst(), H.getEndpoints(e).getSecond());
					}
				}
				
				for ( V v : H.getVertices() ) {
					if ( !this.g.containsVertex(v) ) {
						this.g.addVertex(v);
					}
				}
//			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		Component source = (Component)ae.getSource();
		
		if ( source.getName().equals("SwitchMode") ) {
			if ( this.mode == Mode.Transformation ) {
				switchMode.setText("Transformation");
				this.mode = Mode.Picking;
				graphMouse.setMode(ModalGraphMouse.Mode.PICKING);
			} else {
				switchMode.setText("Picking");
				this.mode = Mode.Transformation;
				graphMouse.setMode(ModalGraphMouse.Mode.TRANSFORMING);
			}
		} else if ( source.getName().equals("Dump") ) {
			this.writeJPEGImage("screenshot.png", "png");
		} else if ( source.getName().equals("Subgraph") ) {

			PickedState<V> selection = vv.getPickedVertexState();
			
			if ( selection.getPicked().size() > 0 ) {
				int depth = Integer.valueOf(subGraphDepth.getText()).intValue();
				
				GraphTools<V,E> cG = new GraphTools<V,E>();
				
				Vector<V> connected = new Vector<V>();
				connected.addAll(selection.getPicked());
				Vector<V> checked = new Vector<V>();
				
				for ( int i = 0 ; i < depth ; i++ ) {
					Vector<V> addList = new Vector<V>();
					for ( V v1 : connected ) {
						if ( !checked.contains(v1) ) {
							checked.add(v1);
							for ( V v2 : g.getNeighbors(v1) ) {
								if ( !connected.contains(v2) ) {
									addList.add(v2);
								}
							}
						}
					}
					for ( V v : addList ) {
						connected.add(v);
					}
				}
				
				AbstractTypedGraph<V,E> subGraph;
				if ( this.defaultEdgeType == EdgeType.UNDIRECTED ) {
					subGraph = cG.copyUndirSparseMultiGraph(g);
				} else {
					subGraph = cG.copyDirSparseMultiGraph(g);
				}
				
				Vector<V> removeList = new Vector<V>(); 
				for ( V v : subGraph.getVertices() ) {
					if ( !connected.contains(v) ) {
						removeList.add(v);
					}
				}
				for ( V v : removeList ) {
					subGraph.removeVertex(v);
				}
				new GraphFrame<V,E>(subGraph, null, "Sub Graph " + subGraphCounter, this.layoutClass, this.edgeLabels);
				
				subGraphCounter++;
			}
		} else if  (source.getName().equals("SelectLayout") ) {
			JComboBox box = (JComboBox)source;
			
			String selection = box.getSelectedItem().toString();
			
			if ( selection.equals("FR") ) {
				this.setLayout(LayoutClass.FR);
			} else if ( selection.equals("FR2") ) {
				this.setLayout(LayoutClass.FR2);
			} else if ( selection.equals("Static") ) {
				this.setLayout(LayoutClass.Static);
			} else if ( selection.equals("Circle") ) {
				this.setLayout(LayoutClass.Circle);
			} else if ( selection.equals("DAG") ) {
				this.setLayout(LayoutClass.DAG);
			} else if ( selection.equals("Spring") ) {
				this.setLayout(LayoutClass.Spring);
			} else if ( selection.equals("Spring2") ) {
				this.setLayout(LayoutClass.Spring2);
			} else if ( selection.equals("ISOM") ) {
				this.setLayout(LayoutClass.ISOM);
			} else if ( selection.equals("PolarPoint") ) {
				this.setLayout(LayoutClass.PolarPoint);
			} else if ( selection.equals("KK") ) {
				this.setLayout(LayoutClass.KK);
			}  
			
			Dimension d = vv.getSize();//new Dimension(600,600);
			layout.setSize(d);

			try {
				Relaxer relaxer = new VisRunner((IterativeContext)layout);
				relaxer.stop();
				relaxer.prerelax();
			} catch (java.lang.ClassCastException e) {}

			StaticLayout<V,E> staticLayout =
					new StaticLayout<V,E>(g, layout);
			LayoutTransition<V,E> lt =
					new LayoutTransition<V,E>(vv, vv.getGraphLayout(),
							staticLayout);
			Animator animator = new Animator(lt);
			animator.start();
			vv.repaint();
		}
	}

	@Override
	public void handleGraphEvent(GraphEvent<V,E> evt) {
		
		vv.getRenderContext().getPickedVertexState().clear();
		vv.getRenderContext().getPickedEdgeState().clear();
		try {

			layout.initialize();

			try {
				Relaxer relaxer = new VisRunner((IterativeContext)layout);
				relaxer.stop();
				relaxer.prerelax();
			} catch (java.lang.ClassCastException e) {
				
			}

			StaticLayout<V,E> staticLayout = new StaticLayout<V,E>(g, layout);
			LayoutTransition<V,E> lt = new LayoutTransition<V,E>(vv, vv.getGraphLayout(), staticLayout);
			animator = new Animator(lt);
			animator.start();
			//vv.getRenderContext().getMultiLayerTransformer().setToIdentity();
			vv.repaint();

		} catch (Exception e) {
//			System.out.println(e);
		}	
	}

	private void setLayout( LayoutClass lC ) {
		this.layoutClass = lC;
		
		Layout<V,E> layoutOld = layout;
		layout = null;
		
		switch ( layoutClass ) {
		case Static:
			layout = new StaticLayout<V,E>(g, layoutOld);
			break;
		case Circle:
			layout = new CircleLayout<V,E>(g);
			break;
		case DAG:
			if ( defaultEdgeType == EdgeType.DIRECTED ) {
				layout = new DAGLayout<V,E>(g);
			}
			break;
		case Spring:
			layout = new SpringLayout<V,E>(g);
			break;
		case Spring2:
			layout = new SpringLayout2<V,E>(g);
			break;
		case FR:
			layout = new FRLayout<V,E>(g);
			break;
		case FR2:
			layout = new FRLayout2<V,E>(g);
			break;
		case ISOM:
			layout = new ISOMLayout<V,E>(g);
			break;
		case KK:
			layout = new KKLayout<V,E>(g);
			break;
		default:
		}
		
		if ( layout == null ) {
			layout = layoutOld;
		}
	}
	
	/**
     * copy the visible part of the graph to a file as a jpeg image
     * @param file
     */
    public void writeJPEGImage(String name, String type) {
    	
    	File file = new File(name);
    	
        int width = vv.getWidth();
        int height = vv.getHeight();

        BufferedImage bi = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = bi.createGraphics();
        vv.paint(graphics);
        graphics.dispose();

        try {
            ImageIO.write(bi, type, file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Use user-provided map to get edge labels
     */
	@Override
	public String transform(E e) {
		if ( edgeLabels != null ) {
			String r = edgeLabels.get(e);
			if ( r != null )
				return r;
			else
				return "";
		} else {
			return "";
		}
	}
}


