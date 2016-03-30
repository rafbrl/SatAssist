package satAssist;

import java.util.Comparator;

public class CompTimedNode implements Comparator<TimedNode> {
    @Override
    public int compare(TimedNode o1, TimedNode o2) {
    	if (o1.getTime().compareTo(o2.getTime()) == 0) {
    		if (o1.getName().compareTo(o2.getName()) == 0) {
    			return o1.getType().compareTo(o2.getType());
    		}
    		return o1.getName().compareTo(o2.getName());
    	}
        return o1.getTime().compareTo(o2.getTime());
    }
}
