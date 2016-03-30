package satAssist;

import org.jgrapht.graph.DefaultEdge;

public class TimedWeightedEdge extends DefaultEdge {
	
	private static final long serialVersionUID = 229708706467350999L;
		
	Integer time = 0;
	Double capacity = 0.;
	

	@Override
	public int hashCode() {
		String concat = time.toString() + capacity.toString() + super.getSource() + super.getTarget();
	    return concat.hashCode();
	}
	
	@Override
	public boolean equals(Object other) {
	    if (this == other)
	       return true;

	    if (!(other instanceof TimedWeightedEdge))
	       return false;

	    TimedWeightedEdge otherEdge = (TimedWeightedEdge) other;

	    return (otherEdge.getTime().compareTo(this.getTime()) == 0) &&
	    		(otherEdge.getCap().compareTo(this.getCap()) == 0) &&
	    		((
	    		(otherEdge.getSource().equals(this.getSource())) && (otherEdge.getTarget().equals(this.getTarget())) ) || 
	    		((otherEdge.getSource().equals(this.getTarget())) && (otherEdge.getTarget().equals(this.getSource()))
	    		));
	}
	
	
	@Override
	public String toString() {
		return "< t = " + String.valueOf(this.time) +" , cap = "+ String.valueOf(this.capacity) +" >";
	}
	
	public Integer getTime() {
		return this.time;
	}
	
	public Double getCap() {
	    return this.capacity;
	}
	
	public void setTime(Integer t) {
		this.time = t;
	}
	
	public void setCap(Double c) {
	    this.capacity = c;
	}
}
