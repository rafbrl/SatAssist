package satAssist;

public class TimedNode {
	private String name;
	private Integer time;
	private Integer type;
	
	public TimedNode(String n, Integer t, Integer b) {
		this.name = n;
		this.time = t;
		this.type = b;
	}
	
	@Override
	public int hashCode() {
		String concat = this.name + this.time.toString() + this.type.toString();
	    return concat.hashCode();
	}
	
	@Override
	public boolean equals(Object other) {
	    if (this == other)
	       return true;

	    if (!(other instanceof TimedNode))
	       return false;

	    TimedNode otherNode= (TimedNode) other;
	    int i = otherNode.getTime().intValue();
	    int j = this.getTime().intValue();
	    int k = otherNode.getType().intValue();
	    int l = this.getType().intValue();
	    //return (otherNode.getName().compareTo(this.getName()) == 0) && (otherTime.compareTo(thisTime) == 0) && otherNode.getType() == this.getType();
	    return (otherNode.getName().compareTo(this.getName()) == 0) && (i == j) && (k == l);
	}
	
	public boolean equalsLessType(Object other) {
	    if (this == other)
	       return true;

	    if (!(other instanceof TimedNode))
	       return false;

	    TimedNode otherNode = (TimedNode) other;
	    int i = otherNode.getTime().intValue();
	    int j = this.getTime().intValue();
	    return otherNode.getName() == this.getName() && (i==j);
	}
	
	@Override
	public String toString() {
		return "< " + this.name + " , "+ String.valueOf(this.time) +" , " + String.valueOf(this.type) +" >";
	}
	
	public String getName() {
		return this.name;
	}
	
	public Integer getTime() {
		return this.time;
	}

	public Integer getType() {
		return this.type;
	}
	
	public void setName(String newName) {
		this.name = newName;
	}
	
	public void setTime(Integer newBuffer) {
		this.time = newBuffer;
	}
	
	public void setType(Integer newType) {
		this.type = newType;
	}
	
}
