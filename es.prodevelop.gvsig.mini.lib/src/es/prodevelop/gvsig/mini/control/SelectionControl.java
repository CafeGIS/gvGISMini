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

package es.prodevelop.gvsig.mini.control;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.MotionEvent;
import es.prodevelop.gvsig.mini.geom.impl.jts.JTSFeature;
import es.prodevelop.gvsig.mini.legend.GeometryCollectionLegend;
import es.prodevelop.gvsig.mini.map.IMapView;
import es.prodevelop.gvsig.mini.map.OnSelectFeatureListener;
import es.prodevelop.gvsig.mini.overlay.FeatureOverlay;
import es.prodevelop.gvsig.mini.overlay.MapOverlay;
import es.prodevelop.gvsig.mini.symbol.GeometryCollectionSymbol;

public class SelectionControl extends AbstractMapControl {

	private FeatureOverlay drawSelectedFeaturesOverlay;

	public SelectionControl(Context context, IMapView map, String name) {
		super(name);
		this.drawSelectedFeaturesOverlay = new FeatureOverlay(context, map,
				"hidden overlay");
		this.drawSelectedFeaturesOverlay
				.setLegend(new GeometryCollectionLegend());
		this.drawSelectedFeaturesOverlay
				.setSymbol(new GeometryCollectionSymbol(Color.YELLOW, 5));
		this.drawSelectedFeaturesOverlay.setHidden(true);
		map.addOverlay(this.drawSelectedFeaturesOverlay);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		this.drawSelectedFeaturesOverlay.clearFeatures();

		final List<MapOverlay> overlays = this.getMapView().getOverlays();

		final int[] pixel = new int[] { (int) e.getX(), (int) e.getY() };

		// As we probably have touched a feature, update the position of the
		// PopupView
		getMapView().getAcetateOverlay().setPopupPos(pixel[0], pixel[1]);

		for (MapOverlay overlay : overlays) {
			if (overlay instanceof FeatureOverlay) {
				this.getFeatureFromOverlay((FeatureOverlay) overlay, pixel);
			}
		}

		final List features = this.drawSelectedFeaturesOverlay.getFeatures();
		if (features != null && features.size() > 0) {
			fireOnFeaturesSelected((ArrayList) features);
			return true;
		} else {
			fireOnFeaturesUnselected((ArrayList) features);
			return false;
		}
	}

	public void getFeatureFromOverlay(FeatureOverlay overlay, int[] pixel) {
		JTSFeature feature = ((FeatureOverlay) overlay).getFeature(pixel);
		if (feature != null) {
			this.drawSelectedFeaturesOverlay.addFeature(feature);
		}

		// FIXME Is supposed to have a plain list of layers?

		// FeatureOverlay ov;
		//
		// final int size = ((FeatureOverlay) overlay).getOverlaysCount();
		//
		// for (int i = 0; i < size; i++) {
		// ov = ((FeatureOverlay) overlay).getOverlay(i);
		// this.getFeatureFromOverlay(ov, pixel);
		// }
	}

	public void fireOnFeaturesSelected(ArrayList features) {
		final ArrayList<OnSelectFeatureListener> listeners = getMapView()
				.getSelectFeatureListeners();
		for (OnSelectFeatureListener listener : listeners) {
			listener.onFeaturesSelected(features);
		}
		getMapView().setDirty();
		getMapView().resumeDraw();
	}

	public void fireOnFeaturesUnselected(ArrayList features) {
		final ArrayList<OnSelectFeatureListener> listeners = getMapView()
				.getSelectFeatureListeners();
		for (OnSelectFeatureListener listener : listeners) {
			listener.onFeaturesUnselected(features);
		}
		getMapView().setDirty();
		getMapView().resumeDraw();
	}

}
