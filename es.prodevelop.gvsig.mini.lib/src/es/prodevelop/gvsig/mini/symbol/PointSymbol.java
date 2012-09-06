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

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import es.prodevelop.gvsig.mini.exceptions.BaseException;
import es.prodevelop.gvsig.mini.geom.Extent;
import es.prodevelop.gvsig.mini.geom.impl.jts.JTSFeature;
import es.prodevelop.gvsig.mini.map.IMapView;

public class PointSymbol extends BaseSymbol {

	public PointSymbol(int color, float pointWidth) {
		super(color, pointWidth);
	}

	public PointSymbol(float pointWidth) {
		super(pointWidth);
	}

	@Override
	public void draw(Canvas c, IMapView maps, JTSFeature feature)
			throws BaseException {
		final Geometry geometry = feature.getProjectedGeometry().getGeometry();
		drawPoint(c, maps, geometry);

	}

	public void drawPoint(Canvas c, IMapView maps, Geometry geometry) {
		if (geometry instanceof Point) {
			final Extent mapExtent = maps.getMRendererInfo().getExtent();
			final double x = ((Point) geometry).getX();
			final double y = ((Point) geometry).getY();
			final int[] coords = maps.getMRendererInfo().toPixels(
					new double[] { x, y });
			if (mapExtent.contains(x, y)) {
				c.drawPoint(coords[0], coords[1], getPaint());
			}
		}
	}
}
