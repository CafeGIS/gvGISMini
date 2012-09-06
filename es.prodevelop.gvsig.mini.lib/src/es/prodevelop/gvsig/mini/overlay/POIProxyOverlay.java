package es.prodevelop.gvsig.mini.overlay;

import java.io.File;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;
import es.alrocar.map.vector.provider.VectorialProvider;
import es.alrocar.map.vector.provider.driver.ProviderDriver;
import es.alrocar.map.vector.provider.driver.poiproxy.POIProxyDriver;
import es.alrocar.map.vector.provider.observer.VectorialProviderListener;
import es.alrocar.map.vector.provider.strategy.IVectorProviderStrategy;
import es.alrocar.map.vector.provider.strategy.impl.BBoxTiledStrategy;
import es.alrocar.map.vector.provider.strategy.impl.TileStrategy;
import es.prodevelop.geodetic.utils.conversion.ConversionCoords;
import es.prodevelop.gvsig.mini.common.CompatManager;
import es.prodevelop.gvsig.mini.common.impl.Tile;
import es.prodevelop.gvsig.mini.exceptions.BaseException;
import es.prodevelop.gvsig.mini.geom.Extent;
import es.prodevelop.gvsig.mini.geom.impl.jts.JTSFeature;
import es.prodevelop.gvsig.mini.legend.PointLegend;
import es.prodevelop.gvsig.mini.map.IMapView;
import es.prodevelop.gvsig.mini.symbol.PointSymbolizer;
import es.prodevelop.gvsig.mini.util.ResourceLoader;
import es.prodevelop.gvsig.mini.util.Utils;
import es.prodevelop.gvsig.mini.utiles.Cancellable;
import es.prodevelop.gvsig.mini.utiles.Utilities;
import es.prodevelop.gvsig.mobile.fmap.proj.CRSFactory;
import es.prodevelop.tilecache.renderer.MapRenderer;

