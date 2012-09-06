package es.prodevelop.gvsig.mini.activities.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.ericharlow.DragNDrop.DragNDropAdapter;

import es.prodevelop.gvsig.mini.R;
import es.prodevelop.gvsig.mini.activities.ActivityBundlesManager;
import es.prodevelop.gvsig.mini.activities.TOCActivity;
import es.prodevelop.gvsig.mini.map.IMapView;
import es.prodevelop.gvsig.mini.overlay.FeatureOverlay;
import es.prodevelop.gvsig.mini.overlay.MapOverlay;
import es.prodevelop.gvsig.mini.views.LegendView;

public class TOCAdapter extends DragNDropAdapter {

	private Context context;
	public ArrayList<Boolean> visibleLayers;
	private TOCActivity activity;

	public TOCAdapter(TOCActivity context, int[] itemLayouts, int[] itemIDs,
			ArrayList<String> content, ArrayList<Boolean> visibleLayers) {
		super(context, itemLayouts, itemIDs, content);
		this.context = context;
		this.activity = context;
		this.visibleLayers = visibleLayers;
	}

	/**
	 * Make a view to hold each row.
	 * 
	 * @see android.widget.ListAdapter#getView(int, android.view.View,
	 *      android.view.ViewGroup)
	 */
	public View getView(final int position, View convertView, ViewGroup parent) {
		// A ViewHolder keeps references to children views to avoid unneccessary
		// calls
		// to findViewById() on each row.
		final ViewHolder holder;

		// When convertView is not null, we can reuse it directly, there is no
		// need
		// to reinflate it. We only inflate a new View when the convertView
		// supplied
		// by ListView is null.
		if (convertView == null) {
			convertView = mInflater.inflate(mLayouts[0], null);

			// Creates a ViewHolder and store references to the two children
			// views
			// we want to bind data to.
			holder = new ViewHolder();
			holder.text = (TextView) convertView.findViewById(mIds[0]);
			holder.dragdrop = (ImageView) convertView
					.findViewById(R.id.dragdrop);
			holder.visible = (CheckBox) convertView
					.findViewById(R.id.layervisible);
			holder.legend = (LegendView) convertView
					.findViewById(R.id.layerlegend);
			holder.optionsButton = (ImageButton) convertView
					.findViewById(R.id.layer_options_btn);
			holder.optionsButton.setVisibility(View.VISIBLE);
			holder.optionsButton.setFocusable(true);

			holder.visible
					.setOnCheckedChangeListener(new OnCheckedChangeListener() {

						@Override
						public void onCheckedChanged(CompoundButton buttonView,
								boolean isChecked) {
							// Changes the visibility of a layer
							final IMapView mapView = ActivityBundlesManager
									.getInstance().getMapView();
							CharSequence text = buttonView.getText();
							if (text != null) {
								MapOverlay overlay = mapView.getOverlay(text
										.toString());
								if (overlay != null) {
									overlay.setVisible(isChecked);
								}
							}

							TOCAdapter.this.visibleLayers = ((TOCActivity) context)
									.buildLayerVisibilityList();
						}
					});

			convertView.setTag(holder);
		} else {
			// Get the ViewHolder back to get fast access to the TextView
			// and the ImageView.
			holder = (ViewHolder) convertView.getTag();
		}

		MapOverlay overlay = this.getMapOverlayFromName(position);
		if (overlay != null && overlay instanceof FeatureOverlay) {
			holder.legend.setLegend(((FeatureOverlay) overlay).getLegend());
			holder.legend.setSymbol(((FeatureOverlay) overlay).getSymbol());
		} else {
			holder.legend.setLegend(null);
		}

		if (holder.optionsButton != null) {
			holder.optionsButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					activity.getItemClickListener().onClick(position, getItem(position),
							holder.optionsButton);
				}
			});
		}

		// Bind the data efficiently with the holder.
		// holder.text.setText(mContent.get(position));
		holder.visible.setText(mContent.get(position));
		holder.visible.setChecked(visibleLayers.get(position));

		if (position == 0) {
			holder.text.setText(mContent.get(position));
			holder.dragdrop.setVisibility(View.GONE);
			holder.visible.setVisibility(View.GONE);
			holder.legend.setVisibility(View.GONE);
		} else {
			holder.text.setText("");
			holder.dragdrop.setVisibility(View.VISIBLE);
			holder.visible.setVisibility(View.VISIBLE);

			if (holder.legend.getLegend() == null) {
				holder.legend.setVisibility(View.GONE);
			} else {
				holder.legend.setVisibility(View.VISIBLE);
			}
		}

		return convertView;
	}

	public MapOverlay getMapOverlayFromName(final int position) {
		final IMapView mapView = ActivityBundlesManager.getInstance()
				.getMapView();
		String layerName = TOCAdapter.this.mContent.get(position);
		MapOverlay overlay = mapView.getOverlay(layerName);
		return overlay;
	}

	static class ViewHolder {
		TextView text;
		ImageView dragdrop;
		CheckBox visible;
		LegendView legend;
		ImageButton optionsButton;
	}

	public void onDrop(int from, int to) {
		super.onDrop(from, to);
		boolean fromB = this.visibleLayers.get(from);
		boolean toB = this.visibleLayers.get(to);
		this.visibleLayers.set(to, fromB);
		this.visibleLayers.set(from, toB);
	}

}
