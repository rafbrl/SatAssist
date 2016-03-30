package satAssist;

import java.util.Comparator;

public class CompIntermittentEdgeEnd implements Comparator<GreedyEdge> {
    @Override
    public int compare(GreedyEdge o1, GreedyEdge o2) {
    	
		if (o1.getEnd().compareTo(o2.getEnd()) == 0) {
			if (o1.getBeg().compareTo(o2.getBeg()) == 0) {
    			if (o1.getTempSrc().getName().compareTo(o2.getTempSrc().getName()) == 0) {
    				o1.getTempDst().getName().compareTo(o2.getTempDst().getName());
    			}
    			return o1.getTempSrc().getName().compareTo(o2.getTempSrc().getName());
    		}
			return o1.getBeg().compareTo(o2.getBeg());
    	}
    	return o1.getEnd().compareTo(o2.getEnd());
    }
}
