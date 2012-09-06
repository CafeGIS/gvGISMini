package es.prodevelop.gvsig.mini.activities;

import java.util.List;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import com.vividsolutions.jts.geom.Envelope;

import es.prodevelop.geodetic.utils.conversion.ConversionCoords;
import es.prodevelop.gvsig.mini.R;
import es.prodevelop.gvsig.mini.exceptions.BaseException;
import es.prodevelop.gvsig.mini.geom.Extent;
import es.prodevelop.gvsig.mini.geom.impl.jts.JTSEnvelope;
import es.prodevelop.gvsig.mini.geom.impl.jts.JTSFeature;
import es.prodevelop.gvsig.mini.overlay.FeatureOverlay;
import es.prodevelop.gvsig.mini.overlay.MapOverlay;
import es.prodevelop.gvsig.mini.util.ActionItem;
import es.prodevelop.gvsig.mini.util.QuickAction;
import es.prodevelop.gvsig.mobile.fmap.proj.CRSFactory;

public class FeatureQuickActionListener extends QuickActionContextListener {

	public FeatureQuickActionListener(Activity activity, int iconId, int titleId) {
		super(activity, iconId, titleId);
		// TODO Auto-generated constructor stub
	}

	public void addZoomToFeature(final QuickAction qa, final JTSFeature feature) {
		final ActionItem item = new ActionItem();

		item.setTitle(activity.getResources().getString(R.string.zoom_feature));
		item.setIcon(activity.getResources().getDrawable(R.drawable.qa_zoom));
		item.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					Envelope envelope = feature.getProjectedGeometry()
							.getGeometry().getEnvelopeInternal();

					if (envelope == null) {
						return;
					}

					ActivityBundlesManager
							.getInstance()
							.getMapView()
							.getMapViewController()
							.zoomToExtent(
									new Extent(envelope.getMinX(), envelope
											.getMinY(), envelope.getMaxX(),
											envelope.getMaxY()), true);
					FeatureQuickActionListener.this
							.notifyObservers(new QuickActionEvent(
									QuickActionEvent.TYPE_ZOOM_FEATURE));
					qa.dismiss();
				} catch (BaseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					qa.dismiss();
				}
			}
		});

		qa.addActionItem(item);
	}

	public void addRemoveFeature(final QuickAction qa, final JTSFeature feature) {
		final MapOverlay overlay = ActivityBundlesManager.getInstance()
				.getCurrentOverlay();

		if (overlay == null) {
			return;
		}

		if (!(overlay instanceof FeatureOverlay)) {
			return;
		}

		final ActionItem item = new ActionItem();

		item.setTitle(activity.getResources().getString(R.string.remove_feature));
		item.setIcon(activity.getResources().getDrawable(R.drawable.qa_remove));
		item.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				boolean removed = ((FeatureOverlay) overlay).removeFeature(feature);
//				List<JTSFeature> features = ActivityBundlesManager.getInstance().getFeatures();
//				features.remove(feature);
				ActivityBundlesManager.getInstance().registerFeatures(((FeatureOverlay) overlay).getFeatures(), overlay);
				FeatureQuickActionListener.this
						.notifyObservers(new QuickActionEvent(
								QuickActionEvent.TYPE_REMOVE_FEATURE));
				qa.dismiss();
			}
		});

		qa.addActionItem(item);
	}

	public boolean onClick(int position, final Object feature, final View anchor) {
		try {
			final QuickAction qa = new QuickAction(anchor);

			if (feature == null)
				return true;

			addZoomToFeature(qa, (JTSFeature) feature);
			addRemoveFeature(qa, (JTSFeature) feature);

			qa.setAnimStyle(QuickAction.ANIM_AUTO);

			qa.show();
		} catch (Exception ex) {
			Log.e("FeatureQuickActionContextListener", ex.getMessage());
		}

		return true;
	}

}
