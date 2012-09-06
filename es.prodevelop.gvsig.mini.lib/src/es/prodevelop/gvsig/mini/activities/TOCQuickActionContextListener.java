package es.prodevelop.gvsig.mini.activities;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import es.prodevelop.gvsig.mini.R;
import es.prodevelop.gvsig.mini.activities.feature.ResultSearchActivity;
import es.prodevelop.gvsig.mini.geom.Extent;
import es.prodevelop.gvsig.mini.map.IMapView;
import es.prodevelop.gvsig.mini.overlay.FeatureOverlay;
import es.prodevelop.gvsig.mini.overlay.MapOverlay;
import es.prodevelop.gvsig.mini.overlay.RouteOverlay;
import es.prodevelop.gvsig.mini.overlay.TileOverlay;
import es.prodevelop.gvsig.mini.util.ActionItem;
import es.prodevelop.gvsig.mini.util.QuickAction;

public class TOCQuickActionContextListener extends QuickActionContextListener {

	public TOCQuickActionContextListener(Activity activity, int iconId,
			int titleId) {
		super(activity, iconId, titleId);
		// TODO Auto-generated constructor stub
	}

	public void addZoomToLayer(final QuickAction qa, final MapOverlay overlay) {
		final ActionItem item = new ActionItem();

		item.setTitle(activity.getResources().getString(R.string.zoom_layer));
		item.setIcon(activity.getResources().getDrawable(R.drawable.qa_zoom));
		item.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Extent extent;
				if (overlay != null) {
					extent = overlay.getExtent();
				} else {
					extent = ActivityBundlesManager.getInstance().getMapView()
							.getBaseLayer().getRenderer().getExtent();
				}
				if (extent != null) {
					ActivityBundlesManager.getInstance().getMapView()
							.getMapViewController().zoomToExtent(extent, true);
				}
				TOCQuickActionContextListener.this
						.notifyObservers(new QuickActionEvent(
								QuickActionEvent.TYPE_ZOOM_EXTENT));
				qa.dismiss();
			}
		});

		qa.addActionItem(item);
	}

	public void addRemoveLayer(final QuickAction qa, final MapOverlay overlay) {
		if (overlay == null) {
			return;
		}

		if (overlay instanceof RouteOverlay) {
			return;
		}

		if (overlay instanceof TileOverlay) {
			if (((TileOverlay) overlay).isTheBaseLayer()) {
				return;
			}
		}
		final ActionItem item = new ActionItem();

		item.setTitle(activity.getResources().getString(R.string.remove_layer));
		item.setIcon(activity.getResources().getDrawable(R.drawable.qa_remove));
		item.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ActivityBundlesManager.getInstance().getMapView()
						.removeOverlay(overlay);
				TOCQuickActionContextListener.this
						.notifyObservers(new QuickActionEvent(
								QuickActionEvent.TYPE_REMOVE_LAYER));
				qa.dismiss();
			}
		});

		qa.addActionItem(item);
	}

	public void addListFeatures(final QuickAction qa, final MapOverlay overlay) {
		final ActionItem item = new ActionItem();

		if (overlay == null) {
			return;
		}

		if (!(overlay instanceof FeatureOverlay)) {
			return;
		}

		item.setTitle(activity.getResources().getString(R.string.list_features));
		item.setIcon(activity.getResources().getDrawable(R.drawable.qa_list));
		item.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(
						TOCQuickActionContextListener.this.activity,
						ResultSearchActivity.class);
				// Register the features so the activity can access them
				ActivityBundlesManager
						.getInstance()
						.getIntentFiller()
						.fillSearchCenter(
								intent,
								ActivityBundlesManager.getInstance()
										.getMapView());
				// At the moment there are no filter capabilities
				intent.putExtra(SearchActivity.HIDE_AUTOTEXTVIEW, true);
				// i.putExtra(ResultSearchActivity.QUERY, query.toString());
				// We use a Singleton instead of packing all the features
				// into a
				// Bundle
				ActivityBundlesManager.getInstance().registerFeatures(
						((FeatureOverlay) overlay).getFeatures(), overlay);
				TOCQuickActionContextListener.this.activity
						.startActivityForResult(intent, TOCActivity.FROM_TOC_REQUEST_CODE);
				TOCQuickActionContextListener.this
						.notifyObservers(new QuickActionEvent(
								QuickActionEvent.TYPE_LIST_FEATURES));

				qa.dismiss();
			}
		});
		// zoomlayer.setOnClickListener(getCenterOnMapListener((JTSFeature)p,
		// qa));

		qa.addActionItem(item);
	}

	public boolean onClick(int position, final Object layerName,
			final View anchor) {
		try {
			final QuickAction qa = new QuickAction(anchor);

			IMapView mapView = ActivityBundlesManager.getInstance()
					.getMapView();

			if (layerName == null)
				return true;

			MapOverlay overlay = mapView.getOverlay(layerName.toString());

			addZoomToLayer(qa, overlay);
			addRemoveLayer(qa, overlay);
			addListFeatures(qa, overlay);

			qa.setAnimStyle(QuickAction.ANIM_AUTO);

			qa.show();
		} catch (Exception ex) {
			Log.e("TOCQuickActionContextListener", ex.getMessage());
		}

		return true;
	}

}
