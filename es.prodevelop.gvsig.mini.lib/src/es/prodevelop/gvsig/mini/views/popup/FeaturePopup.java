package es.prodevelop.gvsig.mini.views.popup;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import es.prodevelop.gvsig.mini.R;
import es.prodevelop.gvsig.mini.exceptions.BaseException;
import es.prodevelop.gvsig.mini.geom.impl.jts.JTSFeature;

public class FeaturePopup extends FrameLayout {

	private LinearLayout layout;
	private TextView title;
	private TextView snippet;

	public FeaturePopup(Context context, int balloonBottomOffset) {

		super(context);

		setPadding(10, 0, 10, balloonBottomOffset);
		layout = new LinearLayout(context);
		layout.setVisibility(VISIBLE);

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.balloon_overlay, layout);
		title = (TextView) v.findViewById(R.id.balloon_item_title);
		snippet = (TextView) v.findViewById(R.id.balloon_item_snippet);

		ImageView close = (ImageView) v.findViewById(R.id.close_img_button);
		close.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				layout.setVisibility(GONE);
			}
		});

		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.NO_GRAVITY;

		addView(layout, params);

	}

	public void setData(JTSFeature item) {

		layout.setVisibility(VISIBLE);
		String t = item.getText();
		String d;
		try {
			d = item.getString(JTSFeature.DESC);
		} catch (BaseException e) {
			// TODO Auto-generated catch block
			d = "No description";
		}

		if (t == null) {
			t = "Esto es una prueba";
		}
		if (title != null) {
			title.setVisibility(VISIBLE);
			title.setText(t);
		} else {
			title.setVisibility(GONE);
		}
		if (d != null) {
			snippet.setVisibility(VISIBLE);
			snippet.setText(d);
		} else {
			snippet.setVisibility(GONE);
		}

	}

}