public class POIProxyOverlay extends FeatureOverlay implements
		VectorialProviderListener {

	private Cancellable cancellable;

	private VectorialProvider provider;

	public POIProxyOverlay(Context context, IMapView mapView, String name) {
		super(context, mapView, name);

		initializeProviderFromRenderer(mapView.getMRendererInfo());
	}

	@Override
	public void onExtentChanged(Extent newExtent, int zoomLevel,
			double resolution) {
		if (!isVisible() || isHidden()) {
			return;
		}
		
		if (zoomLevel != getMapView().getZoomLevel()) {
			cancellable.setCanceled(true);
		}

		Tile[] tiles = getMapView().getBaseLayer().getGrid().getTiles();

		if (tiles == null) {
			return;
		}

		final int[][] tilesToRetrieve = new int[tiles.length][2];

		Tile t;
		for (int i = 0; i < tiles.length; i++) {
			t = tiles[i];
			if (t != null && t.tile != null && t.tile.length == 2) {
				tilesToRetrieve[i][0] = tiles[i].tile[1];
				tilesToRetrieve[i][1] = tiles[i].tile[0];
			}
		}

		double[] minXY = ConversionCoords.reproject(newExtent.getMinX(),
				newExtent.getMinY(),
				CRSFactory.getCRS(getMapView().getMRendererInfo().getSRS()),
				CRSFactory.getCRS(provider.getDriver().getSRS()));
		double[] maxXY = ConversionCoords.reproject(newExtent.getMaxX(),
				newExtent.getMaxY(),
				CRSFactory.getCRS(getMapView().getMRendererInfo().getSRS()),
				CRSFactory.getCRS(provider.getDriver().getSRS()));
		Extent currentExtent = new Extent(minXY[0], minXY[1], maxXY[0],
				maxXY[1]);

		cancellable = Utilities.getNewCancellable();
		provider.getDataAsynch(tilesToRetrieve, getMapView().getZoomLevel(),
				currentExtent, cancellable);
	}

	@Override
	public void onLayerChanged(String layerName) {
		initializeProviderFromRenderer(getMapView().getMRendererInfo());
		super.onLayerChanged(layerName);
		
		final int size = this.getOverlaysCount();
		FeatureOverlay overlay;
		for (int i = 0; i < size; i++) {
			overlay = this.getOverlay(i);
			overlay.onLayerChanged(layerName);
		}
	}

	public void initializeProviderFromRenderer(MapRenderer renderer) {
		IVectorProviderStrategy strategy = new BBoxTiledStrategy();
		if (renderer.getSRS().indexOf("900913") != 0) {
			strategy = new TileStrategy();
		}

		String dir;
		try {
			dir = CompatManager.getInstance().getRegisteredContext()
					.getExternalStorageDirectoryPath()
					+ File.separator
					+ Utils.APP_DIR
					+ File.separator
					+ "poiscache" + File.separator;
			provider = new VectorialProvider(renderer, strategy,
					new POIProxyDriver(getName(), dir), this);
		} catch (BaseException e) {
			// TODO Auto-generated catch block
			Log.e("Error", "No hay contexto" + e.getMessage());
		}

		this.setLegend(new PointLegend());
		
		Bitmap b = ResourceLoader.getBitmap(this.getName());
		if (b != null) {
			this.setSymbol(new PointSymbolizer(b, 5));
		} else {
			this.setSymbol(new PointSymbolizer(ResourceLoader
					.getPointSymbolDrawable(), 5));
		}
	}

	@Override
	public void onVectorDataRetrieved(int[] tile, ArrayList data,
			Cancellable cancellable, int zoomLevel) {
		if (cancellable != null && cancellable.getCanceled()) {
			return;
		}

		if (zoomLevel != getMapView().getZoomLevel()) {
			this.cancellable.setCanceled(true);
		}

		if (data != null) {
			final ArrayList<JTSFeature> features = (ArrayList<JTSFeature>) data;
			FeatureOverlay overlay = new FeatureOverlay(this.getContext(),
					getMapView(), this.getSRS());
			String name = getName(zoomLevel, tile);
			if (!this.containsOverlay(name)) {
				overlay.setName(name);
				overlay.setLegend(this.getLegend());
				overlay.setSymbol(this.getSymbol());
				for (JTSFeature feature : features) {
					overlay.addFeature(feature);
				}
				this.addOverlay(overlay);
			}
		} else {
			clearFeatures();
		}
		this.clearFeatures(zoomLevel);

		getMapView().resumeDraw();
	}

	public boolean containsOverlay(String name) {
		final int size = this.getOverlaysCount();
		FeatureOverlay overlay;
		for (int i = 0; i < size; i++) {
			overlay = this.getOverlay(i);
			if (overlay.getName().compareToIgnoreCase(name) == 0) {
				return true;
			}
		}

		return false;
	}

	public JTSFeature getFeature(int[] pixel) {
		final int size = this.getOverlaysCount();
		FeatureOverlay overlay;
		JTSFeature f = null;
		for (int i = 0; i < size; i++) {
			overlay = this.getOverlay(i);
			f = overlay.getFeature(pixel);
			if (f != null) {
				break;
			}
		}

		return f;
	}
	
	public void clearFeatures() {
		super.clearFeatures();
		final int size = this.getOverlaysCount();
		FeatureOverlay overlay;
		for (int i = 0; i < size; i++) {
			overlay = this.getOverlay(i);
			overlay.clearFeatures();
		}
	}

	public void clearFeatures(int zoomLevel) {
		super.clearFeatures();

		final int size = this.getOverlaysCount();
		FeatureOverlay overlay;
		for (int i = 0; i < size; i++) {
			overlay = this.getOverlay(i);
			if (overlay.getName().startsWith(String.valueOf(zoomLevel) + "_")) {
				this.removeOverlay(overlay);
			}
		}
	}

	public String getName(int zoomLevel, int[] tile) {
		return new StringBuffer(zoomLevel).append("_").append(tile[0])
				.append("_").append(tile[1]).toString();
	}

	@Override
	public void onRawDataRetrieved(int[] tile, Object data,
			Cancellable cancellable, ProviderDriver driver, int zoomLevel) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onDraw(Canvas c, IMapView maps) {
		if (isVisible()) {
			super.onDraw(c, maps);

			final int size = this.getOverlaysCount();
			FeatureOverlay overlay;
			for (int i = 0; i < size; i++) {
				overlay = this.getOverlay(i);
				draw(overlay, c, maps);
			}
		}
	}

}
