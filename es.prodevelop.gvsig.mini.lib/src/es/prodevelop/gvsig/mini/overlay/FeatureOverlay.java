/* gvSIG Mini. A free mobile phone viewer of free maps.
 *
 * Copyright (C) 2011 Prodevelop.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,USA.
 *
 * For more information, contact:
 *
 *   Prodevelop, S.L.
 *   Pza. Don Juan de Villarrasa, 14 - 5
 *   46001 Valencia
 *   Spain
 *
 *   +34 963 510 612
 *   +34 963 510 968
 *   prode@prodevelop.es
 *   http://www.prodevelop.es
 *
 *   gvSIG Mini has been partially funded by IMPIVA (Instituto de la PequeÔøΩa y
 *   Mediana Empresa de la Comunidad Valenciana) &
 *   European Union FEDER funds.
 *   
 *   2011.
 *   author Alberto Romeu aromeu@prodevelop.es
 */

package es.prodevelop.gvsig.mini.overlay;

import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Log;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import es.prodevelop.geodetic.utils.conversion.ConversionCoords;
import es.prodevelop.gvsig.mini.context.ItemContext;
import es.prodevelop.gvsig.mini.exceptions.BaseException;
import es.prodevelop.gvsig.mini.geom.Extent;
import es.prodevelop.gvsig.mini.geom.Feature;
import es.prodevelop.gvsig.mini.geom.Pixel;
import es.prodevelop.gvsig.mini.geom.impl.jts.JTSEnvelope;
import es.prodevelop.gvsig.mini.geom.impl.jts.JTSFeature;
import es.prodevelop.gvsig.mini.legend.GeometryCollectionLegend;
import es.prodevelop.gvsig.mini.legend.ILegend;
import es.prodevelop.gvsig.mini.legend.LineLegend;
import es.prodevelop.gvsig.mini.legend.MultiLineLegend;
import es.prodevelop.gvsig.mini.legend.MultiPointLegend;
import es.prodevelop.gvsig.mini.legend.MultiPolygonLegend;
import es.prodevelop.gvsig.mini.legend.PointLegend;
import es.prodevelop.gvsig.mini.legend.PointsAsLinesLegend;
import es.prodevelop.gvsig.mini.legend.PolygonLegend;
import es.prodevelop.gvsig.mini.map.IMapView;
import es.prodevelop.gvsig.mini.symbol.GeometryCollectionSymbol;
import es.prodevelop.gvsig.mini.symbol.LineSymbol;
import es.prodevelop.gvsig.mini.symbol.MultiLineSymbol;
import es.prodevelop.gvsig.mini.symbol.MultiPointSymbol;
import es.prodevelop.gvsig.mini.symbol.MultiPolygonSymbol;
import es.prodevelop.gvsig.mini.symbol.PointSymbol;
import es.prodevelop.gvsig.mini.symbol.PointSymbolizer;
import es.prodevelop.gvsig.mini.symbol.PointsAsLinesSymbol;
import es.prodevelop.gvsig.mini.symbol.PolygonSymbol;
import es.prodevelop.gvsig.mini.symbol.Symbol;
import es.prodevelop.gvsig.mini.symbol.SymbolComposite;
import es.prodevelop.gvsig.mini.util.ResourceLoader;
import es.prodevelop.gvsig.mobile.fmap.proj.CRSFactory;

public class FeatureOverlay extends MapOverlay {

	private List<JTSFeature> features = new ArrayList<JTSFeature>();
	private Symbol symbol;
	private ILegend legend;
	private List<FeatureOverlay> overlays = new ArrayList<FeatureOverlay>();
	private FeatureOverlay parentLayer = null;
	private String SRS = null;
	private String id = null;
	private String description = null;
	private JTSEnvelope envelope = null;

	private int numPoints = 0;
	private int numMultiPoints = 0;
	private int numLines = 0;
	private int numMultiLines = 0;
	private int numPol = 0;
	private int numMultiPol = 0;
	private int numGeomCol = 0;

