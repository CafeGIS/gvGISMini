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

package es.prodevelop.gvsig.mini.activities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import es.prodevelop.gvsig.mini.R;
import es.prodevelop.gvsig.mini.common.CompatManager;
import es.prodevelop.gvsig.mini.common.IContext;
import es.prodevelop.gvsig.mini.common.IEvent;
import es.prodevelop.gvsig.mini.geom.Extent;
import es.prodevelop.gvsig.mini.map.IMapDownloader;
import es.prodevelop.gvsig.mini.map.IMapView;
import es.prodevelop.gvsig.mini.map.ViewPort;
import es.prodevelop.gvsig.mini.tasks.tiledownloader.TileDownloadCallbackHandler;
import es.prodevelop.gvsig.mini.tasks.tiledownloader.TileDownloadWaiter;
import es.prodevelop.gvsig.mini.utiles.Cancellable;
import es.prodevelop.gvsig.mini.utiles.WorkQueue;
import es.prodevelop.tilecache.IDownloadWaiter;
import es.prodevelop.tilecache.TileDownloaderTask;
import es.prodevelop.tilecache.generator.impl.FitScreenBufferStrategy;
import es.prodevelop.tilecache.layers.Layers;
import es.prodevelop.tilecache.provider.TileProvider;
import es.prodevelop.tilecache.provider.filesystem.strategy.ITileFileSystemStrategy;
import es.prodevelop.tilecache.provider.filesystem.strategy.impl.FileSystemStrategyManager;
import es.prodevelop.tilecache.renderer.MapRenderer;
import es.prodevelop.tilecache.util.ConstantsTileCache;
import es.prodevelop.tilecache.util.Utilities;

