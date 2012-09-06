package es.prodevelop.gvsig.mini.overlay;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Handler;
import es.prodevelop.geodetic.utils.conversion.ConversionCoords;
import es.prodevelop.gvsig.mini.R;
import es.prodevelop.gvsig.mini.activities.OSSettingsUpdater;
import es.prodevelop.gvsig.mini.activities.Settings;
import es.prodevelop.gvsig.mini.common.IBitmap;
import es.prodevelop.gvsig.mini.common.IContext;
import es.prodevelop.gvsig.mini.common.android.BitmapAndroid;
import es.prodevelop.gvsig.mini.common.android.HandlerAndroid;
import es.prodevelop.gvsig.mini.common.impl.Tile;
import es.prodevelop.gvsig.mini.context.ItemContext;
import es.prodevelop.gvsig.mini.geom.Extent;
import es.prodevelop.gvsig.mini.geom.Feature;
import es.prodevelop.gvsig.mini.geom.Pixel;
import es.prodevelop.gvsig.mini.geom.impl.base.Point;
import es.prodevelop.gvsig.mini.map.Grid;
import es.prodevelop.gvsig.mini.map.IMapView;
import es.prodevelop.gvsig.mini.map.LoadCallbackHandler;
import es.prodevelop.gvsig.mini.map.MapView;
import es.prodevelop.gvsig.mini.projection.TileConversor;
import es.prodevelop.gvsig.mini.util.Utils;
import es.prodevelop.gvsig.mini.utiles.Cancellable;
import es.prodevelop.gvsig.mini.yours.Route;
import es.prodevelop.gvsig.mini.yours.RouteManager;
import es.prodevelop.gvsig.mobile.fmap.proj.CRSFactory;
import es.prodevelop.gvsig.mobile.fmap.proj.Projection;
import es.prodevelop.tilecache.layers.Layers;
import es.prodevelop.tilecache.provider.TileProvider;
import es.prodevelop.tilecache.provider.android.AndroidTileProvider;
import es.prodevelop.tilecache.provider.filesystem.strategy.ITileFileSystemStrategy;
import es.prodevelop.tilecache.provider.filesystem.strategy.impl.FileSystemStrategyManager;
import es.prodevelop.tilecache.renderer.MapRenderer;
import es.prodevelop.tilecache.renderer.OSMMercatorRenderer;
import es.prodevelop.tilecache.renderer.wms.OSRenderer;
import es.prodevelop.tilecache.util.Tags;
import es.prodevelop.tilecache.util.Utilities;

public class TileOverlay extends MapOverlay {

	private final static Logger log = LoggerFactory
			.getLogger(TileOverlay.class);

	private Cancellable cancellable = Utilities.getNewCancellable();

	private Grid grid;
	private MapRenderer renderer;
	private TileProvider tileProvider;
	private boolean allValid = true;
	private IContext androidContext;
	private boolean isTheBaseLayer = false;	

	public TileOverlay(Context context, IMapView mapView, String name,
			MapRenderer renderer, IContext androidContext,
			boolean isTheBaseLayer) {
		super(context, mapView, name);
		this.androidContext = androidContext;
		this.isTheBaseLayer = isTheBaseLayer;
		grid = new Grid((MapView) mapView);
		this.renderer = renderer;
		grid.setRenderer(renderer);
		instantiateTileProviderfromSettings();
		mapView.addExtentChangedListener(grid);
	}
	
	public boolean isTheBaseLayer() {
		return isTheBaseLayer;
	}

	public TileProvider getTileProvider() {
		return tileProvider;
	}

	public void setTileProvider(TileProvider tileProvider) {
		this.tileProvider = tileProvider;
	}

	public boolean allValid() {
		return allValid;
	}

	public MapRenderer getRenderer() {
		return renderer;
	}

	public void setRenderer(MapRenderer renderer) {
		this.renderer = renderer;
	}

	@Override
	public ItemContext getItemContext() {
		// TODO Auto-generated method stub
		return null;
	}

	public Extent reprojectExtent(Extent baseExtent) {
		String fromSRS = getMapView().getBaseLayer().getRenderer().getSRS();
		String toSRS = this.getRenderer().getSRS();
		Projection fromProj = CRSFactory.getCRS(fromSRS);
		Projection toProj = CRSFactory.getCRS(toSRS);
		double[] minXY = ConversionCoords.reproject(baseExtent.getMinX(),
				baseExtent.getMinY(), fromProj, toProj);
		double[] maxXY = ConversionCoords.reproject(baseExtent.getMaxX(),
				baseExtent.getMaxY(), fromProj, toProj);
		Extent reprExtent = new Extent(minXY[0], minXY[1], maxXY[0], maxXY[1]);
		return reprExtent;
	}

	public double[] reprojectCoords(Point coords) {
		return reprojectCoords(coords.getX(), coords.getY());
	}

