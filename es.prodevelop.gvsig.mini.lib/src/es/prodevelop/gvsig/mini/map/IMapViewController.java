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

package es.prodevelop.gvsig.mini.map;

import es.prodevelop.gvsig.mini.geom.Extent;
import es.prodevelop.gvsig.mini.geom.Point;
import es.prodevelop.gvsig.mini.geom.android.GPSPoint;
import es.prodevelop.gvsig.mobile.fmap.proj.CRSFactory;

public interface IMapViewController {	
	
	public IMapView getMapView();

	public void setMapCenterFromLonLat(double lon, double lat);

	/**
	 * Sets the zoom level, updates the zoom controls and postInvalidates the
	 * view
	 * 
	 * @param aZoomLevel
	 *            The zoom level
	 */
	public void setZoomLevel(int aZoomLevel);

	/**
	 * Sets a zoom level that fits a resolution. This method is useful for
	 * zoomRectangle, zooming to a route, to a multipoint, etc.
	 * 
	 * @param resolution
	 *            The resolution in the units of the projection of the
	 *            MapRenderer
	 * @see CRSFactory
	 */
	public void setZoomLevelFromResolution(double resolution);

	/**
	 * Checks if the max extent of the MapRenderer contains the xy, sets the
	 * center and postinvalidates the view. The coordinates x,y need to be in
	 * the same SRS as the MapRenderer
	 * 
	 * @param x
	 *            the x coordinate
	 * @param y
	 *            the y coordinate
	 */
	public void setMapCenter(double x, double y);

	/**
	 * Sets the center of the Map from a GPSPoint and postinvalidates the view
	 * 
	 * @param aCenter
	 *            A GPSPoint
	 */
	public void setMapCenter(final GPSPoint aCenter);

	/**
	 * Increase the zoom of the view and applies the Scaler
	 */
	public void zoomIn();

	/**
	 * Decreases the zoom level of the view applying a Scaler
	 */
	public void zoomOut();

	/**
	 * Sets the zoom level and applies a LinearInterpolation Scaler to the view
	 * 
	 * @param aZoomLevel
	 *            The zoom level
	 * @param several
	 *            If changes several zooms level applies the Scaler
	 */
	public void setZoomLevel(final int aZoomLevel, boolean several);

	/**
	 * Zooms the view to fit an Extent
	 * 
	 * @param extent
	 *            The extent to fit
	 * @param layerChanged
	 *            If true then the Scaler is not applied when zooming
	 * @param currentZoomLevel
	 *            The zoom level to get the resolution from
	 */
	public void zoomToExtent(final Extent extent, boolean layerChanged,
			int currentZoomLevel);

	/**
	 * Zooms the view to fit an Extent
	 * 
	 * @param extent
	 *            The extent to fit
	 * @param layerChanged
	 *            If true then the Scaler is not applied when zooming
	 */
	public void zoomToExtent(final Extent extent, boolean layerChanged);

	/**
	 * Zooms the view to fit an Extent
	 * 
	 * @param extent
	 *            The extent to fit
	 * @param layerChanged
	 *            If true then the Scaler is not applied when zooming
	 * @param forceZoomMore
	 *            If true adds one level to the zoom calculated to fit the
	 *            extent
	 * @param animateTo
	 *            If is not null, animates to this point
	 */
	public void zoomToExtent(final Extent extent, boolean layerChanged,
			boolean forceZoomMore, Point animateToCenter);

	/**
	 * Zooms the view to a width, height
	 * 
	 * @param width
	 *            The width to fit the zoom
	 * @param height
	 *            The height to fit the zoom
	 * @param layerChanged
	 *            If true then the Scaler is not applied when zooming
	 * @param currentZoomLevel
	 *            The zoom level to get the resolution from
	 * @param forceZoomMore
	 *            If true adds one level to the zoom calculated to fit the
	 *            extent
	 * @param animateTo
	 *            If is not null, animates to this point
	 */
	public void zoomToSpan(final double width, final double height,
			boolean layerChanged, int currentZoomLevel, boolean forceZoomMore,
			Point animateToCenter);

	/**
	 * Zooms the view to a width, height
	 * 
	 * @param width
	 *            The width to fit the zoom
	 * @param height
	 *            The height to fit the zoom
	 * @param layerChanged
	 *            If true then the Scaler is not applied when zooming
	 */
	public void zoomToSpan(final double width, final double height,
			boolean layerChanged);

	/**
	 * Applies a LinearAnimation to center the view to a xy coordinates
	 * 
	 * @param x
	 *            The x coordinate
	 * @param y
	 *            The y coordinate
	 */
	public void animateTo(final double x, final double y);

	/**
	 * Applies a LinearAnimation to center the view to a xy coordinates
	 * 
	 * @param x
	 *            The x coordinate
	 * @param y
	 *            The y coordinate
	 * @param zoomChanged
	 *            if true, when the animation ends applies a zoomIn
	 */
	public void animateTo(final double x, final double y,
			final boolean zoomChanged);

	/**
	 * Stops a running animation.
	 * 
	 * @param jumpToTarget
	 */
	public void stopAnimation(final boolean jumpToTarget);
}
