package es.prodevelop.gvsig.mini.app.examples;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import es.prodevelop.gvsig.mini.app.GPEMap;
import es.prodevelop.gvsig.mini.app.Initializer;

public class ExampleMap extends GPEMap {

	public void onCreate(Bundle bundle) {
		this.initialize();
		super.onCreate(bundle);
	}

	public void initialize() {
		Initializer.getInstance().addInitializeListener(
				new InitializerHandler());
		try {
			Initializer.getInstance().initialize(getApplicationContext());
		} catch (Exception e) {
			Log.e("ExampleMap", e.getMessage());
		}
	}

	private class InitializerHandler extends Handler {

		public void handleMessage(final Message msg) {
			try {
				switch (msg.what) {
				case Initializer.INITIALIZE_STARTED:
					break;
				case Initializer.INITIALIZE_FINISHED:					
					Log.e("ExampleMap", "Initializer finished");
					break;
				default:
					break;
				}
			} catch (Exception e) {
				Log.e("ExampleMap", e.getMessage());
			}
		}
	}
}
