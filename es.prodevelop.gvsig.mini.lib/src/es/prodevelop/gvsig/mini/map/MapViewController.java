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
 */

package es.prodevelop.gvsig.mini.map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.prodevelop.geodetic.utils.conversion.ConversionCoords;
import es.prodevelop.gvsig.mini.geom.Extent;
import es.prodevelop.gvsig.mini.geom.Point;
import es.prodevelop.gvsig.mini.geom.android.GPSPoint;
import es.prodevelop.gvsig.mini.map.MapView.Scaler;
import es.prodevelop.gvsig.mini.utiles.WorkQueue;
import es.prodevelop.gvsig.mobile.fmap.proj.CRSFactory;
import es.prodevelop.tilecache.renderer.MapRenderer;

public class MapViewController implements IMapViewController {

	private final static Logger log = LoggerFactory
			.getLogger(MapViewController.class);

	private IMapView mapView;
	private AnimationRunner mCurrentAnimationRunner;

	public MapViewController(IMapView mapView) {
		this.mapView = mapView;
		mapView.getInvalidationHandler().sendEmptyMessage(
				IMapView.UPDATE_ZOOM_CONTROLS);
	}

	@Override
	public IMapView getMapView() {
		return this.mapView;
	}

	@Override
	public void setMapCenter(final GPSPoint aCenter) {
		try {
			this.setMapCenterFromLonLat(aCenter.getLongitudeE6() / 1E6,
					aCenter.getLatitudeE6() / 1E6);

		} catch (Exception e) {
			log.error("setMapCenter:", e);
		}
	}

	@Override
	public void setMapCenter(final double x, final double y) {
		try {
			final IMapView mapView = this.getMapView();
			final MapRenderer renderer = mapView.getMRendererInfo();

			if (renderer.getExtent().contains(x, y)) {
				renderer.setCenter(x, y);
				mapView.notifyExtentChangedListeners(
						renderer.getCurrentExtent(), mapView.getZoomLevel(),
						renderer.getCurrentRes());
				mapView.setDirty();
				mapView.resumeDraw();
			}
		} catch (Exception e) {
			log.error("setMapCenter:", e);
		}
	}

	@Override
	public void setZoomLevel(int aZoomLevel) {
		final IMapView mapView = this.getMapView();
		final MapRenderer renderer = mapView.getMRendererInfo();

		int zoomLevel = aZoomLevel;
		if (aZoomLevel < renderer.getZoomMinLevel()) {
			zoomLevel = renderer.getZoomMinLevel();
		} else if (aZoomLevel > renderer.getZOOM_MAXLEVEL()) {
			zoomLevel = renderer.getZOOM_MAXLEVEL();
		}

		mapView.setTempZoomLevel(zoomLevel);

		try {
			renderer.setZoomLevel(zoomLevel);
			getMapView().notifyExtentChangedListeners(
					renderer.getCurrentExtent(), zoomLevel,
					renderer.getCurrentRes());
			mapView.getViewPort().setDist1Pixel(
					mapView.getViewPort().resolutions[zoomLevel]);
		} catch (Exception e) {
			log.error("setZoomLevel:", e);

		}
		mapView.setIsScalling(true);

		mapView.getInvalidationHandler().sendEmptyMessage(
				IMapView.UPDATE_ZOOM_CONTROLS);
	}

	@Override
	public void setZoomLevelFromResolution(double resolution) {
		try {
			final double[] resolutions = mapView.getMRendererInfo().resolutions;
			final int size = resolutions.length;

			double current = 0;
			double moreAccurate = -1;
			int zoomLevel = -1;
			for (int i = 0; i < size; i++) {
				current = resolutions[i];
				double diff = current - resolution;
				if (diff > 0) {
					double moreAccurateDiff = current - moreAccurate;
					if (diff < moreAccurateDiff) {
						moreAccurateDiff = diff;
						zoomLevel = i;
					}
				}
			}
			if (zoomLevel != -1) {
				this.setZoomLevel(zoomLevel);
			}
		} catch (Exception e) {
			log.error("setZoomLevelFromResolution", e);
		}
	}

	@Override
	public void zoomIn() {
		try {
			final IMapView mapView = this.getMapView();
			mapView.getTileProvider().clearPendingQueue();
			mapView.setTempZoomLevel(mapView.getTempZoomLevel() + 1);
			// this.setZoomLevel(this.getMRendererInfo().getZoomLevel() + 1);

			final Scaler mScaler = mapView.getScaler();

			if (mScaler.isFinished()) {
				mScaler.startScale(1.0f, 2.0f, Scaler.DURATION_SHORT);
				mapView.postInvalidate();
			} else {
				mScaler.extendDuration(Scaler.DURATION_SHORT);
				mScaler.setFinalScale(mScaler.getFinalScale() * 2.0f);
			}
		} catch (Exception e) {
			log.error("zoomIn:", e);
		}
	}

