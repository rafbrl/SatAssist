package satAssist;

import org.orekit.time.AbsoluteDate;
import org.orekit.utils.PVCoordinates;

public class ContactEvent implements Comparable<ContactEvent>{
	private AbsoluteDate begin;
	private AbsoluteDate end;
	private String grdStation;
	private String satellite;
	private Double bw;
	private PVCoordinates satInitCoord;
	
	public ContactEvent(String grd, String sat, AbsoluteDate b, AbsoluteDate e) throws Exception {
		if (grd.compareTo(sat) > 0) {
			this.grdStation = grd;
			this.satellite = sat;
		} else if (grd.compareTo(sat) < 0) {
			this.grdStation = sat;
			this.satellite = grd;
		} else {
			throw new Exception("Contact within the same station!");
		}
		
		this.begin = b;
		this.end = e;
		this.bw = SatelliteAssistance.SATBANDWIDTH;
		this.satInitCoord = null;
	}
	/*
	public void setEnd(AbsoluteDate e) {
		this.end = e;
	}
	*/
	public AbsoluteDate getBegin() {
		return this.begin;
	}
	
	public AbsoluteDate getEnd() {
		return this.end;
	}
	
	public void setCoord(PVCoordinates p) {
		this.satInitCoord = p;
	}
	
	public PVCoordinates getCoord() {
		return this.satInitCoord;
	}
	
	public String getGround() {
		return this.grdStation;
	}
	
	public String getSat() {
		return this.satellite;
	}
	
	public double getDur() {
		return this.getEnd().durationFrom(this.getBegin());
	}
	
	public Double getBW() {
		return this.bw * (this.getDur()/SatelliteAssistance.BWAMORT);
	}
	
	@Override
	public String toString() {
		
		int begin = Double.valueOf(this.getBegin().durationFrom(SatelliteAssistance.BEGINDATE)).intValue();
		int end = Double.valueOf(this.getEnd().durationFrom(SatelliteAssistance.BEGINDATE)).intValue();
		double bw = this.getBW();
		return this.getSat().replaceAll("\\s+","") + "," + this.getGround().replaceAll("\\s+","") +  "," + bw + "," + begin + "," + end + "," + this.bw;
	}
	
	@Override
	public int compareTo(ContactEvent o) {
		// TODO Auto-generated method stub
		return this.getBegin().compareTo(o.getBegin());
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		Integer b = Double.valueOf(this.getBegin().durationFrom(SatelliteAssistance.BEGINDATE)).intValue();
		Integer e = Double.valueOf(this.getEnd().durationFrom(SatelliteAssistance.BEGINDATE)).intValue();
		result = prime * result + ((begin == null) ? 0 : b.hashCode());
		result = prime * result + ((bw == null) ? 0 : bw.hashCode());
		result = prime * result + ((end == null) ? 0 : e.hashCode());
		result = prime * result + (((grdStation == null) || (satellite == null)) ? 0 : Math.max(grdStation.hashCode(), satellite.hashCode()));
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ContactEvent other = (ContactEvent) obj;
		if (begin == null) {
			if (other.begin != null)
				return false;
		} else { 
			int b1 = Double.valueOf(this.getBegin().durationFrom(SatelliteAssistance.BEGINDATE)).intValue();
			int b2 = Double.valueOf(other.getBegin().durationFrom(SatelliteAssistance.BEGINDATE)).intValue();
			if (b1 != b2)
				return false;
		}
		if (bw == null) {
			if (other.bw != null)
				return false;
		} else if (!bw.equals(other.bw))
			return false;
		if (end == null) {
			if (other.end != null)
				return false;
		} else {
			int e1 = Double.valueOf(this.getEnd().durationFrom(SatelliteAssistance.BEGINDATE)).intValue();
			int e2 = Double.valueOf(other.getEnd().durationFrom(SatelliteAssistance.BEGINDATE)).intValue();
			if (e1 != e2)
				return false;
		}
		if (grdStation == null) {
			if ((other.grdStation != null) && (other.satellite != null))
				return false;
		}
		if (satellite == null) {
			if ((other.satellite != null) && (other.grdStation != null))
				return false;
		}
		if ((!grdStation.equals(other.grdStation)) && (!grdStation.equals(other.satellite)))
			return false;
		if ((!satellite.equals(other.satellite)) && (!satellite.equals(other.grdStation)))
			return false;
		return true;
	}
	
	
	
}
