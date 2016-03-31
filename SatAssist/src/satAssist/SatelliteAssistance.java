package satAssist;

import com.mxgraph.layout.*;
import com.mxgraph.swing.*;

import satAssist.VisibilityCheck.*;

import java.awt.*;

import org.jgrapht.ext.*;
import org.jgrapht.graph.*;
import org.jgrapht.graph.builder.UndirectedWeightedGraphBuilderBase;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import javax.swing.JApplet;
import javax.swing.JFrame;

import org.apache.commons.math3.util.FastMath;
import org.jgrapht.EdgeFactory;
import org.jgrapht.WeightedGraph;
import org.jgrapht.alg.StoerWagnerMinimumCut;
import org.jgrapht.alg.flow.*;
import org.jgrapht.alg.flow.PushRelabelMaximumFlow;
import org.jgrapht.alg.interfaces.MaximumFlowAlgorithm.MaximumFlow;
import org.orekit.time.AbsoluteDate;
import org.orekit.utils.PVCoordinates;

public class SatelliteAssistance extends JApplet { 
	private static final long serialVersionUID = 1L;
	static final Charset charset = Charset.forName("US-ASCII");
	
	static final boolean TESTING = false;
	
	static final String nodeInfoTest = 		"/Users/Rafa/Desktop/Dev/Java/Eclipse-Workspace/satAssistv4/testing-nodes.txt";
	static final String cntcTimeTest = 		"/Users/Rafa/Desktop/Dev/Java/Eclipse-Workspace/satAssistv4/testing-links.txt";	
	static final String cntcTimesOvlpTest =	"/Users/Rafa/Desktop/Dev/Java/Eclipse-Workspace/satAssistv4/testing-links-with-overlap.txt";
	static final String sourcesFileTest = 	"/Users/Rafa/Desktop/Dev/Java/Eclipse-Workspace/satAssistv4/testing-sources.txt";
	static final String sinksFileTest = 	"/Users/Rafa/Desktop/Dev/Java/Eclipse-Workspace/satAssistv4/testing-sinks.txt";

	static final String cntcTimes = 		"/Users/Rafa/Desktop/Dev/Java/Eclipse-Workspace/satAssistv4/input-contactTimes.txt";
	static final String cntcTimesOvlp = 	"/Users/Rafa/Desktop/Dev/Java/Eclipse-Workspace/satAssistv4/input-contactTimesWithOverlap.txt";	
	static final String nodeInfo = 			"/Users/Rafa/Desktop/Dev/Java/Eclipse-Workspace/satAssistv4/input-nodeInfo.txt";
	static final String sourcesFile = 		"/Users/Rafa/Desktop/Dev/Java/Eclipse-Workspace/satAssistv4/input-sources.txt";
	static final String sinksFile = 		"/Users/Rafa/Desktop/Dev/Java/Eclipse-Workspace/satAssistv4/input-sinks.txt";
	static final String satTorusFile =		"/Users/Rafa/Desktop/Dev/Java/Eclipse-Workspace/satAssistv4/input-sattorus.txt";
	static final String stationInfo = 		"/Users/Rafa/Desktop/Dev/Java/Eclipse-Workspace/satAssistv4/input-stationInfo.txt";
	
	static final String nodeInfoPerm = 		"/Users/Rafa/Desktop/Dev/Java/Eclipse-Workspace/satAssistv4/permanent-nodeInfo.txt";
	static final String cntcTimePerm = 		"/Users/Rafa/Desktop/Dev/Java/Eclipse-Workspace/satAssistv4/permanent-contactTimes.txt";

	static final String homeFolder =		"/Users/Rafa/Desktop/Dev/Java/Eclipse-Workspace/";
	static final String tleFile = 			"/Users/Rafa/Desktop/Dev/Java/Eclipse-Workspace/satAssistv4/tle-iridium.txt";
	
	static final String maxFlowFile = 		"/Users/Rafa/Desktop/Dev/Java/Eclipse-Workspace/satAssistv4/out-maxFlowEdmondsKarp.txt";
	static final String resultsFile = 		"/Users/Rafa/Desktop/Dev/Java/Eclipse-Workspace/satAssistv4/out-results.txt";
	
	static final Dimension DEFAULT_SIZE = new Dimension(1280, 800);
	static Double SATBUFFER = 0.;
	static final Double MAXSATDIST = 6000.;
	static Double INTERSATBW = 1.;
	static final Double DEFAULT_EPSILON = Double.MIN_VALUE;
	static Double SATBANDWIDTH = 1.;
	static Double PROPAGATIONDUR = 1800.;
	static final Double BWAMORT = 1.;
	static AbsoluteDate BEGINDATE = new AbsoluteDate();
	static final String SAT_PREFIX = "iridium";
	static int START_TIME;
	
	static final boolean MAXFLOW_EK = true;
	static final boolean GREEDY_NOSWITCH = false;
	static final boolean GREEDY_SWITCH = false;
	static final boolean PRINT_GREEDY_SWITCH = false; // Print one or the other or none
	static final boolean PRINT_GREEDY_NO_SWITCH = false; // Print one or the other or none
	static final boolean PRINT_CONTACT_GRAPH = false; // Print one or the other or none
	static final boolean PRINT_EVENT_GRAPH = false;
	static final boolean SAVE_MAXFLOW = false; // Print max flow result to txt file

