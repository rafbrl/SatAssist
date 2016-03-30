package satAssist;

import java.util.Comparator;

public class DistComparator implements Comparator<DistanceSatPair> {
	@Override
	public int compare(DistanceSatPair o1, DistanceSatPair o2) {
		return o1.getDist().compareTo(o2.getDist());
	}
}
