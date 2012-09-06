package es.prodevelop.gvsig.mini.activities;

public class QuickActionEvent {
	
	public static final int TYPE_REMOVE_LAYER = 0;
	public static final int TYPE_ZOOM_EXTENT = 1;
	public static final int TYPE_LIST_FEATURES = 2;
	
	public static final int TYPE_REMOVE_FEATURE = 3;
	public static final int TYPE_ZOOM_FEATURE = 4;
	
	private int type = -1;
	
	public QuickActionEvent(int type) {
		this.type = type;
	}
	
	public int getType() {
		return type;
	}

}
