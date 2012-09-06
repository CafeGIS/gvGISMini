package es.prodevelop.gvsig.mini.action;

import android.view.View;

import com.markupartist.android.widget.ActionBar.SelectableAction;

import es.prodevelop.gvsig.mini.control.IMapControl;
import es.prodevelop.gvsig.mini.map.IMapView;

public class MapControlAction implements SelectableAction {

	private IMapView mapView;
	private int drawable;
	private IMapControl control;

	public MapControlAction(IMapView mapView, int drawable, IMapControl control) {
		this.mapView = mapView;
		this.drawable = drawable;
		this.control = control;
	}

	@Override
	public int getDrawable() {
		return drawable;
	}

	@Override
	public void performAction(View view) {
		mapView.setExclusiveControl(control);
	}

	@Override
	public boolean isSelected() {
		return mapView.getExclusiveControl() == control;
	}

}