	@Override
	public void zoomOut() {
		try {
			final IMapView mapView = this.getMapView();
			mapView.getTileProvider().clearPendingQueue();
			final Scaler mScaler = mapView.getScaler();

			if (mapView.getMRendererInfo().getZoomLevel() != 0) {
				mapView.setTempZoomLevel(mapView.getTempZoomLevel() - 1);
				if (mScaler.isFinished()) {
					mScaler.startScale(1.0f, 0.5f, Scaler.DURATION_SHORT);
					mapView.postInvalidate();
				} else {
					mScaler.extendDuration(Scaler.DURATION_SHORT);
					mScaler.setFinalScale(mScaler.getFinalScale() * 0.5f);
				}
			}
		} catch (Exception e) {
			log.error("zoomOut:", e);
		}
	}

	@Override
	public void setZoomLevel(int aZoomLevel, boolean several) {
		try {
			final IMapView mapView = this.getMapView();
			final Scaler mScaler = mapView.getScaler();

			int levels = aZoomLevel - mapView.getMRendererInfo().getZoomLevel();
			mapView.setTempZoomLevel(aZoomLevel);

			if (several) {
				// Zoom in
				if (levels > 0) {
					if (mScaler.isFinished()) {
						mScaler.startScale(1.0f, (float) (levels * 2.0f),
								Scaler.DURATION_SHORT);
						mapView.postInvalidate();
					} else {
						mScaler.extendDuration(Scaler.DURATION_SHORT);
						mScaler.setFinalScale(mScaler.getFinalScale() * 2.0f);
					}
				} else {
					levels = Math.abs(1 / levels);
					if (mScaler.isFinished()) {
						mScaler.startScale(1.0f, (float) (levels / 2.0f),
								Scaler.DURATION_SHORT);
						mapView.postInvalidate();
					} else {
						mScaler.extendDuration(Scaler.DURATION_SHORT);
						mScaler.setFinalScale(mScaler.getFinalScale() * 0.5f);
					}
				}
			} else {
				this.setZoomLevel(aZoomLevel);
			}
		} catch (Exception e) {
			log.error("setZoomLevel", e);
		}
	}

	@Override
	public void zoomToExtent(Extent extent, boolean layerChanged,
			int currentZoomLevel) {
		if (extent == null) {
			return;
		}
		zoomToSpan(extent.getWidth(), extent.getHeight(), layerChanged,
				currentZoomLevel, false, null);
	}

	@Override
	public void zoomToExtent(Extent extent, boolean layerChanged) {
		if (extent == null) {
			return;
		}
		zoomToExtent(extent, layerChanged, false, extent.getCenter());
	}

	@Override
	public void zoomToExtent(Extent extent, boolean layerChanged,
			boolean forceZoomMore, Point animateToCenter) {
		if (extent == null) {
			return;
		}
		zoomToSpan(extent.getWidth(), extent.getHeight(), layerChanged, this
				.getMapView().getZoomLevel(), forceZoomMore, animateToCenter);
	}

	@Override
	public void zoomToSpan(double width, double height, boolean layerChanged,
			int currentZoomLevel, boolean forceZoomMore, Point animateToCenter) {
		if (width <= 0 || height <= 0) {
			if (animateToCenter != null) {
				this.setMapCenter(animateToCenter.getX(),
						animateToCenter.getY());
			}
			return;
		}

		int zoomLevel = getMapView().getZoomLevelFitsExtent(width, height,
				currentZoomLevel);
		if (zoomLevel != -1) {
			if (!layerChanged && zoomLevel <= this.getMapView().getZoomLevel())
				zoomLevel++;
			if (forceZoomMore)
				zoomLevel++;
			if (animateToCenter != null)
				animateTo(animateToCenter.getX(), animateToCenter.getY());
			setZoomLevel(zoomLevel, !layerChanged);
		}
	}

	@Override
	public void zoomToSpan(double width, double height, boolean layerChanged) {
		if (width <= 0 || height <= 0) {
			return;
		}

		int zoomLevel = getMapView().getZoomLevelFitsExtent(width, height);
		if (zoomLevel != -1) {
			if (!layerChanged && zoomLevel <= this.getMapView().getZoomLevel())
				zoomLevel++;
			setZoomLevel(zoomLevel, !layerChanged);
		}
	}

	@Override
	public void animateTo(double x, double y) {
		this.animateTo(x, y, false);
	}

	@Override
	public void animateTo(double x, double y, boolean zoomChanged) {
		mCurrentAnimationRunner = new LinearAnimationRunner(x, y, true,
				zoomChanged);
		WorkQueue.getExclusiveInstance().execute(mCurrentAnimationRunner);
	}

