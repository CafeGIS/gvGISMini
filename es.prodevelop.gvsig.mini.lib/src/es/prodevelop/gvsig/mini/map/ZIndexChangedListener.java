package es.prodevelop.gvsig.mini.map;

/**
 * This should be implemented by the IMapView so it can reorder the layers when
 * the z index of any layer has changed
 * 
 * @author aromeu
 * 
 */
public interface ZIndexChangedListener {

	/**
	 * Fired when the zindex of a layer changes
	 */
	public void onZIndexChanged();

}
