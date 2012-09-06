package es.prodevelop.gvsig.mini.gpe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.gvsig.gpe.lib.api.parser.IAttributesIterator;
import org.gvsig.gpe.lib.api.parser.ICoordinateIterator;
import org.gvsig.gpe.lib.api.parser.IGPEContentHandlerInmGeom;
import org.gvsig.gpe.lib.api.parser.IGPEErrorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;

import es.prodevelop.gvsig.mini.exceptions.BaseException;
import es.prodevelop.gvsig.mini.geom.api.IFeature;
import es.prodevelop.gvsig.mini.geom.impl.base.Feature;
import es.prodevelop.gvsig.mini.geom.impl.base.Feature.Attribute;
import es.prodevelop.gvsig.mini.geom.impl.jts.JTSEnvelope;
import es.prodevelop.gvsig.mini.geom.impl.jts.JTSFeature;
import es.prodevelop.gvsig.mini.geom.impl.jts.JTSGeometry;
import es.prodevelop.gvsig.mini.gpe.containers.MultiGeometry;
import es.prodevelop.gvsig.mini.gpe.containers.MultiPoint;
import es.prodevelop.gvsig.mini.gpe.containers.MultiPolygon;
import es.prodevelop.gvsig.mini.gpe.containers.Polygon;
import es.prodevelop.gvsig.mini.map.IMapView;
import es.prodevelop.gvsig.mini.overlay.FeatureOverlay;
import es.prodevelop.gvsig.mini.overlay.MapOverlay;

public class GPEMiniContentHandler implements IGPEContentHandlerInmGeom {

	private final static Logger log = LoggerFactory
			.getLogger(GPEMiniContentHandler.class);

	private FeatureOverlay overlay;
	private Context context;
	private IMapView mapView;
	private IGPEErrorHandler errorHandler;

	public GPEMiniContentHandler(Context context, IMapView mapView,
			IGPEErrorHandler errorHandler, String name) {
		this.context = context;
		this.mapView = mapView;
		this.errorHandler = errorHandler;
		overlay = new FeatureOverlay(context, mapView, name);
	}

	public FeatureOverlay getOverlay() {
		return this.overlay;
	}

	@Override
	public Object startBbox(String id, ICoordinateIterator coords, String srs) {
		try {
			coords.getDimension();
			double[] min = new double[coords.getDimension()];
			double[] max = new double[coords.getDimension()];
			double[] x = new double[coords.getDimension()];
			double[] y = new double[coords.getDimension()];
			// z = new double[coords.getDimension()];
			try {
				if (coords.hasNext()) {
					coords.next(min);
					if (coords.hasNext()) {
						coords.next(max);
					}
				}
			} catch (IOException e) {
				log.error("GPEMiniContentHandler", "startBBox", e);
				e.printStackTrace();
			}
			x[0] = min[0];
			x[1] = max[0];
			y[0] = min[1];
			y[1] = max[1];
			// if (coords.getDimension()==3){
			// z[0]=min[2];
			// z[1]=max[2];
			// }
			// else{
			// z[0]=0.0;
			// z[1]=0.0;
			// }

			JTSEnvelope envelope = new JTSEnvelope();
			envelope.init(x[0], x[1], y[0], y[1]);
			envelope.setId(id);
			envelope.setSrs(srs);

			return envelope;
		} catch (Exception e) {
			log.error("GPEMiniContentHandler", "startBBox", e);
			errorHandler.addError(e);
			return null;
		}
	}

	@Override
	public void endBbox(Object bbox) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object startLayer(String id, String namespace, String name,
			String description, String srs, IAttributesIterator attributes,
			Object parentLayer, Object bBox) {
		try {
			FeatureOverlay layer = new FeatureOverlay(this.context,
					this.mapView, name);
			layer.setId(id);
			layer.setDescription(description);
			layer.setSRS(srs);
			layer.setParentLayer((FeatureOverlay) parentLayer);
			layer.setEnvelope((JTSEnvelope) bBox);

			if (parentLayer != null)
				((FeatureOverlay) parentLayer).addOverlay(layer);

			overlay.addOverlay(layer);
			return layer;
		} catch (Exception e) {
			log.error("GPEMiniContentHandler", "startLayer", e);
			errorHandler.addError(e);
			return null;
		}
	}

