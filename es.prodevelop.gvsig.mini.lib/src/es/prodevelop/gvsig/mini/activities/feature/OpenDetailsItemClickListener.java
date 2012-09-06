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

package es.prodevelop.gvsig.mini.activities.feature;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import es.prodevelop.gvsig.mini.activities.ActivityBundlesManager;
import es.prodevelop.gvsig.mini.geom.impl.base.Point;

public class OpenDetailsItemClickListener implements OnItemClickListener {

	private Context activity;
	private Point centerMercator;
	public Class cl;

	public OpenDetailsItemClickListener(Context activity,
			double[] centerMercator) {
		this.activity = activity;
		this.setCenter(centerMercator);
	}

	public OpenDetailsItemClickListener(Context activity, Point centerMercator) {
		this.activity = activity;
		this.setCenter(centerMercator);
	}

	public void setCenter(double[] centerMercator) {
		if (this.centerMercator == null) {
			this.centerMercator = new Point(centerMercator[0],
					centerMercator[1]);
		} else {
			this.centerMercator.setX(centerMercator[0]);
			this.centerMercator.setY(centerMercator[1]);
		}
	}

	public void setCenter(Point centerMercator) {
		this.centerMercator = centerMercator;
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

		Object p = arg0.getAdapter().getItem(arg2);

		launchDetailActivity(p);
	}

	public void launchDetailActivity(Object detailObject) {
		Class cl = getDetailsActivityClass();
		if (this.cl != null)
			cl = this.cl;
		Intent i = new Intent(activity, cl);

		i = this.fillIntent(detailObject, activity, centerMercator, i);

		activity.startActivity(i);
	}

	public Intent fillIntent(Object detailObject, Context activity,
			Point centerMercator, Intent i) {
		return ActivityBundlesManager
				.getInstance()
				.getIntentFiller()
				.fillFeatureDetailIntent(detailObject, activity,
						centerMercator, i);
	}

	public Class getDetailsActivityClass() {
		return FeatureDetailsActivity.class;
	}
}
