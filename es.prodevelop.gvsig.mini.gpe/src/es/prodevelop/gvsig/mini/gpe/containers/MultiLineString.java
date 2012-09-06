package es.prodevelop.gvsig.mini.gpe.containers;

import java.util.ArrayList;

import com.vividsolutions.jts.geom.LineString;

public class MultiLineString extends
es.prodevelop.gvsig.mini.gpe.containers.gvsig.MultiLineString {

	private ArrayList geometries = new ArrayList();

	public void addLineString(LineString lineString) {
		geometries.add(lineString);
	}

	public ArrayList getLineStrings() {
		return geometries;
	}

	public LineString getLineStringAt(int index) {
		return (LineString) geometries.get(index);
	}

}
