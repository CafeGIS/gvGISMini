package es.prodevelop.gvsig.mini.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import es.prodevelop.gvsig.mini.activities.MapActivity;
import es.prodevelop.gvsig.mini.gpe.activity.GPETOCActivity;
import es.prodevelop.gvsig.mini.gpe.tasks.DownloadRemoteGPEFilesTask;

public class GPEMap extends MapActivity {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		

//		ResourceLoader.addResource("panoramio", R.drawable.panoramio);
//		ResourceLoader.addResource("buzz", R.drawable.buzz);
//		ResourceLoader.addResource("wikipedia", R.drawable.wikipedia);
//		ResourceLoader.addResource("twitter", R.drawable.twitter);
//		ResourceLoader.addResource("foursquare", R.drawable.foursquare);
//		ResourceLoader.addResource("flickr", R.drawable.flickr);
//		ResourceLoader.addResource("minube", R.drawable.minube);

//		TileOverlay estoEsUnaPrueba1 = null;
//		try {
//			estoEsUnaPrueba1 = new TileOverlay(this, getMapView(),
//					"CARTOCIUDAD", Layers.getInstance().getRenderer(
//							"Cartociudad Fondo (ES)"), CompatManager
//							.getInstance().getRegisteredContext(), false);
//			// getMapView().addOverlay(estoEsUnaPrueba1);
//		} catch (BaseException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		TileOverlay estoEsUnaPrueba = null;
//		try {
//			estoEsUnaPrueba = new TileOverlay(this, getMapView(), "GMAPS",
//					Layers.getInstance().getRenderer("Google Hybrid"),
//					CompatManager.getInstance().getRegisteredContext(), false);
//			// getMapView().addOverlay(estoEsUnaPrueba);
//		} catch (BaseException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		// POIProxyOverlay overlay = new POIProxyOverlay(this, getMapView(),
		// "panoramio");
		// getMapView().addOverlay(overlay);

		// POIProxyOverlay overlay2 = new POIProxyOverlay(this, getMapView(),
		// "minube");
		// getMapView().addOverlay(overlay2);
		//
		// POIProxyOverlay overlay3 = new POIProxyOverlay(this, getMapView(),
		// "foursquare");
		// getMapView().addOverlay(overlay3);
		//
		// POIProxyOverlay overlay4 = new POIProxyOverlay(this, getMapView(),
		// "buzz");
		// getMapView().addOverlay(overlay4);

		// this.addGPELayer("/sdcard/GPXWaypoints.gpx");
		// this.addGPELayer("/sdcard/GPXTrack.gpx");
		// this.addGPELayer("/sdcard/GPXRoute.gpx");
		// this.addGPELayer("/sdcard/earthquake_public.kml");
		// this.addGPELayer("/sdcard/Discovery_Networks.kml");
		// this.addGPELayer("/sdcard/SpainMorocco.kml");

		// this.addGPELayer("/sdcard/KML_Samples.kml");

	}

	@Override
	public void onNewIntent(Intent i) {
		String data = i.getStringExtra("query");

		if (data != null) {
			if (data.indexOf("q=") != -1) {
				data = data.substring(data.indexOf("q=") + 2, data.length());
				new DownloadRemoteGPEFilesTask(this, new Handler())
						.execute(data);
			}

		}

		super.onNewIntent(i);

	}	

	// @Override
	// public boolean onCreateOptionsMenu(Menu pMenu) {
	// super.onCreateOptionsMenu(pMenu);
	//
	// try {
	// pMenu.add(0, 101, 101, "Features");
	// } catch (Exception e) {
	// Log.e("", "", e);
	// }
	//
	// return true;
	// }
	//
	// public boolean onMenuItemSelected(int featureId, MenuItem item) {
	// boolean result = true;
	// try {
	// result = super.onMenuItemSelected(featureId, item);
	// switch (item.getItemId()) {
	// case 100:
	// break;
	// case 101:
	// // FIXME This is only a test!!!
	// Intent intent = new Intent(this, GPETOCActivity.class);
	// startActivity(intent);
	// break;
	// }
	// } catch (Exception e) {
	// Log.e("", "", e);
	// }
	//
	// return result;
	// }

	/**
	 * Starts the LayersActivity with the MapState.gvTilesPath file
	 */
	@Override
	public void viewLayers() {
		try {

			Intent intent = new Intent(this, GPETOCActivity.class);
			startActivity(intent);
		} catch (Exception e) {
			Log.e("GPEMap", "viewLayers: ", e);
		}
	}

	public void addSearchAction() {

	}
}
