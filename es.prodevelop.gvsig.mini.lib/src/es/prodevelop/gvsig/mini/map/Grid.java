package es.prodevelop.gvsig.mini.map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.graphics.Rect;
import es.prodevelop.gvsig.mini.common.impl.Tile;
import es.prodevelop.gvsig.mini.geom.Extent;
import es.prodevelop.gvsig.mini.geom.Pixel;
import es.prodevelop.gvsig.mini.utiles.Cancellable;
import es.prodevelop.tilecache.renderer.MapRenderer;
import es.prodevelop.tilecache.util.Utilities;

public class Grid implements ExtentChangedListener {

	private final static Logger log = LoggerFactory.getLogger(Grid.class);

	private MapView mapView;
	private MapRenderer renderer;
	private Cancellable cancellable = Utilities.getNewCancellable();

	private Tile[] tiles;

	public Grid(MapView mapView) {
		this.mapView = mapView;
	}

	public MapRenderer getRenderer() {
		return renderer;
	}

	public void setRenderer(MapRenderer renderer) {
		this.renderer = renderer;
	}

	public void sortTiles(final Tile[] array2) {
		try {
			final Pixel center = new Pixel(mapView.getCenterPixelX(),
					mapView.getCenterPixelY());
			double e1, e2;
			final int length = array2.length;
			Tile t;
			Tile t1;
			final Pixel temp = new Pixel((this.renderer
					.getMAPTILE_SIZEPX() / 2), (this.renderer
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
			tiles = array2;
		} catch (final Exception e) {
			log.error("sortTiles:", e);
		}
	}

	public Tile[] getTiles() {
		return tiles;
	}

	public void setTiles(Tile[] tiles) {
		this.tiles = tiles;
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

	public void calcGrid(final Extent viewExtent) {

		final int viewWidth = mapView.getMapWidth();
		final int viewHeight = mapView.getMapHeight();

		ViewPort.mapHeight = viewHeight;
		ViewPort.mapWidth = viewWidth;

		final MapRenderer renderer = this.renderer;

		final int zoomLevel = renderer.getZoomLevel();

		final int tileSizePx = renderer.getMAPTILE_SIZEPX();

		int[] centerMapTileCoords = renderer.getMapTileFromCenter();

		final int additionalTilesNeededToLeftOfCenter;
		final int additionalTilesNeededToRightOfCenter;
		final int additionalTilesNeededToTopOfCenter;
		final int additionalTilesNeededToBottomOfCenter;

		Pixel upperLeftCornerOfCenterMapTile = renderer
				.getUpperLeftCornerOfCenterMapTileInScreen(null);

		final int centerMapTileScreenLeft = upperLeftCornerOfCenterMapTile
				.getX();
		final int centerMapTileScreenTop = upperLeftCornerOfCenterMapTile
				.getY();

		final int centerMapTileScreenRight = centerMapTileScreenLeft
				+ tileSizePx;
		final int centerMapTileScreenBottom = centerMapTileScreenTop
				+ tileSizePx;

		additionalTilesNeededToLeftOfCenter = (int) Math
				.ceil((float) centerMapTileScreenLeft / tileSizePx);
		additionalTilesNeededToRightOfCenter = (int) Math
				.ceil((float) (viewWidth - centerMapTileScreenRight)
						/ tileSizePx);
		additionalTilesNeededToTopOfCenter = (int) Math
				.ceil((float) centerMapTileScreenTop / tileSizePx);
		additionalTilesNeededToBottomOfCenter = (int) Math
				.ceil((float) (viewHeight - centerMapTileScreenBottom)
						/ tileSizePx);

		final int[] mapTileCoords = new int[] { centerMapTileCoords[0],
				centerMapTileCoords[1] };

		final int size = (additionalTilesNeededToBottomOfCenter
				+ additionalTilesNeededToTopOfCenter + 1)
				* (additionalTilesNeededToRightOfCenter
						+ additionalTilesNeededToLeftOfCenter + 1);
		Tile[] tiles = new Tile[size];
		int cont = 0;

		final Extent maxExtent = renderer.getExtent();
		// final Extent viewExtent = mapView.getViewPort().calculateExtent(
		// viewWidth, viewHeight, renderer.getCenter());
		// this.getMTileProvider().setViewExtent(viewExtent);

		final String layerName = renderer.getNAME();

		boolean process = true;

		for (int y = -additionalTilesNeededToTopOfCenter; y <= additionalTilesNeededToBottomOfCenter; y++) {
			for (int x = -additionalTilesNeededToLeftOfCenter; x <= additionalTilesNeededToRightOfCenter; x++) {
				process = true;
				if (viewExtent.intersect(maxExtent)) {
					final int tileLeft = this.mapView.getmTouchMapOffsetX()
							+ centerMapTileScreenLeft + (x * tileSizePx);
					final int tileTop = this.mapView.getmTouchMapOffsetY()
							+ centerMapTileScreenTop + (y * tileSizePx);

					double[] coords = new double[] {
							maxExtent.getLefBottomCoordinate().getX(),
							maxExtent.getLefBottomCoordinate().getY() };

					final int[] leftBottom = renderer.toPixels(coords);
					coords = new double[] {
							maxExtent.getRightTopCoordinate().getX(),
							maxExtent.getRightTopCoordinate().getY() };
					final int[] rightTop = renderer.toPixels(coords);

					final Rect r = new Rect();
					r.bottom = leftBottom[1];
					r.left = leftBottom[0];
					r.right = rightTop[0];
					r.top = rightTop[1];

					process = r.intersects(tileLeft, tileTop, tileLeft
							+ tileSizePx / 2, tileTop + tileSizePx / 2);

				}

				if (process) {
					mapTileCoords[0] = centerMapTileCoords[0]
							+ renderer.isTMS() * y;

					mapTileCoords[1] = centerMapTileCoords[1] + x;

					final String tileURLString = renderer.getTileURLString(
							mapTileCoords, zoomLevel);

					final int[] tile = new int[] { mapTileCoords[0],
							mapTileCoords[1] };
					final int tileLeft = this.mapView.getmTouchMapOffsetX()
							+ centerMapTileScreenLeft + (x * tileSizePx);
					final int tileTop = this.mapView.getmTouchMapOffsetY()
							+ centerMapTileScreenTop + (y * tileSizePx);

					// final Extent ext =
					// TileConversor.tileMeterBounds(tile[0], tile[1],
					// resolution, -originX, -originY);
					// System.out.println("Tile: " + tile[0] + ", " +
					// tile[1] + "Extent: " + ext.toString());
					final Tile t = new Tile(tileURLString, tile, new Pixel(
							tileLeft, tileTop), zoomLevel, layerName,
							cancellable, null);
					if (cont < tiles.length)
						tiles[cont] = t;

				}
				cont++;
			}
		}

		this.sortTiles(tiles);
	}

	@Override
	public void onExtentChanged(Extent newExtent, int zoomLevel,
			double resolution) {
//		calcGrid(newExtent);
	}
}
