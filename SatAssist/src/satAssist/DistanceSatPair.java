package satAssist;

public class DistanceSatPair {
	private SatObj s;
	private Double d;
	public DistanceSatPair(SatObj x, Double y) {
		this.s = x;
		this.d = y;
	}
	
	public SatObj getSat() {
		return this.s;
	}
	
	public Double getDist() {
		return this.d;
	}
	@Override
	public String toString() {
		return this.s.getName() +"@"+this.d;
	}
	
	@Override
	public int hashCode() {
		String concat = this.s.getName();// + this.coord.toString();
	    return concat.hashCode();
	}
	@Override
	public boolean equals(Object other) {
	    if (this == other)
	       return true;

	    if (!(other instanceof DistanceSatPair))
	       return false;

	    DistanceSatPair d = (DistanceSatPair) other;
	    
	    // So that the lookup of ArrayList works, we will retrieve the object whose end date hasn't been set yet.
	    return (d.getSat().getName().compareTo(this.s.getName()) == 0);
	}
	
}
