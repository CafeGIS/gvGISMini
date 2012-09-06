package es.prodevelop.gvsig.mini.action;

public interface AddLayerActionListener {

	public static final int REPLACE_BASE_LAYER = 0;
	public static final int ADD_RASTER_LAYER = 1;
	public static final int ADD_VECTOR_LAYER = 2;
	public static final int ADD_REMOTE_VECTOR_LAYER = 3;

	public void onAddLayer(int type);

}
