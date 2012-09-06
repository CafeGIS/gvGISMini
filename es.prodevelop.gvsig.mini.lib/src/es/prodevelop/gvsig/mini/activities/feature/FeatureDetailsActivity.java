package es.prodevelop.gvsig.mini.activities.feature;

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

import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import es.prodevelop.gvsig.mini.R;
import es.prodevelop.gvsig.mini.activities.ActivityBundlesManager;
import es.prodevelop.gvsig.mini.activities.FeatureQuickActionListener;
import es.prodevelop.gvsig.mini.activities.QuickActionContextListener;
import es.prodevelop.gvsig.mini.activities.QuickActionEvent;
import es.prodevelop.gvsig.mini.activities.QuickActionObserver;
import es.prodevelop.gvsig.mini.geom.impl.base.Feature.Attribute;
import es.prodevelop.gvsig.mini.geom.impl.jts.JTSFeature;
import es.prodevelop.gvsig.mini.map.IMapView;
import es.prodevelop.gvsig.mini.overlay.FeatureOverlay;
import es.prodevelop.gvsig.mini.overlay.MapOverlay;
import es.prodevelop.gvsig.mini.views.LegendView;

public class FeatureDetailsActivity extends Activity implements
		QuickActionObserver {

	public final static String CAT = "CAT";
	public final static String SCAT = "SCAT";
	public final static String INFO = "INFO";
	public final static String PHONE = "PHONE";
	public final static String MAIL = "MAIL";
	public final static String URL = "URL";
	public final static String WEB = "WEB";
	public final static String WIKI = "WIKI";
	public final static String IMG = "IMG";
	public final static String ADDR = "ADDR";
	public final static String DESC = "DESC";
	public final static String DIST = "DIST";
	public final static String X = "X";
	public final static String Y = "Y";

	private String dist = "";
	private String selectedText = "";
	protected QuickActionContextListener listener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		try {
			// setTitle(R.string.poi_info);
			final JTSFeature feature = (JTSFeature) ActivityBundlesManager
					.getInstance().getFeatureDetail();

			final DisplayMetrics metrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(metrics);

			LinearLayout layout = (LinearLayout) getLayoutInflater().inflate(
					R.layout.feature_details, null);

			TextView descTV = (TextView) layout.findViewById(R.id.desc);
			TextView distTV = (TextView) layout.findViewById(R.id.dist);
			LegendView poiImg = (LegendView) layout.findViewById(R.id.img);

			MapOverlay ovrl = this.getMapOverlayFromFeature(feature);
			if (ovrl != null && ovrl instanceof FeatureOverlay) {
				poiImg.setLegend(((FeatureOverlay) ovrl).getLegend());
				poiImg.setSymbol(((FeatureOverlay) ovrl).getSymbol());
			}

			final ImageButton bt = (ImageButton) layout
					.findViewById(R.id.show_options);
			bt.setVisibility(View.VISIBLE);

			bt.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					getItemClickListener().onClick(
							-1,
							(JTSFeature) ActivityBundlesManager.getInstance()
									.getFeatureDetail(), bt);
				}
			});

			IMapView mapView = ActivityBundlesManager.getInstance()
					.getMapView();
			final List<MapOverlay> overlays = mapView.getOverlays();

			String descPOI = "FEATURE";
			for (MapOverlay overlay : overlays) {
				if (overlay instanceof FeatureOverlay
						&& ((FeatureOverlay) overlay).getFeatures().contains(
								feature) && overlay.isVisible()
						&& !overlay.isHidden()) {
					descPOI = overlay.getName();
				}
			}

			descTV.setText(descPOI);
			applyOptions(descTV);

			dist = getIntent().getStringExtra(DIST);

			distTV.setText(getResources().getString(R.string.distance) + " "
					+ dist);

			LinearLayout attributesLayout = (LinearLayout) layout
					.findViewById(R.id.extra_data);

			TextView pointLabel = (TextView) getLayoutInflater().inflate(
					R.layout.label_textview, null);
			TextView pointDetail = (TextView) getLayoutInflater().inflate(
					R.layout.detail_textview, null);

			pointLabel.setText("CENTROID");
			pointDetail.setText(feature.getGeometry().getGeometry()
					.getCentroid().toString());
			registerForContextMenu(pointDetail);

			attributesLayout.addView(pointLabel);
			attributesLayout.addView(pointDetail);
			Spanned spannedContent;
			String value;

			if (feature.getAttributes() != null) {
				Iterator it = feature.getAttributes().keySet().iterator();

				String key;
				while (it.hasNext()) {
					key = it.next().toString();

					if (key.compareToIgnoreCase("LAYER_NAME") != 0) {
						TextView label = (TextView) getLayoutInflater()
								.inflate(R.layout.label_textview, null);
						// WebView detail = new WebView(this);

						TextView detail = (TextView) getLayoutInflater()
								.inflate(R.layout.detail_textview, null);

						label.setText(key.toUpperCase());
						value = feature.getAttribute(key).value;

						if (value != null) {
							spannedContent = Html.fromHtml(value);
							detail.setText(spannedContent, BufferType.SPANNABLE);
							applyOptions(detail);
							// detail.loadData("<html><body>" + value +
							// "</body></html>",
							// "text/html", null);
						}

						attributesLayout.addView(label);
						attributesLayout.addView(detail);
					}
				}
			}

			// layout.addView(l);

			setContentView(layout);
			getWindow().setLayout(LayoutParams.FILL_PARENT,
					LayoutParams.FILL_PARENT);
		} catch (Exception e) {
			Log.e("", e.getMessage());
		}
	}

	public void setItemClickListener(QuickActionContextListener listener) {
		this.listener = listener;
	}

	// private Spanned linkifyURL(String url) {
	// try {
	// if (url.toLowerCase().contains("http")
	// || url.toLowerCase().contains("www")) {
	// return Html.fromHtml("<a href=\"" + url + "\"" + ">" + url
	// + "</a>");
	// }
	// return new SpannableString(url);
	// } catch (Exception e) {
	// return new SpannableString(url);
	// }
	// }
	//
	// private Spanned linkifyPhone(String phone) {
	// try {
	// SpannableString ss = new SpannableString(phone);
	//
	// ss.setSpan(new URLSpan("tel:" + phone), 0, phone.length(),
	// Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
	//
	// return ss;
	//
	// } catch (Exception e) {
	// return new SpannableString(phone);
	// }
	// }

	private void applyOptions(TextView t) {
		t.setVisibility(View.VISIBLE);
		t.setMovementMethod(LinkMovementMethod.getInstance());
		Linkify.addLinks(t, Linkify.ALL);
		registerForContextMenu(t);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		try {
			selectedText = ((TextView) v).getText().toString();
			menu.setHeaderTitle(selectedText);
			menu.add(0, v.getId(), 0, getResources().getString(R.string.copy));
			menu.add(0, v.getId(), 1,
					getResources().getString(R.string.share_text));
		} catch (Exception e) {
			Log.e("", e.getMessage());
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		try {
			switch (item.getOrder()) {
			case 0:
				ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
				clipboard.setText(selectedText);
				break;
			case 1:
				final Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setType("text/plain");
				intent.putExtra(Intent.EXTRA_TEXT, selectedText);
				Intent i = Intent.createChooser(intent, getResources()
						.getString(R.string.share_text));
				i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);//
				startActivityForResult(i, 2385);
				break;
			}
		} catch (Exception e) {
			Log.e("", e.getMessage());
		}

		return true;
	}

	public QuickActionContextListener getItemClickListener() {
		if (listener == null) {
			listener = new FeatureQuickActionListener(this, R.drawable.pois,
					R.string.NameFinderActivity_0);
			listener.addObserver(this);
		}
		return listener;
	}

	public MapOverlay getMapOverlayFromFeature(JTSFeature feature) {

		final IMapView mapView = ActivityBundlesManager.getInstance()
				.getMapView();
		Attribute layerName = feature.getAttribute("LAYER_NAME");

		MapOverlay overlay = null;
		if (layerName != null && layerName.value != null) {
			overlay = mapView.getOverlay(layerName.value);
		}

		return overlay;
	}

	@Override
	public void onQuickActionExecuted(QuickActionEvent event) {
		if (event == null) {
			return;
		}

		switch (event.getType()) {
		case QuickActionEvent.TYPE_REMOVE_FEATURE:
			this.finish();			
			break;
		case QuickActionEvent.TYPE_ZOOM_FEATURE:
			this.finish();
			break;
		}
	}

}
