/*
 * Copyright (C) 2010 Eric Harlow
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package es.prodevelop.gvsig.mini.activities;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.ericharlow.DragNDrop.DragListener;
import com.ericharlow.DragNDrop.DragNDropAdapter;
import com.ericharlow.DragNDrop.DragNDropListView;
import com.ericharlow.DragNDrop.DropListener;
import com.ericharlow.DragNDrop.RemoveListener;
import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.AbstractAction;

import es.prodevelop.gvsig.mini.R;
import es.prodevelop.gvsig.mini.action.AddLayerAction;
import es.prodevelop.gvsig.mini.action.AddLayerActionListener;
import es.prodevelop.gvsig.mini.action.MapControlAction;
import es.prodevelop.gvsig.mini.activities.adapter.TOCAdapter;
import es.prodevelop.gvsig.mini.map.IMapView;
import es.prodevelop.gvsig.mini.overlay.MapOverlay;
//import es.prodevelop.gvsig.mini.overlay.POIProxyOverlay;

public class TOCActivity extends ListActivity implements QuickActionObserver,
		AddLayerActionListener {

	public static final int LAYER_LOADED = 0;
	public static final int LAYER_FAIL = 1;

	public static final int POI_PROXY_DIALOG = 9999;
	public static final int FROM_TOC_REQUEST_CODE = 0;
	public static final int RETURN_TO_MAP_ACTIVITY_RESULT = 0;

	private QuickActionContextListener itemClickListener;
	private ActionBar actionBar;

	public TOCHandler handler = new TOCHandler();
	public String[] poiProxyServices = new String[] { "panoramio", "flickr",
			"foursquare", "buzz", "wikipedia", "twitter", "minube", "testwfs",
			"cloudmade", "geonames", "lastfm", "mapquest", "nominatim", "ovi",
			"simplegeo", "youtube" };

	public class TOCHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case LAYER_LOADED:
				TOCAdapter adapter = ((TOCAdapter) TOCActivity.this
						.getListAdapter());
				ArrayList<String> layersList = TOCActivity.this
						.buildLayersList();
				ArrayList<Boolean> layersVisibility = TOCActivity.this
						.buildLayerVisibilityList();
				adapter.mContent = layersList;
				adapter.visibleLayers = layersVisibility;

				adapter.notifyDataSetChanged();
				break;
			case LAYER_FAIL:
				Toast.makeText(TOCActivity.this, msg.obj.toString(),
						Toast.LENGTH_LONG).show();
				break;
			}

		}
	};

	public QuickActionContextListener getItemClickListener() {
		if (itemClickListener == null) {
			itemClickListener = new TOCQuickActionContextListener(this,
					R.drawable.list_button, R.string.layer_options);
			itemClickListener.addObserver(this);
		}
		return this.itemClickListener;
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.dragndroplistview);

		ArrayList<String> content = buildLayersList();
		ArrayList<Boolean> visibility = buildLayerVisibilityList();

		ListView listView = getListView();
		addActionBar(getLayoutInflater(), listView);
		addLayerAction();

		setListAdapter(new TOCAdapter(this, new int[] { R.layout.dragitem },
				new int[] { R.id.layer_name }, content, visibility));// new
																		// DragNDropAdapter(this,content)

		getListView().setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				return getItemClickListener().onClick(arg2,
						getListAdapter().getItem(arg2), arg1);
			}
		});

		if (listView instanceof DragNDropListView) {
			((DragNDropListView) listView).setDropListener(mDropListener);
			((DragNDropListView) listView).setRemoveListener(mRemoveListener);
			((DragNDropListView) listView).setDragListener(mDragListener);
		}
	}

//	@Override
//	public boolean onCreateOptionsMenu(Menu pMenu) {
//		super.onCreateOptionsMenu(pMenu);
//
//		try {
//			pMenu.add(0, 99, 99, "Load POI service");
//		} catch (Exception e) {
//			Log.e("", "", e);
//		}
//
//		return true;
//	}

	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		super.onMenuItemSelected(featureId, item);
		boolean result = false;
		try {
			switch (item.getItemId()) {
			case 99:
				result = true;
				showDialog(POI_PROXY_DIALOG);
				break;
			}
		} catch (Exception e) {
			Log.e("", "", e);
		}

		return result;
	}

	@Override
	public Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		AlertDialog.Builder builder = new Builder(this);
		boolean processed = false;

		switch (id) {
		case POI_PROXY_DIALOG:
			builder.setTitle("Choose your file");
			if (poiProxyServices == null) {
				Log.e("", "Showing file picker before loading the file list");
				dialog = builder.create();
				return dialog;
			}
			builder.setItems(poiProxyServices,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
//							POIProxyOverlay overlay = new POIProxyOverlay(
//									TOCActivity.this, ActivityBundlesManager
//											.getInstance().getMapView(),
//									poiProxyServices[which]);
//							ActivityBundlesManager.getInstance().getMapView()
//									.addOverlay(overlay);
//							Message msg = new Message();
//							msg.what = 0;
//							TOCActivity.this.handler.sendMessage(msg);
						}
					});
			processed = true;
			break;
		}

		if (processed) {
			dialog = builder.show();
		}

		return dialog;
	}

	public ArrayList<String> buildLayersList() {
		final IMapView mapView = ActivityBundlesManager.getInstance()
				.getMapView();

		List<MapOverlay> overlays = mapView.getOverlays();

		ArrayList<String> layerNames = new ArrayList<String>();

		// The top layer is the base layer (of the list) the first layer drawn.
		layerNames.add(mapView.getMRendererInfo().getFullNAME());

		for (MapOverlay overlay : overlays) {
			if (!overlay.isHidden()) {
				layerNames.add(overlay.getName());
			}
		}

		return layerNames;
	}

	public ArrayList<Boolean> buildLayerVisibilityList() {
		final IMapView mapView = ActivityBundlesManager.getInstance()
				.getMapView();

		List<MapOverlay> overlays = mapView.getOverlays();

		ArrayList<Boolean> layerVisibility = new ArrayList<Boolean>();

		// The top layer is the base layer (of the list) the first layer drawn.
		layerVisibility.add(true);

		for (MapOverlay overlay : overlays) {
			if (!overlay.isHidden()) {
				layerVisibility.add(overlay.isVisible());
			}
		}

		return layerVisibility;
	}

	private DropListener mDropListener = new DropListener() {
		public void onDrop(int from, int to) {
			ListAdapter adapter = getListAdapter();
			if (adapter instanceof DragNDropAdapter) {
				((DragNDropAdapter) adapter).onDrop(from, to);
				IMapView mapView = ActivityBundlesManager.getInstance()
						.getMapView();
				MapOverlay overlay = mapView
						.getOverlay(((TOCAdapter) TOCActivity.this
								.getListAdapter()).mContent.get(to));

				if (overlay != null) {
					overlay.setzIndex(to);
				}

				overlay = mapView.getOverlay(((TOCAdapter) TOCActivity.this
						.getListAdapter()).mContent.get(from));

				if (overlay != null) {
					overlay.setzIndex(from);
				}

				mapView.fireZIndexChanged();
				getListView().invalidateViews();
			}
		}
	};

	private RemoveListener mRemoveListener = new RemoveListener() {
		public void onRemove(int which) {
			ListAdapter adapter = getListAdapter();
			if (adapter instanceof DragNDropAdapter) {
				((DragNDropAdapter) adapter).onRemove(which);
				getListView().invalidateViews();
			}
		}
	};

	private DragListener mDragListener = new DragListener() {

		int backgroundColor = 0xe0103010;
		int defaultBackgroundColor;

		public void onDrag(int x, int y, ListView listView) {
			// TODO Auto-generated method stub
		}

		public void onStartDrag(View itemView) {
			itemView.setVisibility(View.INVISIBLE);
			defaultBackgroundColor = itemView.getDrawingCacheBackgroundColor();
			itemView.setBackgroundColor(backgroundColor);
			ImageView iv = (ImageView) itemView.findViewById(R.id.dragdrop);
			if (iv != null)
				iv.setVisibility(View.INVISIBLE);
		}

		public void onStopDrag(View itemView) {
			itemView.setVisibility(View.VISIBLE);
			itemView.setBackgroundColor(defaultBackgroundColor);
			ImageView iv = (ImageView) itemView.findViewById(R.id.dragdrop);
			if (iv != null)
				iv.setVisibility(View.VISIBLE);
		}

	};

	@Override
	public void onQuickActionExecuted(QuickActionEvent event) {
		if (event == null) {
			return;
		}

		switch (event.getType()) {
		case QuickActionEvent.TYPE_LIST_FEATURES:
			// FIXME lanzar activity de listar features;
			break;
		case QuickActionEvent.TYPE_REMOVE_LAYER:
			this.handler.sendEmptyMessage(LAYER_LOADED);
			break;
		case QuickActionEvent.TYPE_ZOOM_EXTENT:
			this.finish();
			break;
		}
	}

	public void addActionBar(LayoutInflater inflater, ListView listView) {
		listView.addHeaderView(inflater.inflate(R.layout.actionbars, null));
		actionBar = (ActionBar) findViewById(R.id.actionbar);
	}

	public ActionBar getActionbar() {
		return this.actionBar;
	}

	public void setActionbar(ActionBar actionbar) {
		this.actionBar = actionbar;
	}

	public void addActionToActionbar(AbstractAction action) {
		actionBar.addAction(action);
	}

	public void addLayerAction() {
		addActionToActionbar(new AddLayerAction(R.drawable.layer_add, this,
				this));
	}

	@Override
	public void onAddLayer(int type) {
		switch (type) {
		case REPLACE_BASE_LAYER:
			this.showLayersActivity();
			break;
		case ADD_RASTER_LAYER:
			break;
		default:
			break;
		}
	}

	public void showLayersActivity() {
		Intent intent = new Intent(this, LayersActivity.class);

		intent.putExtra("loadLayers", true);
		intent.putExtra("gvtiles", ActivityBundlesManager.getInstance()
				.getMapView().getMapState().gvTilesPath);

		startActivityForResult(intent, 1);
	}

	/**
	 * Manages the results of the LayersActivity
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		try {
			super.onActivityResult(requestCode, resultCode, intent);

			switch (requestCode) {
			case 1:
				switch (resultCode) {
				case RESULT_OK:
					String layerName = intent.getStringExtra("layer");
					ActivityBundlesManager.getInstance().getMapView()
							.getMapState().gvTilesPath = intent
							.getStringExtra("gvtiles");
					if (layerName != null) {
						ActivityBundlesManager.getInstance().getMapView()
								.onLayerChanged(layerName);
						this.handler.sendEmptyMessage(LAYER_LOADED);
					}
					break;
				}
				break;
			}
		} catch (Exception e) {
			Log.e("TOCActivity", "Map onActivityResult: ", e);
		}
	}
}