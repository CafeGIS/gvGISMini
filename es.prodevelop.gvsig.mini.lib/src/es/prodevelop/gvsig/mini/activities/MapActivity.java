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

package es.prodevelop.gvsig.mini.activities;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import org.anddev.android.weatherforecast.weather.WeatherCurrentCondition;
import org.anddev.android.weatherforecast.weather.WeatherForecastCondition;
import org.anddev.android.weatherforecast.weather.WeatherSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.markupartist.android.widget.ActionBar;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ZoomControls;
import es.prodevelop.geodetic.utils.conversion.ConversionCoords;
import es.prodevelop.gvsig.mini.R;
import es.prodevelop.gvsig.mini.action.CenterOnGPSAction;
import es.prodevelop.gvsig.mini.action.LayersActivityAction;
import es.prodevelop.gvsig.mini.action.SearchAction;
import es.prodevelop.gvsig.mini.activities.NameFinderActivity.BulletedText;
import es.prodevelop.gvsig.mini.activities.NameFinderActivity.BulletedTextListAdapter;
import es.prodevelop.gvsig.mini.common.CompatManager;
import es.prodevelop.gvsig.mini.context.ItemContext;
import es.prodevelop.gvsig.mini.context.map.DefaultContext;
import es.prodevelop.gvsig.mini.context.map.GPSItemContext;
import es.prodevelop.gvsig.mini.context.map.POIContext;
import es.prodevelop.gvsig.mini.context.map.RouteContext;
import es.prodevelop.gvsig.mini.context.map.RoutePOIContext;
import es.prodevelop.gvsig.mini.context.map.wms.WMSGPSItemContext;
import es.prodevelop.gvsig.mini.context.map.wms.WMSPOIContext;
import es.prodevelop.gvsig.mini.context.map.wms.WMSRouteContext;
import es.prodevelop.gvsig.mini.context.map.wms.WMSRoutePOIContext;
import es.prodevelop.gvsig.mini.control.DoubleTapZoom;
import es.prodevelop.gvsig.mini.control.PanControl;
import es.prodevelop.gvsig.mini.control.SelectionControl;
import es.prodevelop.gvsig.mini.exceptions.BaseException;
import es.prodevelop.gvsig.mini.geom.Feature;
import es.prodevelop.gvsig.mini.geom.FeatureCollection;
import es.prodevelop.gvsig.mini.geom.LineString;
import es.prodevelop.gvsig.mini.geom.Point;
import es.prodevelop.gvsig.mini.location.LocationTimer;
import es.prodevelop.gvsig.mini.map.IMapActivity;
import es.prodevelop.gvsig.mini.map.IMapView;
import es.prodevelop.gvsig.mini.map.MapHandler;
import es.prodevelop.gvsig.mini.map.MapView;
import es.prodevelop.gvsig.mini.map.MapViewListener;
import es.prodevelop.gvsig.mini.namefinder.Named;
import es.prodevelop.gvsig.mini.namefinder.NamedMultiPoint;
import es.prodevelop.gvsig.mini.overlay.AcetateOverlay;
import es.prodevelop.gvsig.mini.overlay.RouteOverlay;
import es.prodevelop.gvsig.mini.overlay.ViewSimpleLocationOverlay;
import es.prodevelop.gvsig.mini.search.PlaceSearcher;
import es.prodevelop.gvsig.mini.tasks.Functionality;
import es.prodevelop.gvsig.mini.tasks.map.GetCellLocationFunc;
import es.prodevelop.gvsig.mini.tasks.weather.WeatherFunctionality;
import es.prodevelop.gvsig.mini.tasks.yours.YOURSFunctionality;
import es.prodevelop.gvsig.mini.user.UserContext;
import es.prodevelop.gvsig.mini.user.UserContextManager;
import es.prodevelop.gvsig.mini.util.Utils;
import es.prodevelop.gvsig.mini.utiles.Constants;
import es.prodevelop.gvsig.mini.utiles.Tags;
import es.prodevelop.gvsig.mini.utiles.WorkQueue;
import es.prodevelop.gvsig.mini.views.CircularRouleteView;
import es.prodevelop.gvsig.mini.yours.RouteManager;
import es.prodevelop.gvsig.mobile.fmap.proj.CRSFactory;
import es.prodevelop.tilecache.layers.Layers;
import es.prodevelop.tilecache.provider.TileProvider;
import es.prodevelop.tilecache.renderer.MapRenderer;
import es.prodevelop.tilecache.renderer.MapRendererManager;
import es.prodevelop.tilecache.renderer.OSMMercatorRenderer;
import es.prodevelop.tilecache.renderer.wms.OSRenderer;

/**
 * //FIXME Hacer algo con ItemContext, Rutas y dem�s Base class for an
 * Activity that displays a IMapView
 * 
 * @author aromeu
 * 
 */
