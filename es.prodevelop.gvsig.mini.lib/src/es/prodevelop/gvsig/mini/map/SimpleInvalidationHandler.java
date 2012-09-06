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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.os.Handler;
import android.os.Message;
import es.prodevelop.tilecache.provider.Downloader;
import es.prodevelop.tilecache.provider.filesystem.impl.TileFilesystemProvider;

public class SimpleInvalidationHandler extends Handler {

	private Logger log = LoggerFactory
			.getLogger(SimpleInvalidationHandler.class);

	/*
	 * Coupled with MapView instead of IMapView to keep fireXXX methods out of
	 * the interface
	 */
	private MapView mapView;

	public SimpleInvalidationHandler(MapView mapView) {
		this.mapView = mapView;
	}

	@Override
	public void handleMessage(final Message msg) {
		try {
			switch (msg.what) {
			case Downloader.MAPTILEDOWNLOADER_SUCCESS_ID:
				// if (!Utils.isSDMounted()) {
				// TileRaster.this.invalidate();
				// TileRaster.this.mTileProvider.getMFSTileProvider().getPendingQueue().remove(((TileEvent)msg.obj).getTile().getTileString());
				// }
				break;
			case Downloader.MAPTILEDOWNLOADER_FAIL_ID:
				// bufferCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
				break;
			// case Downloader.REMOVE_CACHE_URL:
			// // if (!Utils.isSDMounted()) {
			// TileRaster.this.getMTileProvider().getDownloader().
			// // }
			// break;
			case TileFilesystemProvider.MAPTILEFSLOADER_FAIL_ID:
				break;
			case TileFilesystemProvider.MAPTILEFSLOADER_SUCCESS_ID:
				// TileRaster.this.invalidate();
				// TileRaster.this.mTileProvider.getMFSTileProvider().getPendingQueue().remove(((TileEvent)msg.obj).getTile().getTileString());
				break;
			case IMapView.UPDATE_ZOOM_CONTROLS:
				mapView.fireZoomLevelChanged();
				// cleanZoomRectangle();
				// TileRaster.this.postInvalidate();
				break;
			case IMapView.ANIMATION_FINISHED_NEED_ZOOM:
				mapView.getMapViewController().zoomIn();
				break;
			}
		} catch (Exception e) {
			log.error("SimpleInvalidationHandler", e);
		} finally {
			mapView.resumeDraw();
		}
	}
}
