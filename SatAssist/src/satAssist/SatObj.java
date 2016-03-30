package satAssist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import org.orekit.utils.PVCoordinates;

public class SatObj{
	private String name;
	private PVCoordinates coord;
	private double buffer;
	private int links;
	private ArrayList<DistanceSatPair> distances = new ArrayList<DistanceSatPair>();
	
	public SatObj (String s, PVCoordinates p) {
		this.name = s;
		this.coord = p;
		this.buffer = SatelliteAssistance.SATBUFFER;
		this.links = 0;
		//this.distances ;
	}
	
	public void calculateDistances(Set<SatObj> satSet) {
		for (SatObj s : satSet) {
			if (!s.equals(this)) {
				this.distances.add(new DistanceSatPair(s,this.coord.getPosition().distance(s.getCoord().getPosition())));
			}
		}
		Collections.sort(this.distances, new DistComparator());
	}
	
	public ArrayList<DistanceSatPair> getDistances() {
		return this.distances;
	}
	
	public void incLinks() {
		this.links++;
	}
	
	public Integer getLinks() {
		return this.links;
	}
	
	public PVCoordinates getCoord() {
		return this.coord;
	}
	
	public Double getBuffer() {
		return this.buffer;
	}
	
	public String getName() {
		return this.name;
	}
	
	@Override
	public String toString() {
		return this.name.replaceAll("\\s+","")+","+this.buffer;
	}
	
	@Override
	public int hashCode() {
		String concat = this.name;// + this.coord.toString();
	    return concat.hashCode();
	}
	
	@Override
	public boolean equals(Object other) {
	    if (this == other)
	       return true;

	    if (!(other instanceof SatObj))
	       return false;

	    SatObj otherSat = (SatObj) other;
	    
	    // So that the lookup of ArrayList works, we will retrieve the object whose end date hasn't been set yet.
	    return (otherSat.getName().compareTo(this.getName()) == 0);
	}
}
