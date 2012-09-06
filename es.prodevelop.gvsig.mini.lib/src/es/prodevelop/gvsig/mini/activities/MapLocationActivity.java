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

package es.prodevelop.gvsig.mini.activities;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.TextView;
import es.prodevelop.gvsig.mini.R;
import es.prodevelop.gvsig.mini.geom.android.GPSPoint;
import es.prodevelop.gvsig.mini.location.Config;
import es.prodevelop.gvsig.mini.location.LocationHandler;
import es.prodevelop.gvsig.mini.location.LocationTimer;
import es.prodevelop.gvsig.mini.map.IMapLocation;
import es.prodevelop.gvsig.mini.util.Utils;

public abstract class MapLocationActivity extends AboutActivity implements
		IMapLocation {

	private Logger log = LoggerFactory.getLogger(MapLocationActivity.class);

	protected static final String PROVIDER_NAME = LocationManager.GPS_PROVIDER;

	private LocationHandler locationHandler;
	private LocationManager mLocationManager;
	private boolean isLocationHandlerEnabled = false;

	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			Config.setContext(this);

			locationHandler = new LocationHandler(
					(LocationManager) getSystemService(Context.LOCATION_SERVICE),
					this, this);
			initLocation();
			locationHandler
					.setLocationTimer(new LocationTimer(locationHandler));
			this.initializeSensor();
		} catch (Exception e) {
			log.error("onCreate MapLocationActivity", e);
		}
	}

	@Override
	protected void onPause() {
		try {
			log.debug("onPause MapLocation");
			super.onPause();
			this.stopSensor();
			disableGPS();
		} catch (Exception e) {
			log.error("onPause MapLocationActivity", e);
		}
	}

	@Override
	public void onResume() {
		try {
			log.debug("on resume MapLocationActivity");
			super.onResume();
			this.initializeSensor();
			enableGPS();
			// manager.startLocationProviders();
		} catch (Exception e) {
			log.error("onResume MapLocationActivity", e);
		}
	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void stopSensor() {
		try {
			log.debug("stop sensor");
			SensorManager mSensorManager = (SensorManager) this
					.getSystemService(Context.SENSOR_SERVICE);
			mSensorManager.unregisterListener(this);
		} catch (Exception e) {
			log.error("stopSensor", e);
		}
	}

	@Override
	public void startGPS() {
		try {
			this.initLocation();
			locationHandler
					.setLocationTimer(new LocationTimer(locationHandler));
		} catch (Exception e) {
			log.error("startGPS", e);
		}
	}

	@Override
	public void stopGPS() {
		try {
			this.locationHandler.stop();
			this.setLocationHandlerEnabled(false);
		} catch (Exception e) {
			log.error("stopGPS", e);
		}
	}

	@Override
	public LocationHandler getLocationHandler() {
		return this.locationHandler;
	}

	@Override
	public LocationManager getLocationManager() {
		if (this.mLocationManager == null)
			this.mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		return this.mLocationManager;
	}

	@Override
	public void initializeSensor() {
		this.initializeSensor(false);
	}

	@Override
	public void initializeSensor(boolean forceNavigationMode) {
		try {
			try {
				if (!Settings.getInstance().getBooleanValue(
						getText(R.string.settings_key_orientation).toString())
						&& !forceNavigationMode) {
					log.debug("Orientation is disabled in settings");
					return;
				}
			} catch (NoSuchFieldError e) {
				log.error("initializeSensor", e);
				return;
			}

			log.debug("initialize sensor");
			SensorManager mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
			mSensorManager.registerListener(this,
					mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
					SensorManager.SENSOR_DELAY_NORMAL);
		} catch (Exception e) {
			log.error("initializeSensor MapLocationAcitivity", e);
		}
	}

	@Override
	public void initLocation() {
		try {
			try {
				if (!Settings.getInstance().getBooleanValue(
						getText(R.string.settings_key_gps).toString())) {
					log.debug("GPS is disabled in settings");
					return;
				}
			} catch (NoSuchFieldError e) {
				log.error("initLocation", e);
				return;
			}
			List providers = this.getLocationManager().getProviders(true);
			if (providers.size() == 0) {
				log.warn("no location providers enabled");
				this.showLocationSourceDialog();
			} else {
				log.debug("location handler start");
				locationHandler.start();
				this.setLocationHandlerEnabled(true);
				// manager.initLocation();
			}
		} catch (Exception e) {
			log.error("initLocation", e);
		}
	}

	@Override
	public void showLocationSourceDialog() {
		try {
			AlertDialog.Builder alert = new AlertDialog.Builder(this);

			// alert.setIcon(R.drawable.menu00);
			alert.setTitle(R.string.MapLocation_0);
			TextView text = new TextView(this);
			text.setText(R.string.MapLocation_1);

			alert.setView(text);

			alert.setPositiveButton(R.string.ok,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							try {
								startActivity(new Intent(
										"android.settings.LOCATION_SOURCE_SETTINGS"));
							} catch (Exception e) {
								log.error("showLocationSourceDialog", e);
							}
						}
					});

			alert.setNegativeButton(R.string.cancel,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
						}
					});

			alert.show();
		} catch (Exception e) {
			log.error("showLocationSourceDialog", e);
		}
	}

	@Override
	public GPSPoint getLastLocation() {
		try {
			return Utils.locationToGeoPoint(this.mLocationManager
					.getLastKnownLocation(PROVIDER_NAME));
		} catch (Exception e) {
			log.error("getLastLocation", e);
			return null;
		}
	}

	/**
	 * Unregisters LocationListener.
	 */
	@Override
	public void onDestroy() {
		try {
			super.onDestroy();
			log.debug("on destroy MapLocation");
			locationHandler.stop();
		} catch (Exception e) {
			log.error("onDestroy", e);
		}
	}

	@Override
	public void enableGPS() {
		try {
			this.initLocation();
			locationHandler
					.setLocationTimer(new LocationTimer(locationHandler));
		} catch (Exception e) {
			log.error("enableGPS", e);
		}
	}

	@Override
	public void disableGPS() {
		try {
			this.locationHandler.stop();
			this.setLocationHandlerEnabled(false);
		} catch (Exception e) {
			log.error("disableGPS", e);
		}
	}

	@Override
	public void setLocationHandlerEnabled(boolean isLocationHandlerEnabled) {
		this.isLocationHandlerEnabled = isLocationHandlerEnabled;
	}

	@Override
	public boolean isLocationHandlerEnabled() {
		return isLocationHandlerEnabled;
	}

}
