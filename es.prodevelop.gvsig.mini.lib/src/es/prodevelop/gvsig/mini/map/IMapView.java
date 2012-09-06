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
 *   author Fernando González fernando.gonzalez@geomati.co
 */

package es.prodevelop.gvsig.mini.map;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.widget.Scroller;
import es.prodevelop.gvsig.mini.common.impl.Tile;
import es.prodevelop.gvsig.mini.control.IMapControl;
import es.prodevelop.gvsig.mini.exceptions.BaseException;
import es.prodevelop.gvsig.mini.geom.Extent;
import es.prodevelop.gvsig.mini.geom.Feature;
import es.prodevelop.gvsig.mini.geom.Point;
import es.prodevelop.gvsig.mini.map.MapView.Scaler;
import es.prodevelop.gvsig.mini.map.MultiTouchController.MultiTouchObjectCanvas;
import es.prodevelop.gvsig.mini.overlay.AcetateOverlay;
import es.prodevelop.gvsig.mini.overlay.MapOverlay;
import es.prodevelop.gvsig.mini.overlay.TileOverlay;
import es.prodevelop.gvsig.mini.overlay.ViewSimpleLocationOverlay;
import es.prodevelop.tilecache.provider.TileProvider;
import es.prodevelop.tilecache.renderer.MapRenderer;

