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
 *   gvSIG Mini has been partially funded by IMPIVA (Instituto de la Peque�a y
 *   Mediana Empresa de la Comunidad Valenciana) &
 *   European Union FEDER funds.
 *   
 *   2011.
 *   author Alberto Romeu aromeu@prodevelop.es
 */

package es.prodevelop.gvsig.mini.activities.indexer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.prodevelop.gvsig.mini.common.impl.PointDistanceQuickSort;
import es.prodevelop.gvsig.mini.exceptions.BaseException;
import es.prodevelop.gvsig.mini.geom.impl.base.Point;
import es.prodevelop.gvsig.mini.geom.impl.jts.JTSFeature;

public class FeatureDistanceQuickSort extends PointDistanceQuickSort {

	private final Logger logger = LoggerFactory
			.getLogger(FeatureDistanceQuickSort.class);

	public FeatureDistanceQuickSort(Point pointToCompareWith) {
		// FIXME Manage different SRS as at the moment is supposed that all the
		// coordinates are EPSG:4326
		super(new es.prodevelop.gvsig.mini.geom.Point(
				pointToCompareWith.getX(), pointToCompareWith.getY()));
	}

	@Override
	public boolean less(Object x, Object y) {
		JTSFeature featureX = (JTSFeature) x;
		JTSFeature featureY = (JTSFeature) y;

		try {
			com.vividsolutions.jts.geom.Point pX = featureX.getGeometry()
					.getGeometry().getCentroid();
			com.vividsolutions.jts.geom.Point pY = featureY.getGeometry()
					.getGeometry().getCentroid();

			return calcDistance(
					new es.prodevelop.gvsig.mini.geom.Point(pX.getX(),
							pX.getY()),
					new es.prodevelop.gvsig.mini.geom.Point(pY.getX(), pY
							.getY()));
		} catch (BaseException e) {
			logger.error("FeatureDistanceQuicksort", "less", e);
			return true;
		}
	}
}