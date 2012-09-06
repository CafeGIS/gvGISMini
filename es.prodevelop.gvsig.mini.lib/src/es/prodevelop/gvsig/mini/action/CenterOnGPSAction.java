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

package es.prodevelop.gvsig.mini.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.view.View;

import com.markupartist.android.widget.ActionBar.AbstractAction;

import es.prodevelop.gvsig.mini.R;
import es.prodevelop.gvsig.mini.map.IMapActivity;

public class CenterOnGPSAction extends AbstractAction {

	private Logger log = LoggerFactory.getLogger(CenterOnGPSAction.class);

	private IMapActivity mapActivity;

	public CenterOnGPSAction(IMapActivity mapActivity) {
		super(R.drawable.gd_action_bar_locate_myself);
		this.mapActivity = mapActivity;
	}

	@Override
	public void performAction(View view) {
		try {
			mapActivity
					.getMapView()
					.adjustViewToAccuracyIfNavigationMode(
							mapActivity.getMapView().getLocationOverlay().mLocation.acc);
			mapActivity
					.getMapView()
					.getMapViewController()
					.setMapCenterFromLonLat(
							mapActivity.getMapView().getLocationOverlay().mLocation
									.getLongitudeE6() / 1E6,
							mapActivity.getMapView().getLocationOverlay().mLocation
									.getLatitudeE6() / 1E6);
			mapActivity.getMapView().getMapViewController().setZoomLevel(15);
		} catch (Exception e) {
			log.error("My location: ", e);
		}
	}
}
