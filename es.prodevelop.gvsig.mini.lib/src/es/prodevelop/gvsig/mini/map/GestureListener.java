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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.view.MotionEvent;
import android.view.View;
import es.prodevelop.gvsig.mini.control.IMapControl;
import es.prodevelop.gvsig.mini.overlay.AcetateOverlay;
import es.prodevelop.gvsig.mini.overlay.MapOverlay;

public class GestureListener implements IGestureListener {

	private final static Logger log = LoggerFactory
			.getLogger(GestureListener.class);

	/*
	 * Coupled with MapView instead of IMapView to keep fireXXX methods out of
	 * the interface
	 */
	private MapView mapView;
	private boolean invalidateLongPress;

	public GestureListener(MapView mapView) {
		this.mapView = mapView;
	}

	public boolean isInvalidatedLongPress() {
		return invalidateLongPress;
	}

	@Override
	public boolean onDown(MotionEvent e) {
		// Notify controls
		for (IMapControl control : mapView.getControls()) {
			control.onDown(e);
		}

		// FIXME Esto es de NOMADA
		// if (TileRaster.this.map.isPOISlideShown())
		// return true;

		if (!mapView.getScroller().isFinished()) {
			mapView.getScroller().forceFinished(true);
			invalidateLongPress = true;
		} else {
			invalidateLongPress = false;
		}

		mapView.setIsScrolling(false);

		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {

		// Notify controls
		for (IMapControl control : mapView.getControls()) {
			control.onFling(e1, e2, velocityX, velocityY);
		}

		// FIXME Esto es de NOMADA. Subclass
		// if (TileRaster.this.map.isPOISlideShown())
		// return true;

		mapView.startScrolling();
		final int worldSize = mapView.getWorldSizePx();
		mapView.getScroller().fling(((View) mapView).getScrollX(),
				((View) mapView).getScrollY(), (int) (-velocityX * 2 / 5),
				(int) (-velocityY * 2 / 5), -worldSize, worldSize, -worldSize,
				worldSize);
		return true;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// Notify controls
		for (IMapControl control : mapView.getControls()) {
			control.onLongPress(e);
		}

		try {
			// if (TileRaster.this.map.isPOISlideShown())
			// return;
			mapView.onLongPress(e);
		} catch (Exception est) {
			log.error("onLongPress", est);
		}
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// Notify controls
		for (IMapControl control : mapView.getControls()) {
			control.onScroll(e1, e2, distanceX, distanceY);
		}

		// System.out.println("onScroll");
		((View) mapView).scrollBy((int) distanceX, (int) distanceY);
		return true;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		for (IMapControl control : mapView.getControls()) {
			control.onShowPress(e);
		}
	}

	@Override
	public boolean onDoubleTap(MotionEvent e) {
		boolean ret = false;
		for (IMapControl control : mapView.getControls()) {
			if (control.onDoubleTap(e)) {
				/*
				 * One control chose to consume the event
				 */
				ret = true;
			}
		}

		return ret;
	}

	@Override
	public boolean onDoubleTapEvent(MotionEvent e) {
		boolean ret = false;
		for (IMapControl control : mapView.getControls()) {
			if (control.onDoubleTapEvent(e)) {
				/*
				 * One control chose to consume the event
				 */
				ret = true;
			}
		}

		return ret;
	}

	@Override
	public boolean onSingleTapConfirmed(MotionEvent e) {
		if (this.isInvalidatedLongPress())
			return false;

		//AcetateOverlay is the most priority touch event receptor.
		AcetateOverlay acetateOverlay = mapView.getAcetateOverlay();
		if (acetateOverlay != null && acetateOverlay.onSingleTapUp(e, mapView))
			return true;

		for (IMapControl control : mapView.getControls())
			try {
				control.onSingleTapUp(e);
			} catch (Exception ignore) {
				log.error("", ignore);
			}

		try {

			for (MapOverlay osmvo : mapView.getOverlays())
				try {
					if (osmvo.onSingleTapUp(e, mapView))
						return true;
				} catch (Exception ignore) {
					log.error("", ignore);
				}
		} catch (Exception ex) {
			log.error("singletapconfirmed", ex);
		}

		return false;
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		boolean ret = false;
//		for (IMapControl control : mapView.getControls()) {
//			if (control.onSingleTapUp(e)) {
//				/*
//				 * One control chose to consume the event
//				 */
//				ret = true;
//			}
//		}

		return ret;
	}
}