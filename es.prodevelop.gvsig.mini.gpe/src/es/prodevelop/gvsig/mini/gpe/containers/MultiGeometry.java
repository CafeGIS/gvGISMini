package es.prodevelop.gvsig.mini.gpe.containers;

import java.util.ArrayList;

public class MultiGeometry extends es.prodevelop.gvsig.mini.gpe.containers.gvsig.Geometry {
	
private ArrayList geometries = null;
	
	public MultiGeometry(){
		geometries = new ArrayList();
	}

	/**
	 * @return the geometries
	 */
	public ArrayList getGeometries() {
		return geometries;
	}
	
	/**
	 * Adds a new Geometry
	 * @param geometry
	 * Geometry to add
	 */
	public void addGeometry(Object geometry) {
		geometries.add(geometry);
	}
	
	/**
	 * Gets a Geometry at position i
	 * @param i
	 * position
	 */
	public Object getGeometryAt(int i) {
		return geometries.get(i);
	}
}
