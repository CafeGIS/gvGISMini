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

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;
import es.prodevelop.gvsig.mini.R;
import es.prodevelop.gvsig.mini.tasks.Functionality;
import es.prodevelop.gvsig.mini.tasks.TaskHandler;
import es.prodevelop.gvsig.mini.tasks.weather.WeatherFunctionality;
import es.prodevelop.gvsig.mini.yours.RouteManager;

/**
 * This class Handles messages from Functionalities
 * 
 * @author aromeu
 * @author rblanco
 * 
 */
public class MapHandler extends Handler {

	private Logger log = LoggerFactory.getLogger(MapHandler.class);

	private IMapActivity mapActivity;

	public MapHandler(IMapActivity mapActivity) {
		this.mapActivity = mapActivity;
	}

	@Override
	public void handleMessage(final Message msg) {
		try {
			final int what = msg.what;
			switch (what) {
			// case IMapActivity.SHOW_INDETERMINATE_DIALOG:
			// this.mapActivity.showIndeterminateDialog();
			// break;
			case IMapActivity.HIDE_INDETERMINATE_DIALOG:
				this.mapActivity.hideIndeterminateDialog();
				break;
			case TaskHandler.NO_RESPONSE:
				this.mapActivity.hideIndeterminateDialog();
				Toast.makeText((Activity) this.mapActivity, R.string.Map_22,
						Toast.LENGTH_LONG).show();
				break;
			case IMapActivity.SHOW_TOAST:
				Toast t = Toast.makeText((Activity) this.mapActivity,
						msg.obj.toString(), Toast.LENGTH_LONG);
				t.show();
				this.mapActivity.hideIndeterminateDialog();
				break;
			case IMapActivity.SHOW_OK_DIALOG:
				this.mapActivity.hideIndeterminateDialog();
				this.mapActivity.showOKDialog(msg.obj.toString(),
						R.string.getFeatureInfo, true);
				break;
			// case IMapActivity.POI_LIST:
			// log.log(Level.FINE, "POI_LIST");
			// IMapActivity.this.viewLastPOIs();
			// break;
			// case IMapActivity.POI_CLEARED:
			// log.debug("POI_CLEARED");
			// mapActivity.updateContext(IMapActivity.POI_CLEARED);
			// break;
			// case IMapActivity.ROUTE_CLEARED:
			// log.log(Level.FINE, "ROUTE_CLEARED");
			// IMapActivity.this.updateContext(IMapActivity.ROUTE_CLEARED);
			// break;
			case IMapActivity.WEATHER_INITED:
				log.debug("WEATHER_INITED");
				mapActivity.showIndeterminateDialog(R.string.please_wait,
						R.string.Map_14, R.drawable.menu03,
						new OnCancelListener() {
							@Override
							public void onCancel(DialogInterface dialog2) {
								try {
									log.debug("weather canceled");
									mapActivity.getItemContext()
											.cancelCurrentTask();
									mapActivity.hideIndeterminateDialog();
									dialog2.dismiss();
								} catch (Exception e) {
									log.error("onCancelDialog: ", e);
								}
							}
						});

				break;
			case IMapActivity.GETFEATURE_INITED:
				log.debug("GETFEATURE_INITED");
				mapActivity.showIndeterminateDialog(R.string.please_wait,
						R.string.please_wait, R.drawable.menu03,
						new OnCancelListener() {
							@Override
							public void onCancel(DialogInterface dialog2) {
								try {
									log.debug("getFeature canceled");
									mapActivity.getItemContext()
											.cancelCurrentTask();
									mapActivity.hideIndeterminateDialog();
								} catch (Exception e) {
									log.error("onCancelDialog: ", e);
								}
							}
						});
				break;

			case IMapActivity.WEATHER_CANCELED:
				log.debug("WEATHER_CANCELED");
				Toast.makeText((Activity) mapActivity, R.string.Map_15,
						Toast.LENGTH_LONG).show();
				break;
			case IMapActivity.WEATHER_ERROR:
				log.debug("WEATHER_ERROR");
				Toast.makeText((Activity) mapActivity, R.string.Map_8,
						Toast.LENGTH_LONG).show();
				mapActivity.hideIndeterminateDialog();
				break;
			case IMapActivity.WEATHER_SHOW:
				log.debug("WEATHER_SHOW");
				Functionality f = mapActivity.getItemContext()
						.getExecutingFunctionality();
				if (f instanceof WeatherFunctionality)
					mapActivity.showWeather(((WeatherFunctionality) f).ws);
				else {
					log.error("Nof found Weather functionality");
				}
				break;
			case IMapActivity.ROUTE_NO_RESPONSE:
				log.debug("ROUTE_NO_RESPONSE");
				Toast.makeText((Activity) mapActivity, R.string.server_busy,
						Toast.LENGTH_LONG).show();
				mapActivity.hideIndeterminateDialog();
				break;
			case IMapActivity.ROUTE_SUCCEEDED:
				log.debug("ROUTE_SUCCEEDED");
				((MapView) mapActivity.getMapView()).CLEAR_ROUTE = true;
				mapActivity.hideIndeterminateDialog();
				mapActivity
						.getMapView()
						.getMRendererInfo()
						.reprojectGeometryCoordinates(
								RouteManager.getInstance().getRegisteredRoute()
										.getRoute().getFeatureAt(0)
										.getGeometry(), "EPSG:4326");
				mapActivity.updateContext(IMapActivity.ROUTE_SUCCEEDED);
				mapActivity.getMapView().resumeDraw();
				break;
			case IMapActivity.ROUTE_NO_CALCULATED:
				log.debug("ROUTE_NO_CALCULATED");
				Toast.makeText((Activity) mapActivity, R.string.Map_16,
						Toast.LENGTH_LONG).show();
				mapActivity.hideIndeterminateDialog();
				break;
			// case IMapActivity.POI_SHOW:
			// log.debug("POI_SHOW");
			// Functionality nf =
			// mapActivity.getItemContext().getExecutingFunctionality();
			// if (nf instanceof NameFinderFunc) {
			// NameFinderFunc n = (NameFinderFunc) nf;
			// mapActivity.getMapView().getMRendererInfo().reprojectGeometryCoordinates(n.nm,
			// "EPSG:4326");
			// boolean update = IMapActivity.this.showPOIs(n.desc, n.nm);
			// if (update)
			// IMapActivity.this
			// .updateContext(IMapActivity.POI_SUCCEEDED);
			// } else {
			// log.log(Level.FINE, "Nof found NameFinder functionality");
			// }
			// break;
			// case IMapActivity.POI_INITED:
			// log.log(Level.FINE, "POI_INITED");
			// dialog2 = ProgressDialog.show(
			// IMapActivity.this,
			// IMapActivity.this.getResources().getString(
			// R.string.please_wait), IMapActivity.this
			// .getResources().getString(R.string.Map_17),
			// true);
			// dialog2.setCancelable(true);
			// dialog2.setCanceledOnTouchOutside(true);
			// dialog2.setIcon(R.drawable.pois);
			// dialog2.setOnCancelListener(new OnCancelListener() {
			// @Override
			// public void onCancel(DialogInterface dialog2) {
			// try {
			// IMapActivity.this.getItemContext()
			// .cancelCurrentTask();
			// dialog2.dismiss();
			// } catch (Exception e) {
			// log.log(Level.SEVERE, "onCancelDialog: ", e);
			// }
			// }
			// });
			// break;
			case IMapActivity.ROUTE_CANCELED:
				log.debug("ROUTE_CANCELED");
				RouteManager.getInstance().getRegisteredRoute()
						.deleteRoute(false);
				// route.deleteEndPoint();
				// route.deleteStartPoint();
				// yoursFunc.cancel();
				mapActivity.hideIndeterminateDialog();
				// ivCleanRoute.setVisibility(View.INVISIBLE);
				Toast.makeText((Activity) mapActivity, R.string.Map_18,
						Toast.LENGTH_LONG).show();
				mapActivity.getMapView().postInvalidate();
				break;
			// case IMapActivity.POI_CANCELED:
			// log.debug("POI_CANCELED");
			// // ivCleanPois.setVisibility(View.INVISIBLE);
			// // ivShowList.setVisibility(View.INVISIBLE);
			// mapActivity.hideIndeterminateDialog()
			// Toast.makeText(IMapActivity.this, R.string.task_canceled,
			// Toast.LENGTH_LONG).show();
			// nameds = null;
			// osmap.postInvalidate();
			// break;
			case IMapActivity.CALCULATE_ROUTE:
				log.debug("CALCULATE_ROUTE");
				mapActivity.calculateRoute();
				break;
			case IMapActivity.ROUTE_INITED:
				log.debug("ROUTE_INITED");
				RouteManager.getInstance().getRegisteredRoute()
						.deleteRoute(false);
				mapActivity.showIndeterminateDialog(R.string.please_wait,
						R.string.Map_19, R.drawable.routes,
						new OnCancelListener() {
							@Override
							public void onCancel(DialogInterface dialog2) {
								try {
									RouteManager.getInstance()
											.getRegisteredRoute()
											.deleteRoute(false);
									// ivCleanRoute.setVisibility(View.INVISIBLE);
									mapActivity.getMapView().resumeDraw();
									mapActivity.getItemContext()
											.cancelCurrentTask();
								} catch (Exception e) {
									log.error("onCancelDialog: ", e);
								}
							}
						});
				break;
			case IMapActivity.TWEET_SENT:
				log.debug("TWEET_SENT");
				Toast.makeText(((Activity) mapActivity), R.string.Map_20,
						Toast.LENGTH_LONG).show();
				break;
			case IMapActivity.TWEET_ERROR:
				log.debug("TWEET_ERROR");
				Toast t1 = Toast.makeText(((Activity) mapActivity),
						msg.obj.toString(), Toast.LENGTH_LONG);
				t1.show();
				break;
			// case IMapActivity.SHOW_TWEET_DIALOG:
			// log.debug("SHOW_TWEET_DIALOG");
			// IMapActivity.this.showTweetDialog();
			// break;
			// case IMapActivity.SHOW_TWEET_DIALOG_SETTINGS:
			// log.log(Level.FINE, "SHOW_TWEET_DIALOG_SETTINGS");
			// IMapActivity.this.showTweetDialogSettings();
			// break;
			// case IMapActivity.SHOW_POI_DIALOG:
			// log.log(Level.FINE, "SHOW_POI_DIALOG");
			// IMapActivity.this.showPOIDialog();
			// break;
			// case IMapActivity.SHOW_ADDRESS_DIALOG:
			// log.log(Level.FINE, "SHOW_ADDRESS_DIALOG");
			// IMapActivity.this.showSearchDialog();
			// break;
			case IMapActivity.VOID:
				mapActivity.hideIndeterminateDialog();
				break;
			}
		} catch (Exception e) {
			log.error("handleMessage: ", e);
			mapActivity.hideIndeterminateDialog();
		} finally {
			try {
				mapActivity.clearContext();
				mapActivity.getMapView().invalidate();
			} catch (Exception e) {
				log.error("", e);
			}
		}
	}
}