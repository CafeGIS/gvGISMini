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

package es.prodevelop.gvsig.mini.control;

import android.graphics.Canvas;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import es.prodevelop.gvsig.mini.map.IMapView;
import es.prodevelop.gvsig.mini.map.MapView;

public interface IMapControl extends OnGestureListener, OnDoubleTapListener {

	public void setMapView(IMapView mapView);

	public String getName();

	public boolean onTouch(MotionEvent event);

	/**
	 * Called when the control is added to the map view
	 */
	public void activate();

	/**
	 * Called when the control is removed from the map view
	 */
	public void deactivate();

	/**
	 * Draw on the {@link MapView} canvas. It should be used to draw something
	 * in pixel coordinates very fast. In order to draw in map coordinates the
	 * recommended way is to add/remove an overlay on {@link #activate()}/
	 * {@link #deactivate()}
	 * 
	 * @param c
	 */
	public void draw(Canvas c);

}