	public final static int TYPE_POINT = 1;
	public final static int TYPE_MULTIPOINT = 2;
	public final static int TYPE_LINE = 3;
	public final static int TYPE_MULTILINE = 4;
	public final static int TYPE_POLYGON = 5;
	public final static int TYPE_MULTIPOLYGON = 6;
	public final static int TYPE_GEOMETRYCOLLECTION = 7;

	private int type = 0;
	private String originalFile = "";

	public String getOriginalFileName() {
		return originalFile;
	}

	public void setOriginalFileName(String fileName) {
		this.originalFile = fileName;
	}

	public int getGeomType() {
		return type;
	}

	public void setGeomType(int type) {
		this.type = type;

		Random rand = new Random();
		int r = rand.nextInt();
		int g = rand.nextInt();
		int b = rand.nextInt();

		switch (type) {
		case 1:
			if (this.originalFile.toLowerCase().endsWith("gpx")) {
				legend = new PointsAsLinesLegend();
				Symbol lines = new PointsAsLinesSymbol(Color.rgb(r, g, b), 3);
				symbol = lines;
				Symbol points = new PointSymbolizer(
						ResourceLoader.getPointSymbolDrawable(), 5);
				symbol = new SymbolComposite(new Symbol[] { lines, points });
			} else {
				legend = new PointLegend();
				symbol = new PointSymbolizer(
						ResourceLoader.getPointSymbolDrawable(), 5);
				// symbol = new PointSymbol(Color.rgb(r, g, b), 5);
			}
			break;
		case 2:
			legend = new MultiPointLegend();
			symbol = new MultiPointSymbol(Color.rgb(r, g, b), 5);
			break;
		case 3:
			legend = new LineLegend();
			symbol = new LineSymbol(Color.rgb(r, g, b), 5);
			break;
		case 4:
			legend = new MultiLineLegend();
			symbol = new MultiLineSymbol(Color.rgb(r, g, b), 5);
			break;
		case 5:
			legend = new PolygonLegend();
			symbol = new PolygonSymbol(Color.rgb(r, g, b), 3);
			break;
		case 6:
			legend = new MultiPolygonLegend();
			symbol = new MultiPolygonSymbol(Color.rgb(r, g, b), 3);
			break;
		case 7:
			legend = new GeometryCollectionLegend();
			symbol = new GeometryCollectionSymbol(Color.rgb(r, g, b), 5);
			break;
		default:
			break;
		}
	}

	public FeatureOverlay(Context context, IMapView mapView, String name) {
		super(context, mapView, name);
		symbol = new PointSymbol(10);
		legend = new PointLegend();
		// TODO Auto-generated constructor stub
	}

