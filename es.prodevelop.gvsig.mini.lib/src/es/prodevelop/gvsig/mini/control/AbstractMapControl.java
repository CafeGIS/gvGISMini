package es.prodevelop.gvsig.mini.control;

import android.graphics.Canvas;
import android.view.MotionEvent;
import es.prodevelop.gvsig.mini.map.IMapView;

public abstract class AbstractMapControl implements IMapControl {

	private String name;
	private IMapView mapView;

	public AbstractMapControl(String name) {
		this.name = name;
	}
	
	@Override
	public void activate() {
	}

	@Override
	public void deactivate() {
	}
	
	@Override
	public void draw(Canvas c) {
	}

	@Override
	public boolean onDown(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onDoubleTap(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onDoubleTapEvent(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onSingleTapConfirmed(MotionEvent e) {
		return false;
	}

	@Override
	public void setMapView(IMapView mapView) {
		this.mapView = mapView;
	}

	protected IMapView getMapView() {
		return mapView;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean onTouch(MotionEvent event) {
		return false;
	}

}
