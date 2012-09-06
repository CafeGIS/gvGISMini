package es.prodevelop.gvsig.mini.app.examples;

import android.os.Bundle;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import es.prodevelop.gvsig.mini.app.GPEMap;
import es.prodevelop.gvsig.mini.geom.api.IFeature;
import es.prodevelop.gvsig.mini.geom.impl.base.Feature;
import es.prodevelop.gvsig.mini.geom.impl.jts.JTSFeature;
import es.prodevelop.gvsig.mini.geom.impl.jts.JTSGeometry;
import es.prodevelop.gvsig.mini.legend.PointLegend;
import es.prodevelop.gvsig.mini.overlay.FeatureOverlay;
import es.prodevelop.gvsig.mini.symbol.PointSymbolizer;
import es.prodevelop.gvsig.mini.util.ResourceLoader;

public class MemoryVectorLayerMap extends ExampleMap {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//Create a new feature layer
		FeatureOverlay overlay = new FeatureOverlay(this, this.getMapView(),
				"Sample vector layer");
		
		//Set the legend and symbol. This will be a point layer
		overlay.setLegend(new PointLegend());
		overlay.setSymbol(new PointSymbolizer(ResourceLoader.getPointSymbolDrawable(), 5));
		
		//The SRS
		overlay.setSRS("EPSG:4326");
		
		//A feature layer has JTSFeature with attributes and the geometry
		JTSFeature feature = new JTSFeature(null);
		feature.addField(IFeature.ID, "0", Feature.Attribute.TYPE_STRING);
		feature.addField(IFeature.NAME, "Punto de prueba", Feature.Attribute.TYPE_STRING);
		feature.addField("Descripcion", "Esto es un punto de prueba", Feature.Attribute.TYPE_STRING);
		
		//Set the point to the feature
		feature.setGeometry(new JTSGeometry(instantiatePoint(0, 39)));
		
		
		//add the feature to the layer. We can add several
		overlay.addFeature(feature);
		
		
		//finally add the layer to the map
		this.getMapView().addOverlay(overlay);
	}
	
	public Point instantiatePoint(double x, double y) {
		GeometryFactory factory = new GeometryFactory();
		Point p = factory.createPoint(new Coordinate(x, y));
		p.setSRID(4326);
		return p;
	}
}