	@Override
	public void stopAnimation(boolean jumpToTarget) {
		final AnimationRunner currentAnimationRunner = this.mCurrentAnimationRunner;

		if (currentAnimationRunner != null && !currentAnimationRunner.isDone()) {
			// currentAnimationRunner.interrupt();
			if (jumpToTarget) {
				this.setMapCenter(currentAnimationRunner.mTargetX,
						currentAnimationRunner.mTargetY);
			}
		}
	}

	@Override
	public void setMapCenterFromLonLat(double lon, double lat) {
		try {
			if (lon == 0.0 && lat == 0.0)
				return;
			double[] coords = ConversionCoords.reproject(
					lon,
					lat,
					CRSFactory.getCRS("EPSG:4326"),
					CRSFactory.getCRS(this.getMapView().getMRendererInfo()
							.getSRS()));
			this.setMapCenter(coords[0], coords[1]);

		} catch (Exception e) {
			log.error("setMapCenterFromLonLat", e);
		}
	}

	private abstract class AnimationRunner implements Runnable {

		protected final int mSmoothness, mDuration;
		protected final double mTargetX, mTargetY;
		protected boolean mDone = false;

		protected final int mStepDuration;

		protected final double mPanTotalX, mPanTotalY;

		public AnimationRunner(final double aTargetX, final double aTargetY) {
			this(aTargetX, aTargetY, 10, 1000);
		}

		public abstract void setTarget(final double x, final double y);

		public AnimationRunner(final double aTargetX, final double aTargetY,
				final int aSmoothness, final int aDuration) {
			this.mTargetX = aTargetX;
			this.mTargetY = aTargetY;
			this.mSmoothness = aSmoothness;
			this.mDuration = aDuration;
			this.mStepDuration = aDuration / aSmoothness;

			Point center = getMapView().getMRendererInfo().getCenter();

			final double mapCenterX = center.getX();
			final double mapCenterY = center.getY();

			this.mPanTotalX = (mapCenterX - aTargetX);
			this.mPanTotalY = (mapCenterY - aTargetY);
		}

		@Override
		public void run() {
			onRunAnimation();
			this.mDone = true;
		}

		public boolean isDone() {
			return this.mDone;
		}

		public abstract void onRunAnimation();
	}

	private class LinearAnimationRunner extends AnimationRunner {

		protected double mPanPerStepX, mPanPerStepY;
		private boolean zoomChanged = false;

		public LinearAnimationRunner(final double aTargetX,
				final double aTargetY, final boolean animate,
				final boolean zoomChanged) {
			this(aTargetX, aTargetY, 10, 500, animate);
			this.zoomChanged = zoomChanged;
			// start();
		}

		public LinearAnimationRunner(final double aTargetX,
				final double aTargetY, final int aSmoothness,
				final int aDuration, final boolean animate) {
			super(aTargetX, aTargetY, aSmoothness, aDuration);
			if (animate)
				setTarget(aTargetX, aTargetY, aSmoothness);
		}

		public void setTarget(final double x, final double y,
				final int aSmoothness) {
			Point center = getMapView().getMRendererInfo().getCenter();
			final double mapCenterX = center.getX();
			final double mapCenterY = center.getY();

			this.mPanPerStepX = (mapCenterX - x) / aSmoothness;
			this.mPanPerStepY = (mapCenterY - y) / aSmoothness;

			// this.setName("LinearAnimationRunner");
		}

		public void setTarget(final double x, final double y) {
			setTarget(x, y, 10);
		}

		@Override
		public void onRunAnimation() {
			final double panPerStepX = this.mPanPerStepX;
			final double panPerStepY = this.mPanPerStepY;
			final int stepDuration = this.mStepDuration;
			try {
				// while (!mDone) {
				// synchronized (lock) {
				// try {
				// lock.wait();
				// } catch (InterruptedException ignored) {
				//
				// }
				// }

				double newMapCenterX;
				double newMapCenterY;

				Point center;

				for (int i = this.mSmoothness; i > 0; i--) {
					center = getMapView().getMRendererInfo().getCenter();

					newMapCenterX = center.getX() - panPerStepX;
					newMapCenterY = center.getY() - panPerStepY;
					setMapCenter(newMapCenterX, newMapCenterY);

					Thread.sleep(stepDuration);
				}
				setMapCenter(super.mTargetX, super.mTargetY);
				// }
			} catch (final Exception e) {
				// this.interrupt();
			} finally {
				if (zoomChanged)
					getMapView().getInvalidationHandler().sendEmptyMessage(
							IMapView.ANIMATION_FINISHED_NEED_ZOOM);
			}
		}
	}
}