	@Override
	public ItemContext getItemContext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onExtentChanged(Extent newExtent, int zoomLevel,
			double resolution) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onLayerChanged(String layerName) {
		for (JTSFeature feature : this.features) {
			try {
				feature.reprojectGeometry(this.getMapView().getMRendererInfo()
						.getSRS());
			} catch (BaseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void onDraw(Canvas c, IMapView maps) {
		if (isVisible()) {
			if (this.features.size() > 0) {
				this.draw(this, c, maps);
			}

			// FIXME We have a plain list of layers?
			// for (FeatureOverlay overlay : this.overlays) {
			// draw(overlay, c, maps);
			// }
		}
	}

	public void draw(FeatureOverlay overlay, Canvas c, IMapView maps) {
		final Iterator<JTSFeature> featuresIterator = overlay.iteratorFeature();

		legend.draw(c, maps, featuresIterator, symbol);
	}

	public void removeOverlay(MapOverlay overlay) {
		this.overlays.remove(overlay);
	}

	@Override
	protected void onDrawFinished(Canvas c, IMapView maps) {
		// TODO Auto-generated method stub

	}

	@Override
	public Feature getNearestFeature(Pixel pixel) {
		// TODO Auto-generated method stub
		return null;
	}

	// FIXME allow returning more than a Feature
	public JTSFeature getFeature(int[] pixel) {

		if (isHidden() || !isVisible())
			return null;

		double distance = Double.MAX_VALUE;
		Geometry nearest = null;
		int nearestIndex = -1;

		double[] coords = this.getMapView().getMRendererInfo()
				.fromPixels(pixel);

		Coordinate p = new Coordinate();
		p.x = coords[0];
		p.y = coords[1];

		GeometryFactory f = new GeometryFactory();
		Geometry point = f.createPoint(p);

		// encontrar la geometr�a a menor distancia y al mismo tiempo
		// comprobar
		// si alguna contiene al punto
		Geometry geom;
		double tempDistance;
		int i = 0;
		try {
			for (JTSFeature feature : this.features) {
				try {
					geom = feature.getProjectedGeometry().getGeometry();
					if (geom.contains(point)) {
						return feature;
					}

					tempDistance = geom.distance(point);

					if (tempDistance < distance) {
						distance = tempDistance;
						nearest = geom;
						nearestIndex = i;
					}
					i++;
				} catch (BaseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (ConcurrentModificationException ignore) {

		}

		// at 50 pixels of distance
		double minDistance = this.getMapView().getMRendererInfo().resolutions[this
				.getMapView().getZoomLevel()] * 50;

		if (nearest != null) {
			if (nearest.distance(point) <= minDistance) {
				return this.getFeature(nearestIndex);
			}
		}

		return null;

	}

	public void processGeometryType(JTSFeature feature, boolean add) {
		Geometry geom = null;

		try {
			if (feature.getGeometry() == null)
				return;
			geom = feature.getGeometry().getGeometry();
		} catch (BaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (geom != null) {
			if (geom instanceof Point) {
				if (add)
					this.numPoints++;
				else
					this.numPoints--;
			} else if (geom instanceof LineString) {
				if (add)
					this.numLines++;
				else
					this.numLines--;
			} else if (geom instanceof MultiPoint) {
				if (add)
					this.numMultiPoints++;
				else
					this.numMultiPoints--;
			} else if (geom instanceof MultiLineString) {
				if (add)
					this.numMultiLines++;
				else
					this.numMultiLines--;
			} else if (geom instanceof Polygon) {
				if (add)
					this.numPol++;
				else
					this.numPol--;
			} else if (geom instanceof MultiPolygon) {
				if (add)
					this.numMultiPol++;
				else
					this.numMultiPol--;
			} else if (geom instanceof GeometryCollection) {
				if (add)
					this.numGeomCol++;
				else
					this.numGeomCol--;
			}
		}
	}

	public boolean addFeature(JTSFeature object) {
		this.processGeometryType(object, true);
		if (object.getSRS() == null) {
			object.setSRS(this.getSRS());
		}
		try {
			object.reprojectGeometry(getMapView().getMRendererInfo().getSRS());
		} catch (BaseException e) {
			Log.e("", "addFeature", e);
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return features.add(object);
	}

	public void clearFeatures() {
		features.clear();
	}

	public boolean containsFeature(JTSFeature feature) {
		return features.contains(feature);
	}

	public JTSFeature getFeature(int location) {
		return features.get(location);
	}

	public boolean isEmpty() {
		return features.isEmpty();
	}

	public Iterator<JTSFeature> iteratorFeature() {
		return features.iterator();
	}

	public boolean removeFeature(JTSFeature object) {
		this.processGeometryType(object, false);
		return features.remove(object);
	}

	public boolean removeAllFeatures(Collection<JTSFeature> arg0) {
		this.initializeCounters();
		return features.removeAll(arg0);
	}

	public int sizeFeatures() {
		return features.size();
	}

	public JTSFeature[] toArray() {
		return (JTSFeature[]) features.toArray();
	}

	public void addOverlay(FeatureOverlay overlay) {
		this.overlays.add(overlay);
	}

	public int getOverlaysCount() {
		return this.overlays.size();
	}

	public FeatureOverlay getOverlay(int i) {
		return this.overlays.get(i);
	}

	public FeatureOverlay getParentLayer() {
		return parentLayer;
	}

	public void setParentLayer(FeatureOverlay parentLayer) {
		this.parentLayer = parentLayer;
	}

	public boolean isSRSNull() {
		return SRS == null;
	}

	public String getSRS() {
		if (SRS == null) {
			return "EPSG:4326";
		}
		return SRS;
	}

	public void setSRS(String sRS) {
		if (sRS == null) {
			sRS = "EPSG:4326";
			return;
		}
		SRS = sRS;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public JTSEnvelope getEnvelope() {
		return envelope;
	}

	public void setEnvelope(JTSEnvelope envelope) {
		this.envelope = envelope;
	}

	public Symbol getSymbol() {
		return symbol;
	}

	public void setSymbol(Symbol symbol) {
		this.symbol = symbol;
	}

	public ILegend getLegend() {
		return legend;
	}

	public void setLegend(ILegend legend) {
		this.legend = legend;
	}

	public void initializeCounters() {
		this.numGeomCol = 0;
		this.numLines = 0;
		this.numMultiLines = 0;
		this.numMultiPoints = 0;
		this.numMultiPol = 0;
		this.numPoints = 0;
		this.numPol = 0;
	}

	public int getNumPoints() {
		return numPoints;
	}

	public int getNumMultiPoints() {
		return numMultiPoints;
	}

	public int getNumLines() {
		return numLines;
	}

	public int getNumMultiLines() {
		return numMultiLines;
	}

	public int getNumPol() {
		return numPol;
	}

	public int getNumMultiPol() {
		return numMultiPol;
	}

	public int getNumGeomCol() {
		return numGeomCol;
	}

	public List<JTSFeature> getFeatures() {
		return this.features;
	}

	public Extent getExtent() {
		Extent extent = super.getExtent();
		if (extent != null) {
			return extent;
		}
		if (this.envelope != null) {
			String srs = this.envelope.getSrs();
			String originSRS = this.getMapView().getBaseLayer().getRenderer()
					.getSRS();
			double[] minXY = ConversionCoords.reproject(
					this.envelope.getMinX(), this.envelope.getMinY(),
					CRSFactory.getCRS(srs), CRSFactory.getCRS(originSRS));
			double[] maxXY = ConversionCoords.reproject(
					this.envelope.getMaxX(), this.envelope.getMaxY(),
					CRSFactory.getCRS(srs), CRSFactory.getCRS(originSRS));
			this.setExtent(new Extent(minXY[0], minXY[1], maxXY[0], maxXY[1]));
			return super.getExtent();
		} else {
			double minX = Double.MAX_VALUE;
			double minY = Double.MAX_VALUE;
			double maxX = Double.MIN_VALUE;
			double maxY = Double.MIN_VALUE;

			JTSEnvelope envelope;
			String srs = this.getSRS();
			for (JTSFeature feature : this.features) {
				envelope = feature.getEnvelope();
				if (envelope.getMaxX() > maxX) {
					maxX = envelope.getMaxX();
				}
				if (envelope.getMaxY() > maxY) {
					maxY = envelope.getMaxY();
				}
				if (envelope.getMinX() < minX) {
					minX = envelope.getMinX();
				}
				if (envelope.getMinY() < minY) {
					minY = envelope.getMinY();
				}
			}

			if (minX != Double.MAX_VALUE && minY != Double.MAX_VALUE
					&& maxX != Double.MIN_VALUE && maxY != Double.MIN_VALUE) {
				String originSRS = this.getMapView().getBaseLayer()
						.getRenderer().getSRS();
				double[] minXY = ConversionCoords.reproject(minX, minY,
						CRSFactory.getCRS(srs), CRSFactory.getCRS(originSRS));
				double[] maxXY = ConversionCoords.reproject(maxX, maxY,
						CRSFactory.getCRS(srs), CRSFactory.getCRS(originSRS));
				this.setExtent(new Extent(minXY[0], minXY[1], maxXY[0],
						maxXY[1]));
				return super.getExtent();
			}
		}
		return null;
	}
}
