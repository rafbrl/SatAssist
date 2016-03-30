package satAssist;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Iterator;

import org.jgrapht.graph.WeightedMultigraph;

public class ContactDriven {
	
    public WeightedMultigraph<SatAssistNode, TimedWeightedEdge> createContactGraph() {
		WeightedMultigraph<SatAssistNode, TimedWeightedEdge> g = new WeightedMultigraph<SatAssistNode, TimedWeightedEdge>(TimedWeightedEdge.class);

		try {
			// Add vertices
			BufferedReader reader;
			String line = null;
			
			if (!SatelliteAssistance.TESTING) {
				reader = Files.newBufferedReader(new File(SatelliteAssistance.nodeInfo).toPath(), SatelliteAssistance.charset);
			} else {
				reader = Files.newBufferedReader(new File(SatelliteAssistance.nodeInfoTest).toPath(), SatelliteAssistance.charset);
			}
			
			while ((line = reader.readLine()) != null) {
				if (line.length() > 0) {
					if (line.charAt(0) != '#') {
						String name = line.split(",")[0].toString();
						double buffer = Double.valueOf(line.split(",")[1].toString());
						SatAssistNode n = new SatAssistNode(name, buffer);
						g.addVertex(n);
					}
				}
			}
			
			// Add edges
			if (!SatelliteAssistance.TESTING) {
				reader = Files.newBufferedReader(new File(SatelliteAssistance.cntcTimes).toPath(), SatelliteAssistance.charset);
			} else {
				reader = Files.newBufferedReader(new File(SatelliteAssistance.cntcTimeTest).toPath(), SatelliteAssistance.charset);
			}
			
			line = null;
			
			while ((line = reader.readLine()) != null) {
				if (line.length() > 0) {
					if (line.charAt(0) != '#') {
						SatAssistNode src = null;
						SatAssistNode dst = null;
						Iterator<SatAssistNode> it = g.vertexSet().iterator();
						while(it.hasNext()) {
							SatAssistNode s = it.next();
							if (s.getName().compareTo(line.split(",")[0].toString()) == 0) {
								src = s;
							}
							if (s.getName().compareTo(line.split(",")[1].toString()) == 0) {
								dst = s;
							}
							if ((dst != null) && (src != null)) {
								break;
							}
						}
						
						if (src == null) {
							System.out.println("ERROR! Source not found! (line: " + line + " )");
						}
						if (dst == null) {System.out.println("ERROR! Destination not found! (line: " + line + " )");
							
						}
						
						int time = Integer.valueOf(line.split(",")[3].toString());
						double cap = Double.valueOf(line.split(",")[2].toString());

						TimedWeightedEdge e = new TimedWeightedEdge();
						e.setTime(time);
						e.setCap(cap);
						g.addEdge(src, dst, e);
						
						/*
						if (((!g.containsEdge(src, dst)) && (!g.containsEdge(dst, src))) ||
								((g.getEdge(src, dst).getTime() != time)) ||
								((g.getEdge(dst, src).getTime() != time))) {
							TimedWeightedEdge e = g.addEdge(src, dst);
							e.setTime(time);
							e.setCap(cap);
						}
						*/
					}
				}
			}
			
			
		} catch (IOException x) {
			System.out.println("Error!");
		}

        return g;
    }
}