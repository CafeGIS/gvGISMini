package es.prodevelop.gvsig.mini.gpe.containers;

import java.util.ArrayList;

import com.vividsolutions.jts.geom.Point;

public class MultiPoint extends MultiGeometry {

	public void addPoint(Point point) {
		this.addGeometry(point);
	}

	public ArrayList getPoints() {
		return this.getGeometries();
	}

	public Point getPointAt(int index) {
		return (Point) getGeometryAt(index);
	}
}
