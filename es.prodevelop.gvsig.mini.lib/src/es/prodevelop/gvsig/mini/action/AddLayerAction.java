package es.prodevelop.gvsig.mini.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;

import com.markupartist.android.widget.ActionBar.AbstractAction;

import es.prodevelop.gvsig.mini.R;
import es.prodevelop.gvsig.mini.util.ActionItem;
import es.prodevelop.gvsig.mini.util.QuickAction;

public class AddLayerAction extends AbstractAction {

	private Logger log = LoggerFactory.getLogger(AddLayerAction.class);
	private Context context;
	private AddLayerActionListener listener;

	public AddLayerAction(int drawable, Context context,
			AddLayerActionListener listener) {
		super(drawable);
		this.context = context;
		this.listener = listener;
	}

	public void addActions(final QuickAction qa) {
		final ActionItem replaceBaseLayer = new ActionItem();

		replaceBaseLayer.setTitle(context.getResources().getString(
				R.string.replace_base_layer));
		replaceBaseLayer.setIcon(context.getResources().getDrawable(
				R.drawable.layer_replace_base_layer));
		replaceBaseLayer.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				listener.onAddLayer(AddLayerActionListener.REPLACE_BASE_LAYER);
				qa.dismiss();
			}
		});

		qa.addActionItem(replaceBaseLayer);

		// final ActionItem addRasterLayer = new ActionItem();
		//
		// addRasterLayer.setTitle(context.getResources().getString(
		// R.string.add_raster_layer));
		// addRasterLayer.setIcon(context.getResources().getDrawable(
		// R.drawable.layer_raster_add));
		// addRasterLayer.setOnClickListener(new OnClickListener() {
		// @Override
		// public void onClick(View v) {
		// listener.onAddLayer(AddLayerActionListener.ADD_RASTER_LAYER);
		// }
		// });
		//
		// qa.addActionItem(addRasterLayer);

		final ActionItem addVectorLayer = new ActionItem();

		addVectorLayer.setTitle(context.getResources().getString(
				R.string.add_vector_from_disk));
		addVectorLayer.setIcon(context.getResources().getDrawable(
				R.drawable.layer_vector_add));
		addVectorLayer.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				listener.onAddLayer(AddLayerActionListener.ADD_VECTOR_LAYER);
				qa.dismiss();
			}
		});

		qa.addActionItem(addVectorLayer);

		final ActionItem addRemoteLayer = new ActionItem();

		addRemoteLayer.setTitle(context.getResources().getString(
				R.string.add_vector_remote));
		addRemoteLayer.setIcon(context.getResources().getDrawable(
				R.drawable.layer_vector_add_remote));
		addRemoteLayer.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				listener.onAddLayer(AddLayerActionListener.ADD_REMOTE_VECTOR_LAYER);
				qa.dismiss();
			}
		});

		qa.addActionItem(addRemoteLayer);
	}

	@Override
	public void performAction(View view) {
		final QuickAction qa = new QuickAction(view);

		addActions(qa);

		qa.setAnimStyle(QuickAction.ANIM_AUTO);

		qa.show();
	}

}
