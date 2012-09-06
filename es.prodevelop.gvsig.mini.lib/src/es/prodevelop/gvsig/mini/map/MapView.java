/* gvSIG Mini. A free mobile phone viewer of free maps.
 *
 * Copyright (C) 2011 Prodevelop.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,USA.
 *
 * For more information, contact:
 *
 *   Prodevelop, S.L.
 *   Pza. Don Juan de Villarrasa, 14 - 5
 *   46001 Valencia
 *   Spain
 *
 *   +34 963 510 612
 *   +34 963 510 968
 *   prode@prodevelop.es
 *   http://www.prodevelop.es
 *
 *   gvSIG Mini has been partially funded by IMPIVA (Instituto de la PequeÔøΩa y
 *   Mediana Empresa de la Comunidad Valenciana) &
 *   European Union FEDER funds.
 *   
 *   2011.
 *   author Alberto Romeu aromeu@prodevelop.es
 *   author Fernando González fernando.gonzalez@geomati.co
 */

package es.prodevelop.gvsig.mini.map;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Scroller;
import es.prodevelop.geodetic.utils.conversion.ConversionCoords;
import es.prodevelop.gvsig.mini.activities.ActivityBundlesManager;
import es.prodevelop.gvsig.mini.common.IContext;
import es.prodevelop.gvsig.mini.common.impl.Tile;
import es.prodevelop.gvsig.mini.context.ItemContext;
import es.prodevelop.gvsig.mini.control.IMapControl;
import es.prodevelop.gvsig.mini.exceptions.BaseException;
import es.prodevelop.gvsig.mini.geom.Extent;
import es.prodevelop.gvsig.mini.geom.Feature;
import es.prodevelop.gvsig.mini.geom.Pixel;
import es.prodevelop.gvsig.mini.geom.Point;
import es.prodevelop.gvsig.mini.geom.android.GPSPoint;
import es.prodevelop.gvsig.mini.map.MultiTouchController.PointInfo;
import es.prodevelop.gvsig.mini.map.MultiTouchController.PositionAndScale;
import es.prodevelop.gvsig.mini.overlay.AcetateOverlay;
import es.prodevelop.gvsig.mini.overlay.AssignFirstPointOverlay;
import es.prodevelop.gvsig.mini.overlay.MapOverlay;
import es.prodevelop.gvsig.mini.overlay.OverlaySorter;
import es.prodevelop.gvsig.mini.overlay.Paints;
import es.prodevelop.gvsig.mini.overlay.TileOverlay;
import es.prodevelop.gvsig.mini.overlay.ViewSimpleLocationOverlay;
import es.prodevelop.gvsig.mini.util.ResourceLoader;
import es.prodevelop.gvsig.mini.util.Utils;
import es.prodevelop.gvsig.mini.utiles.Cancellable;
import es.prodevelop.gvsig.mini.utiles.WorkQueue;
import es.prodevelop.gvsig.mobile.fmap.proj.CRSFactory;
import es.prodevelop.tilecache.provider.TileProvider;
import es.prodevelop.tilecache.renderer.MapRenderer;
import es.prodevelop.tilecache.util.Utilities;

public class MapView extends SurfaceView implements IMapView {

	// FIXME FAIL!
	public static boolean CLEAR_ROUTE = false;
	public static boolean CLEAR_OFF_POIS = false;

	public static int MAP_WIDTH = 0;
	public static int MAP_HEIGHT = 0;

	private final static Logger log = LoggerFactory.getLogger(MapView.class);

	/**
	 * State of the drawing machine. If dirty, next call to
	 * {@link #draw(Canvas)} will create a new {@link #surfaceImage}. If drawn,
	 * the last created image is shown. The machine will be in process while
	 * painting. Setting the state to dirty will cancel the drawing.
	 */
	private static final int DRAWING_STATUS_DIRTY = 0;
	private static final int DRAWING_STATUS_IN_PROCESS = 1;
	private static final int DRAWING_STATUS_DRAWN = 2;

	private List<ExtentChangedListener> extentChangedListeners = new ArrayList<ExtentChangedListener>();
	private List<IMapControl> controls = new ArrayList<IMapControl>();
	private IMapControl exclusive;

	private Point scrollingCenter;
	private boolean isScrolling = false;
	private boolean invalidateLongPress = false;
	private TileRasterThread surfaceThread;

	private Cancellable cancellable = Utilities.getNewCancellable();
	private AcetateOverlay acetate;
	private ViewSimpleLocationOverlay locationOverlay;

	private float xOff = 0.0f, yOff = 0.0f, relativeScale = 1.0f;
	private PointInfo currTouchPoint;
	private MultiTouchController<Object> multiTouchController;

	private int tempZoomLevel = 0;
	private int mBearing = 0;

	// private MapRenderer mRendererInfo;
	// protected TileProvider mTileProvider;

	private final GestureDetector mGestureDetector = new GestureDetector(
			new GestureListener(this));

	protected List<MapOverlay> mOverlays = new ArrayList<MapOverlay>();

	private int previousRotation = 999;
	private int pixelX;
	private int pixelY;
	private int mTouchMapOffsetX;
	private int mTouchMapOffsetY;
	private int centerPixelX = 0;
	private int centerPixelY = 0;

	private boolean lastTouchEventProcessed = false;

	private IContext androidContext;
	private MotionEvent lastTouchEvent;
	private Feature selectedFeature = null;

	private Canvas bufferCanvas = new Canvas();
	private Bitmap bufferBitmap;

	private Canvas zoomCanvas = new Canvas();
	private Bitmap zoomBitmap;

	private Scaler mScaler;
	private boolean scale = false;

	private SurfaceHolder holder;
	private boolean hasSurface;
	private SimpleInvalidationHandler simpleInvalidationHandler = new SimpleInvalidationHandler(
			this);
	private Scroller mScroller;

	private MapState mapState;
	private IMapViewController controller;
	private ViewPort viewport;
	private Handler mapHandler;

	private boolean navigationMode = false;

	private ArrayList<MapViewListener> mapViewListeners = new ArrayList<MapViewListener>();
	private ArrayList<OnSelectFeatureListener> selectFeatureListener = new ArrayList<OnSelectFeatureListener>();
	private ArrayList<ZIndexChangedListener> zIndexChangedListeners = new ArrayList<ZIndexChangedListener>();
	private OverlaySorter sorter = new OverlaySorter();
	// private Grid grid;
	private TileOverlay baseOverlay;

	/**
	 * Initial status: create {@link #surfaceImage} in the next call to
	 * {@link #draw(Canvas)}
	 */
	private int drawingStatus = DRAWING_STATUS_DIRTY;
	/**
	 * Image to be shown, and optionally created by {@link #draw(Canvas)}
	 */
	private Bitmap surfaceImage;

	/**
	 * The Constructor.
	 * 
	 * @param context
	 *            The context (Usually the Map activity)
	 * @param aRendererInfo
	 *            A MapRenderer
	 * @param width
	 *            The width of the view in pixels
	 * @param height
	 *            The height of the view in pixels
	 */
	public MapView(final Context context, final Handler mapHandler,
			final IContext androidContext, final MapRenderer aRendererInfo,
			int width, int height) {
		super(context);
		this.androidContext = androidContext;
		this.mapHandler = mapHandler;

		try {
			this.initialize(context, width, height, aRendererInfo);
		} catch (Exception e) {
			log.error("onCreate:", e);
		} catch (OutOfMemoryError e) {
			System.gc();
			log.error("onCreate:", e);
		}
	}