	@Override
	public void endLayer(Object layer) {
		try {
			FeatureOverlay overlay = (FeatureOverlay) layer;

			JTSEnvelope extent = null;

			final int size = overlay.getOverlaysCount();
			FeatureOverlay child;
			for (int i = 0; i < size; i++) {
				child = overlay.getOverlay(i);
				if (i == 0) {
					extent = child.getEnvelope();
				} else {
					if (extent != null && child.getEnvelope() != null)
						extent.expandToInclude(child.getEnvelope());
				}
				if (extent != null)
					overlay.setEnvelope(extent);
			}
		} catch (Exception e) {
			log.error("GPEMiniContentHandler", "endLayer", e);
			errorHandler.addError(e);
		}
	}

	@Override
	public void addNameToLayer(String name, Object layer) {
		try {
			((FeatureOverlay) layer).setName(name);
		} catch (Exception e) {
			log.error("GPEMiniContentHandler", "addNameToLayer", e);
			errorHandler.addError(e);
		}
	}

	@Override
	public void addDescriptionToLayer(String description, Object layer) {
		try {
			((FeatureOverlay) layer).setDescription(description);
		} catch (Exception e) {
			log.error("GPEMiniContentHandler", "addDescriptionToLayer", e);
			errorHandler.addError(e);
		}
	}

	@Override
	public void addSrsToLayer(String srs, Object layer) {
		try {
			((FeatureOverlay) layer).setSRS(srs);
		} catch (Exception e) {
			log.error("GPEMiniContentHandler", "addSrsToLayer", e);
			errorHandler.addError(e);
		}
	}

	@Override
	public void addParentLayerToLayer(Object parent, Object layer) {
		try {
			((FeatureOverlay) layer).setParentLayer((FeatureOverlay) parent);
			((FeatureOverlay) parent).addOverlay((FeatureOverlay) layer);
		} catch (Exception e) {
			log.error("GPEMiniContentHandler", "addParentLayerToLayer", e);
			errorHandler.addError(e);
		}
	}

	@Override
	public void addBboxToLayer(Object bbox, Object layer) {
		try {
			if (((FeatureOverlay) layer).isSRSNull()) {
				String srs = ((JTSEnvelope) bbox).getSrs();
				if (srs != null) {
					((FeatureOverlay) layer).setSRS(srs);
				}
			}
			((FeatureOverlay) layer).setEnvelope((JTSEnvelope) bbox);
		} catch (Exception e) {
			log.error("GPEMiniContentHandler", "addBboxToLayer", e);
			errorHandler.addError(e);
		}
	}

	@Override
	public Object startFeature(String id, String namespace, String name,
			IAttributesIterator attributes, Object layer) {
		try {
			JTSFeature feature = new JTSFeature(null);
			feature.addField(IFeature.ID, id, Feature.Attribute.TYPE_STRING);
			feature.addField(IFeature.NAME, name, Feature.Attribute.TYPE_STRING);
			return feature;
		} catch (Exception e) {
			log.error("GPEMiniContentHandler", "startFeature", e);
			errorHandler.addError(e);
			return null;
		}
	}

	@Override
	public void endFeature(Object feature) {

	}

	@Override
	public void addNameToFeature(String name, Object feature) {
		try {
			((JTSFeature) feature).addField("NAME", name,
					Feature.Attribute.TYPE_STRING);
		} catch (Exception e) {
			log.error("GPEMiniContentHandler", "addNameToFeature", e);
			errorHandler.addError(e);
		}
	}

