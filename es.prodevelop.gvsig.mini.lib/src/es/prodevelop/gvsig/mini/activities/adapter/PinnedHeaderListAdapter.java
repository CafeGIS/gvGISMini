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

package es.prodevelop.gvsig.mini.activities.adapter;

import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageButton;
import android.widget.TextView;
import es.prodevelop.android.spatialindex.poi.POI;
import es.prodevelop.geodetic.utils.conversion.ConversionCoords;
import es.prodevelop.gvsig.mini.R;
import es.prodevelop.gvsig.mini.activities.SearchActivity;
import es.prodevelop.gvsig.mini.exceptions.BaseException;
import es.prodevelop.gvsig.mini.geom.impl.base.Point;
import es.prodevelop.gvsig.mini.geom.impl.jts.JTSFeature;
import es.prodevelop.gvsig.mini.overlay.FeatureOverlay;
import es.prodevelop.gvsig.mini.overlay.MapOverlay;
import es.prodevelop.gvsig.mini.util.Utils;
import es.prodevelop.gvsig.mini.utiles.Calculator;
import es.prodevelop.gvsig.mini.views.LegendView;
import es.prodevelop.gvsig.mini.views.PinnedHeaderListView;
import es.prodevelop.gvsig.mobile.fmap.proj.CRSFactory;

