package satAssist;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jgrapht.WeightedGraph;

public class EventDriven <G extends WeightedGraph<V, E>, V extends SatAssistNode, E extends TimedWeightedEdge> {
    public EventGraph<TimedNode, EventEdge> createEventDrivenGraph (G g) {
    	EventGraph<TimedNode, EventEdge> res = new EventGraph<TimedNode, EventEdge>(EventEdge.class);
		
		// Get all times from all edges of the contact graph and sort them in increasing order
		Iterator<E> it = g.edgeSet().iterator();
		E e;
		V src;
		V tgt;
		Set<Integer> times = new HashSet<Integer>();
		
		while (it.hasNext()) {
			e = it.next();
			if (e.getTime() >= 0) {
				times.add(e.getTime());
			}
		}
		
		ArrayList<Integer> timeList = new ArrayList<Integer>(times);		
		Collections.sort(timeList);

		// For each instant, construct a graph with all edges with time stamp t or -1 and their respective vertices
		Set<TimedNode> fixedNodes = new HashSet<TimedNode>();

		for (int i = 0; i < timeList.size(); i++) {
			int t = timeList.get(i);
			it = g.edgeSet().iterator();

			while (it.hasNext()) {
				e = it.next();
				if (e.getTime() == t) {
					// Only nodes in the fixed network are non-intermittent
					/* Bidirectional link, add 4 vertices and 4 edges. 
					   nS0	  nT0
					    | \  / |
					    |  \/  |
				   buffS|  /\  |buffT
						| /  \ |
						vv    vv
					   nS1    nT1
					*/		
					
					src = g.getEdgeSource(e);
					tgt = g.getEdgeTarget(e);
					
					TimedNode nS0 = new TimedNode(src.getName(), t, 0);
					TimedNode nS1 = new TimedNode(src.getName(), t, 1);
					TimedNode nT0 = new TimedNode(tgt.getName(), t, 0);
					TimedNode nT1 = new TimedNode(tgt.getName(), t, 1);
					
					EventEdge nEdgeBuffS = res.getEdge(nS0, nS1);
					EventEdge nEdgeBuffT = res.getEdge(nT0, nT1);
					
					res.addVertex(nS0);
					res.addVertex(nS1);
					res.addVertex(nT0);
					res.addVertex(nT1);
		
					// Add non-intermittent nodes to a list so, latter, we can use it to recreate the whole non-intermittent network attached at that node
					for ( E ee : g.edgesOf(src) ) {
						if (ee.getTime() < 0) {
							fixedNodes.add(nS0); 
							fixedNodes.add(nS1);
						}
					}
					
					for ( E ee : g.edgesOf(tgt) ) {
						if (ee.getTime() < 0) {
							fixedNodes.add(nT0); 
							fixedNodes.add(nT1);
						}
					}
					
					if (e.getCap().doubleValue() > 0.) {
						EventEdge nEdgeSend;
						if ((nEdgeSend = res.addEdge(nT1, nS0)) == null){
							nEdgeSend = res.getEdge(nT1, nS0);
						}
						res.setEdgeWeight(nEdgeSend, Math.max(res.getEdgeWeight(nEdgeSend), e.getCap().doubleValue()));
					}
					
					if (e.getCap().doubleValue() > 0.) {
						EventEdge nEdgeRecv;
						if ((nEdgeRecv= res.addEdge(nS1, nT0)) == null) {
							nEdgeRecv = res.getEdge(nS1, nT0);
						}
						res.setEdgeWeight(nEdgeRecv, Math.max(res.getEdgeWeight(nEdgeRecv), e.getCap().doubleValue()));
					}
					
					if (e.getCap().doubleValue() > 0.) {
						if (nEdgeBuffS == null) {	
							nEdgeBuffS = res.addEdge(nS0, nS1);
						}
						res.setEdgeWeight(nEdgeBuffS, Double.POSITIVE_INFINITY);//0.);
					}

					if (e.getCap().doubleValue() > 0.) {
						if (nEdgeBuffT == null) {
							nEdgeBuffT = res.addEdge(nT0, nT1);
						}
						res.setEdgeWeight(nEdgeBuffT, Double.POSITIVE_INFINITY);//0.);
					}
					
					// Add edge 						res.setEdgeWeight(nEdgeBuffTInv, Double.POSITIVE_INFINITY);//0.);from immediate previous vertex of type 1 to current vertex of type 0
					boolean prevSAdded = false;
					boolean prevTAdded = false;
					for (int j = i - 1; j >= 0; j--) {
						int previousTime = timeList.get(j);
						if (!prevSAdded) {
							if ((src.getBuffer() > 0.) && (previousTime < t)) {
								TimedNode prevS = new TimedNode(src.getName(),previousTime,1);
								if (res.containsVertex(prevS)) {
									// Add edge between from immediately previous node of type 1 to current node of type 0 of current source
									EventEdge prev2currS = res.getEdge(prevS, nS0);
									if (prev2currS == null) {
											prev2currS = res.addEdge(prevS, nS0);
											res.setEdgeWeight(prev2currS, src.getBuffer());//res.getEdgeWeight(nEdgeBuffS));
									}
									prevSAdded = true;
								}
							} else {
								prevSAdded = true;
							}
						}
						if (!prevTAdded) {
							if ((tgt.getBuffer() > 0.) && (previousTime < t)) {
								TimedNode prevT = new TimedNode(tgt.getName(),previousTime,1);
								if (res.containsVertex(prevT)) {
									// Add edge between from immediately previous node of type 1 to current node of type 0 of current target
									EventEdge prev2currT = res.getEdge(prevT, nT0);
									if (prev2currT == null) {
										prev2currT = res.addEdge(prevT, nT0);
										res.setEdgeWeight(prev2currT, tgt.getBuffer());//res.getEdgeWeight(nEdgeBuffT));
									}
									prevTAdded = true;
								}
							} else {
								prevTAdded = true;
							}
						}
						if ((prevSAdded) && (prevTAdded)) break;
					}
				}
			}
		}
		
		// Now, attach the non-intermittent network to all appearances of non-intermittent nodes in the graph at each time t.
		List<TimedNode> fNodesList = new ArrayList<TimedNode>(fixedNodes);		
		Collections.sort(fNodesList, new CompTimedNode());
		
		//SatelliteAssistance.START_TIME = timeList.get(0);
		
		for (int i = 0; i < fNodesList.size(); i+=2) {
			if (!fNodesList.get(i).equalsLessType(fNodesList.get(i+1))) {
				System.out.println("Error! Corrupted non-intermittent node list!");
				return null; 
			}
			
			int t = fNodesList.get(i).getTime();
			int indexOfT = timeList.indexOf(t);
			int deltaT = 1;
			
			if (indexOfT < (timeList.size() - 1)) {
				deltaT = timeList.get(indexOfT + 1) - t;
			} 
			
			for (E edg : g.edgeSet()) {
				if (edg.getTime() < 0) {
					V srcFixed = g.getEdgeSource(edg);
					V tgtFixed = g.getEdgeTarget(edg);
					
					TimedNode nS0 = new TimedNode(srcFixed.getName(), t, 0);
					TimedNode nS1 = new TimedNode(srcFixed.getName(), t, 1);		
					TimedNode nT0 = new TimedNode(tgtFixed.getName(), t, 0);
					TimedNode nT1 = new TimedNode(tgtFixed.getName(), t, 1);
					
					EventEdge nEdgeBuffS = res.getEdge(nS0, nS1);
					EventEdge nEdgeBuffT = res.getEdge(nT0, nT1);
				
					res.addVertex(nS0);
					res.addVertex(nS1);
					res.addVertex(nT0);
					res.addVertex(nT1);
					
					if (res.getEdge(nT1, nS0) == null) {
						if ((edg.getCap().doubleValue() * deltaT) > 0.) {
							EventEdge nEdgeSend;
							if ((nEdgeSend = res.addEdge(nT1, nS0)) == null) {
								nEdgeSend = res.getEdge(nT1, nS0);
							}
							res.setEdgeWeight(nEdgeSend, Math.max(res.getEdgeWeight(nEdgeSend), edg.getCap().doubleValue() * deltaT)); // Multiply the throughput by the duration to get the capacity.
						}
					}
					
					if ((edg.getCap().doubleValue() * deltaT) > 0.) {
						if (nEdgeBuffS == null) {
							nEdgeBuffS = res.addEdge(nS0, nS1);
						}
						res.setEdgeWeight(nEdgeBuffS, Double.POSITIVE_INFINITY);//0.);
					}
					
					if (res.getEdge(nS1, nT0) == null) {
						if ((edg.getCap().doubleValue() * deltaT) > 0.) {
							EventEdge nEdgeRecv;
							if ((nEdgeRecv = res.addEdge(nS1, nT0)) == null) {
								nEdgeRecv = res.getEdge(nS1, nT0);
							}
							res.setEdgeWeight(nEdgeRecv, Math.max(res.getEdgeWeight(nEdgeRecv), edg.getCap().doubleValue() * deltaT));
						}
					}
					
					if ((edg.getCap().doubleValue() * deltaT) > 0.) {
						if (res.getEdge(nT0, nT1) == null) {
							nEdgeBuffT = res.addEdge(nT0, nT1);
						}
						res.setEdgeWeight(nEdgeBuffT, Double.POSITIVE_INFINITY);//0.);
					}
						
					// Add edge from immediate previous vertex of type 1 to current vertex of type 0
					boolean prevSAdded = false;
					boolean prevTAdded = false;
					for (int j = timeList.indexOf(t) - 1; j >= 0; j--) {
						int previousTime = timeList.get(j);
						if (!prevSAdded) {
							if ((srcFixed.getBuffer() > 0.) && (previousTime < t)) {
								TimedNode prevS = new TimedNode(srcFixed.getName(),previousTime,1);
								if (res.containsVertex(prevS)) {
									EventEdge prev2currS = res.getEdge(prevS, nS0);
									if (prev2currS == null) {
										prev2currS = res.addEdge(prevS, nS0);
										res.setEdgeWeight(prev2currS, srcFixed.getBuffer());
									}
									prevSAdded = true;
								}
							} else {
								prevSAdded = true;
							}
						}
						if (!prevTAdded) {
							if ((tgtFixed.getBuffer() > 0.) && (previousTime < t)) {
								TimedNode prevT = new TimedNode(tgtFixed.getName(),previousTime,1);
								if (res.containsVertex(prevT)) {
									EventEdge prev2currT = res.getEdge(prevT, nT0);
									if (prev2currT == null) {
										prev2currT = res.addEdge(prevT, nT0);
										res.setEdgeWeight(prev2currT, tgtFixed.getBuffer());
									}
									prevTAdded = true;
								}
							} else {
								prevTAdded = true;
							}
						}
						if ((prevSAdded) && (prevTAdded)) break;
					}
				}
			}
		}
		
		TimedNode sink = new TimedNode("sink", -1, 0);
		res.addVertex(sink);
		TimedNode source = new TimedNode("source", -1, 0);
		res.addVertex(source);
		
		try {
			BufferedReader reader;
			if (!SatelliteAssistance.TESTING) {
				reader = Files.newBufferedReader(new File(SatelliteAssistance.sourcesFile).toPath(), SatelliteAssistance.charset);
			} else {
				reader = Files.newBufferedReader(new File(SatelliteAssistance.sourcesFileTest).toPath(), SatelliteAssistance.charset);
			}
			String line = null;
			
			while ((line = reader.readLine()) != null) {
				if (line.length() > 0) {
					if (line.charAt(0) != '#') {
						for (TimedNode s : res.vertexSet()) {
							if ((s.getName().compareTo(line.toString()) == 0) ) { //&& (s.getType() == 1)) {
								EventEdge auxEdge = res.addEdge(source, s);
								res.setEdgeWeight(auxEdge, Double.POSITIVE_INFINITY);
							}
						}
					}
				}
			}
			reader.close();
			
			if (!SatelliteAssistance.TESTING) {
				reader = Files.newBufferedReader(new File(SatelliteAssistance.sinksFile).toPath(), SatelliteAssistance.charset);
			} else {
				reader = Files.newBufferedReader(new File(SatelliteAssistance.sinksFileTest).toPath(), SatelliteAssistance.charset);
			}
			line = null;
			
			while ((line = reader.readLine()) != null) {
				if (line.length() > 0) {
					if (line.charAt(0) != '#') {
						for (TimedNode s : res.vertexSet()) {
							if ((s.getName().compareTo(line.toString()) == 0) ) { //&& (s.getType() == 0)) {
								EventEdge auxEdge = res.addEdge(s, sink);
								res.setEdgeWeight(auxEdge, Double.POSITIVE_INFINITY);
							}
						}
					}
				}
			}
			reader.close();
			
		} catch (IOException x) {
			System.out.println("Error!");
		}
		
		res.setSource(source);
		res.setSink(sink);
		
		return res;
	}
	
}