public interface IMapView extends GeoUtils, LayerChangedListener,
		MultiTouchObjectCanvas<Object>, SurfaceHolder.Callback {

	public final static int UPDATE_ZOOM_CONTROLS = 99;
	public final static int ANIMATION_FINISHED_NEED_ZOOM = 100;
	
	public void setBaseLayer(TileOverlay overlay);
	public TileOverlay getBaseLayer();

	/**
	 * Sets the overlay to draw temporary elements on top of the map
	 * 
	 * @param overlay
	 */
	public void setAcetateOverlay(AcetateOverlay overlay);

	/**
	 * Sets the overlay that displays the GPS location
	 * 
	 * @param overlay
	 */
	public void setLocationOverlay(ViewSimpleLocationOverlay overlay);

	/**
	 * An overlay to draw temporary elements on top of the map
	 * 
	 * @return
	 */
	public AcetateOverlay getAcetateOverlay();

	/**
	 * The overlay that displays the GPS location
	 * 
	 * @return
	 */
	public ViewSimpleLocationOverlay getLocationOverlay();

	/**
	 * The layer info of the base layer
	 * 
	 * @return a MapRenderer
	 */
	public MapRenderer getMRendererInfo();

	/**
	 * The bearing of the IMapView canvas in case of the rotation is enabled
	 * 
	 * @return
	 */
	public int getBearing();

	/**
	 * Sets the bearing of the IMapView canvas
	 * 
	 * @param bearing
	 */
	public void setBearing(int bearing);

	/**
	 * Starts the canvas drawing
	 */
	public void resumeDraw();

	/**
	 * True if navigation mode is enabled
	 * 
	 * @return
	 */
	public boolean isNavigationMode();

	/**
	 * Enables or disables the navigation mode
	 * 
	 * @param navigation
	 */
	public void setNavigationMode(boolean navigation);

	public IMapViewController getMapViewController();

	public void setMapViewController(IMapViewController controller);

	public void onLayerChanged(String layerName);

	public void initializeCanvas(int width, int height) throws BaseException;

	public TileProvider getTileProvider();

	public void instantiateTileProviderfromSettings();

	public void centerOnGPSLocation();

	public void persist();

	public void pauseDraw();

	public void setKeepScreenOn(boolean keep);

	public boolean onTrackballEvent(MotionEvent event);

	public void addOverlay(MapOverlay overlay);

	public void clearCache();

	public MapState getMapState();

	public void destroy();

	public void adjustViewToAccuracyIfNavigationMode(float accuracy);

	/**
	 * Sets the current MapRenderer of the view and centers the view on the
	 * center of the max extent of the MapRenderer
	 * 
	 * @param aRenderer
	 *            A MapRenderer
	 */
	public void setRenderer(MapRenderer renderer);

	/**
	 * 
	 * @return The center of the map expressed in SRS:4326
	 */
	public double[] getCenterLonLat();

	/**
	 * 
	 * @return The current zoom level of the view
	 */
	public int getZoomLevel();

	public int getMapWidth();

	public int getMapHeight();

	public void setMapWidth(int mapWidth);

	public void setMapHeight(int mapHeight);

	public ViewPort getViewPort();

	public void setViewPort(ViewPort viewPort);

	public List<MapOverlay> getOverlays();

	public void setTempZoomLevel(int zoomLevel);

	public boolean isScalling();

	public void setIsScalling(boolean isScalling);

	/******************************************************************************/
	public void addControl(IMapControl control);

	public void removeControl(IMapControl control);

	public void clearControls();

	public IMapControl getControlByName(String name);

	public boolean containsOverlay(String name);

	public void removeOverlay(final MapOverlay overlay);

	public void removeOverlay(final String name);

	public MapOverlay getOverlay(String name);

	public void notifyExtentChangedListeners(Extent extent, int zoomLevel,
			double resolution);

	public void onScalingFinished();

	public void computeScale();

	/**
	 * 
	 * @return The final zoom level after the Scale interpolation ends
	 */
	public int getTempZoomLevel();

	public boolean onLongPress(MotionEvent e);

	public void processMultiTouchEvent();

	public void draw(Canvas c);

	/**
	 * Sorts the tiles from the center of the screen to outside to load the
	 * tiles in spiral.
	 * 
	 * @param array2
	 *            the array of tiles
	 */
	public void sortTiles(final Tile[] array2);

	public Handler getInvalidationHandler();

	/**
	 * Calculates a zoom level that fits an extent
	 * 
	 * @param width
	 *            The width of the extent
	 * @param height
	 *            The height of the extent
	 * @param currentZoomLevel
	 *            the current zoom level to get resolution from
	 * @return The zoom level that fits the extent
	 */
	public int getZoomLevelFitsExtent(final double width, final double height,
			final int currentZoomLevel);

	/**
	 * Calculates a zoom level that fits an extent
	 * 
	 * @param width
	 *            The width of the extent
	 * @param height
	 *            The height of the extent
	 * @return The zoom level that fits the extent
	 */
	public int getZoomLevelFitsExtent(final double width, final double height);

	public void invalidate();

	public void postInvalidate();

	public Scaler getScaler();

	/**
	 * Sets the Feature selected by the user when onLongPress and animates the
	 * view to it
	 * 
	 * @param f
	 *            The Feature selected
	 * @deprecated
	 */
	public void setSelectedFeature(Feature f);

	/**
	 * 
	 * @return The TileProvider
	 */
	public TileProvider getMTileProvider();

	public void addExtentChangedListener(ExtentChangedListener listener);

	public void removeExtentChangedListener(ExtentChangedListener listener);

	public void addMapViewListener(MapViewListener listener);

	public void removeMapViewListener(MapViewListener listener);

	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height);

	public void surfaceCreated(SurfaceHolder holder);

	public void surfaceDestroyed(SurfaceHolder holder);

	public void clearBufferCanvas();

	public boolean scaleCanvasForZoom();

	/**
	 * Starts the surfaceThread
	 */
	public void startST();

	/**
	 * Destroys the surfaceThread
	 */
	public void destroyST();

	public Scroller getScroller();

	public void startScrolling();

	public void resetTouchOffset();

	public int getPixelZoomLevel();

	public int getWorldSizePx();

	public void setIsScrolling(boolean isScrolling);

	public List<IMapControl> getControls();

	public Point getScrollingCenter();

	public void setScrollingCenter(double x, double y);

	/**
	 * @deprecated
	 * @return
	 */
	public Feature getSelectedFeature();	

	public void setmTouchMapOffsetX(int touchMapOffsetX);

	public void setmTouchMapOffsetY(int touchMapOffsetY);

	public void setExclusiveControl(IMapControl control);

	public IMapControl getExclusiveControl();
	
	public ArrayList<OnSelectFeatureListener> getSelectFeatureListeners();
	
	public double[] getMyLocationCenterMercator();
	
	public List<MapOverlay> sortOverlays();
	
	public void fireZIndexChanged();

	public void setDirty();

	public void panTo(int touchMapOffsetX, int touchMapOffsetY);	
	
	public boolean isScrolling();
	
	public void forceFinishScroll();
}
