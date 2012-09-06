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
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path.FillType;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;

import es.prodevelop.gvsig.mini.exceptions.BaseException;
import es.prodevelop.gvsig.mini.geom.impl.jts.JTSFeature;
import es.prodevelop.gvsig.mini.map.IMapView;
import es.prodevelop.tilecache.renderer.MapRenderer;

public class PolygonSymbol extends BasePathSymbol {

	private Paint strokePaint = new Paint();

	public PolygonSymbol(int color, float strokeWidth) {
		super(color, strokeWidth);
		strokePaint.setColor(color);
		strokePaint.setStrokeWidth(strokeWidth);
		strokePaint.setStyle(Style.STROKE);
		strokePaint.setAntiAlias(true);
		getPaint().setAlpha(90);
		getPaint().setStyle(Style.FILL);
	}

	public PolygonSymbol(float strokeWidth) {
		super(strokeWidth);
		getPaint().setAlpha(90);
		strokePaint.setStrokeWidth(strokeWidth);
		strokePaint.setStyle(Style.STROKE);
		strokePaint.setAntiAlias(true);
		getPaint().setStyle(Style.FILL);
	}

	@Override
	public void draw(Canvas c, IMapView maps, JTSFeature feature)
			throws BaseException {

		final Geometry geometry = feature.getProjectedGeometry().getGeometry();
		drawPolygon(c, maps, geometry);

	}

	public void drawPolygon(Canvas c, IMapView maps, Geometry geometry)
			throws BaseException {
		resetPath();

		getPath().setFillType(FillType.EVEN_ODD);
		if (geometry instanceof Polygon) {
			Polygon geom = (Polygon) geometry;

			LineString exterior = (LineString) geom.getExteriorRing();

			Coordinate[] coordinates = exterior.getCoordinates();

			final int size = coordinates.length;

			Coordinate coord;
			int[] current;

			final MapRenderer renderer = maps.getMRendererInfo();
			for (int i = 0; i < size; i++) {
				coord = coordinates[i];

				current = renderer.toPixels(new double[] { coord.x, coord.y });

				if (i == 0)
					getPath().moveTo(current[0], current[1]);
				else
					getPath().lineTo(current[0], current[1]);
			}

			getPath().close();

			final int length = geom.getNumInteriorRing();
			LineString hole;
			for (int i = 0; i < length; i++) {
				hole = geom.getInteriorRingN(i);
				coordinates = hole.getCoordinates();

				final int cSize = coordinates.length;

				for (int j = 0; j < cSize; j++) {
					coord = coordinates[j];

					current = renderer
							.toPixels(new double[] { coord.x, coord.y });

					if (j == 0)
						getPath().moveTo(current[0], current[1]);
					else
						getPath().lineTo(current[0], current[1]);
				}

				getPath().close();
			}

			drawPath(c);

			c.drawPath(getPath(), strokePaint);

		}
	}

	/**
	 * Three points are a counter-clockwise turn if ccw > 0, clockwise if ccw <
	 * 0, and collinear if ccw = 0 because ccw is a determinant that gives the
	 * signed area of the triangle formed by p1, p2 and p3.
	 */
	private boolean isCCW(double[] p1, double[] p2, double[] p3) {
		double ccw = (p2[0] - p1[0]) * (p3[1] - p1[1]) - (p2[1] - p1[1])
				* (p3[0] - p1[0]);
		return ccw <= 0;
	}
}