	@Override
	public void addFeatureToLayer(Object feature, Object layer) {
		try {
			if (feature == null)
				return;

			JTSFeature feat = (JTSFeature) feature;
			try {
				Geometry geom = feat.getGeometry().getGeometry();
				if (geom != null && geom.getSRID() > 0) {
					int srid = geom.getSRID();
					feat.setSRS("EPSG:" + srid);
				}
			} catch (Exception e) {

			}

			StringBuffer text = new StringBuffer();

			MapOverlay overlay = (MapOverlay) layer;
			String layerName = overlay.getName();
			if (layerName != null) {
				feat.addField("LAYER_NAME", new Attribute(layerName,
						Attribute.TYPE_STRING));
//				 text.append(layerName).append(" - ");
			}

			if (feat.getAttributes() != null) {
				Iterator it = feat.getAttributes().keySet().iterator();

				Attribute attr;
				String key;
				while (it.hasNext()) {
					key = it.next().toString();
					attr = feat.getAttribute(key);
					if (attr != null && attr.value != null)
						if (key != null && key.toLowerCase().contains("nam")) {
							text.append(attr.value).append(" ");
						}
				}
			}

			feat.setText(text.toString());
			((FeatureOverlay) layer).addFeature(feat);
		} catch (Exception e) {
			log.error("GPEMiniContentHandler", "addFeatureToLayer", e);
			errorHandler.addError(e);
		}
	}

	@Override
	public Object startElement(String namespace, String name, Object value,
			IAttributesIterator attributes, Object parentElement) {
		try {
			if (value == null)
				value = "";
			Feature.Attribute attr = new Feature.Attribute(value.toString(),
					Feature.Attribute.TYPE_UNKNOWN);
			attr.key = name;
			attr.parentAttribute = (Feature.Attribute) parentElement;
			return attr;
		} catch (Exception e) {
			log.error("GPEMiniContentHandler", "startElement", e);
			errorHandler.addError(e);
			return null;
		}
	}

	@Override
	public void endElement(Object element) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addParentElementToElement(Object parent, Object element) {
		try {
			((Feature.Attribute) element).parentAttribute = (Feature.Attribute) parent;
			((Feature.Attribute) parent).attributes.add((Attribute) element);
		} catch (Exception e) {
			log.error("GPEMiniContentHandler", "addParentElementToElement", e);
			errorHandler.addError(e);
		}
	}

	@Override
	public void addElementToFeature(Object element, Object feature) {
		try {
			((JTSFeature) feature).addField((Attribute) element);
		} catch (BaseException e) {
			log.error("GPEMiniContentHandler", "addElementToFeature", e);
			errorHandler.addError(e);
		}
	}

