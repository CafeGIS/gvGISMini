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

package es.prodevelop.gvsig.mini.symbol;

import android.graphics.Canvas;
import android.graphics.Color;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import es.prodevelop.gvsig.mini.exceptions.BaseException;
import es.prodevelop.gvsig.mini.geom.impl.jts.JTSFeature;
import es.prodevelop.gvsig.mini.map.IMapView;
import es.prodevelop.gvsig.mini.util.ResourceLoader;

public class GeometryCollectionSymbol extends BasePathSymbol {

	private PointSymbolizer pointSymbol;
	private MultiPointSymbol multiPointSymbol;
	private LineSymbol lineSymbol;
	private MultiLineSymbol multiLineSymbol;
	private PolygonSymbol polygonSymbol;
	private MultiPolygonSymbol multiPolygonSymbol;

	public GeometryCollectionSymbol(int color, float pointWidth) {
		super(color, pointWidth);
		if (color == Color.YELLOW) {
			pointSymbol = new PointSymbolizer(
					ResourceLoader.getPointSymbolDrawableYellow(), 5);
		} else {
			pointSymbol = new PointSymbolizer(
					ResourceLoader.getPointSymbolDrawable(), pointWidth);
		}

		multiPointSymbol = new MultiPointSymbol(color, pointWidth);
		lineSymbol = new LineSymbol(color, pointWidth);
		multiLineSymbol = new MultiLineSymbol(color, pointWidth);
		polygonSymbol = new PolygonSymbol(color, pointWidth);
		multiPolygonSymbol = new MultiPolygonSymbol(color, pointWidth);
	}

	public GeometryCollectionSymbol(float pointWidth) {
		super(pointWidth);
		pointSymbol = new PointSymbolizer(
				ResourceLoader.getPointSymbolDrawable(), pointWidth);

		multiPointSymbol = new MultiPointSymbol(pointWidth);
		lineSymbol = new LineSymbol(pointWidth);
		multiLineSymbol = new MultiLineSymbol(pointWidth);
		polygonSymbol = new PolygonSymbol(pointWidth);
		multiPolygonSymbol = new MultiPolygonSymbol(pointWidth);
	}

	@Override
	public void draw(Canvas c, IMapView maps, JTSFeature feature)
			throws BaseException {
		final Geometry geometry = feature.getProjectedGeometry().getGeometry();

		if (geometry instanceof Point) {
			pointSymbol.draw(c, maps, feature);
		} else if (geometry instanceof MultiPoint) {
			multiPointSymbol.draw(c, maps, feature);
		} else if (geometry instanceof LineString) {
			lineSymbol.draw(c, maps, feature);
		} else if (geometry instanceof MultiLineString) {
			multiLineSymbol.draw(c, maps, feature);
		} else if (geometry instanceof Polygon) {
			polygonSymbol.draw(c, maps, feature);
		} else if (geometry instanceof MultiPolygon) {
			multiPolygonSymbol.draw(c, maps, feature);
		}
	}
}
