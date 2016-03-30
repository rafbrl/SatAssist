package satAssist;

public class SatAssistNode {
	private String name;
	private Double buffer;
	private Integer passCount;
	
	public SatAssistNode(String n, Double b) {
		this.name = n;
		this.buffer = b;
		this.passCount = 0;
	}
	
	@Override
	public int hashCode() {
		String concat = name + buffer.toString();
	    return concat.hashCode();
	}
	
	@Override
	public boolean equals(Object other) {
	    if (this == other)
	       return true;

	    if (!(other instanceof SatAssistNode))
	       return false;

	    SatAssistNode otherNode= (SatAssistNode) other;
	    int i = otherNode.getBuffer().intValue();
	    int j = this.getBuffer().intValue();
	    return (otherNode.getName().compareTo(this.getName()) == 0) && (i==j);
	}
	
	@Override
	public String toString() {
		return "< " + this.name + " , buff = " + String.valueOf(this.buffer) + " >";
	}
	
	public String getName() {
		return this.name;
	}
	
	public Double getBuffer() {
		return this.buffer;
	}
	
	public void setName(String newName) {
		this.name = newName;
	}
	
	public void setBuffer(Double newBuffer) {
		this.buffer = newBuffer;
	}
	
	public void incPass() {
		this.passCount++;
	}
	
	public void resetPass() {
		this.passCount = 0;
	}
	
	public int getPass() {
		return this.passCount;
	}

}
