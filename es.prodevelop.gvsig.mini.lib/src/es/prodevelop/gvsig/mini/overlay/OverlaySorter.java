package es.prodevelop.gvsig.mini.overlay;

import es.prodevelop.gvsig.mini.common.impl.CollectionQuickSort;

public class OverlaySorter extends CollectionQuickSort {

	@Override
	public boolean less(Object x, Object y) {
		MapOverlay overlayX = (MapOverlay) x;
		MapOverlay overlayY = (MapOverlay) y;

		if (overlayX.getzIndex() < overlayY.getzIndex()) {
			return true;
		} else if (overlayX.getzIndex() > overlayY.getzIndex()) {
			return false;
		} else {
			return compareNames(overlayX.getName().toLowerCase(), overlayY
					.getName().toLowerCase());
		}
	}

	public boolean compareNames(String xx, String yy) {
		char ix = xx.charAt(0);
		char iy = yy.charAt(0);

		int length = Math.min(xx.length(), yy.length());

		for (int i = 1; i < length; i++) {
			if (ix == iy) {
				ix = xx.charAt(i);
				iy = yy.charAt(i);
			} else {
				return (ix < iy);
			}
		}
		return (ix < iy);
	}

}
