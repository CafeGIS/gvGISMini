package es.prodevelop.gvsig.mini.app.examples;

import java.io.File;
import java.io.IOException;

import android.os.Bundle;
import android.util.Log;
import es.prodevelop.gvsig.mini.gpe.driver.DriverGPEFactory;
import es.prodevelop.gvsig.mini.gpe.driver.VectorGPEDriver;

public class KMLLayerMap extends ExampleMap {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		File file = new File("/sdcard/cv-point.kml");

		VectorGPEDriver driver = DriverGPEFactory.createVectorialDriver(
				this.getMapView(), this);
		try {
			driver.open(file);
		} catch (IOException e) {
			Log.e("KMLLayerMap", "on open kml file", e);
		}
		driver.initialize();

		if (driver.getErrorHandler().getErrorsSize() > 0) {
			// Errors occured while loading the KML file
			StringBuffer errores = new StringBuffer();
			for (int p = 0; p < driver.getErrorHandler().getErrorsSize(); p++) {
				errores.append(driver.getErrorHandler().getErrorAt(p));
			}
			Log.e("KMLLayerMap", errores.toString());
		}
	}
}