	public void initialize(Context context, int width, int height,
			final MapRenderer renderer) throws BaseException {
		init();
		this.mapState = new MapState(this);
		this.controller = new MapViewController(this);
		this.mScroller = new Scroller(context);
		multiTouchController = new MultiTouchController<Object>(this,
				getResources(), false);

		this.mScaler = new Scaler(context, new LinearInterpolator());
		// this.instantiateTileProviderfromSettings();
		this.setFocusable(true);
		this.setClickable(true);
		this.setLongClickable(true);
		this.initializeCanvas(width, height);
		this.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				/*
				 * Pause draw during controls handling so that it is possible to
				 * set several parameters atomically (without calls to draw in
				 * the meanwhile)
				 */
				pauseDraw();
				for (IMapControl control : controls) {
					try {
						control.onTouch(event);
					} catch (Exception e) {
						Log.e("MapView", "MapControl exception", e);
					}
				}

				resumeDraw();

				// To let other listeners receive the events
				return false;
			}
		});
		baseOverlay = new TileOverlay(context, this, "TEST", renderer,
				this.androidContext, true);
		this.setRenderer(renderer);

		// Add the map view to a manager so it is disposable for any Activity
		ActivityBundlesManager.getInstance().registerMapView(this);
	}

	public boolean isNavigationMode() {
		return navigationMode;
	}

	public void setNavigationMode(boolean navigation) {
		this.navigationMode = navigation;
	}

	public MapRenderer getMRendererInfo() {
		return getBaseLayer().getRenderer();
	}

	public void addControl(IMapControl control) {
		control.setMapView(this);
		control.activate();
		this.controls.add(control);
	}

	public void removeControl(IMapControl control) {
		this.controls.remove(control);
		control.deactivate();
	}

	public void clearControls() {
		this.controls.clear();
	}

	public boolean containsOverlay(String name) {
		final List<MapOverlay> overlays = this.mOverlays;

		final int size = overlays.size();

		MapOverlay overlay;
		for (int i = 0; i < size; i++) {
			overlay = overlays.get(i);
			if (overlay.getName().compareTo(name) == 0) {
				return true;
			}
		}
		return false;
	}

	public void addOverlay(MapOverlay overlay) {
		overlay.setMapView(this);
		if (containsOverlay(overlay.getName())) {
			overlay.setName(overlay.getName() + "_");
			addOverlay(overlay);
		} else {
			if (overlay instanceof AssignFirstPointOverlay) {
				this.mOverlays.add(0, overlay);
			} else {
				this.mOverlays.add(overlay);
			}

			fireZIndexChanged();
			extentChangedListeners.add(overlay);
		}

		setDirty();
	}

	public void removeOverlay(MapOverlay overlay) {
		if (getOverlays().remove(overlay)) {
			Log.d("", "Overlay removed: " + overlay.getName());

			fireZIndexChanged();
			extentChangedListeners.remove(overlay);
			overlay.destroy();
			overlay = null;
			if (acetate != null) {
				acetate.setPopupVisibility(View.INVISIBLE);
			}
		}
	}

	public void removeOverlay(final String name) {
		WorkQueue.getExclusiveInstance().execute(new Runnable() {
			@Override
			public void run() {
				// getMapActivity().getMapHandler().sendEmptyMessage(
				// IMapActivity.SHOW_INDETERMINATE_DIALOG);
				try {
					final List<MapOverlay> overlays = getOverlays();

					List<MapOverlay> copyOverlays = new ArrayList();

					int size = overlays.size();

					for (int i = 0; i < size; i++) {
						copyOverlays.add(mOverlays.get(i));
					}

					Iterator<MapOverlay> it = copyOverlays.iterator();

					MapOverlay overlay;

					while (it.hasNext()) {
						try {
							overlay = it.next();
							if (overlay.getName().compareTo(name) == 0) {
								removeOverlay(overlay);
								break;
							}
						} catch (Exception e) {
							Log.e("", "Error on RemoveOverlay: " + name);
						}
					}
				} catch (Exception ignore) {

				} finally {
					mapHandler.sendEmptyMessage(IMapActivity.HIDE_INDETERMINATE_DIALOG);
				}
			}
		}, true);
	}

	public MapOverlay getOverlay(String name) {
		final List<MapOverlay> overlays = this.mOverlays;

		final int size = overlays.size();

		MapOverlay overlay;
		for (int i = 0; i < size; i++) {
			overlay = overlays.get(i);
			if (overlay.getName().compareTo(name) == 0) {
				return overlay;
			}
		}
		return null;
	}

	public List<MapOverlay> getOverlays() {
		return this.mOverlays;
	}

	public void notifyExtentChangedListeners(Extent extent, int zoomLevel,
			double resolution) {
		final ArrayList list = (ArrayList) this.extentChangedListeners;
		final int length = list.size();
		for (int i = 0; i < length; i++) {
			((ExtentChangedListener) list.get(i)).onExtentChanged(extent,
					zoomLevel, resolution);
		}
	}

	/**
	 * Sets the current MapRenderer of the view and centers the view on the
	 * center of the max extent of the MapRenderer
	 * 
	 * @param aRenderer
	 *            A MapRenderer
	 */
	public void setRenderer(final MapRenderer aRenderer) {
		try {
			log.debug("set MapRenderer: " + aRenderer.toString());
			this.getBaseLayer().setRenderer(aRenderer);
			// this.mRendererInfo = aRenderer;
			this.setViewPort(new ViewPort());
			this.getMRendererInfo().setCenter(
					aRenderer.getExtent().getCenter().getX(),
					aRenderer.getExtent().getCenter().getY());
			this.getViewPort().resolutions = aRenderer.resolutions;
			this.getViewPort().setDist1Pixel(this.getViewPort().resolutions[0]);
			this.getViewPort().origin = new es.prodevelop.gvsig.mini.geom.Point(
					aRenderer.getOriginX(), aRenderer.getOriginY());
		} catch (Exception e) {
			log.error("setRenderer:", e);
		}
	}

	public IMapViewController getMapViewController() {
		return this.controller;
	}

	public void onScalingFinished() {
		// frontCanvas.drawBitmap(bufferBitmap, 0, 0, normalPaint);
		getMapViewController().setZoomLevel(tempZoomLevel);
		setDirty();
	}

	public void computeScale() {
		if (getScaler().computeScale()) {
			if (getScaler().isFinished())
				onScalingFinished();
			else
				resumeDraw();
		}
	}

	/**
	 * 
	 * @return The final zoom level after the Scale interpolation ends
	 */
	@Override
	public int getTempZoomLevel() {
		return tempZoomLevel;
	}

	/**
	 * 
	 * @return The current zoom level of the view
	 */
	@Override
	public int getZoomLevel() {
		return this.getMRendererInfo().getZoomLevel();
	}

	public boolean onLongPress(MotionEvent e) {

		if (navigationMode
				|| (currTouchPoint != null && currTouchPoint.isDown())
				|| invalidateLongPress) {
			log.debug("longpress on pan mode or navigation does not work");
			return false;
		}

		try {
			if (this.mTouchMapOffsetX < ResourceLoader.MIN_PAN
					&& this.mTouchMapOffsetY < ResourceLoader.MIN_PAN
					&& this.mTouchMapOffsetX > -ResourceLoader.MIN_PAN
					&& this.mTouchMapOffsetY > -ResourceLoader.MIN_PAN) {

				double[] coords = this.getMRendererInfo().fromPixels(
						new int[] { (int) e.getX(), (int) e.getY() });

				for (MapOverlay osmvo : this.mOverlays)
					if (osmvo.onLongPress(e, this)) {
						fireOverlayContextToShow(osmvo.getItemContext(),
								coords[0], coords[1]);
						return true;
					}

				fireDefaultContextToShow(coords[0], coords[1]);

				pixelX = (int) e.getX();
				pixelY = (int) e.getY();
			}
		} catch (Exception ex) {
			log.error("onLongPress:", ex);
		}

		return true;
	}

	private void fireOverlayContextToShow(ItemContext itemContext, double x,
			double y) {
		for (MapViewListener listener : mapViewListeners) {
			listener.overlayContextToShow(itemContext, x, y);
		}
	}

	private void fireDefaultContextToShow(double x, double y) {
		for (MapViewListener listener : mapViewListeners) {
			listener.defaultContextToShow(x, y);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		try {
			for (MapOverlay osmvo : this.mOverlays)
				if (osmvo.onKeyDown(keyCode, event, this))
					return true;

		} catch (Exception e) {
			log.error("onkeydown:", e);
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		try {
			for (MapOverlay osmvo : this.mOverlays)
				if (osmvo.onKeyUp(keyCode, event, this))
					return true;
		} catch (Exception e) {
			log.error("onkeyUp:", e);
		}

		return super.onKeyUp(keyCode, event);
	}

	@Override
	public boolean onTrackballEvent(MotionEvent event) {
		try {
			int x = (int) Math.ceil(event.getX() * 10);
			int y = (int) Math.ceil(event.getY() * 10);
			double[] coords = this.getMRendererInfo()
					.fromPixels(
							new int[] { (int) centerPixelX + x,
									(int) centerPixelY + y });
			System.out.println("x, y: " + coords[0] + ", " + coords[1]);
			this.getMapViewController().setMapCenter(coords[0], coords[1]);
			for (MapOverlay osmvo : this.mOverlays)
				osmvo.onTrackballEvent(event, this);
			// return true;
		} catch (Exception e) {
			log.error("onTrackBallEvent", e);
		}
		return super.onTrackballEvent(event);
	}

	@Override
	public boolean onTouchEvent(final MotionEvent event) {
		try {
			final int size = this.mOverlays.size();
			int i = 0;
			MapOverlay overlay;

			resumeDraw();
			// FIXME esto es de NOMADA
			// if (map.isPOISlideShown())
			// return true;
			// System.out.println("onTouchEvent");
			// Log.d("", event.getX() + ", " + event.getY());
			this.lastTouchEvent = MotionEvent.obtain(event);
			if (event.getAction() == MotionEvent.ACTION_UP) {
				lastTouchEventProcessed = false;
			}
			if (!navigationMode) {
				for (i = 0; i < size; i++) {
					overlay = this.mOverlays.get(i);
					overlay.onTouchEvent(event, this);
				}
				// return true;

				this.mGestureDetector.onTouchEvent(event);

				if (!multiTouchController.onTouchEvent(event)) {
					// System.out.println("onTouchEvent acetate");
					// if (acetate.isFirstTouch()) {
					// System.out.println("first touch");
					// acetate.onTouchEvent(event);
					// } else
					// synchronized (holder) {
					// System.out.println("not first touch");
					if (acetate != null) {
						acetate.onTouchEvent(event);
					}
					// }
				}
			}

		} catch (Exception e) {
			log.error("onTouchEvent:", e);
		}

		return super.onTouchEvent(event);

	}

	public int getMapWidth() {
		return MAP_WIDTH;
	}

	public int getMapHeight() {
		return MAP_HEIGHT;
	}

	public void processMultiTouchEvent() {
		if (lastTouchEvent != null
				&& this.lastTouchEvent.getAction() == MotionEvent.ACTION_UP
				&& this.currTouchPoint != null && !lastTouchEventProcessed) {
			int multiTouchZoom = this.getZoomLevel();
			double scale = this.mScaler.mCurrScale;
			if (scale > 1) {
				multiTouchZoom += Math.ceil(scale - 1);
			} else {
				scale *= 0.1;
				double decimals = scale - Math.ceil(scale);
				if (decimals > 0.5)
					multiTouchZoom -= Math.floor(scale);
				else
					multiTouchZoom -= Math.ceil(scale);

			}

			getMapViewController().setZoomLevel(multiTouchZoom);
			mScaler.forceFinished(true);
			this.currTouchPoint = null;
			this.lastTouchEventProcessed = true;
		}
	}

	public ViewSimpleLocationOverlay getLocationOverlay() {
		return this.locationOverlay;
	}

	public void setLocationOverlay(ViewSimpleLocationOverlay locationOverlay) {
		this.locationOverlay = locationOverlay;
	}

	/**
	 * Set the drawing machine to dirty and ask for redraw
	 * 
	 * @see es.prodevelop.gvsig.mini.map.IMapView#setDirty()
	 */
	@Override
	public void setDirty() {
		Log.i("read", "dirtying", new Exception());
		this.drawingStatus = DRAWING_STATUS_DIRTY;
		post(new Runnable() {

			@Override
			public void run() {
				invalidate();
			}
		});
	}

	@Override
	public void draw(final Canvas c) {
		Log.i("draw", "drawing");
		try {
			final int offsetX = getmTouchMapOffsetX();
			final int offsetY = getmTouchMapOffsetY();
			// c.drawColor(0, PorterDuff.Mode.CLEAR);
			if (!mScaler.isFinished()
					|| (this.currTouchPoint != null && this.currTouchPoint
							.isDown())) {
				c.drawRect(0, 0, getMapWidth(), getMapHeight(),
						Paints.whitePaint);
				Matrix m = c.getMatrix();
				// Log.d("", "Scale: " + mScaler.mCurrScale);
				m.preScale(mScaler.mCurrScale, mScaler.mCurrScale,
						getMapWidth() / 2, getMapHeight() / 2);
				Log.i("draw", "scaling at: " + mScaler.mCurrScale);

				c.setMatrix(m);
				// TODO commented by fergonco, seems to have no effect.
				// c.drawBitmap(bufferBitmap,
				// ViewPort.mTouchMapOffsetX,
				// ViewPort.mTouchMapOffsetY, Paints.normalPaint);
				// System.out.println("scalling");
				// return;
			} else {

				processMultiTouchEvent();

				if (navigationMode) {
					// TileRaster.this.rotatePaint.setAntiAlias(true);
					Paints.normalPaint = Paints.rotatePaint;
					int rotationToDraw = -mBearing
							- getLocationOverlay().getOffsetOrientation();
					if (Math.abs(Math.abs(rotationToDraw)
							- Math.abs(previousRotation)) < Utils.MIN_ROTATION) {
						rotationToDraw = previousRotation;
					} else {
						previousRotation = rotationToDraw;
					}
					c.rotate(rotationToDraw, getMapWidth() / 2,
							getMapHeight() / 2);
				} else {
					Paints.normalPaint = Paints.mPaintR;
				}
			}

			/*
			 * Fling gestures are recognized after other events have been
			 * dispatched so it is possible to have a dirty drawing state while
			 * scrolling. In that case prevent from doing an expensive draw
			 */
			if (drawingStatus == DRAWING_STATUS_DIRTY && !isScrolling) {
				drawingStatus = DRAWING_STATUS_IN_PROCESS;
				Log.i("draw", "is dirty");
				try {
					if (ViewPort.mTouchMapOffsetX != 0
							|| ViewPort.mTouchMapOffsetY != 0) {
						/*
						 * TODO This can be removed if toPixels does not take it
						 * into account. Anyway, these values can change after
						 * this check. So it is not very useful instead
						 */
						throw new RestartDrawException();
					}

					Log.i("draw", "viewport: " + ViewPort.mTouchMapOffsetX
							+ ", " + ViewPort.mTouchMapOffsetY);
					double[] center = getMRendererInfo().fromPixels(
							new int[] { getWidth() / 2, getHeight() / 2 });
					Log.i("draw", "new center: " + center[0] + ", " + center[1]);

					/*
					 * Create and draw the new image
					 */
					Bitmap newSurfaceImage = newSurfaceImage();
					Canvas imageCanvas = new Canvas(newSurfaceImage);

					boolean areAllValid = baseOverlay.allValid();

					// Check for cancelation
					if (drawingStatus == DRAWING_STATUS_DIRTY) {
						throw new RestartDrawException();
					}

					final ArrayList<MapOverlay> overlays = (ArrayList<MapOverlay>) this.mOverlays;
					final int numLayers = overlays.size();
					Log.i("draw", "num overlays: " + numLayers);

					MapOverlay o;
					MapOverlay onTop = null;
					for (int i = 0; i < numLayers; i++) {
						// Check for cancelation
						if (drawingStatus == DRAWING_STATUS_DIRTY) {
							throw new RestartDrawException();
						}
						try {
							o = overlays.get(i);
							if (!o.getName().contains("hid")) {
								o.onManagedDraw(imageCanvas, this);
							} else {
								onTop = o;
							}

						} catch (Exception e) {
							// Concurrent modifications can occur
							Log.e("TileRaster", "draw", e);
						}
					}
					if (onTop != null)
						onTop.onManagedDraw(imageCanvas, this);

					Log.i("draw", "drawing base layer");
//					baseOverlay.onDraw(imageCanvas, this);

					if (locationOverlay != null) {
						locationOverlay.onManagedDraw(imageCanvas, this);
					}

					if (acetate != null) {
						acetate.onManagedDraw(imageCanvas, this);
					}

					// Check for cancelation
					if (drawingStatus == DRAWING_STATUS_DIRTY) {
						throw new RestartDrawException();
					} else {
						/*
						 * Should we redraw?
						 */
						if (areAllValid) {
							drawingStatus = DRAWING_STATUS_DRAWN;
						} else {
							drawingStatus = DRAWING_STATUS_DIRTY;
						}
						postInvalidate();
					}

					// Set the new image
					surfaceImage = newSurfaceImage;
				} catch (RestartDrawException e) {
					Log.i("draw", "drawing cancelled");
					drawingStatus = DRAWING_STATUS_DIRTY;
					postInvalidate();
				} catch (RuntimeException e) {
					drawingStatus = DRAWING_STATUS_DIRTY;
					throw e;
				}
			}

			computeScale();

			// Draw last created image
			Log.i("draw", "showing image");
			Paint paint = new Paint();
			paint.setARGB(255, 255, 255, 255);
			c.drawRect(new Rect(0, 0, getWidth(), getHeight()), paint);
			 baseOverlay.onDraw(c, this);

			c.drawBitmap(surfaceImage, offsetX, offsetY, null);
//			if (locationOverlay != null) {
//				locationOverlay.onManagedDraw(c, this);
//			}

//			if (acetate != null) {
//				acetate.onManagedDraw(c, this);
//			}

			if (drawingStatus == DRAWING_STATUS_DRAWN) {
				pauseDraw();
			}

			for (IMapControl mapControl : controls) {
				mapControl.draw(c);
			}

		} catch (Exception e) {
			log.error("onDraw", e);
		}

	}

	private Bitmap buffer;

	private Bitmap newSurfaceImage() {
		// if (buffer == null) {
		int width = getWidth();
		int height = getHeight();
		buffer = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		// }

		return buffer;

	}

	@Override
	public void panTo(int touchMapOffsetX, int touchMapOffsetY) {
		Log.w("OFFSET", touchMapOffsetX + ", " + touchMapOffsetY);
		/*
		 * Modify the image to be coherent with zero offset and new center until
		 * it is drawn again
		 */
		Bitmap img = newSurfaceImage();
		Canvas canvas = new Canvas(img);
		// if (!this.isScrolling) {
		// canvas.drawRect(new Rect(0, 0, getWidth(), getHeight()),
		// Paints.whitePaint);
		// }
		canvas.drawBitmap(surfaceImage, touchMapOffsetX, touchMapOffsetY, null);
		surfaceImage = img;

		/*
		 * Set zero offset and new center
		 */
		double[] center = getMRendererInfo().fromPixels(
				new int[] { getMapWidth() / 2 - touchMapOffsetX,
						getMapHeight() / 2 - touchMapOffsetY });
		resetTouchOffset();
		getMapViewController().setMapCenter(center[0], center[1]);

		if (getScrollingCenter() != null)
			setScrollingCenter(center[0], center[1]);
		invalidate();
	}

	@Override
	public void sortTiles(final Tile[] array2) {
		try {
			final Pixel center = new Pixel(centerPixelX, centerPixelY);
			double e1, e2;
			final int length = array2.length;
			Tile t;
			Tile t1;
			final Pixel temp = new Pixel((this.getMRendererInfo()
					.getMAPTILE_SIZEPX() / 2), (this.getMRendererInfo()
					.getMAPTILE_SIZEPX() / 2));
			for (int pass = 1; pass < length; pass++) {
				for (int element = 0; element < length - 1; element++) {
					t = array2[element];
					t1 = array2[element + 1];
					if (t != null && t1 != null) {
						e1 = (t.distanceFromCenter.add(temp)).distance(center);
						e2 = (t1.distanceFromCenter.add(temp)).distance(center);
						if (e1 > e2) {
							swap(array2, element, element + 1);
						}
					}
				}
			}
		} catch (final Exception e) {
			log.error("sortTiles:", e);
		}
	}

	private void swap(final Tile[] array3, final int first, final int second) {
		try {
			final Tile hold = array3[first];

			array3[first] = array3[second];
			array3[second] = hold;
		} catch (final Exception e) {
			log.error("swap", e);
		}
	}

	@Override
	public void onLayerChanged(String layerName) {
		try {
			baseOverlay.onLayerChanged(layerName);
			clearBufferCanvas();
			log.debug("on layer changed");
			this.clearCache();

			mapState.persist();

			final List<MapOverlay> overlays = this.mOverlays;

			final int length = overlays.size();

			LayerChangedListener overlay;
			for (int i = 0; i < length; i++) {
				try {
					overlay = overlays.get(i);
					if (overlay instanceof AssignFirstPointOverlay)
						this.removeOverlay(((AssignFirstPointOverlay) overlay)
								.getName());
					else
						overlay.onLayerChanged(layerName);
				} catch (Exception ignore) {

				}
			}

		} catch (Exception e) {
			log.error("onlayerchanged:", e);
		} finally {

		}
	}

	/**
	 * 
	 * @return The center of the map expressed in SRS:4326
	 */
	public double[] getCenterLonLat() {
		double[] res = null;
		try {
			final MapRenderer renderer = this.baseOverlay.getRenderer();
			res = ConversionCoords.reproject(renderer.getCenter().getX(),
					renderer.getCenter().getY(),
					CRSFactory.getCRS(renderer.getSRS()),
					CRSFactory.getCRS("EPSG:4326"));
		} catch (Exception e) {
			log.error("getCenterLonLat:", e);
		}
		return res;
	}

	/**
	 * Calculates a zoom level that fits an extent
	 * 
	 * @param width
	 *            The width of the extent
	 * @param height
	 *            The height of the extent
	 * @param currentZoomLevel
	 *            the current zoom level to get resolution from
	 * @return The zoom level that fits the extent
	 */
	public int getZoomLevelFitsExtent(final double width, final double height,
			final int currentZoomLevel) {
		final Extent mapExtent = ViewPort.calculateExtent(this
				.getMRendererInfo().getCenter(),
				this.getMRendererInfo().resolutions[currentZoomLevel],
				getMapWidth(), getMapHeight());
		final int curZoomLevel = currentZoomLevel;

		final double curWidth = mapExtent.getWidth();
		final double curHeight = mapExtent.getHeight();

		final double diffNeededX = width / curWidth;
		final double diffNeededY = height / curHeight;

		if (Double.isInfinite(diffNeededX) || Double.isInfinite(diffNeededY)) {
			return -1;
		}

		final double diffNeeded = Math.max(diffNeededX, diffNeededY);

		if (diffNeeded > 1) {
			return curZoomLevel - Utils.getNextSquareNumberAbove(diffNeeded);
		} else if (diffNeeded < 0.5) {
			return curZoomLevel
					+ Utils.getNextSquareNumberAbove(1 / diffNeeded) - 1;
		} else {
			return curZoomLevel;
		}
	}

	/**
	 * Calculates a zoom level that fits an extent
	 * 
	 * @param width
	 *            The width of the extent
	 * @param height
	 *            The height of the extent
	 * @return The zoom level that fits the extent
	 */
	public int getZoomLevelFitsExtent(final double width, final double height) {
		return this.getZoomLevelFitsExtent(width, height, getZoomLevel());
	}

	@Override
	public void invalidate() {
		resumeDraw();
		super.invalidate();
	}

	@Override
	public void postInvalidate() {
		resumeDraw();
		super.postInvalidate();
	}

	public void adjustViewToAccuracyIfNavigationMode(final float accuracy) {
		try {
			final MapRenderer renderer = this.getMRendererInfo();
			Point center = renderer.center;
			double[] xy = ConversionCoords.reproject(center.getX(),
					center.getY(), CRSFactory.getCRS(renderer.getSRS()),
					CRSFactory.getCRS("EPSG:900913"));
			double minX = xy[0] - accuracy * 1;
			double maxX = xy[0] + accuracy * 1;
			double minY = xy[1] - accuracy * 1;
			double maxY = xy[1] + accuracy * 1;

			double[] minXY = ConversionCoords.reproject(minX, minY,
					CRSFactory.getCRS("EPSG:900913"),
					CRSFactory.getCRS(renderer.getSRS()));
			double[] maxXY = ConversionCoords.reproject(maxX, maxY,
					CRSFactory.getCRS("EPSG:900913"),
					CRSFactory.getCRS(renderer.getSRS()));

			this.getMapViewController().zoomToExtent(
					new Extent(minXY[0], minXY[1], maxXY[0], maxXY[1]), true);
		} catch (Exception ignore) {

		}
	}

	/**
	 * Sets the Feature selected by the user when onLongPress and animates the
	 * view to it
	 * 
	 * @param f
	 *            The Feature selected
	 */
	public void setSelectedFeature(Feature f) {
		try {
			this.selectedFeature = f;
		} catch (Exception e) {
			log.error("setSelectedFeature", e);
		}
	}

	/**
	 * 
	 * @return The TileProvider
	 */
	public TileProvider getMTileProvider() {
		return baseOverlay.getTileProvider();
	}

	public void addExtentChangedListener(ExtentChangedListener listener) {
		if (listener != null)
			this.extentChangedListeners.add(listener);
	}

	public void removeExtentChangedListener(ExtentChangedListener listener) {
		if (listener != null)
			this.extentChangedListeners.remove(listener);
	}

	public void addMapViewListener(MapViewListener listener) {
		if (listener != null)
			this.mapViewListeners.add(listener);
	}

	public void removeMapViewListener(MapViewListener listener) {
		if (listener != null)
			this.mapViewListeners.remove(listener);
	}

	public void addZIndexChangedListener(ZIndexChangedListener listener) {
		if (listener != null)
			this.zIndexChangedListeners.add(listener);
	}

	public void removeZIndexChangedListener(ZIndexChangedListener listener) {
		if (listener != null)
			this.zIndexChangedListeners.remove(listener);
	}

	public void addSelectFeatureListener(OnSelectFeatureListener listener) {
		if (listener != null)
			this.selectFeatureListener.add(listener);
	}

	public void removeSelectFeatureListener(OnSelectFeatureListener listener) {
		if (listener != null)
			this.selectFeatureListener.remove(listener);
	}

	/**
	 * Clears the pending queues of the TileProvider
	 */
	public void clearCache() {
		try {
			log.debug("clearCache");
			baseOverlay.getTileProvider().clearPendingQueue();
			// this.mTileProvider.clearPendingQueue();
		} catch (Exception e) {
			log.debug("clearCache", e);
		}
	}

	/**
	 * Frees memory
	 */
	public void destroy() {
		try {
			log.debug("destroy tileraster");
			if (acetate != null) {
				acetate.destroy();
			}
			baseOverlay.getTileProvider().destroy();

			for (MapOverlay m : mOverlays) {
				m.destroy();
			}
			// this.mCurrentAnimationRunner.interrupt();
			// this.mCurrentAnimationRunner = null;
		} catch (Exception e) {
			log.debug("destroy", e);
		}
	}

	// --------------------------------------------------------------------------
	// -----------------

	@Override
	public Object getDraggableObjectAtPoint(PointInfo pt) {
		// Just return something non-null, we don't need to keep track of which
		// specific object is being dragged
		return this;
	}

	@Override
	public void getPositionAndScale(Object obj,
			PositionAndScale objPosAndScaleOut) {
		// We start at 0.0f each time the drag position is replaced, because we
		// just want the relative drag distance
		objPosAndScaleOut.set(xOff, yOff, relativeScale);
	}

	@Override
	public void selectObject(Object obj, PointInfo pt) {

	}

	@Override
	public boolean setPositionAndScale(Object obj, PositionAndScale update,
			PointInfo touchPoint) {
		try {
			if (!touchPoint.isMultiTouch()) {
				// currTouchPoint = null;
			} else {
				if (touchPoint.getMultiTouchDiameter() != 0)
					currTouchPoint = touchPoint;
				if (this.currTouchPoint.isDown()) {
					mScaler.mCurrScale = update.getScale();
					// this.zoomToExtent(e, false);
				}
			}
		} catch (Exception e) {
			log.error("", e);

		}

		return true;
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		if (this.surfaceThread != null)
			this.surfaceThread.onWindowResize(width, height);
	}

	public void surfaceCreated(SurfaceHolder holder) {
		hasSurface = true;
		startST();
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		hasSurface = false;
		destroyST();
	}

	public void clearBufferCanvas() {
		if (this.bufferCanvas != null) {
			bufferCanvas.drawRect(0, 0, getMapWidth(), getMapHeight(),
					Paints.whitePaint);
		}

		if (this.zoomCanvas != null) {
			zoomCanvas.drawRect(0, 0, getMapWidth(), getMapHeight(),
					Paints.whitePaint);
		}
	}

	public void setMapHeight(int height) {
		MAP_HEIGHT = height;
	}

	public void setMapWidth(int width) {
		MAP_WIDTH = width;
	}

	public void initializeCanvas(int width, int height) throws BaseException {
		setMapWidth(width);
		setMapHeight(height);

		if (bufferBitmap != null) {
			bufferBitmap.recycle();
			bufferBitmap = null;
		}

		if (bufferBitmap == null) {
			bufferBitmap = Bitmap.createBitmap(getMapWidth(), getMapHeight(),
					Bitmap.Config.RGB_565);
			bufferCanvas.setBitmap(bufferBitmap);
		}

		if (zoomBitmap != null) {
			zoomBitmap.recycle();
			zoomBitmap = null;
		}

		if (zoomBitmap == null) {
			zoomBitmap = Bitmap.createBitmap(getMapWidth(), getMapHeight(),
					Bitmap.Config.RGB_565);
			zoomCanvas.setBitmap(zoomBitmap);
		}

		ViewPort.mapHeight = height;
		ViewPort.mapWidth = width;
		this.centerPixelX = width / 2;
		this.centerPixelY = height / 2;

		if (acetate != null) {
			acetate.setPopupMaxSize(width, height);
		}
	}

	public boolean scaleCanvasForZoom() {
		try {
			final int mapWidth = getMapWidth();
			final int mapHeight = getMapHeight();

			if (scale) {
				zoomCanvas.drawBitmap((Bitmap) bufferBitmap, 0, 0,
						Paints.normalPaint);
				bufferCanvas.drawRect(0, 0, mapWidth, mapHeight,
						Paints.whitePaint);

				if (mScaler.mFinalScale > 0) {
					if (mScaler.mFinalScale < 1) {
						final int scaledWidth = (int) (mapWidth * mScaler.mFinalScale);
						final int scaledHeight = (int) (mapHeight * mScaler.mFinalScale);
						// if width and height are lower than 0, the VM will
						// crash
						if (scaledWidth > 0 && scaledHeight > 0) {
							final Bitmap scaledBitmap = Bitmap
									.createScaledBitmap(zoomBitmap,
											scaledWidth, scaledHeight, true);
							bufferCanvas.drawBitmap(scaledBitmap,
									(mapWidth - (int) (scaledWidth)) / 2,
									(mapHeight - (int) (scaledHeight)) / 2,
									Paints.mPaint);
						}
					} else {
						final float inverseScale = 1 / mScaler.mFinalScale;
						final int x = (mapWidth - (int) ((mapWidth * inverseScale))) / 2;
						final int y = (mapHeight - (int) ((mapHeight * inverseScale))) / 2;
						final int width = (int) (mapWidth * inverseScale);
						final int height = (int) (mapHeight * inverseScale);
						final Bitmap cropBitmap = Bitmap.createBitmap(
								zoomBitmap, x, y, width, height);
						final Bitmap scaledBitmap = Bitmap.createScaledBitmap(
								cropBitmap, (int) (mapWidth),
								(int) (mapHeight), true);
						bufferCanvas.drawBitmap(scaledBitmap, 0, 0,
								Paints.mPaint);
					}
				}
				return true;
			}
		} catch (Exception ignore) {
		} catch (OutOfMemoryError out) {

		} finally {
			scale = false;
		}
		return false;
	}

	public void init() {
		holder = getHolder();
		holder.addCallback(this);
		hasSurface = false;
	}

	/**
	 * Starts the surfaceThread
	 */
	public void startST() {
		if (this.surfaceThread == null) {
			this.surfaceThread = new TileRasterThread(holder, this);

			if (hasSurface) {
				surfaceThread.start();
			}
		}
	}

	/**
	 * Destroys the surfaceThread
	 */
	public void destroyST() {
		if (this.surfaceThread != null) {
			resumeDraw();
			surfaceThread.requestExitAndWait();
			this.surfaceThread = null;
		}
	}

	public Scroller getScroller() {
		return mScroller;
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		Log.i("draw", "scrollChanged: " + l + ", " + t + " - " + oldl + ", "
				+ oldt);
		if ((oldl == 0 && oldt == 0) || !isScrolling)
			return;

		// System.out.println(l + ", " + t + ", " + oldl + ", " + oldt);
		int px = l - oldl;
		int py = t - oldt;
		Log.i("draw", "scrollChanged: " + px + ", " + py);

		// System.out.println("px, py: " + px / 2 + ", " + py / 2);

		// int newCenterPx = this.centerPixelX + px;
		// int newCenterPy = this.centerPixelY + py;

		setmTouchMapOffsetX(getmTouchMapOffsetX() - px);
		setmTouchMapOffsetY(getmTouchMapOffsetY() - py);
		// ViewPort.mTouchMapOffsetX = getmTouchMapOffsetX();
		// ViewPort.mTouchMapOffsetY = getmTouchMapOffsetY();

		// // System.out.println("npx, npy: " + newCenterPx + ", " +
		// newCenterPy);
		//
		// double[] newCenter = ViewPort.toMapPoint(new int[] { newCenterPx,
		// newCenterPy }, getMapWidth(), getMapHeight(), scrollingCenter,
		// this.getMRendererInfo().resolutions[this.getZoomLevel()]);
		//
		// synchronized (holder) {
		// scrollingCenter.setCoordinates(newCenter);
		// this.getMapViewController().setMapCenter(scrollingCenter.getX(),
		// scrollingCenter.getY());
		// }

		// System.out.println("wpx; wpy: " + newCenter[0] + ", " +
		// newCenter[1]);
	}

	public void startScrolling() {
		isScrolling = true;
		Log.i("draw", "start scrolling");
		resetTouchOffset();
		drawingStatus = DRAWING_STATUS_DRAWN;
		this.scrollingCenter = this.getMRendererInfo().getCenter();
	}

	public void resetTouchOffset() {
		this.setmTouchMapOffsetX(0);
		this.setmTouchMapOffsetY(0);
		ViewPort.mTouchMapOffsetX = 0;
		ViewPort.mTouchMapOffsetY = 0;
	}

	@Override
	public void computeScroll() {
		Log.i("draw", "computing scroll");

		// System.out.println("computeScroll");
		if (mScroller.computeScrollOffset()) {
			if (mScroller.isFinished()) {
				Log.i("draw", "scroll finished: " + getmTouchMapOffsetX()
						+ ", " + getmTouchMapOffsetY());
				double[] center = getMRendererInfo().fromPixels(
						new int[] { getMapWidth() / 2 - getmTouchMapOffsetX(),
								getMapHeight() / 2 - getmTouchMapOffsetY() });
				Log.i("draw", "new center: " + center[0] + ", " + center[1]);
				pauseDraw();
				isScrolling = false;
				resetTouchOffset();
				getMapViewController().setMapCenter(center[0], center[1]);
				getMapViewController().setZoomLevel(tempZoomLevel);
				if (getScrollingCenter() != null)
					setScrollingCenter(center[0], center[1]);
				resumeDraw();

			} else {
				scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
			}
			postInvalidate(); // Keep on drawing until the animation has
			// finished.
		}
	}

	@Override
	public boolean isScrolling() {
		return isScrolling;
	}

	@Override
	public void forceFinishScroll() {
		Log.i("draw", "scroll finished: " + getmTouchMapOffsetX() + ", "
				+ getmTouchMapOffsetY());
		double[] center = getMRendererInfo().fromPixels(
				new int[] { getWidth() / 2, getHeight() / 2 });
		Log.i("draw", "new center: " + center[0] + ", " + center[1]);
		pauseDraw();
		isScrolling = false;
		resetTouchOffset();
		getMapViewController().setMapCenter(center[0], center[1]);
		getMapViewController().setZoomLevel(tempZoomLevel);
		if (getScrollingCenter() != null)
			setScrollingCenter(center[0], center[1]);
		resumeDraw();
	}

	public int getPixelZoomLevel() {
		double extentWidth = this.getMRendererInfo().getExtent().getWidth();
		double extentHeight = this.getMRendererInfo().getExtent().getHeight();

		double size = Math.max(extentWidth, extentHeight);

		return (int) (size * this.getMRendererInfo().getMAPTILE_SIZEPX());
	}

	public int getWorldSizePx() {
		return Integer.MAX_VALUE;
	}

	@Override
	public void scrollTo(int x, int y) {
		// System.out.println("scrollTo");
		final int worldSize = getWorldSizePx();
		x %= worldSize;
		y %= worldSize;
		super.scrollTo(x, y);
	}

	public void pauseDraw() {
		if (this.surfaceThread != null)
			this.surfaceThread.pause = true;
	}

	public void resumeDraw() {
		if (this.surfaceThread != null) {
			this.surfaceThread.pause = false;
		}
	}

	class Scaler {

		public static final int DURATION_SHORT = 500;
		public static final int DURATION_LONG = 1000;

		private float mStartScale;
		private float mFinalScale;
		private float mCurrScale;

		private long mStartTime;
		private int mDuration;
		private float mDurationReciprocal;
		private float mDeltaScale;
		private boolean mFinished;
		private Interpolator mInterpolator;

		/**
		 * Create a Scaler with the specified interpolator.
		 */
		public Scaler(Context context, Interpolator interpolator) {
			mFinished = true;
			mInterpolator = interpolator;
		}

		/**
		 * 
		 * Returns whether the scaler has finished scaling.
		 * 
		 * @return True if the scaler has finished scaling, false otherwise.
		 */
		public final boolean isFinished() {
			return mFinished;
		}

		/**
		 * Force the finished field to a particular value.
		 * 
		 * @param finished
		 *            The new finished value.
		 */
		public final void forceFinished(boolean finished) {
			mFinished = finished;
		}

		/**
		 * Returns how long the scale event will take, in milliseconds.
		 * 
		 * @return The duration of the scale in milliseconds.
		 */
		public final int getDuration() {
			return mDuration;
		}

		/**
		 * Returns the current scale factor.
		 * 
		 * @return The new scale factor.
		 */
		public final float getCurrScale() {
			return mCurrScale;
		}

		/**
		 * Returns the start scale factor.
		 * 
		 * @return The start scale factor.
		 */
		public final float getStartScale() {
			return mStartScale;
		}

		/**
		 * Returns where the scale will end.
		 * 
		 * @return The final scale factor.
		 */
		public final float getFinalScale() {
			return mFinalScale;
		}

		/**
		 * Sets the final scale for this scaler.
		 * 
		 * @param newScale
		 *            The new scale factor.
		 */
		public void setFinalScale(float newScale) {
			mFinalScale = newScale;
			mDeltaScale = mFinalScale - mStartScale;
			mFinished = false;
		}

		/**
		 * Call this when you want to know the new scale. If it returns true,
		 * the animation is not yet finished.
		 */
		public boolean computeScale() {
			resumeDraw();
			if (mFinished) {
				Paints.mPaintR = new Paint();
				mCurrScale = 1.0f;
				return false;
			}

			int timePassed = (int) (AnimationUtils.currentAnimationTimeMillis() - mStartTime);

			if (timePassed < mDuration) {
				float x = (float) timePassed * mDurationReciprocal;

				x = mInterpolator.getInterpolation(x);

				mCurrScale = mStartScale + x * mDeltaScale;
				if (mCurrScale == mFinalScale)
					mFinished = true;

			} else {
				mCurrScale = mFinalScale;
				mFinished = true;
			}
			return true;
		}

		/**
		 * Start scaling by providing the starting scale and the final scale.
		 * 
		 * @param startX
		 *            Starting horizontal scroll offset in pixels. Positive
		 *            numbers will scroll the content to the left.
		 * @param startY
		 *            Starting vertical scroll offset in pixels. Positive
		 *            numbers will scroll the content up.
		 * @param dx
		 *            Horizontal distance to travel. Positive numbers will
		 *            scroll the content to the left.
		 * @param dy
		 *            Vertical distance to travel. Positive numbers will scroll
		 *            the content up.
		 * @param duration
		 *            Duration of the scroll in milliseconds.
		 */
		public void startScale(float startScale, float finalScale, int duration) {
			Paints.mPaintR.setFlags(Paint.FILTER_BITMAP_FLAG);

			mFinished = false;
			mDuration = duration;
			mStartTime = AnimationUtils.currentAnimationTimeMillis();
			mStartScale = startScale;
			mFinalScale = finalScale;
			mDeltaScale = finalScale - startScale;
			mDurationReciprocal = 1.0f / (float) mDuration;
			// bufferCanvas.drawBitmap(frontBitmap,
			// TileRaster.this.mTouchMapOffsetX,
			// TileRaster.this.mTouchMapOffsetY, rotatePaint);
		}

		/**
		 * Extend the scale animation. This allows a running animation to scale
		 * further and longer, when used with {@link #setFinalScale(float)}.
		 * 
		 * @param extend
		 *            Additional time to scale in milliseconds.
		 * @see #setFinalScale(float)
		 */
		public void extendDuration(int extend) {
			Paints.mPaintR.setFlags(Paint.FILTER_BITMAP_FLAG);

			int passed = (int) (AnimationUtils.currentAnimationTimeMillis() - mStartTime);
			mDuration = passed + extend;
			mDurationReciprocal = 1.0f / (float) mDuration;
			mFinished = false;
		}
	}

	@Override
	public void setAcetateOverlay(AcetateOverlay overlay) {
		this.acetate = overlay;
		this.addSelectFeatureListener(overlay);
		this.addExtentChangedListener(overlay);
	}

	@Override
	public AcetateOverlay getAcetateOverlay() {
		return this.acetate;
	}

	@Override
	public int getBearing() {
		return this.mBearing;
	}

	@Override
	public void setBearing(int bearing) {
		this.mBearing = bearing;
	}

	@Override
	public void setMapViewController(IMapViewController controller) {
		this.controller = controller;
	}

	@Override
	public TileProvider getTileProvider() {
		return this.baseOverlay.getTileProvider();
	}

	@Override
	public void centerOnGPSLocation() {
		final GPSPoint location = this.getLocationOverlay().mLocation;
		if (location != null) {
			this.getMapViewController().setMapCenter(location);
		}
	}

	@Override
	public void persist() {
		this.mapState.persist();
	}

	@Override
	public MapState getMapState() {
		return this.mapState;
	}

	@Override
	public ViewPort getViewPort() {
		return this.viewport;
	}

	@Override
	public void setViewPort(ViewPort viewPort) {
		this.viewport = viewPort;
	}

	@Override
	public void setTempZoomLevel(int zoomLevel) {
		this.tempZoomLevel = zoomLevel;
	}

	@Override
	public boolean isScalling() {
		return this.scale;
	}

	@Override
	public void setIsScalling(boolean isScalling) {
		this.scale = isScalling;
	}

	@Override
	public IMapControl getControlByName(String name) {
		for (IMapControl control : this.getControls()) {
			if (control.getName().compareToIgnoreCase(name) == 0)
				return control;
		}
		return null;
	}

	@Override
	public Handler getInvalidationHandler() {
		return this.simpleInvalidationHandler;
	}

	@Override
	public Scaler getScaler() {
		return this.mScaler;
	}

	@Override
	public void setIsScrolling(boolean isScrolling) {
		this.isScrolling = isScrolling;
	}

	@Override
	public List<IMapControl> getControls() {
		return this.controls;
	}

	public int getPixelX() {
		return pixelX;
	}

	public void setPixelX(int pixelX) {
		this.pixelX = pixelX;
	}

	public int getPixelY() {
		return pixelY;
	}

	public void setPixelY(int pixelY) {
		this.pixelY = pixelY;
	}

	public int getmTouchMapOffsetX() {
		return mTouchMapOffsetX;
	}

	public void setmTouchMapOffsetX(int mTouchMapOffsetX) {
		this.mTouchMapOffsetX = mTouchMapOffsetX;
	}

	public int getmTouchMapOffsetY() {
		return mTouchMapOffsetY;
	}

	public void setmTouchMapOffsetY(int mTouchMapOffsetY) {
		this.mTouchMapOffsetY = mTouchMapOffsetY;
	}

	public int getCenterPixelX() {
		return centerPixelX;
	}

	public int getCenterPixelY() {
		return centerPixelY;
	}

	@Override
	public Point getScrollingCenter() {
		return this.scrollingCenter;
	}

	@Override
	public void setScrollingCenter(double x, double y) {
		this.scrollingCenter = new Point(x, y);
	}

	@Override
	public Feature getSelectedFeature() {
		return this.selectedFeature;
	}

	@Override
	public ArrayList<OnSelectFeatureListener> getSelectFeatureListeners() {
		return this.selectFeatureListener;
	}

	public void fireZoomLevelChanged() {
		for (MapViewListener listener : mapViewListeners) {
			listener.zoomLevelChanged();
		}
	}

	private void fireExclusiveControlChanged() {
		for (MapViewListener listener : mapViewListeners) {
			listener.exclusiveControlChanged();
		}
	}

	@Override
	public void setExclusiveControl(IMapControl control) {
		if (this.exclusive != null) {
			removeControl(exclusive);
		}
		addControl(control);
		this.exclusive = control;
		fireExclusiveControlChanged();
	}

	@Override
	public IMapControl getExclusiveControl() {
		return exclusive;
	}

	@Override
	public double[] getMyLocationCenterMercator() {

		try {
			Point center = getLocationOverlay().getLocationLonLat();
			double[] lonlat = ConversionCoords.reproject(center.getX(),
					center.getY(), CRSFactory.getCRS("EPSG:4326"),
					CRSFactory.getCRS("EPSG:900913"));
			return lonlat;
		} catch (Exception e) {
			// Probably a NullPointerException can occur
			Log.e("MapVIew", "getMyLocationCenterMercator", e);
			return new double[] { 0, 0 };
		}
	}

	@Override
	public List<MapOverlay> sortOverlays() {
		List<MapOverlay> overlays = getOverlays();
		Object[] sorted = sorter.sort(overlays);

		MapOverlay ov;
		for (int i = 0; i < sorted.length; i++) {
			ov = (MapOverlay) sorted[i];
			ov.setzIndex(i);
			overlays.set(i, ov);
		}

		return overlays;
	}

	@Override
	public void fireZIndexChanged() {
		this.mOverlays = this.sortOverlays();
		resumeDraw();

		for (ZIndexChangedListener listener : this.zIndexChangedListeners) {
			listener.onZIndexChanged();
		}
	}

	@Override
	public void setBaseLayer(TileOverlay overlay) {
		baseOverlay = overlay;
	}

	@Override
	public TileOverlay getBaseLayer() {
		return baseOverlay;
	}

	@Override
	public void instantiateTileProviderfromSettings() {
		this.baseOverlay.instantiateTileProviderfromSettings();
	}

}

class TileRasterThread extends Thread {
	private SurfaceHolder surfaceHolder;
	private IMapView view;
	private boolean done = false;
	public boolean pause = false;

	public TileRasterThread(SurfaceHolder surfaceHolder, IMapView view) {
		this.surfaceHolder = surfaceHolder;
		this.view = view;
	}

	// public void setRunning(boolean run) {
	// this.run = run;
	// }

	public SurfaceHolder getSurfaceHolder() {
		return surfaceHolder;
	}

	@Override
	public void run() {
		Canvas c;
		while (!done) {
			while (pause && view.getScaler().isFinished()) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			c = null;
			try {
				c = surfaceHolder.lockCanvas();
				// if (this.view.acetate.isFirstTouch())
				// synchronized (surfaceHolder) {
				// this.view.onDraw(c);
				// this.view.scaleCanvasForZoom();
				// }
				// else {
				this.view.draw(c);
				this.view.scaleCanvasForZoom();
				// }
			} finally {
				if (c != null) {
					surfaceHolder.unlockCanvasAndPost(c);
				}
			}
		}
	}

	public void requestExitAndWait() {
		done = true;
		try {
			join();
		} catch (InterruptedException ex) {

		}
	}

	public void onWindowResize(int w, int h) {
		view.resumeDraw();
	}
}