	@Override
	public Object startPoint(String id, ICoordinateIterator coords, String srs) {
		try {
			double[] buffer = new double[coords.getDimension()];
			try {
				if (coords.hasNext()) {
					coords.next(buffer);
				}
			} catch (IOException e) {
				log.error("GPEMiniContentHandler", "startPoint", e);
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			double x = buffer[0];
			double y = buffer[1];

			GeometryFactory factory = new GeometryFactory();
			Point point = factory.createPoint(new Coordinate(x, y));
			setSRID(point, srs);
			return point;
		} catch (Exception e) {
			log.error("GPEMiniContentHandler", "startPoint", e);
			errorHandler.addError(e);
			return null;
		}
	}

	public void setSRID(Geometry geometry, String srs) {
		if (srs == null)
			return;
		int index = srs.indexOf("EPSG:");

		if (index != -1) {
			try {
				int srid = Integer.valueOf(srs.substring(5, srs.length()));
				geometry.setSRID(srid);
			} catch (Exception e) {
				log.error("GPEMiniContentHandler", "setSRID", e);
				this.errorHandler.addError(e);
			}
		}
	}

	@Override
	public void endPoint(Object point) {

	}

	@Override
	public Object startLineString(String id, ICoordinateIterator coords,
			String srs) {
		ArrayList list = new ArrayList();

		double[] buffer;
		try {
			while (coords.hasNext()) {
				buffer = new double[coords.getDimension()];
				coords.next(buffer);
				list.add(buffer);
			}

			final int size = list.size();
			Coordinate[] coordinates = new Coordinate[size];

			double[] c;
			for (int i = 0; i < size; i++) {
				c = (double[]) list.get(i);
				coordinates[i] = new Coordinate();
				coordinates[i].x = c[0];
				coordinates[i].y = c[1];
			}

			GeometryFactory factory = new GeometryFactory();
			com.vividsolutions.jts.geom.LineString line = factory
					.createLineString(coordinates);
			setSRID(line, srs);
			return line;
		} catch (IOException e) {
			log.error("GPEMiniContentHandler", "startLineString", e);
			// TODO Auto-generated catch block
			errorHandler.addError(e);
			return null;
		}
	}

	@Override
	public void endLineString(Object lineString) {

	}

	@Override
	public Object startLinearRing(String id, ICoordinateIterator coords,
			String srs) {
		GeometryFactory factory = new GeometryFactory();
		try {
			double[] buffer;
			ArrayList coordinates = new ArrayList();
			while (coords.hasNext()) {
				buffer = new double[coords.getDimension()];
				coords.next(buffer);
				coordinates.add(buffer);
			}

			final int coordsSize = coordinates.size();
			Coordinate[] coord = new Coordinate[coordsSize];
			for (int i = 0; i < coordsSize; i++) {
				buffer = (double[]) coordinates.get(i);
				coord[i] = new Coordinate();
				coord[i].x = buffer[0];
				coord[i].y = buffer[1];
			}

			LinearRing l = factory.createLinearRing(coord);
			setSRID(l, srs);
			return l;
		} catch (Exception e) {
			log.error("GPEMiniContentHandler", "startLinearRing", e);
			errorHandler.addError(e);
			return null;
		}
	}

	@Override
	public void endLinearRing(Object linearRing) {
		// return linearRing;
	}

	@Override
	public Object startPolygon(String id, ICoordinateIterator coords, String srs) {
		try {
			Polygon polygon = new Polygon();
			GeometryFactory factory = new GeometryFactory();
			try {
				double[] buffer;
				ArrayList coordinates = new ArrayList();
				while (coords.hasNext()) {
					buffer = new double[coords.getDimension()];
					coords.next(buffer);
					coordinates.add(buffer);
				}

				final int coordsSize = coordinates.size();
				Coordinate[] coord = new Coordinate[coordsSize];
				for (int i = 0; i < coordsSize; i++) {
					buffer = (double[]) coordinates.get(i);
					coord[i] = new Coordinate();
					coord[i].x = buffer[0];
					coord[i].y = buffer[1];
				}

				polygon.setLinearRing(factory.createLinearRing(coord));
			} catch (IOException e) {
				log.error("GPEMiniContentHandler", "startPolygon", e);
				e.printStackTrace();
			}

			polygon.setId(id);
			polygon.setSrs(srs);
			return polygon;
		} catch (Exception e) {
			log.error("GPEMiniContentHandler", "startPolygon", e);
			errorHandler.addError(e);
			return null;
		}
	}

	@Override
	public Object endPolygon(Object polygon) {
		try {
			if (polygon == null)
				return null;
			GeometryFactory factory = new GeometryFactory();

			Polygon p = (Polygon) polygon;

			final int holesSize = p.getInnerBoundary().size();
			LinearRing[] holes = new LinearRing[holesSize];

			for (int i = 0; i < holesSize; i++) {
				holes[i] = (LinearRing) ((Polygon) p.getInnerBoundary().get(i))
						.getLinearRing();
			}

			com.vividsolutions.jts.geom.Polygon pol = factory.createPolygon(
					p.getLinearRing(), holes);
			setSRID(pol, ((Polygon) polygon).getSrs());
			return pol;
		} catch (Exception e) {
			log.error("GPEMiniContentHandler", "endPolygon", e);
			errorHandler.addError(e);
			return null;
		}
	}

	@Override
	public void addInnerPolygonToPolygon(Object innerPolygon, Object Polygon) {
		try {
			((Polygon) Polygon).addInnerBoundary(innerPolygon);
		} catch (Exception e) {
			log.error("GPEMiniContentHandler", "addInnerPolygonToPolygon", e);
			errorHandler.addError(e);
		}
	}

	@Override
	public Object startInnerPolygon(String id, ICoordinateIterator coords,
			String srs) {
		return this.startPolygon(id, coords, srs);
	}

	@Override
	public void endInnerPolygon(Object innerPolygon) {
		// return innerPolygon;
	}

	@Override
	public Object startMultiPoint(String id, String srs) {
		try {
			MultiPoint p = new MultiPoint();
			p.setId(id);
			p.setSrs(srs);
			return p;
		} catch (Exception e) {
			log.error("GPEMiniContentHandler", "startMultiPoint", e);
			errorHandler.addError(e);
			return null;
		}
	}

	@Override
	public Object endMultiPoint(Object multiPoint) {
		try {
			GeometryFactory factory = new GeometryFactory();

			final int numPoints = ((MultiPoint) multiPoint).getGeometries()
					.size();
			Point[] points = new Point[numPoints];

			for (int i = 0; i < numPoints; i++) {
				points[i] = ((MultiPoint) multiPoint).getPointAt(i);
			}

			com.vividsolutions.jts.geom.MultiPoint mp = factory
					.createMultiPoint(points);
			setSRID(mp, ((MultiPoint) multiPoint).getSrs());
			return mp;
		} catch (Exception e) {
			log.error("GPEMiniContentHandler", "endMultiPoint", e);
			errorHandler.addError(e);
			return null;
		}
	}

	@Override
	public void addPointToMultiPoint(Object point, Object multiPoint) {
		try {
			((MultiPoint) multiPoint).addPoint((Point) point);
		} catch (Exception e) {
			log.error("GPEMiniContentHandler", "addPointToMultiPoint", e);
			errorHandler.addError(e);
		}
	}

	@Override
	public Object startMultiLineString(String id, String srs) {
		try {
			es.prodevelop.gvsig.mini.gpe.containers.MultiLineString multiLineString = new es.prodevelop.gvsig.mini.gpe.containers.MultiLineString();
			multiLineString.setId(id);
			multiLineString.setSrs(srs);
			return multiLineString;
		} catch (Exception e) {
			log.error("GPEMiniContentHandler", "startMultiLineString", e);
			errorHandler.addError(e);
			return null;
		}
	}

	@Override
	public Object endMultiLineString(Object multiLineString) {
		try {
			GeometryFactory factory = new GeometryFactory();

			ArrayList lines = (ArrayList) ((es.prodevelop.gvsig.mini.gpe.containers.MultiLineString) multiLineString)
					.getLineStrings();

			final int size = lines.size();
			LineString[] lineStrings = new LineString[size];

			for (int i = 0; i < size; i++) {
				lineStrings[i] = (LineString) lines.get(i);
			}

			com.vividsolutions.jts.geom.MultiLineString multiLine = factory
					.createMultiLineString(lineStrings);
			setSRID(multiLine,
					((es.prodevelop.gvsig.mini.gpe.containers.MultiLineString) multiLineString)
							.getSrs());
			multiLineString = null;
			lines = null;
			return multiLine;
		} catch (Exception e) {
			log.error("GPEMiniContentHandler", "endMultiLineString", e);
			errorHandler.addError(e);
			return null;
		}
	}

	@Override
	public void addLineStringToMultiLineString(Object lineString,
			Object multiLineString) {
		try {
			((es.prodevelop.gvsig.mini.gpe.containers.MultiLineString) multiLineString)
					.addLineString((LineString) lineString);
		} catch (Exception e) {
			log.error("GPEMiniContentHandler",
					"addLineStringToMultiLineString", e);
			errorHandler.addError(e);
		}
	}

	@Override
	public Object startMultiPolygon(String id, String srs) {
		try {
			MultiPolygon p = new MultiPolygon();
			p.setId(id);
			p.setSrs(srs);
			return p;
		} catch (Exception e) {
			log.error("GPEMiniContentHandler", "startMultiPolygon", e);
			errorHandler.addError(e);
			return null;
		}
	}

	@Override
	public Object endMultiPolygon(Object multiPolygon) {
		try {
			GeometryFactory factory = new GeometryFactory();

			final int numPolys = ((MultiPolygon) multiPolygon).getGeometries()
					.size();
			com.vividsolutions.jts.geom.Polygon[] polys = new com.vividsolutions.jts.geom.Polygon[numPolys];

			for (int i = 0; i < numPolys; i++) {
				polys[i] = ((MultiPolygon) multiPolygon).getPolygonAt(i);
			}

			com.vividsolutions.jts.geom.MultiPolygon po = factory
					.createMultiPolygon(polys);
			setSRID(po, ((MultiPolygon) multiPolygon).getSrs());
			return po;
		} catch (Exception e) {
			log.error("GPEMiniContentHandler", "endMultiPolygon", e);
			errorHandler.addError(e);
			return null;
		}
	}

	@Override
	public void addPolygonToMultiPolygon(Object polygon, Object multiPolygon) {
		try {
			((MultiPolygon) multiPolygon)
					.addPolygon((com.vividsolutions.jts.geom.Polygon) polygon);
		} catch (Exception e) {
			log.error("GPEMiniContentHandler", "addPolygonToMultiPolygon", e);
			errorHandler.addError(e);
		}
	}

	@Override
	public Object startMultiGeometry(String id, String srs) {
		try {
			MultiGeometry g = new MultiGeometry();
			g.setId(id);
			g.setSrs(srs);
			return g;
		} catch (Exception e) {
			log.error("GPEMiniContentHandler", "startMultiGeometry", e);
			errorHandler.addError(e);
			return null;
		}
	}

	@Override
	public Object endMultiGeometry(Object multiGeometry) {
		try {
			GeometryFactory factory = new GeometryFactory();

			ArrayList geoms = (ArrayList) ((es.prodevelop.gvsig.mini.gpe.containers.MultiGeometry) multiGeometry)
					.getGeometries();

			final int size = geoms.size();
			Geometry[] geometries = new Geometry[size];

			for (int i = 0; i < size; i++) {
				geometries[i] = (Geometry) geoms.get(i);
			}

			com.vividsolutions.jts.geom.GeometryCollection multiGeom = factory
					.createGeometryCollection(geometries);

			setSRID(multiGeom,
					((es.prodevelop.gvsig.mini.gpe.containers.MultiGeometry) multiGeometry)
							.getSrs());

			return multiGeom;
		} catch (Exception e) {
			log.error("GPEMiniContentHandler", "endMultiGeometry", e);
			errorHandler.addError(e);
			return null;
		}
	}

	@Override
	public void addGeometryToMultiGeometry(Object geometry, Object multiGeometry) {
		try {
			((MultiGeometry) multiGeometry).addGeometry(geometry);
		} catch (Exception e) {
			log.error("GPEMiniContentHandler", "addGeometryToMultigeometry", e);
			errorHandler.addError(e);
		}
	}

	@Override
	public void addBboxToFeature(Object bbox, Object feature) {
		try {
			if (((JTSFeature) feature).getSRS() == null) {
				String srs = ((JTSEnvelope) bbox).getSrs();
				if (srs != null) {
					((JTSFeature) feature).setSRS(srs);
				}
			}
			((JTSFeature) feature).setEnvelope((JTSEnvelope) bbox);
		} catch (Exception e) {
			log.error("GPEMiniContentHandler", "addBboxToFeature", e);
			errorHandler.addError(e);
		}
	}

	@Override
	public void addGeometryToFeature(Object geometry, Object feature) {
		try {
			if (geometry  != null && feature != null) {
				((JTSFeature) feature).setGeometry(new JTSGeometry(
						(Geometry) geometry));	
			}
		} catch (Exception e) {
			log.error("GPEMiniContentHandler", "addGeometryToFeature", e);
			errorHandler.addError(e);
		}
	}

	@Override
	public Object startMetadata(String type, String data,
			IAttributesIterator attributes) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addMetadataToFeature(Object metadata, Object feature) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addMetadataToMetadata(Object metadata, Object parent) {
		// TODO Auto-generated method stub

	}

	@Override
	public void endMetadata(Object metadata) {
		// TODO Auto-generated method stub

	}

	@Override
	public void endTime(Object time) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object startTime(String name, String description, String type,
			String time, Object previous, Object next) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object startTime(String type, String time) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addTimeToFeature(Object time, Object feature) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object startCurve(String id, ICoordinateIterator coords, String srs) {
		return this.startLineString(id, coords, srs);
	}

	@Override
	public Object startCurve(String id, String srs) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void endCurve(Object Curve) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addSegmentToCurve(Object segment, Object curve) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object startMultiCurve(String id, String srs) {
		return null;
	}

	@Override
	public void endMultiCurve(Object multiCurve) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addCurveToMultiCurve(Object curve, Object multiCurve) {

	}
}
