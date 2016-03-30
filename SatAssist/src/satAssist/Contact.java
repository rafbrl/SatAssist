package satAssist;

import org.orekit.time.AbsoluteDate;

public class Contact {
	private String e1;
	private String e2;
	private AbsoluteDate t;
	private Double bw;
	
	public Contact (String s1, String s2, AbsoluteDate d, Double b) {
		this.e1 = s1;
		this.e2 = s2;
		this.t = d;
		this.bw = b;
	}
	
	public String getSource() {
		return this.e1;
	}
	
	public String getTarget() {
		return this.e2;
	}
	
	public AbsoluteDate getTime() {
		return this.t;
	}
	
	public void setBW(double b) {
		this.bw = b;
	}
	
	public Double getBW() {
		return this.bw;
	}
	
	@Override
	public int hashCode() {
		String concat = this.e1 + this.e2 + this.t.toString();
	    return concat.hashCode();
	}
	
	@Override
	public boolean equals(Object other) {
	    if (this == other)
	       return true;

	    if (!(other instanceof Contact))
	       return false;

	    Contact otherEvnt = (Contact) other;
	    
	    // So that the lookup of ArrayList works, we will retrieve the object whose bandwidth hasn't been set yet.
	    return (otherEvnt.getSource().compareTo(this.getSource()) == 0) && (otherEvnt.getTarget().compareTo(this.getTarget()) == 0) && (this.getBW() == null);
	}


	
}