public class MapActivity extends MapDownloader implements IMapActivity,
		MapViewListener {

	private Logger log = LoggerFactory.getLogger(MapActivity.class);

	public final static int CODE_SETTINGS = 3215;

	private IMapView mapView;
	private boolean recenterOnGPS = false;
	private long lastTimeSensorChanged = 0;

	private CircularRouleteView rouleteView;
	private RelativeLayout relativeLayout;
	private ZoomControls zoomControls;
	private MenuItem navigationItem;
	private MenuItem myNavigator;
	private MenuItem offlineModeItem;
	private MenuItem myLocationButton;
	private MenuItem myZoomRectangle;
	private MenuItem mySearchDirection;
	private MenuItem myDownloadLayers;
	private MenuItem myDownloadTiles;
	private MenuItem mySettings;
	private MenuItem myAbout;
	private MenuItem myWhats;
	private MenuItem myLicense;

	private int gpsStatus;

	private ItemContext context;

	// FIXME WTF is this?
	private boolean backpressed = false;
	private boolean backpressedroulette = false;

	private UserContextManager contextManager; // singleton with user contexts
	// list
	protected UserContext userContext;
	private MapHandler mapHandler;

	public int getGPSStatus() {
		return gpsStatus;
	}

	public void setGPSStatus(int status) {
		this.gpsStatus = status;
	}

	public void enableMenuItem(MenuItem item, boolean enabled) {
		if (item != null)
			item.setEnabled(enabled);
	}

	public IMapView getMapView() {
		return mapView;
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		try {
			final IMapView mapView = this.getMapView();
			boolean repaint = mapView
					.getMRendererInfo()
					.getCurrentExtent()
					.contains(
							mapView.getLocationOverlay().reprojectedCoordinates);

			mapView.setBearing((int) event.values[0]);
			long current = System.currentTimeMillis();
			if (!mapView.isNavigationMode() && repaint) {
				mapView.setDirty();
			} else {
				if (current - lastTimeSensorChanged > Utils.REPAINT_TIME
						&& repaint) {
					mapView.setDirty();
					lastTimeSensorChanged = current;
				}
			}
		} catch (Exception e) {
			log.error("onSensorChanged: ", e);
		}
	}

	@Override
	public void onLocationChanged(Location pLoc) {
		try {
			final IMapView mapView = this.getMapView();

			if (pLoc == null)
				return;

			mapView.getLocationOverlay().setLocation(
					Utils.locationToGeoPoint(pLoc), pLoc.getAccuracy(),
					pLoc.getProvider());
			if (this.isRecenterOnGPSLocationActive() && pLoc.getLatitude() != 0
					&& pLoc.getLongitude() != 0 && mapView.isNavigationMode()) {

				double[] coords = ConversionCoords.reproject(
						pLoc.getLongitude(), pLoc.getLatitude(),
						CRSFactory.getCRS("EPSG:4326"),
						CRSFactory.getCRS(mapView.getMRendererInfo().getSRS()));
				mapView.getMapViewController().animateTo(coords[0], coords[1]);
			}

			if (mapView.getLocationOverlay().mLocation != null) {
				this.enableMenuItem(this.navigationItem, true);
			}
		} catch (Exception e) {
			log.error("onLocationChanged: ", e);
		} finally {

		}
	}

	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		this.setGPSStatus(status);
	}

	@Override
	public void onSettingChange(String key, Object value) {
		final IMapView mapView = this.getMapView();
		try {
			if (key.compareTo(getText(R.string.settings_key_gps).toString()) == 0) {
				if (Boolean.valueOf(value.toString()).booleanValue()) {
					this.enableGPS();
				} else {
					this.disableGPS();
				}
			} else if (key.compareTo(getText(R.string.settings_key_orientation)
					.toString()) == 0) {
				if (Boolean.valueOf(value.toString()).booleanValue()) {
					this.initializeSensor();
				} else {
					this.stopSensor();
				}
			} else if (key.compareTo(getText(R.string.settings_key_tile_name)
					.toString()) == 0) {
				String current = mapView.getTileProvider().getMFSTileProvider()
						.getStrategy().getTileNameSuffix();

				// tile suffix has changed
				if (current.compareTo("." + value.toString()) != 0) {
					mapView.instantiateTileProviderfromSettings();
				}

			} else if (key
					.compareTo(getText(R.string.settings_key_offline_maps)
							.toString()) == 0) {
				if (Boolean.valueOf(value.toString()).booleanValue()) {
					mapView.getTileProvider()
							.setMode(TileProvider.MODE_OFFLINE);
				} else {
					int mode = Settings.getInstance()
							.getIntValue(
									getText(R.string.settings_key_list_mode)
											.toString());
					mapView.getTileProvider().setMode(mode);
				}
			} else if (key.compareTo(getText(R.string.settings_key_list_mode)
					.toString()) == 0) {
				if (Settings.getInstance().getBooleanValue(
						getText(R.string.settings_key_offline_maps).toString())) {
					mapView.getTileProvider()
							.setMode(TileProvider.MODE_OFFLINE);
				} else {
					mapView.getTileProvider().setMode(
							Integer.valueOf(value.toString()).intValue());
				}
			} else if (key.compareTo(getText(
					R.string.settings_key_list_strategy).toString()) == 0) {
				mapView.instantiateTileProviderfromSettings();
			} else if (key.compareTo(getText(R.string.settings_key_os_key)
					.toString()) == 0
					|| key.compareTo(getText(R.string.settings_key_os_url)
							.toString()) == 0
					|| key.compareTo(getText(R.string.settings_key_os_custom)
							.toString()) == 0) {
				if (mapView.getMRendererInfo() instanceof OSRenderer) {
					OSRenderer osr = (OSRenderer) mapView.getMRendererInfo();
					OSSettingsUpdater
							.synchronizeRendererWithSettings(osr, this);
				}
			} else if (key.compareTo(getText(R.string.settings_key_gps_dist)
					.toString()) == 0
					|| key.compareTo(getText(R.string.settings_key_gps_time)
							.toString()) == 0) {
				this.disableGPS();
				this.enableGPS();
			} else if (key.compareTo(getText(R.string.settings_key_gps_cell)
					.toString()) == 0) {
				if (Boolean.valueOf(value.toString()).booleanValue()) {
					this.getLocationHandler().setLocationTimer(
							new LocationTimer(this.getLocationHandler()));
				} else {
					this.getLocationHandler().finalizeCellLocation();
				}
			}
		} catch (Exception e) {
			log.error("onSettingChange", e);
		}
	}

	@Override
	public void initialize(Bundle savedInstance) {
		// TODO Auto-generated method stub

	}

	@Override
	public MapHandler getMapHandler() {
		if (mapHandler == null) {
			mapHandler = new MapHandler(this);
		}
		return this.mapHandler;
	}

	@Override
	public boolean isRecenterOnGPSLocationActive() {
		return this.recenterOnGPS;
	}

	@Override
	public void setRecenterOnGPSLocationActive(boolean active) {
		this.recenterOnGPS = active;
	}

	@Override
	public void reloadLayerAfterDownload() {
		final IMapView mapView = MapActivity.this.getMapView();
		mapView.resumeDraw();
		mapView.onLayerChanged(mapView.getMRendererInfo().getFullNAME());
		try {
			mapView.initializeCanvas(mapView.getMapWidth(),
					mapView.getMapHeight());
		} catch (BaseException e) {
			// TODO Auto-generated catch block
			log.error("reloadLayerAfterDownload", e);
		}
	}

	@Override
	public void onStartDownload() {
		this.getMapView().getTileProvider().clearPendingQueue();
		super.onStartDownload();
	}

	@Override
	public void onNewIntent(Intent i) {
		try {
			if (i == null) {
				Log.d("Map", "intent is null");
				return;
			}

			try {

				setIntent(i);
				processGeoAction(i);
				if (Intent.ACTION_SEARCH.equals(i.getAction())) {
					processActionSearch(i);
					return;
				} else {
					String mapLayer = i.getStringExtra("layer");
					log.debug("previous layer: " + mapLayer);
					if (mapLayer != null) {
						getMapView().onLayerChanged(mapLayer);
						log.debug("map loaded");
					}
				}
			} catch (Exception e) {
				log.error("onNewIntent", e);

			}
		} catch (Exception e) {
			log.error("onNewIntent", e);
		}
	}

	public void processGeoAction(Intent i) {
		if (i == null)
			return;
		int zoom = i.getIntExtra("z", -1);
		if (zoom == -1) {
			zoom = i.getIntExtra("zoom", -1);
		}

		if (zoom == -1)
			return;
		i.putExtra("zoom", -1);

		final double lat = i.getDoubleExtra("lat", 0);
		final double lon = i.getDoubleExtra("lon", 0);

		getMapView().getMapViewController().setMapCenterFromLonLat(lon, lat);
		getMapView().getMapViewController().setZoomLevel(zoom);
	}

	private void processOfflineIntentActoin(Intent i) throws Exception {
		log.debug("Map", "OFFLINE_INTENT_ACTION");
		String completeURLString = i.getStringExtra(Constants.URL_STRING);
		if (completeURLString == null)
			return;
		i.removeExtra(Constants.URL_STRING);
		completeURLString = completeURLString.replaceAll("&gt;",
				MapRenderer.NAME_SEPARATOR);
		String urlString = completeURLString.split(";")[1];
		String layerName = completeURLString.split(";")[0];
		MapRenderer renderer = MapRendererManager.getInstance()
				.getMapRendererFactory()
				.getMapRenderer(layerName, urlString.split(","));
		// if (renderer.isOffline())
		// renderer.setNAME(renderer.getNAME() + MapRenderer.NAME_SEPARATOR
		// + renderer.getOfflineLayerName());
		Layers.getInstance().addLayer(completeURLString);
		Layers.getInstance().persist();
		log.debug("Map", renderer.getFullNAME());
		getMapView().onLayerChanged(renderer.getFullNAME());
	}

	@Override
	public void onPause() {
		try {
			log.debug("onPause");
			super.onPause();
			// if (wl != null && wl.isHeld())
			getMapView().pauseDraw();
			getMapView().setKeepScreenOn(false);
		} catch (Exception e) {
			log.error("onPause: ", e);
		}
	}

	/**
	 * Launches GetCellLocationFunc
	 */
	public void obtainCellLocation() {
		try {
			GetCellLocationFunc cellLocationFunc = new GetCellLocationFunc(
					this, 0);
			cellLocationFunc.launch();
		} catch (Exception e) {
			log.error("obtaincelllocation", e);
		}
	}

	@Override
	public void onLowMemory() {
		try {
			log.debug("onLowMemory");
			getMapView().getTileProvider().onLowMemory();
			super.onLowMemory();
			// Toast.makeText(this, R.string.Map_25, Toast.LENGTH_LONG).show();
			getMapView().persist();
			System.gc();
		} catch (Exception e) {
			log.error("onLowMemory: ", e);
			System.gc();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		try {
			if (keyCode == KeyEvent.KEYCODE_BACK) {

				// FIXME Esto es de NOMADA....
				// if (osmap.removeExpanded()) {
				// return true;
				// }

				if (getMapView().getAcetateOverlay().getPopupVisibility() == View.VISIBLE) {
					getMapView().getAcetateOverlay().setPopupVisibility(
							View.INVISIBLE);
					return true;
				}

				log.debug("KEY BACK pressed");
				if (!backpressedroulette) {

					backpressed = true;
					// this.onDestroy();
					Intent i = new Intent();
					i.putExtra("exit", true);
					setResult(RESULT_OK, i);
					finish();

					// return false;
					super.onKeyDown(keyCode, event);
				} else {
					clearContext();
					backpressedroulette = false;
					return true;
				}
			}
			return super.onKeyDown(keyCode, event);
		} catch (Exception e) {
			log.error("onKeyDown: ", e);
			return false;
		}
	}

	@Override
	public void onConfigurationChanged(Configuration config) {
		try {
			super.onConfigurationChanged(config);
			log.debug("onConfigurationChanged");
			if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
				this.getMapView()
						.getLocationOverlay()
						.setOffsetOrientation(
								ViewSimpleLocationOverlay.PORTRAIT_OFFSET_ORIENTATION);
			} else if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
				this.getMapView()
						.getLocationOverlay()
						.setOffsetOrientation(
								ViewSimpleLocationOverlay.LANDSCAPE_OFFSET_ORIENTATION);
			}
			DisplayMetrics metrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(metrics);
			getMapView().initializeCanvas(metrics.widthPixels,
					metrics.heightPixels);
		} catch (Exception e) {
			log.error("onConfigurationChanged: ", e);
		}
	}

	public ItemContext getItemContext() {
		return this.context;
	}

	/**
	 * Sets the current ItemContext of the application
	 * 
	 * @param context
	 */
	public void setContext(ItemContext context) {
		try {
			if (context == null) {
				this.setContext(new DefaultContext(this));
				log.debug("setContext: " + "DefaultContext");
			} else {
				try {
					Functionality f = this.getItemContext()
							.getExecutingFunctionality();
					if (f != null)
						context.setExecutingFunctionality(f);
				} catch (Exception e) {
					log.error("setContext", e);

				}
				this.context = null;
				this.context = context;

				log.debug("setContext: " + context.getClass().getName());
			}
		} catch (Exception e) {
			log.error("setContext: ", e);
		}
	}

	/**
	 * Removes the current CircularRouleteView from the RouleteLayout
	 */
	public void clearContext() {
		try {
			if (rouleteView == null)
				return;
			final int size = rouleteView.getChildCount();

			View child = null;
			for (int i = 0; i < size; i++) {
				try {
					child = rouleteView.getChildAt(i);
					if (child != null)
						child.getBackground().setCallback(null);
					child = null;
				} catch (Exception e) {

				}
			}

			relativeLayout.removeView(rouleteView);
		} catch (Exception e) {
			log.error("clearContext: ", e);
		}
	}

	/**
	 * Instantiates a CircularRouleteView @see Contextable, ItemContext
	 * 
	 * @param context
	 *            The ItemContext that contains the IDs of the Drawables to
	 *            instantiate buttons and associate Functionalities to it
	 */
	@Override
	public void overlayContextToShow(ItemContext itemContext, double x, double y) {
		try {
			getMapView().getMapViewController().animateTo(x, y);
			
			final ItemContext context = itemContext;

			if (context == null)
				return;

			showCircularView(context);

		} catch (Exception e) {
			log.error("showContext: ", e);
		}
	}

	public void showCircularView(ItemContext context) {
		LayoutInflater factory = LayoutInflater.from(this);
		final int[] viewsID = context.getViewsId();
		final int size = viewsID.length;
		String description = mapView.getAcetateOverlay().getDescription();

		HashMap h = context.getFunctionalities();

		if (h == null)
			return;

		int id;
		View view;
		Functionality func;
		rouleteView = new CircularRouleteView(this, true);

		for (int i = 0; i < size; i++) {
			try {
				id = viewsID[i];
				view = (View) factory.inflate(id, null);
				func = (Functionality) h.get(id);
				if (func != null)
					view.setOnClickListener(func);
				rouleteView.addView(view);
			} catch (Exception e) {
				log.error("show context", e);
			}
		}

		rouleteView.setVisibility(View.VISIBLE);
		final int count = relativeLayout.getChildCount();
		View v;
		for (int i = 0; i < count; i++) {
			v = relativeLayout.getChildAt(i);
			if (v instanceof CircularRouleteView) {
				relativeLayout.removeView(v);
			}
		}
		relativeLayout.addView(rouleteView);

		// Change user context to store that the CircularRouleteView has
		// been
		// shown once at least
		userContext.setUsedCircleMenu(true);
		userContext.setLastExecCircle();
		backpressedroulette = true;
	}

	/**
	 * Instantiates a CircularRouleteView @see Contextable, ItemContext
	 * 
	 * @param context
	 *            The ItemContext that contains the IDs of the Drawables to
	 *            instantiate buttons and associate Functionalities to it
	 */
	public void showContext(ItemContext context) {
		try {
			log.debug("show context");

			context = updateContextWMS(context);
			if (context == null)
				return;

			showCircularView(context);
		} catch (Exception e) {
			log.error("showContext: ", e);
		}
	}

	/**
	 * This method applies some logic to update the ItemContext from the current
	 * Map state Map.VOID: No route and pois calculated Map.ROUTE_SUCCEEDED:
	 * Route calculated Map.POI_SUCCEEDED: POI calculated Map.POI_CLEARED: POI
	 * has been cleared Map.ROUTE_CLEARED: Route has been cleared
	 * 
	 * @param state
	 */
	public void updateContext(int state) {
		try {
			log.debug("updateContext");
			switch (state) {
			case IMapActivity.VOID:
				log.debug("VOID");
				this.setContext(new DefaultContext(this));
				break;
			case IMapActivity.ROUTE_SUCCEEDED:
				log.debug("ROUTE_SUCEEDED");
				this.setContext(new RouteContext(this));
				break;
			// case Map.POI_SUCCEEDED:
			// log.log(Level.FINE, "POI_SUCCEEDED");
			// if (RouteManager.getInstance().getRegisteredRoute() != null
			// && RouteManager.getInstance().getRegisteredRoute()
			// .getState() == Tags.ROUTE_WITH_2_POINT) {
			// this.setContext(new RoutePOIContext(this));
			// } else {
			// this.setContext(new POIContext(this));
			// }
			// break;
			// case Map.POI_CLEARED:
			// log.log(Level.FINE, "POI_CLEARED");
			// if (RouteManager.getInstance().getRegisteredRoute() != null
			// && RouteManager.getInstance().getRegisteredRoute()
			// .getState() == Tags.ROUTE_WITH_2_POINT) {
			// this.setContext(new RouteContext(this));
			// } else {
			// this.setContext(new DefaultContext(this));
			// }
			// break;
			case IMapActivity.ROUTE_CLEARED:
				log.debug("ROUTE_CLEARED");
				// if (nameds != null && nameds.getNumPoints() > 0) {
				// this.setContext(new POIContext(this));
				// } else {
				this.setContext(new DefaultContext(this));
				// }
				break;
			}
			updateContextWMS(this.context);
		} catch (Exception e) {
			log.error("updateContext: ", e);
		}
	}

	public ItemContext updateContextWMS(ItemContext context) {
		try {
			if (this.getMapView().getMRendererInfo().getType() != MapRenderer.WMS_RENDERER) {
				if (context instanceof WMSRoutePOIContext)
					context = new RoutePOIContext(this);
				else if (context instanceof WMSRouteContext)
					context = new RouteContext(this);
				else if (context instanceof WMSPOIContext) {
					if (RouteManager.getInstance().getRegisteredRoute() != null
							&& RouteManager.getInstance().getRegisteredRoute()
									.getState() == Tags.ROUTE_WITH_2_POINT) {
						context = new RoutePOIContext(this);
					} else {
						context = new POIContext(this);
					}
				}

				else if (context instanceof WMSGPSItemContext)
					if (RouteManager.getInstance().getRegisteredRoute() != null
							&& RouteManager.getInstance().getRegisteredRoute()
									.getState() == Tags.ROUTE_WITH_2_POINT) {
						context = new RouteContext(this);
					} else {
						context = new DefaultContext(this);
					}
				else if (context instanceof POIContext)
					if (RouteManager.getInstance().getRegisteredRoute() != null
							&& RouteManager.getInstance().getRegisteredRoute()
									.getState() == Tags.ROUTE_WITH_2_POINT) {
						context = new RoutePOIContext(this);
					}

				this.setContext(context);
				return context;
			}

			if (context instanceof RoutePOIContext)
				context = new WMSRoutePOIContext(this);
			else if (context instanceof RouteContext)
				context = new WMSRouteContext(this);
			else if (context instanceof POIContext)
				if (RouteManager.getInstance().getRegisteredRoute() != null
						&& RouteManager.getInstance().getRegisteredRoute()
								.getState() == Tags.ROUTE_WITH_2_POINT) {
					context = new WMSRoutePOIContext(this);
				} else {
					context = new WMSPOIContext(this);
				}
			else if (context instanceof DefaultContext
					|| context instanceof GPSItemContext)
				if (RouteManager.getInstance().getRegisteredRoute() != null
						&& RouteManager.getInstance().getRegisteredRoute()
								.getState() == Tags.ROUTE_WITH_2_POINT) {
					context = new WMSRouteContext(this);
				} else {
					context = new WMSGPSItemContext(this);
				}

			this.setContext(context);
			return context;
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}

	@Override
	protected void onStop() {
		try {
			// mSensorManager.unregisterListener(mSensorListener);
			super.onStop();
		} catch (Exception e) {
			log.error("onStop: ", e);
		}
	}

	@Override
	public boolean onTrackballEvent(MotionEvent event) {
		try {
			return this.getMapView().onTrackballEvent(event);
		} catch (Exception e) {
			log.error("onTrackBallEvent: ", e);
			return false;
		}
	}

	private void processRouteAction(Intent i) {
		if (i == null)
			return;
		if (i.getBooleanExtra(RouteManager.ROUTE_MODIFIED, false)) {
			this.calculateRoute();
			i.putExtra(RouteManager.ROUTE_MODIFIED, false);
		}
	}

	@Override
	public void onResume() {
		try {
			log.debug("onResume");
			super.onResume();
			getMapView().resumeDraw();
			processGeoAction(getIntent());
			processRouteAction(getIntent());
			processOfflineIntentActoin(getIntent());
			if (getMapView().isNavigationMode()
					&& isRecenterOnGPSLocationActive())
				getMapView().setKeepScreenOn(true);
			// if (navigation && recenterOnGPS)
			// wl.acquire();
			// mSensorManager.registerListener(mSensorListener, mSensorManager
			// .getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
			// SensorManager.SENSOR_DELAY_FASTEST);
		} catch (Exception e) {
			log.error("onResume: ", e);
		}
	}

	/**
	 * Called when the activity is first created.
	 * */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		try {
			super.onCreate(savedInstanceState);
			try {
				setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);
				CompatManager.getInstance().getRegisteredLogHandler()
						.configureLogger(log);
				Settings.getInstance().addOnSettingsChangedListener(this);
				log.debug("on create");

				onNewIntent(getIntent());
			} catch (Exception e) {
				log.error("onCreate", e);
				// log.log(Level.SEVERE,e.getMessage());
			} finally {

			}

			this.setContext(new DefaultContext(this));

			// nameFinderTask = new NameFinderTask(this, handler);
			relativeLayout = new RelativeLayout(this);
			// TMSRenderer t = TMSRenderer.getTMSRenderer(
			// "http://www.idee.es/wms-c/PNOA/PNOA/1.0.0/PNOA/");
			loadSettings(savedInstanceState);
			loadMap(savedInstanceState);

			// Load UserContexts
			// First create/get UserContextManager singleton instance
			contextManager = UserContextManager.getInstance();
			// Create UserContext and register it in the manager
			userContext = new UserContext();
			contextManager.Register(userContext);
			// add new user contexts here
			// Load previously persisted user context data
			contextManager.loadContexts();
			userContext.incExecutionCounter(); // update execution number

			boolean isSaved = false;
			try {
				if (savedInstanceState != null)
					isSaved = savedInstanceState.getBoolean("isSaved", false);
			} catch (Exception e) {
				log.error("isSaved: ", e);
			}

			if (isSaved) {
				log.debug("Restoring from previous state");
				loadRoute(savedInstanceState);
				// loadPois(savedInstanceState);
				loadUI(savedInstanceState);
				loadMap(savedInstanceState);
				loadCenter(savedInstanceState);
			} else {
				log.error("Not restoring from previous state");
				loadUI(savedInstanceState);
				boolean succeed = getMapView().getMapState().load();
				if (!succeed) {
					log.debug("map state was not persisted. Loading Mapnik");
					getMapView().getMapViewController().setZoomLevel(3);
					getMapView().getMapViewController().setMapCenter(0, 0);
					getMapView().setRenderer(
							OSMMercatorRenderer.getMapnikRenderer());
				}
			}
			this.setContentView(relativeLayout);

			addActionBar(getLayoutInflater());

			/*
			 * Add items and set the contentbar title
			 */

			// actionBar.setTitle(R.string.action_bar_title);
			// actionBar.setHomeLogo(R.drawable.icon);
			// actionBar.setHomeAction(new MyCenterLocation());

			addLayersActivityAction();
			addMyLocationAction();
			addSearchAction();

			// this.addContentView(actionbar, R.layout.pruebas);
			// if (isSaved) setContentView(null);
			// enableAcelerometer(savedInstanceState);

			/*
			 * if (!userContext.isUsedCircleMenu()) { // The Circle Context Menu
			 * (Roulette) has never been displayed, // so let's // give the user
			 * a hint about it Toast t = Toast.makeText(this, R.string.Map_23,
			 * Toast.LENGTH_LONG); t.show(); }
			 */

			// Intercept a possible intent with a search executed from the
			// search
			// dialog, towards the application
			Intent i = getIntent();

			this.processActionSearch(i);
			this.processGeoAction(i);
			this.processOfflineIntentActoin(i);

			int hintId = 0;
			hintId = userContext.getHintMessage();
			if (hintId != 0) {
				Toast t = Toast.makeText(this, hintId, Toast.LENGTH_LONG);
				t.show();
			}

		} catch (Exception e) {
			log.error("", e);
			LogFeedbackActivity.showSendLogDialog(this);
		} finally {
			// this.obtainCellLocation();
		}
	}

	public void addAcetate() {
		getMapView().setAcetateOverlay(
				new AcetateOverlay(this, getMapView(),
						AcetateOverlay.DEFAULT_NAME));
	}

	public void addMyLocationAction() {
		addActionToActionbar(new CenterOnGPSAction(this));
	}

	public void addLayersActivityAction() {
		addActionToActionbar(new LayersActivityAction(this));
	}

	public void addSearchAction() {
		addActionToActionbar(new SearchAction(this));
	}

	public void processActionSearch(Intent i) {
		if (Intent.ACTION_SEARCH.equals(i.getAction())) {
			String q = i.getStringExtra(SearchManager.QUERY);
			if (q == null)
				q = i.getDataString();
			final String query = q;
			searchInNameFinder(query, false);
			// Intent newIntent = new Intent(this, ResultSearchActivity.class);
			// newIntent.putExtra(SearchActivity.HIDE_AUTOTEXTVIEW, true);
			// newIntent.putExtra(ResultSearchActivity.QUERY, query.toString());
			// fillSearchCenter(newIntent);
			// startActivity(newIntent);
			// return;
		}
	}

	/**
	 * Starts the NameFinderActivity after the NameFinderFunc has finished
	 * 
	 * @param descr
	 *            An array with the description of the results of the NameFinder
	 *            service
	 * @param nm
	 *            A NamedMultiPoint with the full results of the service
	 * @return True if the activity is started, false if the results were not
	 *         correct
	 */
	public boolean showPOIs(String[] descr, NamedMultiPoint nm) {
		try {
			if (descr == null) {
				log.debug("show pois with descr null, returning...");
				return false;
			}

			if (nm == null || nm.getNumPoints() <= 0) {
				log.debug("show pois with nm null, returning...");
				AlertDialog.Builder alert = new AlertDialog.Builder(this);

				hideIndeterminateDialog();
				Toast.makeText(this, R.string.Map_1, Toast.LENGTH_LONG).show();

				return false;
			}

			Intent intent = new Intent(this, NameFinderActivity.class);
			String[] res = new String[nm.getPoints().length];

			for (int i = 0; i < nm.getNumPoints(); i++) {
				res[i] = ((Named) nm.getPoint(i)).description;
			}
			intent.putExtra("test", res);

			startActivityForResult(intent, 0);

			hideIndeterminateDialog();
			return true;

		} catch (Exception e) {
			log.error("showPOIS: ", e);
			return false;
		} finally {
			hideIndeterminateDialog();
		}
	}

	/**
	 * Starts the LayersActivity with the MapState.gvTilesPath file
	 */
	@Override
	public void viewLayers() {
		try {
			log.debug("Launching load layers activity");
			Intent intent = new Intent(this, LayersActivity.class);

			intent.putExtra("loadLayers", true);
			intent.putExtra("gvtiles", getMapView().getMapState().gvTilesPath);

			startActivityForResult(intent, 1);
		} catch (Exception e) {
			log.error("viewLayers: ", e);
		}
	}

	/**
	 * Checks that the route can be calculated and launches route calculation
	 */
	public void calculateRoute() {
		try {
			log.debug("calculate route");
			if (RouteManager.getInstance().getRegisteredRoute().canCalculate()) {
				if (!Settings.getInstance().isOfflineMode()) {
					this.launchRouteCalculation();
					// User context update
					this.userContext.setUsedRoutes(true);
					this.userContext.setLastExecRoutes();
				} else {
					Toast.makeText(this, R.string.in_offline_mode,
							Toast.LENGTH_LONG).show();
				}
			}
		} catch (Exception e) {
			log.error("calculateRoute: ", e);
			Toast t = Toast.makeText(this, R.string.Map_2, Toast.LENGTH_LONG);
			t.show();
		} finally {
		}
	}

	/**
	 * Launches YOURSFunctionality
	 */
	public void launchRouteCalculation() {
		try {
			log.debug("launching route calculation");
			RouteManager.getInstance().getRegisteredRoute().iscancelled = false;
			YOURSFunctionality yoursFunc = new YOURSFunctionality(this, 0);
			yoursFunc.onClick(null);
			userContext.setUsedRoutes(true);
		} catch (Exception e) {
			log.error("launchRouteCalculation: ", e);
		}
	}

	/**
	 * Manages the results of the NameFinderActivity, LayersActivity
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		try {
			super.onActivityResult(requestCode, resultCode, intent);
			processGeoAction(intent);
			log.debug("onActivityResult (code, resultCode): " + requestCode
					+ ", " + resultCode);
			if (requestCode != CODE_SETTINGS && intent == null) {
				log.debug("intent was null, returning from onActivityResult");
				return;
			}

			switch (requestCode) {
			case 0:
				switch (resultCode) {
				case 0:
					// log.debug(Level.FINE,
					// "from NameFinderActivity: route from here");
					// int pos = Integer.parseInt(intent.getExtras()
					// .get("selected").toString());
					// Named p = (Named) nameds.getPoint(pos);
					// RouteManager.getInstance().getRegisteredRoute()
					// .setStartPoint(new Point(p.getX(), p.getY()));
					// calculateRoute();
					// getMapView().getMapViewController().setMapCenter(
					// p.projectedCoordinates.getX(),
					// p.projectedCoordinates.getY());
					break;
				case 1:
					// log.debug("from NameFinderActivity: route to here");
					// int pos1 = Integer.parseInt(intent.getExtras()
					// .get("selected").toString());
					// Named p1 = (Named) nameds.getPoint(pos1);
					// RouteManager.getInstance().getRegisteredRoute()
					// .setEndPoint(new Point(p1.getX(), p1.getY()));
					// calculateRoute();
					// getMapView().getMapViewController().setMapCenter(
					// p1.projectedCoordinates.getX(),
					// p1.projectedCoordinates.getY());
					break;
				case 2:
					// log.debug("from NameFinderActivity: set map center");
					// int pos2 = Integer.parseInt(intent.getExtras()
					// .get("selected").toString());
					// Named p2 = (Named) this.nameds.getPoint(pos2);
					// getMapView().getMapViewController().setMapCenterFromLonLat(
					// p2.getX(), p2.getY());
					// if (getMapView().getMRendererInfo() instanceof
					// OSMMercatorRenderer)
					// getMapView().getMapViewController().setZoomLevel(
					// p2.zoom);
					break;
				}

				break;
			case 1:
				switch (resultCode) {
				case RESULT_OK:
					log.debug("from LayersActivity");
					String layerName = intent.getStringExtra("layer");
					getMapView().getMapState().gvTilesPath = intent
							.getStringExtra("gvtiles");
					if (layerName != null) {
						log.debug("load layer: " + layerName);
						getMapView().onLayerChanged(layerName);
					}
					break;
				}
				break;
			}
		} catch (Exception e) {
			log.error("Map onActivityResult: ", e);
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(final Menu menu) {
		try {
			boolean isOffline = true;
			try {
				isOffline = Settings.getInstance().getBooleanValue2(
						getResources().getString(
								R.string.settings_key_offline_maps));
			} catch (NoSuchFieldError e) {
				isOffline = true;
			}
			offlineModeItem.setChecked(isOffline);
			return true;
		} catch (Exception e) {
			log.error("", e);
			return false;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu pMenu) {
		try {
//			mySearchDirection = pMenu.add(0, 0, 0, R.string.Map_3).setIcon(
//					R.drawable.menu00);
			myLocationButton = pMenu.add(0, 2, 2, R.string.Map_4).setIcon(
					R.drawable.menu_location);
			pMenu.add(0, 3, 3, R.string.Map_5).setIcon(R.drawable.menu02);
			myDownloadTiles = pMenu.add(0, 4, 4, R.string.download_tiles_14)
					.setIcon(R.drawable.layerdonwload);
			myNavigator = pMenu.add(0, 6, 6, R.string.Map_Navigator)
					.setIcon(R.drawable.menu_navigation).setEnabled(true);
			offlineModeItem = pMenu
					.add(0, 8, 8, R.string.settings_mode_offline).setCheckable(
							true);
			offlineModeItem
					.setOnMenuItemClickListener(new OnMenuItemClickListener() {

						@Override
						public boolean onMenuItemClick(MenuItem item) {
							item.setChecked(!item.isChecked());

							Settings.getInstance()
									.putValue(
											getResources()
													.getString(
															R.string.settings_key_offline_maps),
											item.isChecked());
							Settings.getInstance().notifyObserversWithChanges();
							Settings.getInstance()
									.updateBooleanSharedPreference(
											getResources()
													.getString(
															R.string.settings_key_offline_maps),
											item.isChecked(), MapActivity.this);
							return true;
						}
					});
			mySettings = pMenu.add(0, 7, 7, R.string.Map_31).setIcon(
					android.R.drawable.ic_menu_preferences);
			myWhats = pMenu.add(0, 9, 9, R.string.Map_30).setIcon(
					R.drawable.menu_location);
			myLicense = pMenu.add(0, 10, 10, R.string.Map_29).setIcon(
					R.drawable.menu_location);
			myAbout = pMenu.add(0, 11, 11, R.string.Map_28).setIcon(
					R.drawable.menu_location);
		} catch (Exception e) {
			log.error("onCreateOptionsMenu: ", e);
		}
		return true;
	}

	public void fillSearchCenter(Intent i) {
		double[] lonlat = getMapView().getMyLocationCenterMercator();
		i.putExtra("lon", lonlat[0]);
		i.putExtra("lat", lonlat[1]);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		boolean result = true;
		try {
			result = super.onMenuItemSelected(featureId, item);
			switch (item.getItemId()) {
			case 0:
				try {
					MapActivity.this.showSearchDialog();
				} catch (Exception e) {
					log.error("showAddressDialog: ", e);
				}
				break;
			case 1:
				//

				break;
			case 2:
				try {
					log.error("click on my location menu item");
					if (this.getMapView().getLocationOverlay().mLocation == null
							|| (this.getMapView().getLocationOverlay().mLocation
									.getLatitudeE6() == 0 && this.getMapView()
									.getLocationOverlay().mLocation
									.getLongitudeE6() == 0)) {
						Toast.makeText(this, R.string.Map_24, Toast.LENGTH_LONG)
								.show();
						return true;
					}
					getMapView()
							.adjustViewToAccuracyIfNavigationMode(
									this.getMapView().getLocationOverlay().mLocation.acc);
					getMapView()
							.getMapViewController()
							.setMapCenterFromLonLat(
									this.getMapView().getLocationOverlay().mLocation
											.getLongitudeE6() / 1E6,
									this.getMapView().getLocationOverlay().mLocation
											.getLatitudeE6() / 1E6);
					// }
				} catch (Exception e) {
					log.error("My location: ", e);
				}
				break;
			case 3:
				viewLayers();
				break;
			case 4:
				if (Utils.isSDMounted()) {
					if (getMapView().getMRendererInfo().allowsMassiveDownload()) {
						MapActivity.this.showDownloadTilesDialog(getMapView()
								.getMRendererInfo(), getMapView());
					} else {

						this.showOKDialog(getText(R.string.not_download_tiles)
								.toString(), R.string.warning, false);
					}
				}

				else
					Toast.makeText(this, R.string.LayersActivity_1,
							Toast.LENGTH_LONG).show();
				break;
			case 5:
				try {
					Intent i = new Intent(Intent.ACTION_VIEW,
							Uri.parse("market://search?q=nomada prodevelop"));
					startActivity(i);
				} catch (Exception e) {
					log.error("OpenWebsite: ", e);
				}
				break;
			case 6:
				try {
					// FIXME WTF!!
					recenterOnGPS = !recenterOnGPS;

					if (myLocationButton != null)
						myLocationButton.setEnabled(!recenterOnGPS);
					if (myZoomRectangle != null)
						myZoomRectangle.setEnabled(!recenterOnGPS);
					if (myDownloadLayers != null)
						myDownloadLayers.setEnabled(!recenterOnGPS);
					if (mySearchDirection != null)
						mySearchDirection.setEnabled(!recenterOnGPS);
					if (mySettings != null)
						mySettings.setEnabled(!recenterOnGPS);

					// myGPSButton.setEnabled(!recenterOnGPS);

					if (!recenterOnGPS) {
						log.debug("recentering on GPS after check MyLocation on");
						zoomControls.setVisibility(View.VISIBLE);
						myNavigator.setIcon(R.drawable.menu_navigation);

					} else {
						log.debug("stop recentering on GPS after check MyLocation off");//
						zoomControls.setVisibility(View.INVISIBLE);
						myNavigator.setIcon(R.drawable.menu_navigation_off);
					}

					if (!getMapView().isNavigationMode()) {
						this.initializeSensor(true);
						this.showNavigationModeAlert();
					} else {
						try {
							if (Settings.getInstance().getBooleanValue(
									getText(R.string.settings_key_orientation)
											.toString()))
								this.stopSensor();
						} catch (Exception e) {
							log.error("navigation mode off", e);
						}

						log.debug("navigation mode off");
						setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
						getMapView().setNavigationMode(false);
						getMapView().setKeepScreenOn(false);
					}
				} catch (Exception e) {
					log.error("switchPanMode: ", e);
				}
				break;
			case 7:
				Intent i = new Intent(this, SettingsActivity.class);
				startActivity(i);
				break;
			case 9:
				try {
					showWhatsNew();
				} catch (Exception e) {
					log.error("OpenWebsite: ", e);
				}
				break;
			case 10:
				try {
					showLicense();
				} catch (Exception e) {
					log.error("OpenWebsite: ", e);
				}
				break;
			case 11:
				try {
					showAboutDialog();
				} catch (Exception e) {
					log.error("OpenWebsite: ", e);
				}
				break;
			default:
				break;
			}
		} catch (Exception e) {
			log.error("", e);
		}
		return result;
	}

	protected void showNavigationModeAlert() {
		try {
			RadioGroup r = new RadioGroup(this);
			RadioButton r1 = new RadioButton(this);
			r1.setText(R.string.portrait);
			r1.setId(0);
			RadioButton r2 = new RadioButton(this);
			r2.setText(R.string.landscape);
			r2.setId(1);
			r.addView(r1);
			r.addView(r2);
			r.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(RadioGroup arg0, int arg1) {
					try {
						getMapView().centerOnGPSLocation();
						getMapView().setKeepScreenOn(true);
						getMapView().setNavigationMode(true);
						getMapView().onLayerChanged(
								getMapView().getMRendererInfo().getFullNAME());
						// final MapRenderer r =
						// Map.this.osmap.getMRendererInfo();
						MapActivity.this.getMapView().getMapViewController()
								.setZoomLevel(17, true);
						switch (arg1) {
						case 0:
							log.debug("navigation mode vertical on");
							setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
							break;
						case 1:
							log.debug("navigation mode horizontal on");
							setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
							break;
						default:
							break;
						}
					} catch (Exception e) {
						log.error("onCheckedChanged", e);
					}
				}

			});
			AlertDialog.Builder alertCache = new AlertDialog.Builder(this);
			alertCache
					.setView(r)
					.setIcon(R.drawable.menu_navigation)
					.setTitle(R.string.Map_Navigator)
					.setPositiveButton(R.string.ok,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub

								}

							}).create();
			alertCache.show();
			r1.setChecked(true);
		} catch (Exception e) {
			log.error("", e);
		}
	}

	/**
	 * Shows an AlertDialog with the results from WeatherFunctionality
	 * 
	 * @param ws
	 *            The results from WeatherFunctionality
	 */
	public void showWeather(WeatherSet ws) {
		try {
			log.debug("showWeather");
			if (ws == null) {
				log.error("ws == null: Can't get weather. Check another location");
				Toast.makeText(this, R.string.Map_8, Toast.LENGTH_LONG).show();
				return;
			}
			if (ws.getWeatherCurrentCondition() == null) {
				hideIndeterminateDialog();
				AlertDialog.Builder alertW = new AlertDialog.Builder(this);
				alertW.setCancelable(true);
				alertW.setIcon(R.drawable.menu03);
				alertW.setTitle(R.string.error);
				if (ws.place == null || ws.place.compareTo("") == 0) {
					ws.place = this.getResources().getString(R.string.Map_9);
				}

				log.debug("The weather in " + ws.place + " is not available");
				alertW.setMessage(String.format(
						this.getResources().getString(R.string.Map_10),
						ws.place));

				alertW.setNegativeButton(R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
							}
						});
				alertW.show();
				return;
			}

			AlertDialog.Builder alertW = new AlertDialog.Builder(this);
			alertW.setCancelable(true);
			alertW.setIcon(R.drawable.menu03);
			alertW.setTitle(this.getResources().getString(R.string.Map_11)
					+ " " + ws.place);

			final ListView lv = new ListView(this);

			BulletedTextListAdapter adapter = new BulletedTextListAdapter(this);

			WeatherCurrentCondition wc = ws.getWeatherCurrentCondition();

			adapter.addItem(new BulletedText(new StringBuffer()
					.append(this.getResources().getString(R.string.Map_12))
					.append(" - ").append(wc.getTempCelcius()).append(" C")
					.append("\n").append(wc.getCondition()).append("\n")
					.append(wc.getWindCondition()).append("\n")
					.append(wc.getHumidity()).toString(), BulletedText
					.getRemoteImage(new URL("http://www.google.com"
							+ wc.getIconURL()))).setSelectable(false));

			ArrayList<WeatherForecastCondition> l = ws
					.getWeatherForecastConditions();

			WeatherForecastCondition temp;
			for (int i = 0; i < l.size(); i++) {
				try {
					temp = l.get(i);
					adapter.addItem(new BulletedText(new StringBuffer()
							.append(temp.getDayofWeek()).append(" - ")
							.append(temp.getTempMinCelsius()).append(" C")
							.append("/").append(temp.getTempMaxCelsius())
							.append(" C").append("\n")
							.append(temp.getCondition()).toString(),
							BulletedText
									.getRemoteImage(new URL(
											"http://www.google.com"
													+ temp.getIconURL())))
							.setSelectable(false));
				} catch (Exception e) {
					log.error("showWeather: ", e);
				}
			}

			lv.setAdapter(adapter);
			lv.setPadding(10, 0, 10, 0);

			alertW.setView(lv);

			alertW.setNegativeButton(
					this.getResources().getString(R.string.back),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
						}
					});

			alertW.show();
			hideIndeterminateDialog();
		} catch (Exception e) {
			log.error("showWeather: ", e);
		} finally {
		}
	}

	/**
	 * Launches the WeatherFunctionality
	 * 
	 * @param x
	 *            The x coordinate
	 * @param y
	 *            The y coordinate
	 * @param SRS
	 *            The SRS in which the coordinates are expressed
	 */
	public void getWeather(double x, double y, String SRS) {
		try {
			log.debug("Launching weather functionality");
			double[] coords = ConversionCoords.reproject(x, y,
					CRSFactory.getCRS(SRS), CRSFactory.getCRS("EPSG:4326"));

			// FIXME All this stuff can be extracted out of the activity. For
			// example we can have a manager to register functionalities
			// attached to a MenuItem id
			WeatherFunctionality w = new WeatherFunctionality(this, 0,
					coords[1], coords[0]);
			w.onClick(null);
			userContext.setUsedWeather(true);
			userContext.setLastExecWeather();
		} catch (Exception e) {
			log.error("getWeather: ", e);
		}
	}

	/**
	 * Shows an AlertDialog indicating that the route calculation has failed
	 */
	public void showRouteError() {
		try {
			log.debug("Show route error");
			hideIndeterminateDialog();
			AlertDialog.Builder alert = new AlertDialog.Builder(this);
			alert.setIcon(R.drawable.routes);
			alert.setTitle(R.string.error);
			alert.setMessage(R.string.Map_13);

			alert.setNegativeButton(R.string.ok,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
						}
					});

			alert.show();
		} catch (Exception e) {
			log.error("showRouteError: ", e);
		}
	}

	@Override
	public void onDestroy() {
		try {
			super.onDestroy();
			this.destroy();

		} catch (Exception e) {
			log.error("onDestroy: ", e);
		}
	}

	/**
	 * Frees memory
	 */
	@Override
	public void destroy() {
		try {
			log.debug("onDestroy map activity");

			try {
				log.debug("release wake lock");
				getMapView().setKeepScreenOn(false);
			} catch (Exception e) {
				log.error("release wake lock", e);

			}

			try {
				log.debug("persist map");
				getMapView().persist();
			} catch (Exception e) {
				log.error("Persist mapstate: ", e);
			}

			hideIndeterminateDialog();

			getMapView().clearCache();
			this.stopSensor();
			if (backpressed) {
				log.debug("back key pressed");
				// cosas a hacer para la limpieza

				// this.getGPSManager().stopLocationProviders();
				WorkQueue.getInstance().finalize();
				WorkQueue.getExclusiveInstance().finalize();
				WorkQueue.getPOIInstance().finalize();

				backpressed = false;

			}
			try {
				contextManager.saveContexts();
			} catch (Exception e) {
				log.error("onDestroy. contextManager.saveContexts(): ", e);
			}
			log.debug("destroy");
			getMapView().destroy();
			// route.destroy();
			// nameds.destroy();
			// osmap = null;
			// nameds = null;
		} catch (Exception e) {
			log.error("destroy", e);
		}
	}

	/**
	 * Load route stored on a Bundle when the configuration has changed or the
	 * Activity has been restarted
	 * 
	 * @param outState
	 *            The Bundle @see {@link #onSaveInstanceState(Bundle)}
	 */
	public void loadRoute(Bundle outState) {
		try {
			log.debug("load route from saved instance");
			boolean isDone = outState.getBoolean("isDone", true);

			double startPointX = outState.getDouble("StartPointX");
			double startPointY = outState.getDouble("StartPointY");
			double endPointX = outState.getDouble("EndPointX");
			double endPointY = outState.getDouble("EndPointY");
			Point starPoint = new Point(startPointX, startPointY);
			Point endPoint = new Point(endPointX, endPointY);

			if (starPoint.equals(endPoint)) {
				log.debug("start and end point equals. The route will not be loaded"
						+ "return");
			}

			RouteManager.getInstance().getRegisteredRoute()
					.setStartPoint(starPoint);
			RouteManager.getInstance().getRegisteredRoute()
					.setEndPoint(endPoint);
			if (!isDone) {
				log.debug("Route was calculating before saving instance: Relaunch it");
				this.launchRouteCalculation();
				// return;
			}
			log.debug("Loading route points");
			int routeSize = outState.getInt("RouteSize");
			double[] xCoords = new double[routeSize];
			double[] yCoords = new double[routeSize];
			for (int i = 0; i < routeSize; i++) {
				xCoords[i] = outState.getDouble("RX" + i);
				yCoords[i] = outState.getDouble("RY" + i);
			}
			LineString line = new LineString(xCoords, yCoords);
			FeatureCollection f = new FeatureCollection();
			f.addFeature(new Feature(line));
			RouteManager.getInstance().getRegisteredRoute().setRoute(f);
			log.debug("route loaded");
		} catch (Exception e) {
			log.error("loadRoute: ", e);
		}
	}

	/**
	 * Save route when the configuration has changed or the Activity has been
	 * restarted
	 * 
	 * @param outState
	 *            The Bundle @see {@link #onSaveInstanceState(Bundle)}
	 */
	public void saveRoute(Bundle outState) {
		try {
			Point routeStart = RouteManager.getInstance().getRegisteredRoute()
					.getStartPoint();
			Point routeEnd = RouteManager.getInstance().getRegisteredRoute()
					.getEndPoint();
			FeatureCollection r = RouteManager.getInstance()
					.getRegisteredRoute().getRoute();

			boolean isDone = true;
			try {
				Functionality yoursF = getItemContext()
						.getExecutingFunctionality();
				if (yoursF != null && yoursF instanceof YOURSFunctionality) {
					isDone = !yoursF.isActive();
				}
				outState.putBoolean("isDone", isDone);
			} catch (Exception e) {

			}
			outState.putDouble("StartPointX", routeStart.getX());
			outState.putDouble("StartPointY", routeStart.getY());
			outState.putDouble("EndPointX", routeEnd.getX());
			outState.putDouble("EndPointY", routeEnd.getY());

			boolean save = false;
			if (r != null && r.getSize() > 0) {
				Feature f = r.getFeatureAt(0);
				LineString l = (LineString) f.getGeometry();
				outState.putInt("RouteSize", l.getXCoords().length);
				for (int i = 0; i < l.getXCoords().length; i++) {
					save = true;
					outState.putDouble("RX" + i, l.getXCoords()[i]);
					outState.putDouble("RY" + i, l.getYCoords()[i]);
				}
			}

			log.debug("route saved");
		} catch (Exception e) {
			log.debug("savRoute: ", e);
		}
	}

	/**
	 * Load map info stored on a Bundle when the configuration has changed or
	 * the Activity has been restarted
	 * 
	 * @param outState
	 *            The Bundle @see {@link #onSaveInstanceState(Bundle)}
	 * @throws BaseException
	 */
	@Override
	public void loadMap(Bundle outState) throws BaseException {
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		try {
			log.debug("load map from saved instance");
			String mapLayer = outState.getString("maplayer");
			log.debug("previous layer: " + mapLayer);
			// OSMMercatorRenderer t = OSMMercatorRenderer.getMapnikRenderer();
			// this.osmap = new TileRaster(this, aContext, t,
			// metrics.widthPixels,
			// metrics.heightPixels);
			getMapView().onLayerChanged(mapLayer);
			getMapView().getLocationOverlay().loadState(outState);
			log.debug("map loaded");
		} catch (Exception e) {
			log.error("loadMap: ", e);
			OSMMercatorRenderer t = OSMMercatorRenderer.getMapnikRenderer();
			try {
				this.mapView = new MapView(this, this.getMapHandler(),
						CompatManager.getInstance().getRegisteredContext(), t,
						metrics.widthPixels, metrics.heightPixels);
				this.mapView.addControl(new PanControl());
				this.mapView.addControl(new DoubleTapZoom());
				this.mapView.addControl(new SelectionControl(this, mapView,
						"Feature selection control"));

				this.mapView.addMapViewListener(this);

				addAcetate();
				// osmap.initializeCanvas(metrics.widthPixels,
				// metrics.heightPixels);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		} finally {
			try {
				if (outState == null)
					return;
				String contextName = outState.getString("contextClassName");
				log.debug("loading previous context: " + contextName);

				ItemContext context = (ItemContext) Class.forName(contextName)
						.newInstance();
				if (context != null) {
					context.setMap(this);
					this.setContext(context);
				}
			} catch (IllegalAccessException e) {
				log.error("", e);
			} catch (InstantiationException e) {
				log.error("", e);
			} catch (ClassNotFoundException e) {
				log.error("", e);
			} catch (Exception e) {
				log.error("", e);
			}
		}
	}

	/**
	 * Save settings when the configuration has changed or the Activity has been
	 * restarted
	 * 
	 * @param outState
	 *            The Bundle @see {@link #onSaveInstanceState(Bundle)}
	 */
	@Override
	public void saveSettings(Bundle outState) {
		try {
			// outState.putFloat("dataTransfer", this.datatransfer2);
			outState.putBoolean("dataNavigation", getMapView()
					.isNavigationMode());
			outState.putBoolean("dataRecent",
					this.isRecenterOnGPSLocationActive());
		} catch (Exception e) {
			log.error("saveSettings: ", e);
		}
	}

	/**
	 * Load settings stored on a Bundle when the configuration has changed or
	 * the Activity has been restarted
	 * 
	 * @param outState
	 *            The Bundle @see {@link #onSaveInstanceState(Bundle)}
	 */
	@Override
	public void loadSettings(Bundle outState) {
		try {
			if (outState == null)
				return;
			// this.datatransfer2 = outState.getFloat("dataTransfer");
			getMapView().setNavigationMode(
					outState.getBoolean("dataNavigation"));
			this.setRecenterOnGPSLocationActive(outState
					.getBoolean("dataRecent"));
			if (getMapView().isNavigationMode()
					&& isRecenterOnGPSLocationActive())
				getMapView().setKeepScreenOn(true);
		} catch (Exception e) {
			log.error("loadSettings: ", e);
		}
	}

	/**
	 * Save map info (current layer, GPS location, ItemContext...) when the
	 * configuration has changed or the Activity has been restarted
	 * 
	 * @param outState
	 *            The Bundle @see {@link #onSaveInstanceState(Bundle)}
	 */
	@Override
	public void saveMap(Bundle outState) {
		try {
			log.debug("save map to bundle");
			outState.putString("maplayer", getMapView().getMRendererInfo()
					.getFullNAME());
			getMapView().getLocationOverlay().saveState(outState);
			ItemContext context = this.getItemContext();
			if (context != null)
				outState.putString("contextClassName", context.getClass()
						.getName());
			else
				outState.putString("contextClassName",
						DefaultContext.class.getName());
			log.debug("map saved");
		} catch (Exception e) {
			log.debug("saveMap: ", e);
		}
	}

	/**
	 * Synchronize zoomcontrols with TileRaster.MapRenderer zoom level
	 */
	@Override
	public void zoomLevelChanged() {
		try {
			MapRenderer r = getMapView().getMRendererInfo();

			if (r.getZoomLevel() > r.getZoomMinLevel())
				zoomControls.setIsZoomOutEnabled(true);
			else
				zoomControls.setIsZoomOutEnabled(false);

			if (r.getZOOM_MAXLEVEL() > r.getZoomLevel()) {
				zoomControls.setIsZoomInEnabled(true);
			} else {
				zoomControls.setIsZoomInEnabled(false);
			}
		} catch (Exception e) {
			log.error("updateZoomControl: ", e);
		}
	}

	@Override
	public void addLocationOverlay() {
		this.getMapView().setLocationOverlay(
				new ViewSimpleLocationOverlay(this, getMapView(),
						ViewSimpleLocationOverlay.DEFAULT_NAME));
	}

	/**
	 * Instantiates the UI: TileRaster, ZoomControls, SlideBar in a
	 * RelativeLayout
	 * 
	 * @param savedInstanceState
	 */
	@Override
	public void loadUI(Bundle savedInstanceState) {
		try {
			log.debug("load UI");
			final LayoutInflater factory = LayoutInflater.from(this);
			relativeLayout.addView((View) this.getMapView(),
					new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,
							LayoutParams.FILL_PARENT));
			zoomControls = new ZoomControls(this);
			final TextView l = new TextView(this);
			final RelativeLayout.LayoutParams sParams = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.FILL_PARENT,
					RelativeLayout.LayoutParams.FILL_PARENT);
			//
			relativeLayout.addView(l, sParams);

			/* Creating the main Overlay */
			{
				this.getMapView().addOverlay(
						new RouteOverlay(this, getMapView(),
								RouteOverlay.DEFAULT_NAME));
				addLocationOverlay();

			}

			final RelativeLayout.LayoutParams zzParams = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.WRAP_CONTENT,
					RelativeLayout.LayoutParams.WRAP_CONTENT);
			zzParams.addRule(RelativeLayout.ALIGN_BOTTOM);
			zzParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			relativeLayout.addView(zoomControls, zzParams);
			zoomControls.setId(107);
			zoomControls.setVisibility(View.VISIBLE);

			zoomControls.setOnZoomInClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					try {
						MapActivity.this.getMapView().getMapViewController()
								.zoomIn();

					} catch (Exception e) {
						log.error("onZoomInClick: ", e);
					}
				}
			});
			zoomControls.setOnZoomOutClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					try {
						MapActivity.this.getMapView().getMapViewController()
								.zoomOut();
						// Map.this.osmap.switchPanMode();
					} catch (Exception e) {
						log.error("onZoomOutClick: ", e);
					}
				}
			});

			log.debug("ui loaded");
		} catch (Exception e) {
			log.error("", e);
		} catch (OutOfMemoryError ou) {
			System.gc();
			log.error("", ou);
		}
	}

	/**
	 * Loads the last map center and zoom level stored on a Bundle when the
	 * configuration has changed or the Activity has been restarted
	 * 
	 * @param outState
	 *            The Bundle @see {@link #onSaveInstanceState(Bundle)}
	 */
	@Override
	public void loadCenter(Bundle savedInstanceState) {
		try {
			log.debug("load center from saved instance");
			double lat = savedInstanceState.getDouble("lat");
			double longit = savedInstanceState.getDouble("longit");
			int zoomlvl = savedInstanceState.getInt("zoomlvl");
			log.debug("lat, lon, zoom: " + lat + ", " + longit + ", " + zoomlvl);

			this.getMapView().getMapViewController().setMapCenter(longit, lat);
			this.getMapView().getMapViewController().setZoomLevel(zoomlvl);
			log.debug("lat, lon, zoom: " + longit + ", " + lat + ", " + zoomlvl);
		} catch (Exception e) {
			log.error("loadCenter: ", e);
			getMapView().getMRendererInfo().centerOnBBox();
		}
	}

	/**
	 * Saves the map center and zoom level on a Bundle when the configuration
	 * has changed or the Activity has been restarted
	 * 
	 * @param outState
	 *            The Bundle @see {@link #onSaveInstanceState(Bundle)}
	 */
	@Override
	public void saveCenter(Bundle outState) {
		try {
			log.debug("save center to bundle");
			final Point center = this.getMapView().getMRendererInfo()
					.getCenter();
			final int zoomLevel = this.getMapView().getMRendererInfo()
					.getZoomLevel();
			outState.putDouble("lat", center.getY());
			outState.putDouble("longit", center.getX());
			outState.putInt("zoomlvl", zoomLevel);
			log.debug("lat, lon, zoom: " + center.getY() + ", " + center.getX()
					+ ", " + zoomLevel);
		} catch (Exception e) {
			log.error("saveCenter: ", e);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		try {
			log.debug("onSaveInstanceState");
			outState.putBoolean("isSaved", true);
			super.onSaveInstanceState(outState);
			saveSettings(outState);
			saveMap(outState);
		} catch (Exception e) {
			log.error("onSaveInstanceState: ", e);
		}
		try {
			saveCenter(outState);
		} catch (Exception e) {
			log.error("", e);
		}
		try {
			saveRoute(outState);
		} catch (Exception e) {
			log.error("", e);
		}
	}

	/**
	 * Shows an AlertDialog to the user to input the query string for NameFinder
	 * 
	 * @deprecated There should be a single search dialog
	 * 
	 */
	public void showPOIDialog() {
		// FIXME Take a look to this when integrate POIProxy
		try {
			log.debug("showPOIDialog");
			AlertDialog.Builder alertPOI = new AlertDialog.Builder(this);

			alertPOI.setIcon(R.drawable.poismenu);
			alertPOI.setTitle(R.string.Map_21);

			final EditText inputPOI = new EditText(this);

			alertPOI.setView(inputPOI);

			alertPOI.setPositiveButton(R.string.search,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							try {
								Editable value = inputPOI.getText();
								// Call to NameFinder with the text
								searchInNameFinder(value.toString(), true);

							} catch (Exception e) {
								log.error("clickNameFinder: ", e);
							}
							return;
						}
					});

			alertPOI.setNegativeButton(R.string.cancel,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
						}
					});

			alertPOI.show();
		} catch (Exception e) {
			log.error("", e);
		}
	}

	/**
	 * Show an AlertDialog to the user to input the query string for NameFinder
	 * addresses
	 */
	@Override
	public void showSearchDialog() {
		try {
			// this.onSearchRequested();
			log.debug("show address dialog");
			AlertDialog.Builder alert = new AlertDialog.Builder(this);

			alert.setIcon(R.drawable.menu00);
			alert.setTitle(R.string.Map_3);
			final EditText input = new EditText(this);
			alert.setView(input);

			alert.setPositiveButton(R.string.alert_dialog_text_search,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							try {
								Editable value = input.getText();
								// Call to NameFinder with the text
								searchInNameFinder(value.toString(), false);

							} catch (Exception e) {
								log.error("clickNameFinderAddress: ", e);
							}
							return;
						}
					});

			alert.setNegativeButton(R.string.alert_dialog_text_cancel,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
						}
					});

			alert.show();

		} catch (Exception e) {
			log.error("", e);
		}
	}

	/*
	 * Perform a search for all types of different searches (POI, address,
	 * search manager,...) using the NameFinder consumer It acts as a fa�ade
	 * for all searches launched from Map activity to be resolved by the
	 * NameFinder query: text to be sought
	 */
	private void searchInNameFinder(String query, boolean nearOfCenter) {
		try {
			if (!query.trim().equals("")) {
				PlaceSearcher search;

				if (!nearOfCenter) {
					search = new PlaceSearcher(this, query);
				} else {
					double[] center = getMapView().getCenterLonLat();
					search = new PlaceSearcher(this, query, center[0],
							center[1]);
				}
			}
		} catch (Exception e) {
			log.error("searchWithNameFinder: ", e);
		}
		return;

	}

	@Override
	public void defaultContextToShow(double x, double y) {
		getMapView().getMapViewController().animateTo(x, y);
		showContext(getItemContext());
	}

	@Override
	public void exclusiveControlChanged() {
		getActionbar().refreshSelectableActions();
	}
}
