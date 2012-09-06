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

import org.anddev.android.weatherforecast.weather.WeatherSet;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import es.prodevelop.gvsig.mini.activities.OnSettingsChangedListener;
import es.prodevelop.gvsig.mini.context.ItemContext;
import es.prodevelop.gvsig.mini.exceptions.BaseException;
import es.prodevelop.gvsig.mini.map.GeoUtils;

/**
 * Interface methods that must be supported by any Activity containing an
 * IMapView.
 * 
 * @author aromeu
 * 
 */
public interface IMapActivity extends IMapLocation, IMapDownloader,
		OnSettingsChangedListener, GeoUtils {

	public static final int ROUTE_CANCELED = 100;
	public static final int ROUTE_INITED = 101;
	public static final int WEATHER_INITED = 102;
	public static final int TWEET_SENT = 103;
	public static final int SHOW_TWEET_DIALOG = 104;
	public static final int POI_INITED = 105;
	public static final int SHOW_POI_DIALOG = 106;
	public static final int SHOW_ADDRESS_DIALOG = 107;
	public static final int ROUTE_CLEARED = 108;
	public static final int POI_LIST = 109;
	public static final int VOID = 110;
	public static final int POI_CLEARED = 111;
	public static final int TWEET_ERROR = 112;
	public static final int SHOW_TOAST = 113;
	public static final int SHOW_OK_DIALOG = 114;
	public static final int GETFEATURE_INITED = 115;
	public static final int SHOW_TWEET_DIALOG_SETTINGS = 116;
	public static final int SHOW_LOADING = 117;
	public static final int HIDE_LOADING = 118;
	public static final int SHOW_INDETERMINATE_DIALOG = 119;
	public static final int HIDE_INDETERMINATE_DIALOG = 120;
	public static final int POI_CANCELED = 1;
	public static final int POI_SUCCEEDED = 2;
	public static final int ROUTE_SUCCEEDED = 3;
	public static final int ROUTE_NO_RESPONSE = 4;
	public static final int ROUTE_NO_CALCULATED = 5;
	public static final int POI_SHOW = 6;
	public static final int ROUTE_ORIENTATION_CHANGED = 7;
	public static final int WEATHER_SHOW = 8;
	public static final int WEATHER_CANCELED = 9;
	public static final int WEATHER_ERROR = 10;
	public static final int CALCULATE_ROUTE = 11;

	/**
	 * Initializes the activity, call this method inside of the onCreate method
	 * of an Activity
	 * 
	 * @param savedInstance
	 */
	public void initialize(Bundle savedInstance);

	/**
	 * Frees memory. Call this method inside of the onDestroy method of an
	 * Activity
	 */
	public void destroy();

	/**
	 * A Handler to manage messages from the MessageQueue
	 * 
	 * @return
	 */
	public MapHandler getMapHandler();

	/**
	 * 
	 * @return
	 */
	public IMapView getMapView();

	/**
	 * True if each time the GPS location changes the center of the map changes
	 * (so the map is redrawn)
	 * 
	 * @return
	 */
	public boolean isRecenterOnGPSLocationActive();

	public void setRecenterOnGPSLocationActive(boolean active);

	public int getGPSStatus();

	public void setContext(ItemContext context);

	public ItemContext getItemContext();

	public void obtainCellLocation();

	/**
	 * Removes the current CircularRouleteView from the RouleteLayout
	 */
	public void clearContext();

	public void showCircularView(ItemContext context);

	/**
	 * Saves the map center and zoom level on a Bundle when the configuration
	 * has changed or the Activity has been restarted
	 * 
	 * @param outState
	 *            The Bundle @see {@link #onSaveInstanceState(Bundle)}
	 */
	public void saveCenter(Bundle outState);

	/**
	 * Loads the last map center and zoom level stored on a Bundle when the
	 * configuration has changed or the Activity has been restarted
	 * 
	 * @param outState
	 *            The Bundle @see {@link #onSaveInstanceState(Bundle)}
	 */
	public void loadCenter(Bundle savedInstanceState);

	/**
	 * Instantiates the UI: TileRaster, ZoomControls, SlideBar in a
	 * RelativeLayout
	 * 
	 * @param savedInstanceState
	 */
	public void loadUI(Bundle savedInstanceState);

	public void addLocationOverlay();

	/**
	 * Save map info (current layer, GPS location, ItemContext...) when the
	 * configuration has changed or the Activity has been restarted
	 * 
	 * @param outState
	 *            The Bundle @see {@link #onSaveInstanceState(Bundle)}
	 */
	public void saveMap(Bundle outState);

	/**
	 * Load settings stored on a Bundle when the configuration has changed or
	 * the Activity has been restarted
	 * 
	 * @param outState
	 *            The Bundle @see {@link #onSaveInstanceState(Bundle)}
	 */
	public void loadSettings(Bundle outState);

	/**
	 * Save settings when the configuration has changed or the Activity has been
	 * restarted
	 * 
	 * @param outState
	 *            The Bundle @see {@link #onSaveInstanceState(Bundle)}
	 */
	public void saveSettings(Bundle outState);

	/**
	 * Load map info stored on a Bundle when the configuration has changed or
	 * the Activity has been restarted
	 * 
	 * @param outState
	 *            The Bundle @see {@link #onSaveInstanceState(Bundle)}
	 * @throws BaseException
	 */
	public void loadMap(Bundle outState) throws BaseException;

	/**
	 * Starts the LayersActivity with the MapState.gvTilesPath file
	 */
	public void viewLayers();

	public Context getBaseContext();

	/**
	 * Show an AlertDialog to the user to input the query string for NameFinder
	 * addresses
	 */
	public void showSearchDialog();

	public void showWeather(WeatherSet ws);

	/**
	 * This method applies some logic to update the ItemContext from the current
	 * Map state Map.VOID: No route and pois calculated Map.ROUTE_SUCCEEDED:
	 * Route calculated Map.POI_SUCCEEDED: POI calculated Map.POI_CLEARED: POI
	 * has been cleared Map.ROUTE_CLEARED: Route has been cleared
	 * 
	 * @param state
	 */
	public void updateContext(int state);

	/**
	 * Checks that the route can be calculated and launches route calculation
	 */
	public void calculateRoute();
}