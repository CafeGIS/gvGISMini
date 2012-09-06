package es.prodevelop.gvsig.mini.gpe.containers;

import java.util.ArrayList;

public class MultiPolygon extends MultiGeometry {

	public void addPolygon(com.vividsolutions.jts.geom.Polygon polygon) {
		this.addGeometry(polygon);
	}

	public ArrayList getPolygons() {
		return this.getGeometries();
	}

	public com.vividsolutions.jts.geom.Polygon getPolygonAt(int index) {
		return (com.vividsolutions.jts.geom.Polygon) this.getGeometryAt(index);
	}
}
