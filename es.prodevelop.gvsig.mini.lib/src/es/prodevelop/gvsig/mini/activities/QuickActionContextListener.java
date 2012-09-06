package es.prodevelop.gvsig.mini.activities;

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
 */

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import es.prodevelop.gvsig.mini.R;
import es.prodevelop.gvsig.mini.activities.NameFinderActivity.BulletedTextListAdapter;
import es.prodevelop.gvsig.mini.geom.Point;
import es.prodevelop.gvsig.mini.geom.impl.jts.JTSFeature;
import es.prodevelop.gvsig.mini.tasks.poi.CenterPOIOnMap;
import es.prodevelop.gvsig.mini.util.ActionItem;
import es.prodevelop.gvsig.mini.util.QuickAction;
import es.prodevelop.gvsig.mini.utiles.Tags;
import es.prodevelop.gvsig.mini.yours.Route;
import es.prodevelop.gvsig.mini.yours.RouteManager;

//FIXME Implement this. use ItemContext to fill the QuickAction
public class QuickActionContextListener {

	protected Activity activity;
	int iconId;
	int titleId;
	Drawable icon;
	protected AlertDialog dialog;
	BulletedTextListAdapter adapter;
	private List<QuickActionObserver> observers = new ArrayList<QuickActionObserver>();

	public QuickActionContextListener(Activity activity, int iconId, int titleId) {
		this.activity = activity;
		this.iconId = iconId;
		this.titleId = titleId;
	}

	public QuickActionContextListener(SearchActivity activity, Drawable icon,
			int titleId) {
		this.activity = activity;
		this.icon = icon;
		this.titleId = titleId;
	}

	public OnClickListener getCenterOnMapListener(final JTSFeature p,
			final QuickAction qa) {
		return new OnClickListener() {
			@Override
			public void onClick(View v) {
				// SHOW ON MAP
				try {
					com.vividsolutions.jts.geom.Point pj = p.getGeometry()
							.getGeometry().getCentroid();

					CenterPOIOnMap centerOnMap = new CenterPOIOnMap(null, 0,
							new Point(pj.getX(), pj.getY()), activity,
							MapActivity.class);
					centerOnMap.execute();
					qa.dismiss();
				} catch (Exception e) {
					Log.e("", "Exception on show map", e);
				}
			}
		};
	}

	public void addObserver(QuickActionObserver observer) {
		this.observers.add(observer);
	}

	public void removeObserver(QuickActionObserver observer) {
		this.observers.remove(observer);
	}

	public void notifyObservers(QuickActionEvent event) {
		for (QuickActionObserver observer : observers) {
			observer.onQuickActionExecuted(event);
		}
	}