public class PinnedHeaderListAdapter extends FilteredLazyAdapter implements
		PinnedHeaderListView.PinnedHeaderAdapter, OnScrollListener {
	private boolean mDisplaySectionHeaders = true;

	// private MapPreview preview;
	// private LinearLayout lastPreviewLayout;

	/**
	 * An approximation of the background color of the pinned header. This color
	 * is used when the pinned header is being pushed up. At that point the
	 * header "fades away". Rather than computing a faded bitmap based on the
	 * 9-patch normally used for the background, we will use a solid color,
	 * which will provide better performance and reduced complexity.
	 */
	private int mPinnedHeaderBackgroundColor = 0xff202020;

	public PinnedHeaderListAdapter(SearchActivity activity) {
		super(activity);
		try {
			// preview = new MapPreview(activity, CompatManager.getInstance()
			// .getRegisteredContext(), activity.metrics.widthPixels,
			// activity.metrics.heightPixels / 2);
		} catch (Exception e) {
			Log.e("Pinned", e.getMessage());
		}
	}

	public boolean getDisplaySectionHeadersEnabled() {
		return mDisplaySectionHeaders;
	}

	@Override
	public boolean isEmpty() {
		// FIXME implement this
		return false;
	}

	@Override
	public int getCount() {
		try {
			return activity.getResultsList().size();

		} catch (Exception ignore) {
			return -1;
		}
	}

	@Override
	public Object getItem(int arg0) {
		try {

			return activity.getResultsList().get(arg0);

		} catch (Exception ignore) {
			return null;
		}
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	@Override
	public View getView(final int arg0, View convertView, ViewGroup arg2) {
		final ViewHolder holder;

		// When convertView is not null, we can reuse it directly, there is
		// no need
		// to reinflate it. We only inflate a new View when the convertView
		// supplied
		// by ListView is null.
		final JTSFeature p = (JTSFeature) getItem(arg0);

		if (convertView == null) {
			convertView = activity.getLayoutInflater().inflate(
					R.layout.street_row, null);

			// Creates a ViewHolder and store references to the two children
			// views
			// we want to bind data to.
			holder = new ViewHolder();
			holder.text = (TextView) convertView.findViewById(R.id.desc);
			holder.dist = (TextView) convertView.findViewById(R.id.dist);
			holder.poiImg = (LegendView) convertView.findViewById(R.id.img);

			// LinearLayout l = new LinearLayout(activity);
			// final RelativeLayout.LayoutParams zzParams = new
			// RelativeLayout.LayoutParams(
			// RelativeLayout.LayoutParams.WRAP_CONTENT,
			// RelativeLayout.LayoutParams.WRAP_CONTENT);
			// zzParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
			// zzParams.addRule(RelativeLayout.CENTER_VERTICAL);
			// // zzParams.setMargins(20, 20, 20, 20);
			// ((LinearLayout) ((LinearLayout) convertView)
			// .findViewById(R.id.map_preview)).addView(l, zzParams);
			// holder.previewLayout = l;
			// holder.preview = preview;
			holder.optionsButton = (ImageButton) convertView
					.findViewById(R.id.show_options);
			// holder.detailsButton = (Button) convertView
			// .findViewById(R.id.show_details);
			holder.optionsButton.setVisibility(View.VISIBLE);
			holder.optionsButton.setFocusable(false);
			// holder.detailsButton.setFocusable(false);

			convertView.setTag(holder);
		} else {
			// Get the ViewHolder back to get fast access to the TextView
			// and the ImageView.
			holder = (ViewHolder) convertView.getTag();
		}

		// ((TextView) arg1).setTextSize(26);
		// ((TextView) arg1).setTextColor(Color.WHITE);

		if (p == null)
			return convertView;
		holder.optionsButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				activity.getItemClickListener().onClick(arg0, p,
						holder.optionsButton);
			}
		});
		// holder.detailsButton.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// Intent i = new Intent(activity, POIDetailsActivity.class);
		// if (p != null && p instanceof OsmPOI) {
		// OsmPOI poi = (OsmPOI) p;
		// InvokeIntents.fillIntentPOIDetails(poi,
		// activity.getCenter(), i, activity);
		//
		// activity.startActivity(i);
		// } else {
		// // throw exception
		// }
		// }
		// });
		String subc = POI.DEFAULT_DESCRIPTION;

		// FIXME Load images recursivelly?

		String desc = "";
		if (p.getText() != null) {
			desc = Html.fromHtml(p.getText()).toString();
		}

		// if (holder.preview != null) {
		// if (arg0 == pos && p.getX() != 0 && p.getY() != 0) {
		// if (lastPreviewLayout != null)
		// lastPreviewLayout.removeView(preview);
		// holder.previewLayout.addView(preview);
		// lastPreviewLayout = holder.previewLayout;
		// holder.previewLayout.setVisibility(View.VISIBLE);
		// holder.optionsButton.setVisibility(View.VISIBLE);
		// if (!(p instanceof OsmPOIStreet))
		// holder.detailsButton.setVisibility(View.VISIBLE);
		//
		// holder.preview.setMapCenterFromLonLat(p);
		// if ((getItem(arg0) instanceof OsmPOIStreet)) {
		// holder.preview.setExtent(((OsmPOIStreet) p)
		// .getBoundingBox());
		// }
		// } else {
		// holder.previewLayout.setVisibility(View.GONE);
		// holder.optionsButton.setVisibility(View.GONE);
		// holder.detailsButton.setVisibility(View.GONE);
		//
		// }
		// }

		// Bind the data efficiently with the holder.
		holder.text.setText(desc);

		String distanceText = "";
		try {
			p.getGeometry().getGeometry();

			final Point centerM = getCenterMercator();

			// final double distance = centerM.distance(ConversionCoords
			// .reproject(p.getX(), p.getY(),
			// CRSFactory.getCRS("EPSG:4326"),
			// CRSFactory.getCRS("EPSG:900913")));
			double[] centerXY = ConversionCoords.reproject(centerM.getX(),
					centerM.getY(), CRSFactory.getCRS("EPSG:900913"),
					CRSFactory.getCRS("EPSG:4326"));

			if (centerXY[0] == 0 || centerXY[1] == 0)
				distanceText = activity.getResources().getString(
						R.string.location_not_available);
			else {
				final com.vividsolutions.jts.geom.Point centroid = p
						.getGeometry().getGeometry().getCentroid();
				final double distance = Calculator.latLonDist(centroid.getX(),
						centroid.getY(), centerXY[0], centerXY[1]);
				distanceText = Utils.formatDistance(distance);
			}
		} catch (Exception e) {
			// Feature without geometry
		}

		holder.dist.setText(activity.getResources()
				.getString(R.string.distance) + " " + distanceText);
		MapOverlay overlay = this.getMapOverlayFromFeature(p);
		if (overlay != null && overlay instanceof FeatureOverlay) {
			holder.poiImg.setLegend(((FeatureOverlay) overlay).getLegend());
			holder.poiImg.setSymbol(((FeatureOverlay) overlay).getSymbol());
		}

		int realPosition = getRealPosition(arg0);

		bindSectionHeader(convertView, realPosition, mDisplaySectionHeaders);
		return convertView;
	}

	private void bindSectionHeader(View itemView, int position,
			boolean displaySectionHeaders) {
		// final LinearLayout view = (LinearLayout) itemView;
		TextView t = (TextView) itemView.findViewById(R.id.section_text);
		if (!displaySectionHeaders) {
			t.setVisibility(View.GONE);
			// view.setDividerVisible(true);
		} else {
			final int section = getSectionForPosition(position);
			if (getPositionForSection(section) == position) {
				String title = (String) getSections()[section];
				// view.setSectionHeader(title);
				t.setText(title);
				t.setVisibility(View.VISIBLE);
			} else {
				t.setVisibility(View.GONE);
				// view.setDividerVisible(false);
				// view.setSectionHeader(null);
			}

			// // move the divider for the last item in a section
			// if (getPositionForSection(section + 1) - 1 == position) {
			// // view.setDividerVisible(false);
			// t.setVisibility(View.VISIBLE);
			// } else {
			// // view.setDividerVisible(true);
			// t.setVisibility(View.GONE);
			// }
		}
		// POIItemView t = (POIItemView) itemView;
		// if (!displaySectionHeaders) {
		// t.mHeaderTextView.setVisibility(View.GONE);
		// // view.setDividerVisible(true);
		// } else {
		// final int section = getSectionForPosition(position);
		// if (getPositionForSection(section) == position) {
		// String title = (String) mIndexer.getSections()[section];
		// // view.setSectionHeader(title);
		// t.mHeaderTextView.setText(title);
		// t.mHeaderTextView.setVisibility(View.VISIBLE);
		// } else {
		// t.mHeaderTextView.setVisibility(View.GONE);
		// // view.setDividerVisible(false);
		// // view.setSectionHeader(null);
		// }
		//
		// // // move the divider for the last item in a section
		// // if (getPositionForSection(section + 1) - 1 == position) {
		// // // view.setDividerVisible(false);
		// // t.setVisibility(View.VISIBLE);
		// // } else {
		// // // view.setDividerVisible(true);
		// // t.setVisibility(View.GONE);
		// // }
		// }
	}

	@Override
	public boolean isEnabled(int position) {
		return true;
	}

	private int getRealPosition(int pos) {
		// FIXME
		return pos;
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		if (view instanceof PinnedHeaderListView) {
			((PinnedHeaderListView) view).configureHeaderView(firstVisibleItem);
		}
	}

	/**
	 * Computes the state of the pinned header. It can be invisible, fully
	 * visible or partially pushed up out of the view.
	 */
	public int getPinnedHeaderState(int position) {
		if (mIndexer == null) {
			return PINNED_HEADER_GONE;
		}

		int realPosition = getRealPosition(position);
		if (realPosition < 0) {
			return PINNED_HEADER_GONE;
		}

		// The header should get pushed up if the top item shown
		// is the last item in a section for a particular letter.
		int section = getSectionForPosition(realPosition);
		int nextSectionPosition = getPositionForSection(section + 1);
		if (nextSectionPosition != -1
				&& realPosition == nextSectionPosition - 1) {
			return PINNED_HEADER_PUSHED_UP;
		}

		return PINNED_HEADER_VISIBLE;
	}

	/**
	 * Configures the pinned header by setting the appropriate text label and
	 * also adjusting color if necessary. The color needs to be adjusted when
	 * the pinned header is being pushed up from the view.
	 */
	public void configurePinnedHeader(View header, int position, int alpha) {
		PinnedHeaderCache cache = (PinnedHeaderCache) header.getTag();
		if (cache == null) {
			cache = new PinnedHeaderCache();
			cache.titleView = (TextView) header.findViewById(R.id.section_text);
			cache.textColor = cache.titleView.getTextColors();
			cache.background = header.getBackground();
			header.setTag(cache);
		}

		int realPosition = getRealPosition(position);
		int section = getSectionForPosition(realPosition);

		if (section < 0) {
			section = 0;
			mDisplaySectionHeaders = false;
		} else {
			mDisplaySectionHeaders = true;
		}

		String title = (String) getSections()[section];
		cache.titleView.setText(title);

		if (alpha == 255) {
			// Opaque: use the default background, and the original text color
			header.setBackgroundDrawable(cache.background);
			cache.titleView.setTextColor(cache.textColor);
		} else {
			// Faded: use a solid color approximation of the background, and
			// a translucent text color
			header.setBackgroundColor(Color.rgb(
					Color.red(mPinnedHeaderBackgroundColor) * alpha / 255,
					Color.green(mPinnedHeaderBackgroundColor) * alpha / 255,
					Color.blue(mPinnedHeaderBackgroundColor) * alpha / 255));

			int textColor = cache.textColor.getDefaultColor();
			cache.titleView.setTextColor(Color.argb(alpha,
					Color.red(textColor), Color.green(textColor),
					Color.blue(textColor)));
		}
	}

	final static class PinnedHeaderCache {
		public TextView titleView;
		public ColorStateList textColor;
		public Drawable background;
	}

	@Override
	public void onScrollStateChanged(AbsListView arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

}
