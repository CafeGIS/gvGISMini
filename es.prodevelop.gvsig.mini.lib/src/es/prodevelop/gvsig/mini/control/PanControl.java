package es.prodevelop.gvsig.mini.control;

import android.util.Log;
import android.view.MotionEvent;
import es.prodevelop.gvsig.mini.map.IMapView;

public class PanControl extends AbstractMapControl implements IMapControl {

	private int touchDownX;
	private int touchDownY;
	private int touchMapOffsetX;
	private int touchMapOffsetY;
	
	public final static String name = "PAN_CONTROL";

	public PanControl() {
		super(name);
	}

	@Override
	public boolean onTouch(MotionEvent event) {
		final IMapView mapView = getMapView();

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			touchDownX = (int) event.getX();
			touchDownY = (int) event.getY();
			Log.w("OFFSET DOWN", touchMapOffsetX + ", " + touchMapOffsetY);
			if (mapView.isScrolling()) {
				mapView.forceFinishScroll();
			}
			mapView.invalidate();
			return true;
		case MotionEvent.ACTION_MOVE:
			touchMapOffsetX = (int) event.getX() - touchDownX;
			touchMapOffsetY = (int) event.getY() - touchDownY;
			
			mapView.setmTouchMapOffsetX(touchMapOffsetX);
			mapView.setmTouchMapOffsetY(touchMapOffsetY);

			// ViewPort.mTouchMapOffsetX = touchMapOffsetX;
			// ViewPort.mTouchMapOffsetY = touchMapOffsetY;

			Log.w("OFFSET MOVE", touchMapOffsetX + ", " + touchMapOffsetY);
			mapView.postInvalidate();
			return true;
		case MotionEvent.ACTION_UP:
			touchMapOffsetX = (int) event.getX() - touchDownX;
			touchMapOffsetY = (int) event.getY() - touchDownY;
			Log.w("OFFSET UP", touchMapOffsetX + ", " + touchMapOffsetY);
			mapView.panTo(touchMapOffsetX, touchMapOffsetY);
		}
		return true;
	}
	
	public int getOffsetX() {
		return touchMapOffsetX;
	}
	
	public int getOffsetY() {
		return touchMapOffsetY;
	}
}