	public double[] reprojectCoords(double x, double y) {
		String fromSRS = getMapView().getBaseLayer().getRenderer().getSRS();
		String toSRS = this.getRenderer().getSRS();
		Projection fromProj = CRSFactory.getCRS(fromSRS);
		Projection toProj = CRSFactory.getCRS(toSRS);
		double[] minXY = ConversionCoords.reproject(x, y, fromProj, toProj);
		return minXY;
	}

	@Override
	public void onExtentChanged(Extent newExtent, int zoomLevel,
			double resolution) {
		Extent reprExtent = reprojectExtent(newExtent);
		grid.calcGrid(reprExtent);
	}

	@Override
	public String getName() {
		if (this.renderer != null)
			return this.renderer.getNAME() + " - " + super.getName();
		else
			return super.getName();
	}

	@Override
	public void onLayerChanged(String layerName) {
		try {
			if (!isTheBaseLayer) {
				return;
			}

			getMapView().clearCache();

			MapRenderer previous = this.getRenderer();
			MapRenderer renderer = Layers.getInstance().getRenderer(layerName);

			if (renderer instanceof OSRenderer)
				OSSettingsUpdater.synchronizeRendererWithSettings(
						(OSRenderer) renderer, getContext());

			Tags.DEFAULT_TILE_SIZE = renderer.getMAPTILE_SIZEPX();
			es.prodevelop.gvsig.mini.utiles.Tags.DEFAULT_TILE_SIZE = renderer
					.getMAPTILE_SIZEPX();
			TileConversor.pixelsPerTile = renderer.getMAPTILE_SIZEPX();

			tileProvider.destroy();

			Utils.BUFFER_SIZE = 2;
			instantiateTileProviderfromSettings();
			Extent previousExtent = getMapView().getViewPort().calculateExtent(
					getMapView().getMapWidth(), getMapView().getMapHeight(),
					previous.getCenter());
			if (renderer != null)
				this.setRenderer(renderer);

			try {
				final Route route = RouteManager.getInstance()
						.getRegisteredRoute();
				if (route != null)
					renderer.reprojectGeometryCoordinates(route.getRoute()
							.getFeatureAt(0).getGeometry(), previous.getSRS());
			} catch (Exception e) {
				log.error("reprojecting route:", e);
			}

			double[] newCenter = previous.transformCenter(renderer.getSRS());
			boolean contains = renderer.getExtent().contains(newCenter);

			double[] minXY = ConversionCoords.reproject(
					previousExtent.getMinX(), previousExtent.getMinY(),
					CRSFactory.getCRS(previous.getSRS()),
					CRSFactory.getCRS(renderer.getSRS()));
			double[] maxXY = ConversionCoords.reproject(
					previousExtent.getMaxX(), previousExtent.getMaxY(),
					CRSFactory.getCRS(previous.getSRS()),
					CRSFactory.getCRS(renderer.getSRS()));
			Extent currentExtent = new Extent(minXY[0], minXY[1], maxXY[0],
					maxXY[1]);
			if (renderer.isOffline()) {

				renderer.centerOnBBox();
				getMapView().getMapViewController().setMapCenter(
						renderer.getCenter().getX(),
						renderer.getCenter().getY());
				getMapView().getMapViewController().zoomToExtent(
						renderer.getOfflineExtent(), true);
				Settings.getInstance()
						.updateStringSharedPreference(
								getContext().getText(
										R.string.settings_key_list_strategy)
										.toString(),
								ITileFileSystemStrategy.FLATX, getContext());
				Settings.getInstance().updateBooleanSharedPreference(
						getContext()
								.getText(R.string.settings_key_offline_maps)
								.toString(), new Boolean(true), getContext());
				instantiateTileProviderfromSettings();

			} else {
				if (contains) {
					getMapView().getMapViewController().setMapCenter(
							newCenter[0], newCenter[1]);
					if (previous instanceof OSMMercatorRenderer
							&& renderer instanceof OSMMercatorRenderer) {
						getMapView().getMapViewController().setZoomLevel(
								previous.getZoomLevel(), false);
					} else {
						getMapView().getMapViewController().zoomToExtent(
								currentExtent, true);
					}
				} else {
					renderer.centerOnBBox();
					getMapView().getMapViewController().setMapCenter(
							renderer.getCenter().getX(),
							renderer.getCenter().getY());
					// Point p = renderer.getExtent().getCenter();
					// this.setMapCenter(p.getX(), p.getY());
				}
			}

		} catch (Exception e) {
			log.error("onlayerchanged:", e);
		} finally {

		}
	}

