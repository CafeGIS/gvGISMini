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

package es.prodevelop.gvsig.mini.tasks.twitter;

import java.util.logging.Level;
import java.util.logging.Logger;

import android.app.Activity;
import android.content.Intent;
import es.prodevelop.gvsig.mini.R;
import es.prodevelop.gvsig.mini.common.CompatManager;
import es.prodevelop.gvsig.mini.exceptions.BaseException;
import es.prodevelop.gvsig.mini.map.IMapActivity;
import es.prodevelop.gvsig.mini.tasks.Functionality;
import es.prodevelop.gvsig.mini.tasks.TaskHandler;

public class ShareMyLocationFunc extends Functionality {

	private final static Logger log = Logger
			.getLogger(ShareMyLocationFunc.class.getName());
	private int res = TaskHandler.FINISHED;

	public ShareMyLocationFunc(IMapActivity map, int id) {
		super(map, id);
		try {
			CompatManager.getInstance().getRegisteredLogHandler()
					.configureLogger(log);
		} catch (BaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Launches an Intent chooser to any application to receive the
	 * Intent.ACTION_SEND with a given text
	 * 
	 * @param text
	 *            The text to share, usually my GPS location
	 */
	public void share(String text) {
		try {
			log.log(Level.FINE, "share");
			final Intent intent = new Intent(Intent.ACTION_SEND);
			intent.setType("text/plain");
			intent.putExtra(Intent.EXTRA_TEXT, text);
			Intent i = Intent.createChooser(intent, getMap().getBaseContext()
					.getString(R.string.share));
			i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);//
			((Activity) getMap()).startActivityForResult(i, 2385);
		} catch (Exception e) {
			log.log(Level.SEVERE, "share", e);
		}
	}

	@Override
	public boolean execute() {
		try {
			share(buildTweet());
			// String user = getMap().twituser;
			// String pass = getMap().twitpass;
			//
			// if (user != null && pass != null) {
			// this.sendMyLocationTweet(user, pass);
			// } else {
			// getMap().getMapHandler()
			// .sendEmptyMessage(Map.SHOW_TWEET_DIALOG);
			// }
		} catch (Exception e) {
			log.log(Level.SEVERE, "", e);
		} finally {
			return true;
		}
	}

	@Override
	public int getMessage() {
		return res;
	}

	/**
	 * Trims the current map center to 6 decimals
	 * 
	 * @return The lon/lat map center with 6 decimals
	 */
	public double[] getLatLonTrimed() {
		try {
			double[] coords = getMap().getMapView().getCenterLonLat();

			int lat = (int) (coords[1] * 1E6);
			int lon = (int) (coords[0] * 1E6);

			double latDouble = lat / 1E6;
			double lonDouble = lon / 1E6;

			return new double[] { lonDouble, latDouble };
		} catch (Exception e) {
			log.log(Level.SEVERE, "", e);
			return null;
		}
	}

	/**
	 * Builds the tweet message: Mi Ubicaci�n :
	 * http://www.opentouchmap.org/?lat=39.472393&lon=-0.382493&zoom=14
	 * lat:39.472393 lon:-0.382493
	 * 
	 * @return A string to tweet
	 */
	public String buildTweet() {
		try {

			final double[] res = this.getLatLonTrimed();

			double latDouble = res[1];
			double lonDouble = res[0];

			int zoomin = getMap().getMapView().getZoomLevel();

			return new StringBuffer()
					.append(((Activity) getMap()).getResources().getString(
							R.string.TweetMyLocationFunc_0))
					.append(" : http://www.opentouchmap.org/?lat=")
					.append(latDouble)
					.append("&lon=")
					.append(lonDouble)
					.append("&zoom=")
					.append(zoomin)
					.append(" ")
					.append(((Activity) getMap()).getResources().getString(
							R.string.lat))
					.append(":")
					.append(latDouble)
					.append(" ")
					.append(((Activity) getMap()).getResources().getString(
							R.string.lon)).append(":").append(lonDouble)
					.toString();

		} catch (Exception e) {
			log.log(Level.SEVERE, "", e);
			return null;
		}
	}

}
