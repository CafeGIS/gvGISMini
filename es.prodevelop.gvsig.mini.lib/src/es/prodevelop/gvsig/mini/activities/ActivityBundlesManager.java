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

package es.prodevelop.gvsig.mini.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import es.prodevelop.geodetic.utils.conversion.ConversionCoords;
import es.prodevelop.gvsig.mini.R;
import es.prodevelop.gvsig.mini.activities.feature.FeatureDetailsActivity;
import es.prodevelop.gvsig.mini.activities.feature.ResultSearchActivity;
import es.prodevelop.gvsig.mini.exceptions.BaseException;
import es.prodevelop.gvsig.mini.geom.impl.base.Feature;
import es.prodevelop.gvsig.mini.geom.impl.jts.JTSFeature;
import es.prodevelop.gvsig.mini.map.IMapView;
import es.prodevelop.gvsig.mini.overlay.MapOverlay;
import es.prodevelop.gvsig.mini.util.Utils;
import es.prodevelop.gvsig.mini.utiles.Calculator;
import es.prodevelop.gvsig.mobile.fmap.proj.CRSFactory;

/**
 * A manager to provide data needed to Activities, such as
 * {@link ResultSearchActivity} or {@link FeatureDetailsActivity}
 * 
 * Instead of packing all the data into {@link Bundle} we use a singleton and
 * register data as needed
 * 
 * @author aromeu
 * 
 */
public class ActivityBundlesManager {

	private static ActivityBundlesManager manager;

	private List features;
	private Feature featureDetail;
	private IntentFiller filler;
	private MapOverlay overlay;

	private HashMap<String, Object> data = new HashMap<String, Object>();
	private IMapView mapView;

	/**
	 * A singleton
	 * 
	 * @return {@link ActivityBundlesManager}
	 */
	public static ActivityBundlesManager getInstance() {
		if (manager == null) {
			manager = new ActivityBundlesManager();
			manager.registerIntentFiller(new ActivityBundlesManager.IntentFiller());
		}

		return manager;
	}

	/**
	 * Default method to register the needed data for an activity
	 * 
	 * @param data
	 *            The data to register
	 * @param activity
	 *            The {@link Activity} that will need the data
	 */
	public void registerData(Object data, Class activity) {
		this.data.put(activity.toString(), data);
	}

	/**
	 * Returns the data previously registered for a given {@link Activity}
	 * 
	 * @param activity
	 *            The {@link Activity}
	 * @return The data registered
	 */
	public Object getData(Class activity) {
		return this.data.get(activity.toString());
	}

	/**
	 * Clears the data registered for an {@link Activity}
	 * 
	 * @param activity
	 *            The {@link Activity}
	 */
	public void removeData(Class activity) {
		this.data.remove(activity.toString());
	}

	/**
	 * Sets a list of features to be provided by the manager
	 * 
	 * @param features
	 *            A list of {@link Feature}
	 * @param overlay
	 *            The {@link MapOverlay} which contains the features
	 */
	public void registerFeatures(List features, MapOverlay overlay) {
		this.features = features;
		this.overlay = overlay;
	}

	public MapOverlay getCurrentOverlay() {
		return this.overlay;
	}

	/**
	 * Sets a single feature to be provided by the manager
	 * 
	 * @param feature
	 *            A single {@link Feature}
	 */
	public void registerFeatureDetail(Feature feature) {
		this.featureDetail = feature;
	}

	/**
	 * Returns all the {@link Feature} registered
	 * 
	 * @return A List of {@link Feature}
	 */
	public List getFeatures() {
		return this.features;
	}

	/**
	 * Returns the {@link Feature} registered previously via
	 * {@link #registerFeatureDetail(Feature)}
	 * 
	 * @return
	 */
	public Feature getFeatureDetail() {
		return this.featureDetail;
	}

	/**
	 * Adds a list of {@link Feature} to the current registered features
	 * 
	 * @param features
	 *            The List of {@link Feature} to register
	 */
	public void addFeatures(List features) {
		if (this.features == null) {
			this.features = new ArrayList();
		}

		this.features.addAll(features);
	}

	/**
	 * Removes a list of {@link Feature} of the current registered features
	 * 
	 * @param features
	 *            The List of {@link Feature} to remove
	 */
	public void removeFeatures(List features) {
		if (this.features == null) {
			return;
		}

		this.features.removeAll(features);
	}

	public void registerIntentFiller(IntentFiller filler) {
		this.filler = filler;
	}

	public IntentFiller getIntentFiller() {
		return this.filler;
	}

	public static class IntentFiller {

		public void fillSearchCenter(Intent i, IMapView mapView) {
			double[] lonlat = mapView.getMyLocationCenterMercator();
			i.putExtra("lon", lonlat[0]);
			i.putExtra("lat", lonlat[1]);
		}

		public Intent fillFeatureDetailIntent(Object detailObject,
				Context context,
				es.prodevelop.gvsig.mini.geom.impl.base.Point centerMercator,
				Intent i) {
			ActivityBundlesManager.getInstance().registerFeatureDetail(
					(Feature) detailObject);

			double[] xy = ConversionCoords.reproject(centerMercator.getX(),
					centerMercator.getY(), CRSFactory.getCRS("EPSG:900913"),
					CRSFactory.getCRS("EPSG:4326"));

			centerMercator.setX(xy[0]);
			centerMercator.setY(xy[1]);
			String dist = "";
			if (xy[0] == 0 && xy[1] == 0)
				dist = context.getResources().getString(
						R.string.location_not_available);
			else {
				com.vividsolutions.jts.geom.Point p;
				try {
					p = (com.vividsolutions.jts.geom.Point) ((JTSFeature) detailObject)
							.getGeometry().getGeometry().getCentroid();
					// Coordinate c = new Coordinate();
					// c.x = xy[0];
					// c.y = xy[1];
					// com.vividsolutions.jts.geom.Point centerLonLat = new
					// com.vividsolutions.jts.geom.Point(c, null, 4326);
					// final double distance = p.distance(centerLonLat);
					final double distance = Calculator.latLonDist(p.getX(),
							p.getY(), xy[0], xy[1]);
					dist = Utils.formatDistance(distance);
				} catch (BaseException e) {
					Log.e("OpenDetailsItemClickListener", "fillIntent", e);
				}

				i.putExtra("DIST", dist);
			}

			return i;
		}
	}

	/**
	 * Registers a IMapView implementation so an Activity can access to it
	 * 
	 * @param mapView
	 *            The IMapView implementation
	 */
	public void registerMapView(IMapView mapView) {
		this.mapView = mapView;
	}

	/**
	 * Returns an IMapView implementation registered
	 * 
	 * @return
	 */
	public IMapView getMapView() {
		return this.mapView;
	}
}
