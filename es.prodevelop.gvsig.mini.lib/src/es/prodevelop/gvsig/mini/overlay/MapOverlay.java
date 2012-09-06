/* gvSIG Mini. A free mobile phone viewer of free maps.
 *
 * Copyright (C) 2009 Prodevelop.
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
 *   2009.
 *   author Rub�n Blanco rblanco@prodevelop.es
 *
 *
 * Original version of the code made by Nicolas Gramlich.
 * No header license code found on the original source file.
 * 
 * Original source code downloaded from http://code.google.com/p/osmdroid/
 * package org.andnav.osm.views.overlay;
 * 
 * License stated in that website is 
 * http://www.gnu.org/licenses/lgpl.html
 * As no specific LGPL version was specified, LGPL license version is LGPL v2.1
 * is assumed.
 *  
 * 
 */

package es.prodevelop.gvsig.mini.overlay;

import android.content.Context;
import android.graphics.Canvas;
import android.view.KeyEvent;
import android.view.MotionEvent;
import es.prodevelop.gvsig.mini.context.Contextable;
import es.prodevelop.gvsig.mini.geom.Extent;
import es.prodevelop.gvsig.mini.geom.Feature;
import es.prodevelop.gvsig.mini.geom.Pixel;
import es.prodevelop.gvsig.mini.map.ExtentChangedListener;
import es.prodevelop.gvsig.mini.map.GeoUtils;
import es.prodevelop.gvsig.mini.map.IMapView;
import es.prodevelop.gvsig.mini.map.LayerChangedListener;

/**
 * A very very simple abstraction of a GIS Layer
 * 
 * @author aromeu
 * @author rblanco
 * 
 */
public abstract class MapOverlay implements GeoUtils, Contextable,
		ExtentChangedListener, LayerChangedListener {

	private IMapView mapView;
	private Context context;

	private String name = "";
	private boolean isVisible = true;
	private String description = "";
	private Extent extent;
	private boolean hidden = false;
	private int zIndex = Integer.MAX_VALUE;

	/**
	 * The constructor
	 * 
	 * @param context
	 * @param mapView
	 */
	public MapOverlay(final Context context, final IMapView mapView, String name) {
		this.context = context;
		this.mapView = mapView;
		this.name = name;
	}

	/**
	 * Called when drawing the IMapView view
	 * 
	 * @param c
	 *            The canvas to draw in
	 * @param maps
	 *            The IMapView instance
	 */
	public void onManagedDraw(final Canvas c, final IMapView maps) {
		if (!isVisible())
			return;
		onDraw(c, maps);
		onDrawFinished(c, maps);
	}

	public int getzIndex() {
		return zIndex;
	}

	/**
	 * Changes the z index of the layer. You should call immediatelly after
	 * calling this method {@link IMapView#fireZIndexChanged()}
	 * 
	 * @param zIndex
	 */
	public void setzIndex(int zIndex) {
		this.zIndex = zIndex;
	}

	public void setMapView(IMapView map) {
		this.mapView = map;
	}

	/**
	 * Draws the MapOVerlay
	 * 
	 * @param c
	 *            The canvas to draw in
	 * @param maps
	 *            The IMapView instance
	 */
	protected abstract void onDraw(final Canvas c, final IMapView maps);

	/**
	 * Actions to do after onDraw
	 * 
	 * @param c
	 *            The canvas to draw in
	 * @param maps
	 *            The IMapView instance
	 */
	protected abstract void onDrawFinished(final Canvas c, final IMapView maps);

	public boolean onKeyDown(final int keyCode, KeyEvent event,
			final IMapView mapView) {
		return false;
	}

	public boolean onKeyUp(final int keyCode, KeyEvent event,
			final IMapView mapView) {
		return false;
	}

	public boolean onTouchEvent(final MotionEvent event, final IMapView mapView) {
		return false;
	}

	public boolean onTrackballEvent(final MotionEvent event,
			final IMapView mapView) {
		return false;
	}

	public boolean onSingleTapUp(MotionEvent e, IMapView osmtile) {
		return onLongPress(e, osmtile);
	}

	/**
	 * 
	 * 
	 * 
	 * @param e
	 * @param osmtile
	 * @return
	 */
	public boolean onLongPress(MotionEvent e, IMapView osmtile) {
		return false;
	}

	/**
	 * Returns the nearest Feature in the MapOverlay to a pixel x-y
	 * 
	 * @param pixel
	 * @return
	 */
	public abstract Feature getNearestFeature(Pixel pixel);

	public Context getContext() {
		return context;
	}

	public IMapView getMapView() {
		return mapView;
	}

	/**
	 * Used to free memory
	 */
	public void destroy() {

	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isVisible() {
		return isVisible;
	}

	public void setVisible(boolean isVisible) {
		this.isVisible = isVisible;
	}

	/**
	 * If the layer is hidden. A hidden layer can be visible (is drawn over the
	 * map) but is not show in the layer tree
	 * 
	 * @return
	 */
	public boolean isHidden() {
		return this.hidden;
	}

	/**
	 * If the layer is hidden. A hidden layer can be visible (is drawn over the
	 * map) but is not show in the layer tree
	 * 
	 * @return
	 */
	public void setHidden(boolean isHidden) {
		this.hidden = isHidden;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Extent getExtent() {
		return extent;
	}

	public void setExtent(Extent extent) {
		this.extent = extent;
	}
}