	@Override
	public void onDraw(Canvas c, IMapView maps) {
		boolean someTileNull = false;
		boolean areAllValid = true;

		if (!this.isTheBaseLayer) {
			this.renderer.setZoomLevel(getMapView().getBaseLayer()
					.getRenderer().getZoomLevel());
			final double[] newCenter = reprojectCoords(getMapView()
					.getBaseLayer().getRenderer().getCenter().getX(),
					getMapView().getBaseLayer().getRenderer().getCenter()
							.getY());
			this.renderer.setCenter(newCenter[0], newCenter[1]);
		}

		grid.setRenderer(this.renderer);
		Extent viewExtent = getMapView().getViewPort().calculateExtent(
				getMapView().getMapWidth(), getMapView().getMapHeight(),
				getMapView().getBaseLayer().getRenderer().getCenter());

		if (this.isTheBaseLayer) {
			grid.calcGrid(viewExtent);
		} else {
			grid.calcGrid(reprojectExtent(viewExtent));
		}

		Tile[] tiles = grid.getTiles();

		final int length = tiles.length;
		Tile temp;
		IBitmap currentMapTile;
		if (isTheBaseLayer)
			c.drawRect(0, 0, maps.getMapWidth(), maps.getMapHeight(),
					Paints.whitePaint);
		// bufferCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
		for (int j = 0; j < length; j++) {
			temp = tiles[j];
			if (temp != null) {
				currentMapTile = this.tileProvider.getMapTile(temp,
						cancellable, null);
				if (currentMapTile != null) {
					if (areAllValid
							&& !((BitmapAndroid) currentMapTile).isValid) {
						areAllValid = false;
					}

					// /*****/
					// Shader gradientShader = new LinearGradient(0, 0,
					// mapWidth, mapHeight,
					// Color.argb(0, 0, 0, 0), Color.argb(255, 0,
					// 0, 0), TileMode.CLAMP);
					//
					// Shader bitmapShader = new BitmapShader(
					// (Bitmap) currentMapTile.getBitmap(),
					// TileMode.CLAMP, TileMode.CLAMP);
					//
					// Shader composeShader = new ComposeShader(
					// bitmapShader, gradientShader,
					// new PorterDuffXfermode(Mode.DST_OUT));
					// Paint mPaint = new Paint();
					// mPaint.setShader(composeShader);
					// /******/
					if (!((BitmapAndroid) currentMapTile).isValid) {
						if (!this.isTheBaseLayer) {

						} else {
							c.drawBitmap((Bitmap) currentMapTile.getBitmap(),
									temp.distanceFromCenter.getX(),
									temp.distanceFromCenter.getY(),
									Paints.normalPaint);
						}
					} else {
						c.drawBitmap((Bitmap) currentMapTile.getBitmap(),
								temp.distanceFromCenter.getX(),
								temp.distanceFromCenter.getY(),
								Paints.normalPaint);
					}

					// System.out.println("paint tile");

					// temp.destroy();
					temp = null;
				} else {
					// System.out.println("tile null");
					areAllValid = false;
					someTileNull = true;

					// c.drawText("Loading", TileRaster.mapWidth / 2,
					// TileRaster.mapHeight - 35, Paints.mPaint);
					// c.drawBitmap(bufferBitmap,
					// TileRaster.lastTouchMapOffsetX,
					// TileRaster.lastTouchMapOffsetY,
					// Paints.normalPaint);
				}
			}
		}
		tiles = null;

		if (!someTileNull) {
			this.tileProvider.setLastZoomLevelRequested(maps.getZoomLevel());
		}

		allValid = areAllValid;
	}

	@Override
	protected void onDrawFinished(Canvas c, IMapView maps) {
		// TODO Auto-generated method stub

	}

	@Override
	public Feature getNearestFeature(Pixel pixel) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public Grid getGrid() {
		return grid;
	}

	public void instantiateTileProviderfromSettings() {
		Handler lh = new LoadCallbackHandler(getMapView()
				.getInvalidationHandler());
		int mode = TileProvider.MODE_ONLINE;
		String tileName = "tile.gvSIG";
		String dot = ".";
		String strategy = ITileFileSystemStrategy.FLATX;
		ITileFileSystemStrategy t = FileSystemStrategyManager.getInstance()
				.getStrategyByName(strategy);

		boolean offline = false;
		try {
			offline = Settings.getInstance().getBooleanValue(
					getContext().getText(R.string.settings_key_offline_maps)
							.toString());

			if (offline)
				mode = TileProvider.MODE_OFFLINE;
			else
				mode = Settings.getInstance().getIntValue(
						getContext().getText(R.string.settings_key_list_mode)
								.toString());
		} catch (NoSuchFieldError e) {

		}

		try {
			tileName = Settings.getInstance().getStringValue(
					getContext().getText(R.string.settings_key_tile_name)
							.toString());
		} catch (NoSuchFieldError e) {

		}

		try {
			strategy = Settings.getInstance().getStringValue(
					getContext().getText(R.string.settings_key_list_strategy)
							.toString());
		} catch (NoSuchFieldError e) {

		}

		String tileSuffix = dot + tileName;
		t = FileSystemStrategyManager.getInstance().getStrategyByName(strategy);
		t.setTileNameSuffix(tileSuffix);

		this.tileProvider = new AndroidTileProvider(this.androidContext,
				new HandlerAndroid(lh), getMapView().getMapWidth(),
				getMapView().getMapHeight(), 256, mode, t);
	}

}
