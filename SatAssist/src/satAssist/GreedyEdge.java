package satAssist;

public class GreedyEdge extends TimedWeightedEdge {
	private static final long serialVersionUID = 1L;
	
	Integer begin;
	Integer end;
	Double bw;
	GreedyNode temptativeSrc;
	GreedyNode temptativeDst;
	
	public GreedyEdge(Integer b, Integer e, Double r, GreedyNode s, GreedyNode d) {
		this.begin = b;
		this.end = e;
		this.bw = r;
		this.temptativeSrc = s;
		this.temptativeDst = d;
	}
	
	public Double getMaxBW() {
		return this.getBW()*(this.getEnd() - this.getBeg());
	}
	
	public Double getBW() {
		return this.bw;
	}
	
	public Integer getBeg() {
		return this.begin;
	}
	
	public Integer getEnd() {
		return this.end;
	}
	
	public GreedyNode getTempSrc() {
		return this.temptativeSrc;
	}
	
	public GreedyNode getTempDst() {
		return this.temptativeDst;
	}
	
	@Override
	public int hashCode() {
		String concat = begin.toString() + end.toString() + super.getSource() + super.getTarget();
	    return concat.hashCode();
	}
	
	@Override
	public boolean equals(Object other) {
	    if (this == other)
	       return true;

	    if (!(other instanceof GreedyEdge))
	       return false;

	    GreedyEdge otherEdge = (GreedyEdge) other;

	    return (otherEdge.getBeg().compareTo(this.getBeg()) == 0) &&
	    		(otherEdge.getEnd().compareTo(this.getEnd()) == 0) &&
	    		((
	    		(otherEdge.getSource().equals(this.getSource())) && (otherEdge.getTarget().equals(this.getTarget())) ) || 
	    		((otherEdge.getSource().equals(this.getTarget())) && (otherEdge.getTarget().equals(this.getSource()))
	    		));
	}
	
	
	
	/*
	@Override
	public String toString() {
		return String.valueOf(this.getWeight());
	}
	*/
	
}

