package es.prodevelop.gvsig.mini.map;

import java.util.ArrayList;

/**
 * Fires events when selected features change
 * 
 * @author aromeu
 * 
 */
public interface OnSelectFeatureListener {

	/**
	 * Fires this event when new features are selected
	 * 
	 * @param features
	 *            The features selected
	 */
	public void onFeaturesSelected(ArrayList features);

	/**
	 * Fires an event when some features are unselected
	 * 
	 * @param features
	 *            The features unselected
	 */
	public void onFeaturesUnselected(ArrayList features);

}
