package satAssist;

import java.util.ArrayList;

public class GreedyNode extends SatAssistNode {

	ArrayList<GreedyNode> busyWith;
	int i;
	boolean isSatellite = false;
	
	public GreedyNode(String name, Double buffer, String type) {
		super(name, buffer);
		this.busyWith = new ArrayList<GreedyNode>();
		this.i = 0;
		if (type.compareTo("sat") == 0) {
			this.isSatellite = true;
		} else {
			this.isSatellite = false;
		}
	}
	
	public void setBusy(GreedyNode node) {
		if (node == null) {
			System.out.println("Error! Null node in greedynode!");
		}
		this.busyWith.add(node);
	}
	
	public boolean isBusy() {
		if (this.isSatellite()) {
			return this.busyWith.size() > 1;
		} else {
			return this.busyWith.size() > 0;
		}
	}
	
	public void setFree(GreedyNode n) {
		this.busyWith.remove(n);
	}
	
	public GreedyNode getOccupied() {
		if (!this.isBusy()) {
			return null;
		}
		i++;
		return this.busyWith.get(i % this.busyWith.size());
	}
	
	public boolean isSatellite() {
		if (SatelliteAssistance.TESTING) {
			return this.isSatellite;
		} else {
			return this.getName().toLowerCase().contains(SatelliteAssistance.SAT_PREFIX);
		}
	}
	
	/*
	@Override
	public boolean equals(Object other) {
	    if (!(other instanceof String))
	       return false;

	    String otherNode = (String) other;
	    return otherNode.compareTo(this.getName()) == 0;
	}
	*/
}