	public boolean onClick(int position, final Object p, final View anchor) {
		try {
			final QuickAction qa = new QuickAction(anchor);

			final ActionItem showMapItem = new ActionItem();

			showMapItem.setTitle(activity.getResources().getString(
					R.string.show_map));
			showMapItem.setIcon(activity.getResources().getDrawable(
					R.drawable.bt_poi_s));
			showMapItem.setOnClickListener(getCenterOnMapListener(
					(JTSFeature) p, qa));

			qa.addActionItem(showMapItem);

			// final ActionItem startRouteItem = new ActionItem();
			//
			// startRouteItem.setTitle(activity.getResources().getString(
			// R.string.NameFinderActivity_1));
			// startRouteItem.setIcon(activity.getResources().getDrawable(
			// R.drawable.bt_start_s));
			// startRouteItem.setOnClickListener(new OnClickListener() {
			// @Override
			// public void onClick(View v) {
			// try {
			// RouteManager.getInstance().getRegisteredRoute()
			// .setStartPoint(p);
			// Toast.makeText(
			// activity,
			// activity.getResources().getString(
			// R.string.start_point),
			// Toast.LENGTH_LONG).show();
			// checkCalculateRoute();
			// qa.dismiss();
			// } catch (Exception e) {
			// Log.e("", "exception on start route");
			// }
			// }
			// });
			//
			// qa.addActionItem(startRouteItem);
			//
			// final ActionItem finishRouteItem = new ActionItem();
			//
			// finishRouteItem.setTitle(activity.getResources().getString(
			// R.string.NameFinderActivity_2));
			// finishRouteItem.setIcon(activity.getResources().getDrawable(
			// R.drawable.bt_finish_s));
			// finishRouteItem.setOnClickListener(new OnClickListener() {
			// @Override
			// public void onClick(View v) {
			// try {
			// // TO HERE
			// RouteManager.getInstance().getRegisteredRoute()
			// .setEndPoint(p);
			// Toast.makeText(
			// activity,
			// activity.getResources().getString(
			// R.string.end_point), Toast.LENGTH_LONG)
			// .show();
			// checkCalculateRoute();
			// qa.dismiss();
			// } catch (Exception e) {
			// Log.e("", "Exceptoin on finish route");
			// }
			// }
			// });
			//
			// qa.addActionItem(finishRouteItem);
			//
			// final ActionItem shareItem = new ActionItem();
			//
			// shareItem.setTitle(activity.getResources()
			// .getString(R.string.share));
			// shareItem.setIcon(activity.getResources().getDrawable(
			// R.drawable.bt_shareicon_s));
			// shareItem.setOnClickListener(new OnClickListener() {
			// @Override
			// public void onClick(View v) {
			// try {
			// // GEO SHARE
			// ShareAnyPOITask sh = new ShareAnyPOITask(activity, p);
			// sh.execute();
			// qa.dismiss();
			// } catch (Exception e) {
			// Log.e("", "Error on share");
			// }
			// }
			// });
			//
			// qa.addActionItem(shareItem);
			//
			// addBookmark(p, qa);
			//
			// if (findNear) {
			//
			// final ActionItem poisNearItem = new ActionItem();
			//
			// poisNearItem.setTitle(activity.getResources().getString(
			// R.string.find_pois_near));
			// poisNearItem.setIcon(activity.getResources().getDrawable(
			// R.drawable.bt_around_poi_s));
			// poisNearItem.setOnClickListener(new OnClickListener() {
			// @Override
			// public void onClick(View v) {
			// try {
			// // FIND POIS NEAR
			// InvokePOIIntents.findPOISNear(activity,
			// p.toShortString(6));
			// qa.dismiss();
			// } catch (Exception e) {
			// Log.e("", "Error on poisNear");
			// }
			// }
			// });
			//
			// qa.addActionItem(poisNearItem);
			//
			// final ActionItem streetsNearItem = new ActionItem();
			//
			// streetsNearItem.setTitle(activity.getResources().getString(
			// R.string.find_streets_near));
			// streetsNearItem.setIcon(activity.getResources().getDrawable(
			// R.drawable.bt_around_adress_s));
			// streetsNearItem.setOnClickListener(new OnClickListener() {
			// @Override
			// public void onClick(View v) {
			// try {
			// // FIND STREETS
			// InvokePOIIntents.findStreetsNear(activity,
			// p.toShortString(2));
			// qa.dismiss();
			// } catch (Exception e) {
			// Log.e("", "Error on streets near");
			// }
			// }
			// });
			//
			// qa.addActionItem(streetsNearItem);
			// }
			//
			// final ActionItem streetviewItem = new ActionItem();
			//
			// streetviewItem.setTitle(activity.getResources().getString(
			// R.string.street_view));
			// streetviewItem.setIcon(activity.getResources().getDrawable(
			// R.drawable.bt_streetview_s));
			// streetviewItem.setOnClickListener(new OnClickListener() {
			// @Override
			// public void onClick(View v) {
			// try {
			// // STREET VIEW
			// ShowStreetViewFromPoint s = new ShowStreetViewFromPoint(
			// activity, p);
			// s.execute();
			// qa.dismiss();
			// } catch (Exception e) {
			// Log.e("", "Error on show street view");
			// }
			// }
			// });
			//
			// qa.addActionItem(streetviewItem);
			//
			// if (p instanceof OsmPOI) {
			// final OsmPOI poi = (OsmPOI) p;
			// if (poi.getPhone() != null && poi.getPhone().length() > 0) {
			// final ActionItem callItem = new ActionItem();
			//
			// callItem.setTitle(activity.getResources().getString(
			// R.string.call_number)
			// + " [" + poi.getPhone() + "]");
			// callItem.setIcon(activity.getResources().getDrawable(
			// R.drawable.bt_call_s));
			// callItem.setOnClickListener(new OnClickListener() {
			// @Override
			// public void onClick(View v) {
			// try {
			// InvokeIntents.invokeCallActivity(activity,
			// poi.getPhone());
			// qa.dismiss();
			// } catch (Exception e) {
			// Log.e("", "Error on call");
			// }
			// }
			// });
			//
			// qa.addActionItem(callItem);
			// }
			//
			// // WIKI
			// if (poi.getWikipedia() != null
			// && poi.getWikipedia().length() > 0) {
			// final ActionItem wikiItem = new ActionItem();
			//
			// wikiItem.setTitle(activity.getResources().getString(
			// R.string.show_wiki)
			// /* + " [" + poi.getWikipedia() + "]" */);
			// wikiItem.setIcon(activity.getResources().getDrawable(
			// R.drawable.bt_wikipedia_s));
			// wikiItem.setOnClickListener(new OnClickListener() {
			// @Override
			// public void onClick(View v) {
			// try {
			// InvokeIntents.openBrowser(activity,
			// poi.getWikipedia());
			// qa.dismiss();
			// } catch (Exception e) {
			// Log.e("", "Error on wiki");
			// }
			// }
			// });
			//
			// qa.addActionItem(wikiItem);
			// }
			//
			// // WEB
			// if (poi.getWebsite() != null && poi.getWebsite().length() > 0) {
			// final ActionItem browserItem = new ActionItem();
			//
			// browserItem.setTitle(activity.getResources().getString(
			// R.string.show_web)
			// /* + " [" + poi.getWebsite() + "]" */);
			// browserItem.setIcon(activity.getResources().getDrawable(
			// R.drawable.bt_openbrowser_s));
			// browserItem.setOnClickListener(new OnClickListener() {
			// @Override
			// public void onClick(View v) {
			// try {
			// InvokeIntents.openBrowser(activity,
			// poi.getWebsite());
			// qa.dismiss();
			// } catch (Exception e) {
			// Log.e("", "Error on open browser");
			// }
			// }
			// });
			//
			// qa.addActionItem(browserItem);
			// }
			// }

			qa.setAnimStyle(QuickAction.ANIM_AUTO);

			qa.show();
		} catch (Exception ex) {
			Log.e("POIItem", ex.getMessage());
		}

		return true;
	}

	private void checkCalculateRoute() {
		final Route r = RouteManager.getInstance().getRegisteredRoute();
		if (r == null)
			return;

		if (r.getState() == Tags.ROUTE_WITH_2_POINT) {
			showRouteOKDialog();
		}
	}

	public void showRouteOKDialog() {

		try {

			// AlertDialog.Builder alert = new AlertDialog.Builder(activity);
			//
			// alert.setTitle(R.string.info);
			// TextView text = new TextView(activity);
			// text.setText("          "
			// + activity.getResources().getString(
			// R.string.calculate_route));
			// alert.setView(text);
			//
			// alert.setPositiveButton(R.string.ok,
			// new DialogInterface.OnClickListener() {
			// public void onClick(DialogInterface dialog,
			// int whichButton) {
			// try {
			// InvokeIntents.routeModified(activity, MapPOI.class);
			// activity.finish();
			// } catch (Exception e) {
			//
			// }
			// }
			// });
			//
			// alert.setNegativeButton(R.string.cancel,
			// new DialogInterface.OnClickListener() {
			// public void onClick(DialogInterface dialog,
			// int whichButton) {
			//
			// }
			// });
			//
			// dialog = alert.show();
		} catch (Exception e) {

		}
	}
}
