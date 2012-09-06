package es.prodevelop.gvsig.mini.control;

import android.view.MotionEvent;

public class DoubleTapZoom extends AbstractMapControl implements IMapControl {

	public DoubleTapZoom() {
		super("Double tap zoom");
	}

	@Override
	public boolean onDoubleTap(MotionEvent e) {
		double[] coords = getMapView().getMRendererInfo().fromPixels(
				new int[] { (int) e.getX(), (int) e.getY() });
		getMapView().getMapViewController()
				.animateTo(coords[0], coords[1], true);
		
		return false;
	}
	
}