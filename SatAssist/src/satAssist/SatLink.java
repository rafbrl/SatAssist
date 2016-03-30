package satAssist;

public class SatLink{
	private SatObj s1;
	private SatObj s2;
	private double bw;
	
	public SatLink (SatObj x1, SatObj x2) {
		if (x1.getName().compareTo(x2.getName()) < 0) {
			this.s1 = x1;
			this.s2 = x2;
		} else {
			this.s1 = x2;
			this.s2 = x1;
		}
		this.bw = SatelliteAssistance.INTERSATBW;
	}
	
	public SatObj getSrc() {
		return this.s1;
	}
	
	public SatObj getTgt() {
		return this.s2;
	}
	
	public Double getBW() {
		return this.bw;
	}
	
	@Override
	public String toString() {
		return this.s1.getName().replaceAll("\\s+","")+","+this.s2.getName().replaceAll("\\s+","")+","+this.bw+",-1";
	}
	
	@Override
	public int hashCode() {
		String concat = s1.getName()+this.s2.getName();// + this.coord.toString();
	    return concat.hashCode();
	}
	
	@Override
	public boolean equals(Object other) {
	    if (this == other)
	       return true;

	    if (!(other instanceof SatLink))
	       return false;

	    SatLink othLink = (SatLink) other;
	    
	    // So that the lookup of ArrayList works, we will retrieve the object whose end date hasn't been set yet.
	    return (othLink.s1.getName().compareTo(this.s1.getName()) == 0) && (othLink.s2.getName().compareTo(this.s2.getName()) == 0);
	}
}