	public static void main (String[] args) {
		SatelliteAssistance applet = new SatelliteAssistance();
        applet.init();

        JFrame frame = new JFrame();
        frame.getContentPane().add(applet);
        frame.setTitle("JGraphT Adapter to JGraph Demo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
	}
	
    public void init() {
    	int interSatBW = 1;
    	int satBW = 1;
    	int satBuff = 1;
    	int halfHours = 1;
    	try {
    		PrintWriter resFile = new PrintWriter(new BufferedWriter(new FileWriter(resultsFile, false)));
	    	for (satBuff = 0; satBuff <= 1000; satBuff+=10) {
	    		SATBUFFER = 1. * satBuff;
		    	for (interSatBW = 0; interSatBW <= 20; interSatBW++) {
		    		INTERSATBW = 1. * interSatBW;
				    for (satBW = 1; satBW <= 20; satBW++) {
				    	SATBANDWIDTH = 1. * satBW;
					    for (halfHours = 1; halfHours <= 20; halfHours++) {
					    	PROPAGATIONDUR = 1800. * halfHours;
					    	long startTime;
					    	
					    	if (!TESTING) {
					    		System.out.println(">>> Calculating contact list...");
					    		startTime = System.currentTimeMillis();
					    		calculateContactList();
					    		System.out.println(">>> Calculated! ( " + (System.currentTimeMillis() - startTime) + "ms )");
					    	}
					    	
					    	startTime = System.currentTimeMillis();
					
					    	ContactDriven cntc = new ContactDriven();
					        WeightedMultigraph<SatAssistNode, TimedWeightedEdge> contactGraph = cntc.createContactGraph();
					        
					        System.out.println(">>> Contact graph done! [ E = " + contactGraph.edgeSet().size() + " V = " + contactGraph.vertexSet().size() + " ] ( "  + (System.currentTimeMillis() - startTime) + "ms )");
					        
					        startTime = System.currentTimeMillis();
					        
					        EventDriven<WeightedMultigraph<SatAssistNode, TimedWeightedEdge>, SatAssistNode, TimedWeightedEdge> evnt = new EventDriven<WeightedMultigraph<SatAssistNode, TimedWeightedEdge>, SatAssistNode, TimedWeightedEdge>();
					        EventGraph<TimedNode, EventEdge> evntGraph = evnt.createEventDrivenGraph(contactGraph);
					        
					        System.out.println(">>> Event driven graph done! [ E = " + evntGraph.edgeSet().size() + " V = " + evntGraph.vertexSet().size() + " ] ( " + (System.currentTimeMillis() - startTime) + "ms )");
					        
					        if (PRINT_CONTACT_GRAPH) {
					        	JGraphXAdapter<SatAssistNode, TimedWeightedEdge> jgxAdapter = new JGraphXAdapter<SatAssistNode, TimedWeightedEdge>(contactGraph);
					        	System.out.println(">>> Printing contact graph now!");
					            
					            getContentPane().add(new mxGraphComponent(jgxAdapter));
					            resize(DEFAULT_SIZE);
					           
					            jgxAdapter.setCellsDisconnectable(false);
					            jgxAdapter.setConnectableEdges(false);
					            jgxAdapter.setCellsEditable(false);
					            jgxAdapter.setConnectableEdges(false);
					            jgxAdapter.setAllowDanglingEdges(false);
					            
					            mxCircleLayout layout = new mxCircleLayout(jgxAdapter);
					            layout.execute(jgxAdapter.getDefaultParent());
					        
					            System.out.println(">>> Print done contact graph done!");
					        }
					        
					        if (PRINT_EVENT_GRAPH) {
					        	JGraphXAdapter<TimedNode, EventEdge> jgxAdapter = new JGraphXAdapter<TimedNode, EventEdge>(evntGraph);
					        	System.out.println(">>> Printing contact graph now!");
					            
					            getContentPane().add(new mxGraphComponent(jgxAdapter));
					            resize(DEFAULT_SIZE);
					           
					            jgxAdapter.setCellsDisconnectable(false);
					            jgxAdapter.setConnectableEdges(false);
					            jgxAdapter.setCellsEditable(false);
					            jgxAdapter.setConnectableEdges(false);
					            jgxAdapter.setAllowDanglingEdges(false);
					            
					            mxCircleLayout layout = new mxCircleLayout(jgxAdapter);
					            layout.execute(jgxAdapter.getDefaultParent());
					        
					            System.out.println(">>> Print done contact graph done!");
					        }
					        
					        for (EventEdge e : evntGraph.edgeSet()) {
					        	if (evntGraph.getEdgeWeight(e) <= Double.MIN_VALUE) {
					        		System.out.println("### Problem! Zero weighted edge: " + e + " weight: " + evntGraph.getEdgeWeight(e));
					        	}
					        }
					        
					        if (MAXFLOW_EK) {
					        	startTime = System.currentTimeMillis();
					
						        EdmondsKarpMaximumFlow<TimedNode, EventEdge> maxFlow = new EdmondsKarpMaximumFlow<TimedNode, EventEdge>(evntGraph);
						    	MaximumFlow<TimedNode, EventEdge> result = maxFlow.buildMaximumFlow(evntGraph.getSource(), evntGraph.getSink());
					
						    	System.out.println(">>> Max-flow EK = " + result.getValue() + " ( " + (System.currentTimeMillis() - startTime) + "ms )");
						    	
						    	resFile.println(SATBUFFER + ";" + INTERSATBW + ";" + SATBANDWIDTH + ";" + PROPAGATIONDUR + ";" + result.getValue() + ";" + (System.currentTimeMillis() - startTime));
						    	resFile.flush();
						    	if (SAVE_MAXFLOW) {
						    		try {
						            	PrintWriter karp = new PrintWriter(new BufferedWriter(new FileWriter(maxFlowFile, false)));
						            	for (Map.Entry<EventEdge, Double> entry : result.getFlow().entrySet()) {
						            		if (entry.getValue().intValue() > 0) {
						            			karp.println(evntGraph.getEdgeSource(entry.getKey()) + ";" + (evntGraph.getEdgeSource(entry.getKey()).getTime() - START_TIME) + ";" + evntGraph.getEdgeTarget(entry.getKey()) + ";" + (evntGraph.getEdgeTarget(entry.getKey()).getTime() - START_TIME) + ";"+ new Double(evntGraph.getEdgeWeight(entry.getKey())).intValue() + ";" + entry.getValue().intValue());
						            		}
						            	}
						            	karp.close();
						            } catch (IOException exception) {
						    			System.out.println("Error!");
						    		}
						    	}
					        }
					    	
					        if (GREEDY_NOSWITCH) {
					        	startTime = System.currentTimeMillis();
						
						    	System.out.println(">>> Start no switch greedy strategy...");
						    	SimpleDirectedWeightedGraph<TimedNode, EventEdge> greedyNoSwitch = greedyStrategy();
						    	
						    	System.out.println(">>> Greedy No Switch Done! ( " + (System.currentTimeMillis() - startTime) + "ms )");
						    	if (PRINT_GREEDY_NO_SWITCH) {
						    		JGraphXAdapter<TimedNode, EventEdge> jgxAdapter = new JGraphXAdapter<TimedNode, EventEdge>(greedyNoSwitch);
						    		getContentPane().add(new mxGraphComponent(jgxAdapter));
							        resize(DEFAULT_SIZE);
							       
							        jgxAdapter.setCellsDisconnectable(false);
							        jgxAdapter.setConnectableEdges(false);
							        jgxAdapter.setCellsEditable(false);
							        jgxAdapter.setConnectableEdges(false);
							        jgxAdapter.setAllowDanglingEdges(false);
							        
							        mxCircleLayout layout = new mxCircleLayout(jgxAdapter);
							        layout.execute(jgxAdapter.getDefaultParent());
						    	}
					        }
					        
					        if (GREEDY_SWITCH) {
					        	startTime = System.currentTimeMillis();
					        	
					        	System.out.println(">>> Start switch greedy strategy...");
					        	SimpleDirectedWeightedGraph<TimedNode, EventEdge> greedySwitch = greedySwitchingStrategy();
					        	
					        	System.out.println(">>> Greedy Switch Done! ( " + (System.currentTimeMillis() - startTime) + "ms )");
					        	
					        	if (PRINT_GREEDY_SWITCH) {
					        		JGraphXAdapter<TimedNode, EventEdge> jgxAdapter = new JGraphXAdapter<TimedNode, EventEdge>(greedySwitch);
					        		getContentPane().add(new mxGraphComponent(jgxAdapter));
					    	        resize(DEFAULT_SIZE);
					    	       
					    	        jgxAdapter.setCellsDisconnectable(false);
					    	        jgxAdapter.setConnectableEdges(false);
					    	        jgxAdapter.setCellsEditable(false);
					    	        jgxAdapter.setConnectableEdges(false);
					    	        jgxAdapter.setAllowDanglingEdges(false);
					    	        
					    	        mxCircleLayout layout = new mxCircleLayout(jgxAdapter);
					    	        layout.execute(jgxAdapter.getDefaultParent());
					        	}
					        }
					        
					        System.out.println(">>> DONE! <<<");
					        
					    }
				    }
		    	}
	    	}
	    	resFile.close();
    	} catch (IOException exception) {
			System.out.println("Error!");
		}
    }
	
    public static void calculateContactList() {
    	try {
    		String line;
    		
			PrintWriter outNodeInfo = new PrintWriter(new BufferedWriter(new FileWriter(nodeInfo, false)));
			PrintWriter outSatTopo = new PrintWriter(new BufferedWriter(new FileWriter(satTorusFile, false)));
			BufferedReader readerStations = Files.newBufferedReader(new File(stationInfo).toPath(), charset);
			BufferedReader readerConfNodes = Files.newBufferedReader(new File(nodeInfoPerm).toPath(), charset);
			BufferedReader readerConfLinks = Files.newBufferedReader(new File(cntcTimePerm).toPath(), charset);
			PrintWriter outContactTimes = new PrintWriter(new BufferedWriter(new FileWriter(cntcTimes, false)));
			PrintWriter outContactTimesWithOverlap = new PrintWriter(new BufferedWriter(new FileWriter(cntcTimesOvlp, false)));
			
			while ((line = readerConfNodes.readLine()) != null) {
				outNodeInfo.println(line);
			}
			
			while ((line = readerConfLinks.readLine()) != null) {
				outContactTimes.println(line);
				outContactTimesWithOverlap.println(line);
			}
			
			
			List<ContactEvent> ctcList = new ArrayList<ContactEvent>();

			List<ContactEvent> ctcWithOverlap = new ArrayList<ContactEvent>();
			List<ContactEvent> ctcNoOverlap = new ArrayList<ContactEvent>();
			
			while ((line = readerStations.readLine()) != null) {
				String[] station = line.split(",");
				
				ctcList.addAll(VisibilityCheck.getContactList(station[0],
						FastMath.toRadians(Double.valueOf(station[1])),
						FastMath.toRadians(Double.valueOf(station[2])),
						Double.valueOf(station[3])));
			}
		    	// With the contact list, we have to solve the interval superposition: aka Skyline Problem
				Collections.sort(ctcList, new CompContactEventBegin());
				PriorityQueue<ContactEvent> ctcEndMinHeap = new PriorityQueue<ContactEvent>(ctcList.size(), new CompContactEventEnd());	
				ctcEndMinHeap.clear();
				ctcEndMinHeap.add(ctcList.get(0));
				AbsoluteDate prevBegin = ctcList.get(0).getBegin();
				
				for (int i = 1; i < ctcList.size(); i++) {
					// First, remove from the queue all the contacts that already expired.
					if (!ctcEndMinHeap.isEmpty()) {
						while ((!ctcEndMinHeap.isEmpty()) && (ctcEndMinHeap.peek().getEnd().compareTo(ctcList.get(i).getBegin()) <= 0)) {
							ContactEvent finalizedContact = ctcEndMinHeap.poll();
							try {
								ctcNoOverlap.add(new ContactEvent(finalizedContact.getGround(), finalizedContact.getSat(), prevBegin, finalizedContact.getEnd()));
							} catch (Exception e) {
								System.out.println(e);
							}
							// As the contact expired, it must intercept all the other contacts (expired or not) creating a bunch of new contact with the same duration
							Iterator<ContactEvent> it = ctcEndMinHeap.iterator();
							while (it.hasNext()) {
								ContactEvent nextContact = it.next();
								if (nextContact.getBegin().compareTo(finalizedContact.getEnd()) <= 0) {
									try {
										ctcNoOverlap.add(new ContactEvent(nextContact.getGround(), nextContact.getSat(), prevBegin, finalizedContact.getEnd()));
									} catch (Exception e) {
										System.out.println(e);
									}
								}
							}
							prevBegin = finalizedContact.getEnd();
						}
					}
					
					// Now, create as many new non-overlapping contacts as there are overlappings in the current queue.
					Iterator<ContactEvent> it = ctcEndMinHeap.iterator();
					while (it.hasNext()) {
						ContactEvent ongoingContact = it.next();
						try {
							ctcNoOverlap.add(new ContactEvent(ongoingContact.getGround(), ongoingContact.getSat(), prevBegin, ctcList.get(i).getBegin()));
						} catch (Exception e) {
							System.out.println(e);
						}
					}
					
					// Now, set the new beginning to the current start and add the current contact to the set of ongoing contacts.
					prevBegin = ctcList.get(i).getBegin();
					ctcEndMinHeap.add(ctcList.get(i));
				}
				ctcWithOverlap.addAll(ctcWithOverlap.size(), ctcList);
			//}
			HashSet<ContactEvent> hashSetCtc = new HashSet<ContactEvent>();
			HashSet<ContactEvent> hashSetCtcWithOverlap = new HashSet<ContactEvent>();
			Collections.sort(ctcWithOverlap, new CompContactEventBegin());
			Collections.sort(ctcNoOverlap, new CompContactEventBegin());
			
			BEGINDATE = ctcNoOverlap.get(0).getBegin();
			
			hashSetCtc.addAll(ctcNoOverlap);
			hashSetCtcWithOverlap.addAll(ctcWithOverlap);
			// Now, ctcNoOverlap contains the list of all non overlapping contact pairs, start times and durations.
			// Create input files for the other functions
			
			HashSet<SatObj> satList = new HashSet<SatObj>();
			
			for (int i = 0; i < ctcList.size(); i++) {
				satList.add(new SatObj(ctcList.get(i).getSat(), ctcList.get(i).getCoord()));
			}
			
			HashSet<SatLink> satLinks = new HashSet<SatLink>();
			
			// Populate list of satellites file and connection topology
			for (SatObj s : satList) {
				outNodeInfo.println(s);
				s.calculateDistances(satList);
				ArrayList<DistanceSatPair> d = s.getDistances();
				// Creates links with the closest ones or with the nearest two
				for (int i = 0; i < 4; i++) {
					if ((i < 3) || (d.get(i).getDist() < MAXSATDIST)) {
						satLinks.add(new SatLink(s, d.get(i).getSat()));
					}
				}
			}
			
			for (SatLink l : satLinks) {
				outSatTopo.println(l);
				if (l.getBW() > 0) {
					outContactTimes.println(l);
					outContactTimesWithOverlap.println(l);
				}
			}
			
			//for (ContactEvent ce : ctcNoOverlap) {
			for (ContactEvent ce : hashSetCtc) {
				if (ce.getBW() > 0) {
					outContactTimes.println(ce);
				}
			}
			
			//for (ContactEvent ce : ctcWithOverlap) {
			for (ContactEvent ce : hashSetCtcWithOverlap) {
				if (ce.getBW() > 0) {
					outContactTimesWithOverlap.println(ce);
				}
			}
			
			outContactTimesWithOverlap.close();
			outContactTimes.close();
			outSatTopo.close();
			outNodeInfo.close();
			
			
    	} catch (IOException x) {
			System.out.println("Error! Could not open input files!");
		}
    	
    }
    
    public static SimpleDirectedWeightedGraph<TimedNode, EventEdge>  greedyStrategy() { //void greedyStrategy() {
    	GreedyGraph<GreedyNode, GreedyEdge> gGreedy = new GreedyGraph<GreedyNode, GreedyEdge>(GreedyEdge.class);
    	SimpleDirectedWeightedGraph<TimedNode, EventEdge>  ret = null;
    	
    	try {
    		BufferedReader reader;
    		if (!TESTING) {
    			reader = Files.newBufferedReader(new File(nodeInfo).toPath(), charset);
    		} else {
    			reader = Files.newBufferedReader(new File(nodeInfoTest).toPath(), charset);
    		}
    		String line = null;
    		
    		// First, add all vertices.
    		while ((line = reader.readLine()) != null) {
				if (line.length() > 0) {
					if (line.charAt(0) != '#') {
						String name = line.split(",")[0].toString();
						double buffer = Double.valueOf(line.split(",")[1].toString());
						GreedyNode n;
						if (!TESTING) {
							n = new GreedyNode(name, buffer, "");
						} else {
							n = new GreedyNode(name, buffer, line.split(",")[2].toString());
						}
						gGreedy.addVertex(n);
					}
				}
			}
    		
    		if (!TESTING) {
    			reader = Files.newBufferedReader(new File(cntcTimesOvlp).toPath(), charset);
    		} else {
    			reader = Files.newBufferedReader(new File(cntcTimesOvlpTest).toPath(), charset);
    		}
    		line = null;
    		List<GreedyEdge> intermittentEdges = new ArrayList<GreedyEdge>();
    		
    		// Second, create the fixed topology and add all intermittent edges to a list (intermittentEdges).
			while ((line = reader.readLine()) != null) {
				if ((line.charAt(0) != '#') && (line.length() > 0)) {
					if (Integer.valueOf(line.split(",")[3].toString()) < 0) { // The first few lines all have t=-1 representing the fixed topo.
						GreedyNode src = gGreedy.getVertexByName(line.split(",")[0].toString());
						GreedyNode dst = gGreedy.getVertexByName(line.split(",")[1].toString());
						
						if ((src == null) || (dst == null)) {
							System.out.println("ERROR! Source or destination in greedy graph not found!");
						}
						
						GreedyEdge e = new GreedyEdge(-1,-1, Double.valueOf(line.split(",")[2].toString()), src, dst);
						if (!gGreedy.addEdge(src, dst, e)) {
							e = gGreedy.getEdge(src, dst);
						}
						e.setTime(-1);
						e.setCap(Double.valueOf(line.split(",")[2].toString()));
						//gGreedy.setEdgeWeight(e, );
					} else {
						GreedyEdge e = new GreedyEdge(Integer.valueOf(line.split(",")[3].toString()),
								Integer.valueOf(line.split(",")[4].toString()), 
								Double.valueOf(line.split(",")[5].toString()), 
								gGreedy.getVertexByName(line.split(",")[0].toString()), 
								gGreedy.getVertexByName(line.split(",")[1].toString()));
						
						intermittentEdges.add(e);
					}
				}
			}
			
			// Now, gGreedy has all non-intermittent links and all nodes (intermittent or not).
			Collections.sort(intermittentEdges, new CompIntermittentEdgeBeg());
			
			PriorityQueue<GreedyEdge> intermittentEdgeEndMinHeap = new PriorityQueue<GreedyEdge>(intermittentEdges.size(), new CompIntermittentEdgeEnd());	
			intermittentEdgeEndMinHeap.clear();
			
			// First, add the the first contact of the list to the gGreedy graph and to the heap (setting the end vertices as busy).
			intermittentEdgeEndMinHeap.add(intermittentEdges.get(0));
			int latestFlowTime = intermittentEdges.get(0).getBeg();
			intermittentEdges.get(0).getTempSrc().setBusy(intermittentEdges.get(0).getTempDst());
			intermittentEdges.get(0).getTempDst().setBusy(intermittentEdges.get(0).getTempSrc());
			gGreedy.addEdge(intermittentEdges.get(0).getTempSrc(), intermittentEdges.get(0).getTempDst(), intermittentEdges.get(0));
			
			// Initialize variables.
			double maxFlow = 0.;
			long blockedContacts = 0L;
			
			for (int i = 1; i < intermittentEdges.size(); i++) {
				// First, remove from the queue all the contacts that already expired.
				//if (!intermittentEdgeEndMinHeap.isEmpty()) {
				while ((!intermittentEdgeEndMinHeap.isEmpty()) && (intermittentEdgeEndMinHeap.peek().getEnd().compareTo(intermittentEdges.get(i).getBeg()) <= 0)) {
					// The edge on the top of the Heap has ended. Take it out, add it to the gGreedy graph, calculate the Max-Flow, and remove it from the graph.
					GreedyEdge finalizedEdge = intermittentEdgeEndMinHeap.poll();
					//gGreedy.setEdgeWeight(finalizedEdge, finalizedEdge.getBW()*(finalizedEdge.getEnd() - finalizedEdge.getBeg()));
					finalizedEdge.setCap(finalizedEdge.getBW()*(finalizedEdge.getEnd() - finalizedEdge.getBeg()));
					finalizedEdge.setTime(1);
					
					// Set all other intermittent edges weights to current moment.
					for (GreedyEdge e : intermittentEdgeEndMinHeap) {
						//gGreedy.setEdgeWeight(e, e.getBW()*(finalizedEdge.getEnd() - Math.max(latestFlowTime, e.getBeg())));
						e.setCap(e.getBW()*(finalizedEdge.getEnd() - Math.max(latestFlowTime, e.getBeg())));
						e.setTime(1);
					}
					
					// Now, calculate the Max Flow (min cut) on the current network before removing the finalized edge.
					
					EventDriven<GreedyGraph<GreedyNode, GreedyEdge>, GreedyNode, GreedyEdge> evnt = new EventDriven<GreedyGraph<GreedyNode, GreedyEdge>, GreedyNode, GreedyEdge>();
					EventGraph<TimedNode, EventEdge> evntGraph = evnt.createEventDrivenGraph(gGreedy);
					
					ret = evntGraph;
			        
					EdmondsKarpMaximumFlow<TimedNode, EventEdge> maxFlowGraph = new EdmondsKarpMaximumFlow<TimedNode, EventEdge>(evntGraph);
			        maxFlow = maxFlow + maxFlowGraph.buildMaximumFlow(evntGraph.getSource(), evntGraph.getSink()).getValue();
					
					// Finally, remove the finalized edge from the gGreedy graph and set both end as free.
					finalizedEdge.getTempDst().setFree(finalizedEdge.getTempSrc());
					finalizedEdge.getTempSrc().setFree(finalizedEdge.getTempDst());
					gGreedy.removeEdge(finalizedEdge);
					if (gGreedy.containsEdge(finalizedEdge)) {
						System.out.println("ERROR! Edge not removed!");
					}
					
					// Update the latest time that max flow was calculated.
					latestFlowTime = finalizedEdge.getEnd();
					
				}
				//}
				
				// Now, if the current edge doesn't connect nodes that are already being used by intermittent edges, add it to the graph and heap.
				if ((!intermittentEdges.get(i).getTempSrc().isBusy()) && (!intermittentEdges.get(i).getTempDst().isBusy())) {
					// Both ends are free, so first add edge to heap
					intermittentEdgeEndMinHeap.add(intermittentEdges.get(i)); 
					// Set both ends as busy
					intermittentEdges.get(i).getTempSrc().setBusy(intermittentEdges.get(i).getTempDst());
					intermittentEdges.get(i).getTempDst().setBusy(intermittentEdges.get(i).getTempSrc());
					// Add edge to gGreedy graph
					gGreedy.addEdge(intermittentEdges.get(i).getTempSrc(), intermittentEdges.get(i).getTempDst(), intermittentEdges.get(i));
				} else {
					blockedContacts++;
				}
				
			}
			
			System.out.println("Blocked contacts: " + blockedContacts);
			System.out.println("Good contacts: " + (intermittentEdges.size() - blockedContacts));
			
			System.out.println("Total greedy max flow: " + maxFlow);
			
    	} catch (IOException e) {}
    	
    	return ret;
    }

    public static SimpleDirectedWeightedGraph<TimedNode, EventEdge>  greedySwitchingStrategy() { //void greedyStrategy() {
    	GreedyGraph<GreedyNode, GreedyEdge> gGreedy = new GreedyGraph<GreedyNode, GreedyEdge>(GreedyEdge.class);
    	SimpleDirectedWeightedGraph<TimedNode, EventEdge>  ret = null;
    	
    	try {
    		BufferedReader reader;
    		if (!TESTING) {
    			reader = Files.newBufferedReader(new File(nodeInfo).toPath(), charset);
    		} else {
    			reader = Files.newBufferedReader(new File(nodeInfoTest).toPath(), charset);
    		}
    		String line = null;
    		
    		// First, add all vertices.
    		while ((line = reader.readLine()) != null) {
				if (line.length() > 0) {
					if (line.charAt(0) != '#') {
						String name = line.split(",")[0].toString();
						double buffer = Double.valueOf(line.split(",")[1].toString());
						GreedyNode n;
						if (!TESTING) {
							n = new GreedyNode(name, buffer, "");
						} else {
							n = new GreedyNode(name, buffer, line.split(",")[2].toString());
						}
						gGreedy.addVertex(n);
					}
				}
			}
    		
    		if (!TESTING) {
    			reader = Files.newBufferedReader(new File(cntcTimesOvlp).toPath(), charset);
    		} else {
    			reader = Files.newBufferedReader(new File(cntcTimesOvlpTest).toPath(), charset);
    		}
    		line = null;
    		List<GreedyEdge> intermittentEdges = new ArrayList<GreedyEdge>();
    		
    		// Second, create the fixed topology and add all intermittent edges to a list (intermittentEdges).
			while ((line = reader.readLine()) != null) {
				if ((line.charAt(0) != '#') && (line.length() > 0)) {
					if (Integer.valueOf(line.split(",")[3].toString()) < 0) { // The first few lines all have t=-1 representing the fixed topo.
						GreedyNode src = gGreedy.getVertexByName(line.split(",")[0].toString());
						GreedyNode dst = gGreedy.getVertexByName(line.split(",")[1].toString());
						
						if ((src == null) || (dst == null)) {
							System.out.println("ERROR! Source or destination in greedy graph not found!");
						}
						
						GreedyEdge e = new GreedyEdge(-1,-1, Double.valueOf(line.split(",")[2].toString()), src, dst);
						if (!gGreedy.addEdge(src, dst, e)) {
							e = gGreedy.getEdge(src, dst);
						}
						e.setTime(-1);
						e.setCap(Double.valueOf(line.split(",")[2].toString()));
						//gGreedy.setEdgeWeight(e, );
					} else {
						GreedyEdge e = new GreedyEdge(Integer.valueOf(line.split(",")[3].toString()),
								Integer.valueOf(line.split(",")[4].toString()), 
								Double.valueOf(line.split(",")[5].toString()), 
								gGreedy.getVertexByName(line.split(",")[0].toString()), 
								gGreedy.getVertexByName(line.split(",")[1].toString()));
						
						intermittentEdges.add(e);
					}
				}
			}
			
			// Now, gGreedy has all non-intermittent links and all nodes (intermittent or not).
			Collections.sort(intermittentEdges, new CompIntermittentEdgeBeg());
			
			PriorityQueue<GreedyEdge> intermittentEdgeEndMinHeap = new PriorityQueue<GreedyEdge>(intermittentEdges.size(), new CompIntermittentEdgeEnd());	
			intermittentEdgeEndMinHeap.clear();
			
			// First, add the the first contact of the list to the gGreedy graph and to the heap (setting the end vertices as busy).
			intermittentEdgeEndMinHeap.add(intermittentEdges.get(0));
			int latestFlowTime = intermittentEdges.get(0).getBeg();
			intermittentEdges.get(0).getTempSrc().setBusy(intermittentEdges.get(0).getTempDst());
			intermittentEdges.get(0).getTempDst().setBusy(intermittentEdges.get(0).getTempSrc());
			gGreedy.addEdge(intermittentEdges.get(0).getTempSrc(), intermittentEdges.get(0).getTempDst(), intermittentEdges.get(0));
			
			// Initialize variables.
			double maxFlow = 0.;
			long blockedContacts = 0L;
			
			for (int i = 1; i < intermittentEdges.size(); i++) {
				// First, remove from the queue all the contacts that already expired.
				//if (!intermittentEdgeEndMinHeap.isEmpty()) {
				while ((!intermittentEdgeEndMinHeap.isEmpty()) && (intermittentEdgeEndMinHeap.peek().getEnd().compareTo(intermittentEdges.get(i).getBeg()) <= 0)) {
					// The edge on the top of the Heap has ended. Take it out, add it to the gGreedy graph, calculate the Max-Flow, and remove it from the graph.
					GreedyEdge finalizedEdge = intermittentEdgeEndMinHeap.poll();
					//gGreedy.setEdgeWeight(finalizedEdge, finalizedEdge.getBW()*(finalizedEdge.getEnd() - finalizedEdge.getBeg()));
					finalizedEdge.setCap(finalizedEdge.getBW()*(finalizedEdge.getEnd() - finalizedEdge.getBeg()));
					finalizedEdge.setTime(1);
					
					// Set all other intermittent edges weights to current moment.
					for (GreedyEdge e : intermittentEdgeEndMinHeap) {
						//gGreedy.setEdgeWeight(e, e.getBW()*(finalizedEdge.getEnd() - Math.max(latestFlowTime, e.getBeg())));
						e.setCap(e.getBW()*(finalizedEdge.getEnd() - Math.max(latestFlowTime, e.getBeg())));
						e.setTime(1);
					}
					
					// Now, calculate the Max Flow (min cut) on the current network before removing the finalized edge.
					
					EventDriven<GreedyGraph<GreedyNode, GreedyEdge>, GreedyNode, GreedyEdge> evnt = new EventDriven<GreedyGraph<GreedyNode, GreedyEdge>, GreedyNode, GreedyEdge>();
					EventGraph<TimedNode, EventEdge> evntGraph = evnt.createEventDrivenGraph(gGreedy);
					
					ret = evntGraph;
			        
					EdmondsKarpMaximumFlow<TimedNode, EventEdge> maxFlowGraph = new EdmondsKarpMaximumFlow<TimedNode, EventEdge>(evntGraph);
			        maxFlow = maxFlow + maxFlowGraph.buildMaximumFlow(evntGraph.getSource(), evntGraph.getSink()).getValue();
					
					// Finally, remove the finalized edge from the gGreedy graph and set both end as free.
					finalizedEdge.getTempDst().setFree(finalizedEdge.getTempSrc());
					finalizedEdge.getTempSrc().setFree(finalizedEdge.getTempDst());
					gGreedy.removeEdge(finalizedEdge);
					if (gGreedy.containsEdge(finalizedEdge)) {
						System.out.println("ERROR! Edge not removed!");
					}
					
					// Update the latest time that max flow was calculated.
					latestFlowTime = finalizedEdge.getEnd();
					
				}
				//}
				
				// Now, if the current edge doesn't connect nodes that are already being used by intermittent edges, add it to the graph and heap.
				if ((!intermittentEdges.get(i).getTempSrc().isBusy()) && (!intermittentEdges.get(i).getTempDst().isBusy())) {
					// Both ends are free, so first add edge to heap
					intermittentEdgeEndMinHeap.add(intermittentEdges.get(i)); 
					// Set both ends as busy
					intermittentEdges.get(i).getTempSrc().setBusy(intermittentEdges.get(i).getTempDst());
					intermittentEdges.get(i).getTempDst().setBusy(intermittentEdges.get(i).getTempSrc());
					// Add edge to gGreedy graph
					gGreedy.addEdge(intermittentEdges.get(i).getTempSrc(), intermittentEdges.get(i).getTempDst(), intermittentEdges.get(i));
				} else if (!intermittentEdges.get(i).getTempSrc().isBusy()) { // In this strategy, switch connections if the new contact has more 
					// Source is free, target is busy
					// Since the target is busy, get the other aerial link connected to the target
					GreedyEdge otherEdge = gGreedy.getEdge(intermittentEdges.get(i).getTempDst(), intermittentEdges.get(i).getTempDst().getOccupied());
					double otherLinkBW = otherEdge.getBW();
					double otherLinkRemainingTime = otherEdge.getEnd() - intermittentEdges.get(i).getBeg();
					if ((otherLinkBW * otherLinkRemainingTime) < intermittentEdges.get(i).getMaxBW()) { 
						// New contact will provide more bw than current, switch.
						// First, calculate the max flow in the current graph with the current edge and increment the previous max flow
						// Set all other intermittent edges weights to current moment.
						for (GreedyEdge e : intermittentEdgeEndMinHeap) {
							//gGreedy.setEdgeWeight(e, e.getBW()*(finalizedEdge.getEnd() - Math.max(latestFlowTime, e.getBeg())));
							e.setCap(e.getBW()*(intermittentEdges.get(i).getBeg() - Math.max(latestFlowTime, e.getBeg())));
							e.setTime(1);
						}
						
						// Now, calculate the Max Flow (min cut) on the current network before removing the finalized edge.
						
						EventDriven<GreedyGraph<GreedyNode, GreedyEdge>, GreedyNode, GreedyEdge> evnt = new EventDriven<GreedyGraph<GreedyNode, GreedyEdge>, GreedyNode, GreedyEdge>();
						EventGraph<TimedNode, EventEdge> evntGraph = evnt.createEventDrivenGraph(gGreedy);
						
						ret = evntGraph;
				        
						EdmondsKarpMaximumFlow<TimedNode, EventEdge> maxFlowGraph = new EdmondsKarpMaximumFlow<TimedNode, EventEdge>(evntGraph);
				        maxFlow = maxFlow + maxFlowGraph.buildMaximumFlow(evntGraph.getSource(), evntGraph.getSink()).getValue();
						
				        // Remove edge from graph and from heap
						otherEdge.getTempDst().setFree(otherEdge.getTempSrc());
						otherEdge.getTempSrc().setFree(otherEdge.getTempDst());
						intermittentEdgeEndMinHeap.remove(otherEdge);
						gGreedy.removeEdge(otherEdge);
						if (gGreedy.containsEdge(otherEdge)) {
							System.out.println("ERROR! Edge not removed!");
						}
						// Update the latest time that max flow was calculated.
						latestFlowTime = intermittentEdges.get(i).getBeg();
						
						// Now, add the new one to the graph and to the heap
						intermittentEdgeEndMinHeap.add(intermittentEdges.get(i)); 
						// Set both ends as busy
						intermittentEdges.get(i).getTempSrc().setBusy(intermittentEdges.get(i).getTempDst());
						intermittentEdges.get(i).getTempDst().setBusy(intermittentEdges.get(i).getTempSrc());
						// Add edge to gGreedy graph
						gGreedy.addEdge(intermittentEdges.get(i).getTempSrc(), intermittentEdges.get(i).getTempDst(), intermittentEdges.get(i));
						
					} else { // Current contact provides more bw than new, don't switch.
						blockedContacts++;
					}
				} else if (!intermittentEdges.get(i).getTempDst().isBusy()) {
					// Source is busy, target is free
					// Since the source is busy, get the other aerial link connected to the source
					GreedyEdge otherEdge = gGreedy.getEdge(intermittentEdges.get(i).getTempSrc(), intermittentEdges.get(i).getTempSrc().getOccupied());
					double otherLinkBW = otherEdge.getBW();
					double otherLinkRemainingTime = otherEdge.getEnd() - intermittentEdges.get(i).getBeg();
					if ((otherLinkBW * otherLinkRemainingTime) < intermittentEdges.get(i).getMaxBW()) { 
						// New contact will provide more bw than current, switch.
						// First, calculate the max flow in the current graph with the current edge and increment the previous max flow
						// Set all other intermittent edges weights to current moment.
						for (GreedyEdge e : intermittentEdgeEndMinHeap) {
							//gGreedy.setEdgeWeight(e, e.getBW()*(finalizedEdge.getEnd() - Math.max(latestFlowTime, e.getBeg())));
							e.setCap(e.getBW()*(intermittentEdges.get(i).getBeg() - Math.max(latestFlowTime, e.getBeg())));
							e.setTime(1);
						}
						
						// Now, calculate the Max Flow (min cut) on the current network before removing the finalized edge.
						
						EventDriven<GreedyGraph<GreedyNode, GreedyEdge>, GreedyNode, GreedyEdge> evnt = new EventDriven<GreedyGraph<GreedyNode, GreedyEdge>, GreedyNode, GreedyEdge>();
						EventGraph<TimedNode, EventEdge> evntGraph = evnt.createEventDrivenGraph(gGreedy);
						
						ret = evntGraph;
				        
						EdmondsKarpMaximumFlow<TimedNode, EventEdge> maxFlowGraph = new EdmondsKarpMaximumFlow<TimedNode, EventEdge>(evntGraph);
				        maxFlow = maxFlow + maxFlowGraph.buildMaximumFlow(evntGraph.getSource(), evntGraph.getSink()).getValue();
						
				        // Remove edge from graph and from heap
						otherEdge.getTempDst().setFree(otherEdge.getTempSrc());
						otherEdge.getTempSrc().setFree(otherEdge.getTempDst());
						intermittentEdgeEndMinHeap.remove(otherEdge);
						gGreedy.removeEdge(otherEdge);
						if (gGreedy.containsEdge(otherEdge)) {
							System.out.println("ERROR! Edge not removed!");
						}
						// Update the latest time that max flow was calculated.
						latestFlowTime = intermittentEdges.get(i).getBeg();
						
						// Now, add the new one to the graph and to the heap
						intermittentEdgeEndMinHeap.add(intermittentEdges.get(i)); 
						// Set both ends as busy
						intermittentEdges.get(i).getTempSrc().setBusy(intermittentEdges.get(i).getTempDst());
						intermittentEdges.get(i).getTempDst().setBusy(intermittentEdges.get(i).getTempSrc());
						// Add edge to gGreedy graph
						gGreedy.addEdge(intermittentEdges.get(i).getTempSrc(), intermittentEdges.get(i).getTempDst(), intermittentEdges.get(i));
						
					} else { // Current contact provides more bw than new, don't switch.
						blockedContacts++;
					}
				} else { 
					// Source and target are busy
					// Since both are busy, get both of them and compare
					GreedyEdge otherSrcEdge = gGreedy.getEdge(intermittentEdges.get(i).getTempSrc(), intermittentEdges.get(i).getTempSrc().getOccupied());
					double otherSrcLinkBW = otherSrcEdge.getBW();
					double otherSrcLinkRemainingTime = otherSrcEdge.getEnd() - intermittentEdges.get(i).getBeg();
					
					//System.out.println(intermittentEdges.get(i).getTempSrc() + " --- " + intermittentEdges.get(i).getTempSrc().getOccupied().getOccupied());
					//System.out.println(intermittentEdges.get(i).getTempDst() + " --- " + intermittentEdges.get(i).getTempDst().getOccupied());
					//System.out.println(otherSrcEdge);
					//System.out.println(intermittentEdges.get(i).getTempSrc() + " --- " + intermittentEdges.get(i).getTempDst());
					//System.out.println(intermittentEdges.get(i).getTempDst() + " == " + intermittentEdges.get(i).getTempDst().getOccupied().getOccupied());
					
					GreedyEdge otherDstEdge = gGreedy.getEdge(intermittentEdges.get(i).getTempDst(), intermittentEdges.get(i).getTempDst().getOccupied());
					double otherDstLinkBW = otherDstEdge.getBW();
					double otherDstLinkRemainingTime = otherDstEdge.getEnd() - intermittentEdges.get(i).getBeg();
					
					if (((otherDstLinkBW * otherDstLinkRemainingTime) + (otherSrcLinkBW * otherSrcLinkRemainingTime)) < intermittentEdges.get(i).getMaxBW()) { 
						// New contact will provide more bw than both the current contacts combined.
						// First, calculate the max flow in the current graph with the current edge and increment the previous max flow
						// Set all other intermittent edges weights to current moment.
						for (GreedyEdge e : intermittentEdgeEndMinHeap) {
							//gGreedy.setEdgeWeight(e, e.getBW()*(finalizedEdge.getEnd() - Math.max(latestFlowTime, e.getBeg())));
							e.setCap(e.getBW()*(intermittentEdges.get(i).getBeg() - Math.max(latestFlowTime, e.getBeg())));
							e.setTime(1);
						}
						
						// Now, calculate the Max Flow (min cut) on the current network before removing the finalized edge.
						
						EventDriven<GreedyGraph<GreedyNode, GreedyEdge>, GreedyNode, GreedyEdge> evnt = new EventDriven<GreedyGraph<GreedyNode, GreedyEdge>, GreedyNode, GreedyEdge>();
						EventGraph<TimedNode, EventEdge> evntGraph = evnt.createEventDrivenGraph(gGreedy);
						
						ret = evntGraph;
				        
						EdmondsKarpMaximumFlow<TimedNode, EventEdge> maxFlowGraph = new EdmondsKarpMaximumFlow<TimedNode, EventEdge>(evntGraph);
				        maxFlow = maxFlow + maxFlowGraph.buildMaximumFlow(evntGraph.getSource(), evntGraph.getSink()).getValue();
						
				        // Remove both edges from graph and from heap
				        otherSrcEdge.getTempDst().setFree(otherSrcEdge.getTempSrc());
				        otherSrcEdge.getTempSrc().setFree(otherSrcEdge.getTempDst());
						intermittentEdgeEndMinHeap.remove(otherSrcEdge);
						gGreedy.removeEdge(otherSrcEdge);
						
						otherDstEdge.getTempDst().setFree(otherDstEdge.getTempSrc());
						otherDstEdge.getTempSrc().setFree(otherDstEdge.getTempDst());
						intermittentEdgeEndMinHeap.remove(otherDstEdge);
						gGreedy.removeEdge(otherDstEdge);
						
						if (gGreedy.containsEdge(otherSrcEdge) || gGreedy.containsEdge(otherDstEdge)) {
							System.out.println("ERROR! Edge not removed!");
						}
						
						// Update the latest time that max flow was calculated.
						latestFlowTime = intermittentEdges.get(i).getBeg();
						
						// Now, add the new one to the graph and to the heap
						intermittentEdgeEndMinHeap.add(intermittentEdges.get(i)); 
						// Set both ends as busy
						intermittentEdges.get(i).getTempSrc().setBusy(intermittentEdges.get(i).getTempDst());
						intermittentEdges.get(i).getTempDst().setBusy(intermittentEdges.get(i).getTempSrc());
						// Add edge to gGreedy graph
						gGreedy.addEdge(intermittentEdges.get(i).getTempSrc(), intermittentEdges.get(i).getTempDst(), intermittentEdges.get(i));
						
					} else { // Current contacts provides more bw than new, don't switch.
						blockedContacts++;
					}
				}
			}
			
			System.out.println("Blocked contacts (switching): " + blockedContacts);
			System.out.println("Good contacts (switching): " + (intermittentEdges.size() - blockedContacts));
			
			System.out.println("Total greedy max flow (switching): " + maxFlow);
			
    	} catch (IOException e) {}
    	
    	return ret;
    }


    public static SimpleDirectedWeightedGraph<TimedNode, EventEdge>  greedyAllStrategy() { //void greedyStrategy() {
    	GreedyGraph<GreedyNode, GreedyEdge> gGreedy = new GreedyGraph<GreedyNode, GreedyEdge>(GreedyEdge.class);
    	SimpleDirectedWeightedGraph<TimedNode, EventEdge>  ret = null;
    	
    	try {
    		BufferedReader reader;
    		if (!TESTING) {
    			reader = Files.newBufferedReader(new File(nodeInfo).toPath(), charset);
    		} else {
    			reader = Files.newBufferedReader(new File(nodeInfoTest).toPath(), charset);
    		}
    		String line = null;
    		
    		// First, add all vertices.
    		while ((line = reader.readLine()) != null) {
				if (line.length() > 0) {
					if (line.charAt(0) != '#') {
						String name = line.split(",")[0].toString();
						double buffer = Double.valueOf(line.split(",")[1].toString());
						GreedyNode n;
						if (!TESTING) {
							n = new GreedyNode(name, buffer, "");
						} else {
							n = new GreedyNode(name, buffer, line.split(",")[2].toString());
						}
						gGreedy.addVertex(n);
					}
				}
			}
    		
    		if (!TESTING) {
    			reader = Files.newBufferedReader(new File(cntcTimesOvlp).toPath(), charset);
    		} else {
    			reader = Files.newBufferedReader(new File(cntcTimesOvlpTest).toPath(), charset);
    		}
    		line = null;
    		List<GreedyEdge> intermittentEdges = new ArrayList<GreedyEdge>();
    		
    		// Second, create the fixed topology and add all intermittent edges to a list (intermittentEdges).
			while ((line = reader.readLine()) != null) {
				if ((line.charAt(0) != '#') && (line.length() > 0)) {
					if (Integer.valueOf(line.split(",")[3].toString()) < 0) { // The first few lines all have t=-1 representing the fixed topo.
						GreedyNode src = gGreedy.getVertexByName(line.split(",")[0].toString());
						GreedyNode dst = gGreedy.getVertexByName(line.split(",")[1].toString());
						
						if ((src == null) || (dst == null)) {
							System.out.println("ERROR! Source or destination in greedy graph not found!");
						}
						
						GreedyEdge e = new GreedyEdge(-1,-1, Double.valueOf(line.split(",")[2].toString()), src, dst);
						if (!gGreedy.addEdge(src, dst, e)) {
							e = gGreedy.getEdge(src, dst);
						}
						e.setTime(-1);
						e.setCap(Double.valueOf(line.split(",")[2].toString()));
						//gGreedy.setEdgeWeight(e, );
					} else {
						GreedyEdge e = new GreedyEdge(Integer.valueOf(line.split(",")[3].toString()),
								Integer.valueOf(line.split(",")[4].toString()), 
								Double.valueOf(line.split(",")[5].toString()), 
								gGreedy.getVertexByName(line.split(",")[0].toString()), 
								gGreedy.getVertexByName(line.split(",")[1].toString()));
						
						intermittentEdges.add(e);
					}
				}
			}
			
			// Now, gGreedy has all non-intermittent links and all nodes (intermittent or not).
			Collections.sort(intermittentEdges, new CompIntermittentEdgeBeg());
			
			PriorityQueue<GreedyEdge> intermittentEdgeEndMinHeap = new PriorityQueue<GreedyEdge>(intermittentEdges.size(), new CompIntermittentEdgeEnd());	
			intermittentEdgeEndMinHeap.clear();
			
			// First, add the the first contact of the list to the gGreedy graph and to the heap (setting the end vertices as busy).
			intermittentEdgeEndMinHeap.add(intermittentEdges.get(0));
			int latestFlowTime = intermittentEdges.get(0).getBeg();
			intermittentEdges.get(0).getTempSrc().setBusy(intermittentEdges.get(0).getTempDst());
			intermittentEdges.get(0).getTempDst().setBusy(intermittentEdges.get(0).getTempSrc());
			gGreedy.addEdge(intermittentEdges.get(0).getTempSrc(), intermittentEdges.get(0).getTempDst(), intermittentEdges.get(0));
			
			// Initialize variables.
			double maxFlow = 0.;
			long blockedContacts = 0L;
			
			for (int i = 1; i < intermittentEdges.size(); i++) {
				// First, remove from the queue all the contacts that already expired.
				//if (!intermittentEdgeEndMinHeap.isEmpty()) {
				while ((!intermittentEdgeEndMinHeap.isEmpty()) && (intermittentEdgeEndMinHeap.peek().getEnd().compareTo(intermittentEdges.get(i).getBeg()) <= 0)) {
					// The edge on the top of the Heap has ended. Take it out, add it to the gGreedy graph, calculate the Max-Flow, and remove it from the graph.
					GreedyEdge finalizedEdge = intermittentEdgeEndMinHeap.poll();
					//gGreedy.setEdgeWeight(finalizedEdge, finalizedEdge.getBW()*(finalizedEdge.getEnd() - finalizedEdge.getBeg()));
					finalizedEdge.setCap(finalizedEdge.getBW()*(finalizedEdge.getEnd() - finalizedEdge.getBeg()));
					finalizedEdge.setTime(1);
					
					// Set all other intermittent edges weights to current moment.
					for (GreedyEdge e : intermittentEdgeEndMinHeap) {
						//gGreedy.setEdgeWeight(e, e.getBW()*(finalizedEdge.getEnd() - Math.max(latestFlowTime, e.getBeg())));
						e.setCap(e.getBW()*(finalizedEdge.getEnd() - Math.max(latestFlowTime, e.getBeg())));
						e.setTime(1);
					}
					
					// Now, calculate the Max Flow (min cut) on the current network before removing the finalized edge.
					
					EventDriven<GreedyGraph<GreedyNode, GreedyEdge>, GreedyNode, GreedyEdge> evnt = new EventDriven<GreedyGraph<GreedyNode, GreedyEdge>, GreedyNode, GreedyEdge>();
					EventGraph<TimedNode, EventEdge> evntGraph = evnt.createEventDrivenGraph(gGreedy);
					
					ret = evntGraph;
			        
					EdmondsKarpMaximumFlow<TimedNode, EventEdge> maxFlowGraph = new EdmondsKarpMaximumFlow<TimedNode, EventEdge>(evntGraph);
			        maxFlow = maxFlow + maxFlowGraph.buildMaximumFlow(evntGraph.getSource(), evntGraph.getSink()).getValue();
					
					// Finally, remove the finalized edge from the gGreedy graph and set both end as free.
					finalizedEdge.getTempDst().setFree(finalizedEdge.getTempSrc());
					finalizedEdge.getTempSrc().setFree(finalizedEdge.getTempDst());
					gGreedy.removeEdge(finalizedEdge);
					if (gGreedy.containsEdge(finalizedEdge)) {
						System.out.println("ERROR! Edge not removed!");
					}
					
					// Update the latest time that max flow was calculated.
					latestFlowTime = finalizedEdge.getEnd();
					
				}
				//}
				
				intermittentEdgeEndMinHeap.add(intermittentEdges.get(i));
				
			}
			
    	} catch (IOException e) {}
    	
    	return ret;
    }

}