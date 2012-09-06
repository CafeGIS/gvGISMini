package es.prodevelop.gvsig.mini.gpe.driver;

import android.content.Context;
import es.prodevelop.gvsig.mini.map.IMapView;


public class DriverGPEFactory {

	public static VectorGPEDriver createVectorialDriver(IMapView mapView,
			Context context) {
		VectorGPEDriver driver = new VectorGPEDriver(context, mapView);
		return driver;
	}
}
