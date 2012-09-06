/* gvSIG Mini. A free mobile phone viewer of free maps.
 *
 * Copyright (C) 2009 Prodevelop.
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
 *   gvSIG Mini has been partially funded by IMPIVA (Instituto de la Peque�a y
 *   Mediana Empresa de la Comunidad Valenciana) &
 *   European Union FEDER funds.
 *   
 *   2010.
 *   author Alberto Romeu aromeu@prodevelop.es 
 *   author Ruben Blanco rblanco@prodevelop.es 
 *   
 */

package es.prodevelop.gvsig.mini.overlay;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import android.app.Activity;
import android.app.LauncherActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;
import es.prodevelop.android.spatialindex.cluster.Cluster;
import es.prodevelop.geodetic.utils.conversion.ConversionCoords;
import es.prodevelop.gvsig.mini.R;
import es.prodevelop.gvsig.mini.activities.ActivityBundlesManager;
import es.prodevelop.gvsig.mini.activities.SearchActivity;
import es.prodevelop.gvsig.mini.activities.feature.OpenDetailsItemClickListener;
import es.prodevelop.gvsig.mini.activities.feature.ResultSearchActivity;
import es.prodevelop.gvsig.mini.common.CompatManager;
import es.prodevelop.gvsig.mini.context.ItemContext;
import es.prodevelop.gvsig.mini.exceptions.BaseException;
import es.prodevelop.gvsig.mini.geom.Extent;
import es.prodevelop.gvsig.mini.geom.Feature;
import es.prodevelop.gvsig.mini.geom.IGeometry;
import es.prodevelop.gvsig.mini.geom.Pixel;
import es.prodevelop.gvsig.mini.map.IMapView;
import es.prodevelop.gvsig.mini.map.OnSelectFeatureListener;
import es.prodevelop.gvsig.mini.views.PopupView;
import es.prodevelop.gvsig.mobile.fmap.proj.CRSFactory;

/**
 * A MapOverlay to draw transient geometries. For example the rectangle zoom
 * envelope. It also manages touchevents
 * 
 * @author aromeu
 * @author rblanco
 * 
 */
