package es.prodevelop.gvsig.mini.gpe.containers;

import com.vividsolutions.jts.geom.LinearRing;

public class Polygon extends es.prodevelop.gvsig.mini.gpe.containers.gvsig.Polygon {

	private LinearRing linearRing;

	public void setLinearRing(LinearRing linearRing) {
		this.linearRing = linearRing;
	}

	public LinearRing getLinearRing() {
		return this.linearRing;
	}
}