public abstract class MapDownloader extends MapLocationActivity implements
		IMapDownloader {

	private Logger log = LoggerFactory.getLogger(MapDownloader.class);

	private TileDownloadWaiterDelegate tileWaiter;
	private LinearLayout downloadTilesLayout;
	private ProgressBar downloadTilesPB;
	private Button downloadTilesButton;
	private TextView totalTiles;
	private TextView totalMB;
	private TextView totalZoom;
	private TileDownloaderTask t;
	private SeekBar downTilesSeekBar;

	private AlertDialog downloadTileAlert;
	private Cancellable downloadCancellable;

	/**
	 * Called when the activity is first created.
	 * */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		try {
			super.onCreate(savedInstanceState);
			tileWaiter = new TileDownloadWaiterDelegate(this);
		} catch (Exception e) {
			log.error("", e);
		} finally {
		}
	}

	public TileDownloadWaiterDelegate getTileDownloadDelegate() {
		return this.tileWaiter;
	}

	public void setTileDownloadDelegate(TileDownloadWaiterDelegate delegate) {
		this.tileWaiter = delegate;
	}

	private int previousTime = 0;

	private synchronized void updateDownloadTilesDialog() {
		try {
			runOnUiThread(new Runnable() {
				public void run() {
					int time = (int) ((System.currentTimeMillis() - tileWaiter
							.getInitDownloadTime()) / 1000);

					if (time - previousTime < 1 && !isDownloadTilesFinished) {
						return;
					}

					if (tileWaiter.getInitDownloadTime() > 0)
						previousTime = time;

					MapDownloader.this.downloadTilesPB.setMax((int) tileWaiter
							.getTotalTilesToProcess());
					MapDownloader.this.downloadTilesPB
							.setProgress((int) tileWaiter.getDownloadedNow());
					long totalDownloaded = MapDownloader.this.tileWaiter
							.getDownloadedNow();
					long total = MapDownloader.this.tileWaiter
							.getTotalTilesToProcess();
					int perc = 0;
					if (total != 0)
						perc = (int) ((double) totalDownloaded / (double) total * 100.0);

					((TextView) MapDownloader.this.downloadTilesLayout
							.findViewById(R.id.download_perc_text))
							.setText(perc + "%" + " - "
									+ tileWaiter.getTilesDownloaded() + "-"
									+ tileWaiter.getTilesFailed() + "-"
									+ tileWaiter.getTilesFromFS() + "-"
									+ tileWaiter.getTilesSkipped() + "-"
									+ tileWaiter.getTilesDeleted() + "-"
									+ tileWaiter.getTilesNotFound() + "-" + "/"
									+ total);

					String downloadedMB = String
							.valueOf(MapDownloader.this.tileWaiter
									.getBytesDownloaded() / 1024 / 1024);

					if (downloadedMB.length() > 4) {
						downloadedMB = downloadedMB.substring(0, 4);
					}

					((TextView) MapDownloader.this.downloadTilesLayout
							.findViewById(R.id.downloaded_mb_text))
							.setText(MapDownloader.this
									.getText(R.string.download_tiles_09)
									+ " "
									+ downloadedMB + " MB");

					String elapsed = es.prodevelop.gvsig.mini.utiles.Utilities
							.getTimeHoursMinutesSecondsString(time);

					((TextView) MapDownloader.this.downloadTilesLayout
							.findViewById(R.id.download_time_text))
							.setText(MapDownloader.this
									.getText(R.string.download_tiles_10)
									+ " "
									+ elapsed);

					if (totalDownloaded == 0)
						totalDownloaded = 1;

					int estimated = (int) (total * time / totalDownloaded)
							- time;

					String estimatedTime = es.prodevelop.gvsig.mini.utiles.Utilities
							.getTimeHoursMinutesSecondsString(estimated);
					((TextView) MapDownloader.this.downloadTilesLayout
							.findViewById(R.id.download_time_estimated_text))
							.setText(MapDownloader.this
									.getText(R.string.download_tiles_11)
									+ " "
									+ estimatedTime);
				}
			});
		} catch (Exception e) {
			log.error("updateDownloadTilesDialog", e);
		}
	}

	public double getBytesDownloaded() {
		return tileWaiter.getBytesDownloaded();
	}

	public IDownloadWaiter getDownloadWaiter() {
		return tileWaiter.getDownloadWaiter();
	}

	public long getDownloadedNow() {
		return tileWaiter.getDownloadedNow();
	}

	public long getEndDownloadTime() {
		return tileWaiter.getEndDownloadTime();
	}

	public Object getHandler() {
		return tileWaiter.getHandler();
	}

	public long getInitDownloadTime() {
		return tileWaiter.getInitDownloadTime();
	}

	public int getTilesDeleted() {
		return tileWaiter.getTilesDeleted();
	}

	public int getTilesDownloaded() {
		return tileWaiter.getTilesDownloaded();
	}

	public int getTilesFailed() {
		return tileWaiter.getTilesFailed();
	}

	public int getTilesFromFS() {
		return tileWaiter.getTilesFromFS();
	}

	public int getTilesNotFound() {
		return tileWaiter.getTilesNotFound();
	}

	public int getTilesSkipped() {
		return tileWaiter.getTilesSkipped();
	}

	public long getTotalTilesToProcess() {
		return tileWaiter.getTotalTilesToProcess();
	}

	public long getTotalTime() {
		return tileWaiter.getTotalTime();
	}

	public void onDownloadCanceled() {
		tileWaiter.onDownloadCanceled();
		runOnUiThread(new Runnable() {
			public void run() {
				MapDownloader.this.enableGPS();
				Toast.makeText(MapDownloader.this, R.string.download_tiles_12,
						Toast.LENGTH_LONG).show();
				MapDownloader.this.reloadLayerAfterDownload();
			}
		});
	}

	public void reloadLayerAfterDownload() {

	}

	public void onFailDownload(IEvent event) {
		tileWaiter.onFailDownload(event);
		this.updateDownloadTilesDialog();
	}

	public void onFatalError(IEvent event) {
		tileWaiter.onFatalError(event);
		this.updateDownloadTilesDialog();
	}

	public void onFinishDownload() {
		runOnUiThread(new Runnable() {
			public void run() {
				MapDownloader.this.enableGPS();
				isDownloadTilesFinished = true;
				MapDownloader.this.downloadTilesButton
						.setText(R.string.alert_dialog_text_ok);
				MapDownloader.this.reloadLayerAfterDownload();
			}
		});
	}

	public void onNewMessage(int ID, IEvent event) {
		switch (ID) {
		case IContext.DOWNLOAD_CANCELED:
			this.onDownloadCanceled();
			break;
		case IContext.FINISH_DOWNLOAD:
			this.onFinishDownload();
			break;
		case IContext.START_DOWNLOAD:
			this.onStartDownload();
			break;
		case IContext.TILE_DOWNLOADED:
			this.onTileDownloaded(event);
			break;
		case IContext.TOTAL_TILES_COUNT:
			this.onTotalNumTilesRetrieved(Integer.valueOf(event.getMessage()));
			break;
		}
	}

	private boolean isDownloadTilesFinished = false;

	public void onStartDownload() {
		this.disableGPS();
		isDownloadTilesFinished = false;
		this.previousTime = 0;
		tileWaiter.onStartDownload();
		runOnUiThread(new Runnable() {
			public void run() {
				MapDownloader.this.downloadTilesButton
						.setText(R.string.alert_dialog_cancel);
			}
		});
	}

	public void onTileDeleted(IEvent event) {
		tileWaiter.onTileDeleted(event);
	}

	public void onTileDownloaded(IEvent event) {
		tileWaiter.onTileDownloaded(event);
		this.updateDownloadTilesDialog();
	}

	public void onTileLoadedFromFileSystem(IEvent event) {
		tileWaiter.onTileLoadedFromFileSystem(event);
		this.updateDownloadTilesDialog();
	}

	public void onTileNotFound(IEvent event) {
		tileWaiter.onTileNotFound(event);
		this.updateDownloadTilesDialog();
	}

	public void onTileSkipped(IEvent event) {
		tileWaiter.onTileSkipped(event);
		this.updateDownloadTilesDialog();
	}

	public void onTotalNumTilesRetrieved(final long totalNumTiles) {
		runOnUiThread(new Runnable() {
			public void run() {
				MapDownloader.this.tileWaiter
						.onTotalNumTilesRetrieved(totalNumTiles);
				MapDownloader.this.totalTiles.setText(MapDownloader.this
						.getText(R.string.download_tiles_06)
						+ " "
						+ totalNumTiles);
				double totalMB = totalNumTiles * 10 / 1024;
				MapDownloader.this.totalMB.setText(MapDownloader.this
						.getText(R.string.download_tiles_07) + " " + totalMB);
			}
		});
	}

	public void resetCounter() {
		tileWaiter.resetCounter();
		this.updateDownloadTilesDialog();
	}

	public void updateDataTransfer(int size) {
		tileWaiter.updateDataTransfer(size);
	}

	class TileDownloadWaiterDelegate extends TileDownloadWaiter {

		public TileDownloadWaiterDelegate(IDownloadWaiter w) {
			super(w);
		}
	}

	@Override
	public AlertDialog getDownloadTilesDialog() {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(DialogInterface arg0, int arg1, KeyEvent arg2) {
				if (arg2.getKeyCode() == KeyEvent.KEYCODE_BACK) {
					Toast.makeText(MapDownloader.this,
							R.string.download_tiles_13, Toast.LENGTH_LONG)
							.show();
				}
				return true;
			}

		});

		builder.setIcon(R.drawable.layerdonwload);
		builder.setTitle(R.string.download_tiles_14);

		this.downloadTilesLayout = (LinearLayout) this.getLayoutInflater()
				.inflate(R.layout.download_tiles_pd, null);

		downloadTilesPB = (ProgressBar) this.downloadTilesLayout
				.findViewById(R.id.ProgressBar01);

		downloadTilesButton = (Button) this.downloadTilesLayout
				.findViewById(R.id.download_tiles_button);

		downloadTilesButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				MapDownloader.this.downloadCancellable.setCanceled(true);
				MapDownloader.this.downloadTileAlert.dismiss();
			}
		});

		builder.setView(this.downloadTilesLayout);

		return builder.create();
	}

	/**
	 * Show an AlertDialog to the user to input the query string for NameFinder
	 * addresses
	 */
	@Override
	public void showDownloadTilesDialog(final MapRenderer currentRenderer,
			final IMapView mapView) {
		try {
			log.debug("show address dialog");
			AlertDialog.Builder alert = new AlertDialog.Builder(this);

			final LinearLayout l = (LinearLayout) this.getLayoutInflater()
					.inflate(R.layout.download_tiles, null);
			this.totalMB = (TextView) l
					.findViewById(R.id.download_total_transfer_text);
			this.totalTiles = (TextView) l
					.findViewById(R.id.download_total_tiles_text);
			this.totalZoom = (TextView) l
					.findViewById(R.id.download_zoom_level_text);
			this.downTilesSeekBar = (SeekBar) l
					.findViewById(R.id.download_zoom_level_seekbar);
			this.downTilesSeekBar
					.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

						@Override
						public void onProgressChanged(SeekBar arg0,
								int progress, boolean arg2) {
							try {
								MapDownloader.this
										.instantiateTileDownloaderTask(l,
												progress, currentRenderer,
												mapView.getMapWidth(),
												mapView.getMapHeight());

							} catch (Exception e) {
								log.error("onProgressChanged", e);
							}
						}

						@Override
						public void onStartTrackingTouch(SeekBar seekBar) {
							// TODO Auto-generated method stub

						}

						@Override
						public void onStopTrackingTouch(SeekBar seekBar) {
							// TODO Auto-generated method stub

						}
					});
			alert.setIcon(R.drawable.layerdonwload);
			alert.setTitle(R.string.download_tiles_14);
			this.downTilesSeekBar.setProgress(50);
			((CheckBox) l.findViewById(R.id.download_tiles_overwrite_check))
					.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View arg0) {
							MapDownloader.this.instantiateTileDownloaderTask(l,
									MapDownloader.this.downTilesSeekBar
											.getProgress(), currentRenderer,
									mapView.getMapWidth(), mapView
											.getMapHeight());
						}
					});

			alert.setView(l);

			alert.setPositiveButton(R.string.download_tiles_14,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							try {
								MapDownloader.this.resetCounter();
								MapDownloader.this.downloadTileAlert = MapDownloader.this
										.getDownloadTilesDialog();
								MapDownloader.this.downloadTileAlert.show();
								WorkQueue.getExclusiveInstance().execute(t);
							} catch (Exception e) {
								log.error("clickNameFinderAddress: ", e);
							} finally {
								// setRequestedOrientation(ActivityInfo.
								// SCREEN_ORIENTATION_SENSOR);
							}
							return;
						}
					});

			alert.setNegativeButton(R.string.alert_dialog_text_cancel,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							// setRequestedOrientation(ActivityInfo.
							// SCREEN_ORIENTATION_SENSOR);
							mapView.resumeDraw();
							MapDownloader.this.reloadLayerAfterDownload();
						}
					});

			// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			alert.show();
		} catch (Exception e) {
			mapView.resumeDraw();
			log.error("", e);
		}
	}

	@Override
	public void instantiateTileDownloaderTask(LinearLayout l, int progress,
			MapRenderer currentRenderer, int mapWidth, int mapHeight) {
		try {
			final boolean updateTiles = ((CheckBox) l
					.findViewById(R.id.download_tiles_overwrite_check))
					.isChecked();

			MapRenderer renderer = Layers.getInstance().getRenderer(
					currentRenderer.getNAME());

			String tileName = "tile.gvsig";
			String dot = ".";
			String strategy = ITileFileSystemStrategy.QUADKEY;
			ITileFileSystemStrategy ts = FileSystemStrategyManager
					.getInstance().getStrategyByName(strategy);

			try {
				tileName = Settings.getInstance().getStringValue(
						getText(R.string.settings_key_tile_name).toString());
			} catch (Exception e) {

			}

			try {
				strategy = Settings.getInstance()
						.getStringValue(
								getText(R.string.settings_key_list_strategy)
										.toString());
			} catch (Exception e) {

			}

			String tileSuffix = dot + tileName;
			ts = FileSystemStrategyManager.getInstance().getStrategyByName(
					strategy);
			ts.setTileNameSuffix(tileSuffix);

			int fromZoomLevel = currentRenderer.getZoomLevel();

			int totalZoomLevels = currentRenderer.getZOOM_MAXLEVEL()
					- fromZoomLevel;

			int z = currentRenderer.getZOOM_MAXLEVEL()
					- currentRenderer.getZoomMinLevel();

			int toZoomLevel = (totalZoomLevels * progress / 100)
					+ fromZoomLevel;

			MapDownloader.this.totalZoom.setText(MapDownloader.this
					.getText(R.string.download_tiles_05)
					+ " "
					+ fromZoomLevel
					+ "/" + toZoomLevel);

			Extent extent = ViewPort
					.calculateExtent(currentRenderer.getCenter(),
							currentRenderer.resolutions[currentRenderer
									.getZoomLevel()], mapWidth, mapHeight);

			if (extent.area() < currentRenderer.getExtent().area())
				renderer.setExtent(extent);

			/*
			 * Once we set the extent, we have to update the center of the
			 * MapRenderer
			 */
			renderer.setCenter(renderer.getExtent().getCenter().getX(),
					renderer.getExtent().getCenter().getY());

			/*
			 * Get a new Cancellable instance (one different each time the task
			 * is executed)
			 */
			downloadCancellable = Utilities.getNewCancellable();

			/*
			 * Instantiate the download waiter. The implementation should
			 * properly update the UI as the task is processing tiles
			 */

			TileDownloadCallbackHandler callBackHandler = new TileDownloadCallbackHandler(
					MapDownloader.this);
			ConstantsTileCache.DEFAULT_NUM_THREADS = 4;

			/*
			 * Finally instantiate the TileDownloaderTask. Automatically
			 * launches to the IDownloadWaiter implementation the
			 * onTotalNumTilesRetrieved event (prior to execute the task)
			 * 
			 * This instance will donwload from 0 to 3 zoom level of the whole
			 * world extent of OSM (Mapnik) server. Not apply any ITileSorter
			 * nor ITileBufferIntersector and applies the FlatX file system
			 * strategy with a minimum buffer to fit the map size.
			 */
			int mode = TileProvider.MODE_ONLINE;
			if (updateTiles) {
				mode = TileProvider.MODE_UPDATE;
			}
			t = new TileDownloaderTask(CompatManager.getInstance()
					.getRegisteredContext(), renderer, fromZoomLevel,
					toZoomLevel, downloadCancellable, renderer.getExtent(),
					null, callBackHandler, null, mode, ts,
					new FitScreenBufferStrategy(), true);
		} catch (Exception e) {
			log.error("", e);
		}
	}
}
