package org.NooLab.field.repulsive.components.topology;

public class ParticleLinkage {

	public int linkIsDirectedTowards = -1;
	
	public double maxDistance = -1.0;
	protected double minDistance = -1.0;
	
	
	public ParticleLinkage(){
		
	}
	
	
	
	public int getLinkIsDirectedTowards() {
		return linkIsDirectedTowards;
	}
	public void setLinkIsDirectedTowards(int linkIsDirectedTowards) {
		this.linkIsDirectedTowards = linkIsDirectedTowards;
	}
	public double getMaxDistance() {
		return maxDistance;
	}
	public void setMaxDistance(double maxDistance) {
		this.maxDistance = maxDistance;
	}
	public double getMinDistance() {
		return minDistance;
	}
	public void setMinDistance(double minDistance) {
		this.minDistance = minDistance;
	}

	
}