public class AcetateOverlay extends MapOverlay implements
		OnSelectFeatureListener {

	int touchCounter = 0;
	final static int TOUCH_COUNTER = 20;
	public final static String DEFAULT_NAME = "ACETATE";
	private int lastZoomLevel = -1;
	private Class detailClass;

	protected PopupView popupView;
	private double[] popupCenter;
	private OpenDetailsItemClickListener featureDetailOpener;

	public AcetateOverlay(Context context, IMapView mapView, String name) {
		super(context, mapView, name);
		try {
			CompatManager.getInstance().getRegisteredLogHandler()
					.configureLogger(log);
		} catch (BaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			int maxWidth = getMapView().getMapWidth();
			int maxHeight = getMapView().getMapHeight();
			popupView = new PopupView((Activity) context, maxWidth, maxHeight);
			path = new Path();
			t = getMapView();
			featureDetailOpener = new OpenDetailsItemClickListener(
					this.getContext(), getMapView()
							.getMyLocationCenterMercator());
		} catch (Exception e) {
			log.log(Level.SEVERE, "", e);
		}
	}

	public void setDetailClass(Class cl) {
		this.detailClass = cl;
	}

	public Class getDetailClass() {
		return this.detailClass;
	}

	private final static Logger log = Logger.getLogger(AcetateOverlay.class
			.getName());

	private int mTouchDownX;
	private int mTouchDownY;
	public static int mTouchMapOffsetX;
	public static int mTouchMapOffsetY;
	private int fromX = -1;
	private int fromY = -1;
	private int toX = -1;
	private int toY = -1;
	IMapView t;
	Path path;

	private ArrayList lastSelectedFeatures;

	@Override
	protected void onDraw(Canvas c, IMapView maps) {
		try {
			// if (lastZoomLevel == -1 || lastZoomLevel == maps.getZoomLevel())
			// {

			final ArrayList features = this.lastSelectedFeatures;

			if (features != null && features.size() > 0) {
				int[] xy = maps.getMRendererInfo().toPixels(
						new double[] { popupCenter[0], popupCenter[1] });
				popupView.setPos(xy[0], xy[1]);
				popupView.dispatchDraw(c);
			}
			// } else {
			// this.setPopupVisibility(View.INVISIBLE);
			// }

			// FIXME
			// if (drawZoomRectangle && rectangle != null) {
			// if (rectangle.width() <= 0 || rectangle.height() <= 0) {
			// rectangle.left -= 1;
			// rectangle.right += 1;
			// rectangle.top -= 1;
			// rectangle.bottom += 1;
			// }
			// c.drawRect(rectangle, Paints.rectanglePaint);
			// }
			// if (toX >= 0 && toY >= 0 && fromX >= 0 && fromY >= 0
			// && !maps.panMode) {
			// path.rewind();
			// path.moveTo(fromX, fromY);
			// path.lineTo(fromX, toY);
			// path.lineTo(toX, toY);
			// path.lineTo(toX, fromY);
			// path.lineTo(fromX, fromY);
			// c.drawPath(path, Paints.filledPaint);
			// c.drawPath(path, Paints.rectanglePaint);
			// }
		} catch (Exception e) {
			log.log(Level.SEVERE, "", e);
		}
	}

	public void drawClusterRect(final Canvas c, final IGeometry f,
			final IMapView maps) {
		if (f instanceof Cluster && popupView.getVisibility() == View.VISIBLE) {
			final Cluster cl = (Cluster) f;
			final Extent bbox = cl.getBoundingBox();
			final String SRS = getMapView().getMRendererInfo().getSRS();
			double[] minXY = ConversionCoords.reproject(bbox.getMinX(),
					bbox.getMinY(), CRSFactory.getCRS("EPSG:4326"),
					CRSFactory.getCRS(SRS));
			double[] maxXY = ConversionCoords.reproject(bbox.getMaxX(),
					bbox.getMaxY(), CRSFactory.getCRS("EPSG:4326"),
					CRSFactory.getCRS(SRS));

			int[] minPixXY = maps.getMRendererInfo().toPixels(minXY);
			int[] maxPixXY = maps.getMRendererInfo().toPixels(maxXY);

			c.drawRect((float) minPixXY[0], (float) maxPixXY[1],
					(float) maxPixXY[0], (float) minPixXY[1],
					Paints.circlePaint);
		}
	}

	@Override
	protected void onDrawFinished(Canvas c, IMapView maps) {
		// TODO Auto-generated method stub

	}

	private void updateRectangle() {
		int toX = mTouchMapOffsetX + mTouchDownX;
		int toY = mTouchDownY + mTouchMapOffsetY;

		if (this.mTouchDownX < toX) {
			fromX = this.mTouchDownX;
			this.toX = toX;
		} else {
			fromX = toX;
			this.toX = this.mTouchDownX;
		}

		if (this.mTouchDownY < toY) {
			fromY = this.mTouchDownY;
			this.toY = toY;
		} else {
			fromY = toY;
			this.toY = this.mTouchDownY;
		}
	}

	/**
	 * Retrieves the nerest feature from the pressed pixel and pass them to
	 * IMapView
	 * 
	 * @param e
	 * @param osmtile
	 * @return
	 */
	public boolean onLongPress(MotionEvent e, IMapView osmtile) {
		try {
			if (onSingleTapUp(e, osmtile))
				return true;
			Pixel pixel = new Pixel((int) e.getX(), (int) e.getY());
			Feature f = getNearestFeature(pixel);
			if (f != null) {
				this.getMapView().setSelectedFeature(f);
				return true;
			} else {
				// getMapView().map.getPopup().setVisibility(View.INVISIBLE);
				this.getMapView().setSelectedFeature(null);
				return false;
			}
		} catch (Exception ex) {
			return false;
		}

	}

	public boolean onTouchEvent(final MotionEvent event) {
		return false;
	}

	public boolean onSingleTapUp(MotionEvent e, IMapView osmtile) {
		if (popupView.onTouchEvent(e)) {			
			if (this.lastSelectedFeatures != null) {
				if (this.lastSelectedFeatures.size() > 1) {
					Intent intent = new Intent(this.getContext(),
							ResultSearchActivity.class);
					// Register the features so the activity can access them
					ActivityBundlesManager.getInstance().getIntentFiller()
							.fillSearchCenter(intent, getMapView());
					// At the moment there are no filter capabilities
					intent.putExtra(SearchActivity.HIDE_AUTOTEXTVIEW, true);
					// i.putExtra(ResultSearchActivity.QUERY, query.toString());
					// We use a Singleton instead of packing all the features
					// into a
					// Bundle
					ActivityBundlesManager.getInstance().registerFeatures(
							this.lastSelectedFeatures, null);
					getContext().startActivity(intent);
				} else {
					featureDetailOpener.setCenter(getMapView()
							.getMyLocationCenterMercator());
					featureDetailOpener
							.launchDetailActivity(this.lastSelectedFeatures
									.get(0));
				}
				return true;
			}
		}

		return false;
	}

	private void resetRectangle() {
		fromX = -1;
		fromY = -1;
		toX = -1;
		toY = -1;
	}

	boolean drawZoomRectangle = false;
	Rect rectangle;

	public void drawZoomRectangle(Rect r) {
		try {
			drawZoomRectangle = true;
			rectangle = r;
		} catch (Exception e) {
			log.log(Level.SEVERE, "", e);
		}
	}

	public void cleanZoomRectangle() {
		try {
			drawZoomRectangle = false;
			rectangle = null;
		} catch (Exception e) {
			log.log(Level.SEVERE, "", e);
		}
	}

	@Override
	/**
	 * returns null
	 */
	public Feature getNearestFeature(Pixel pixel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	/**
	 * returns null
	 */
	public ItemContext getItemContext() {
		return null;
	}

	@Override
	public void destroy() {
		try {
			Paints.filledPaint = null;
			Paints.rectanglePaint = null;
			path = null;
		} catch (Exception e) {
			log.log(Level.SEVERE, "", e);
		}
	}

	public boolean isFirstTouch() {
		return ++touchCounter < TOUCH_COUNTER;
	}

	@Override
	public void onExtentChanged(Extent newExtent, int zoomLevel,
			double resolution) {
		if (lastZoomLevel != zoomLevel)
			setPopupVisibility(View.INVISIBLE);
	}

	@Override
	public void onLayerChanged(String layerName) {
		// TODO Auto-generated method stub

	}

	private PopupView getPopup() {
		return this.popupView;
	}

	public int getPopupVisibility() {
		return getPopup().getVisibility();
	}

	public void updatePopup(ArrayList features) {
		if (features != null && features.size() > 0) {
			String text = this.getContext().getResources()
					.getString(R.string.show_details);
			if (features.size() == 1) {
				text = ((es.prodevelop.gvsig.mini.geom.impl.base.Feature) features
						.get(0)).getText();
				if (text == null) {
					text = (String) getContext().getText(R.string.show_details);
				}
			}
			setPopupText(text);
			setPopupVisibility(View.VISIBLE);
		} else {
			this.setPopupVisibility(View.INVISIBLE);
		}
	}

	public void setPopupVisibility(int visibility) {
		getPopup().setVisibility(visibility);
		lastZoomLevel = getMapView().getZoomLevel();
		getMapView().resumeDraw();
	}

	public void setPopupText(String text) {
		getPopup().setText(text);
		getMapView().resumeDraw();
	}

	public void setPopupPos(int x, int y) {
		popupCenter = getMapView().getMRendererInfo().fromPixels(
				new int[] { x, y });
		getPopup().setPos(x, y);
		getMapView().resumeDraw();
	}

	public void setPopupMaxSize(int maxX, int maxY) {
		getPopup().setMaxSize(maxX, maxY);
		getMapView().resumeDraw();
	}

	@Override
	public void onFeaturesSelected(ArrayList features) {
		this.lastSelectedFeatures = features;
		updatePopup(features);
	}

	@Override
	public void onFeaturesUnselected(ArrayList features) {
		this.lastSelectedFeatures = features;
		updatePopup(features);
	}
}
