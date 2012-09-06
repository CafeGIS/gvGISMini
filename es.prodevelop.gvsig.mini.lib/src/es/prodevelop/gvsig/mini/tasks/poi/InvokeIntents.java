/* gvSIG Mini. A free mobile phone viewer of free maps.
 *
 * Copyright (C) 2010 Prodevelop.
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
 *   2010.
 *   author Alberto Romeu aromeu@prodevelop.es
 *   
 */

package es.prodevelop.gvsig.mini.tasks.poi;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import es.prodevelop.geodetic.utils.conversion.ConversionCoords;
import es.prodevelop.gvsig.mini.geom.Point;
import es.prodevelop.gvsig.mini.yours.RouteManager;
import es.prodevelop.gvsig.mobile.fmap.proj.CRSFactory;

public class InvokeIntents {

	public static void invokeCallActivity(Context context, String phone) {
		try {
			Intent intent = new Intent(Intent.ACTION_CALL);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setData(Uri.parse("tel:" + phone));
			context.startActivity(intent);
		} catch (Exception e) {
			Log.e("", "Failed to invoke call", e);
		}
	}

	public static void invokeURI(Context context, String uri) {
		Intent intent = new Intent();
		intent.setData(Uri.parse(uri));
		intent.setAction("android.intent.action.VIEW");
		context.startActivity(intent);
	}

	public static void openBrowser(Context context, String url) {
		if (url == null)
			return;
		if (!url.startsWith("http://") && !url.startsWith("https://"))
			url = "http://" + url;
		Intent browserIntent = new Intent("android.intent.action.VIEW",
				Uri.parse(url));
		context.startActivity(browserIntent);
	}

	public static void routeModified(Context context, Class mapClass) {
		Intent i = new Intent(context, mapClass);
		i.putExtra(RouteManager.ROUTE_MODIFIED, true);
		context.startActivity(i);
	}

	private static void fillCenterMercator(Intent intent, String point) {
		Point centerLatLon = Point.parseString(point);
		double[] centerMercator = ConversionCoords.reproject(
				centerLatLon.getX(), centerLatLon.getY(),
				CRSFactory.getCRS("EPSG:4326"),
				CRSFactory.getCRS("EPSG:900913"));

		intent.putExtra("lon", centerMercator[0]);
		intent.putExtra("lat", centerMercator[1]);
	}

}
