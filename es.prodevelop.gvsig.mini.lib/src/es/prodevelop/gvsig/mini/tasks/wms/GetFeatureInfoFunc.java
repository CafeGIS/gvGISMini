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
 *   gvSIG Mini has been partially funded by IMPIVA (Instituto de la Peque�a y
 *   Mediana Empresa de la Comunidad Valenciana) &
 *   European Union FEDER funds.
 *   
 *   2011.
 *   author Alberto Romeu aromeu@prodevelop.es
 */

package es.prodevelop.gvsig.mini.tasks.wms;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gvsig.remoteclient.wms.WMSStatus;

import android.app.Activity;
import android.os.Message;
import es.prodevelop.gvsig.mini.R;
import es.prodevelop.gvsig.mini.common.CompatManager;
import es.prodevelop.gvsig.mini.exceptions.BaseException;
import es.prodevelop.gvsig.mini.map.IMapActivity;
import es.prodevelop.gvsig.mini.tasks.Functionality;
import es.prodevelop.gvsig.mini.tasks.TaskHandler;
import es.prodevelop.gvsig.mini.tasks.poi.InvokeIntents;
import es.prodevelop.gvsig.mini.util.Utils;
import es.prodevelop.gvsig.mini.wms.FMapWMSDriver;
import es.prodevelop.gvsig.mini.wms.FMapWMSDriverFactory;
import es.prodevelop.gvsig.mini.wms.WMSCancellable;
import es.prodevelop.tilecache.renderer.MapRenderer;
import es.prodevelop.tilecache.renderer.wms.WMSRenderer;

public class GetFeatureInfoFunc extends Functionality {

	private int res;
	private Logger logger = Logger
			.getLogger(GetFeatureInfoFunc.class.getName());

	public GetFeatureInfoFunc(IMapActivity map, int id) {
		super(map, id);
		try {
			CompatManager.getInstance().getRegisteredLogHandler()
					.configureLogger(logger);
		} catch (BaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public boolean execute() {
		try {
			if (!Utils.isSDMounted()) {
				Message m = Message.obtain();
				m.what = IMapActivity.SHOW_TOAST;
				m.obj = ((Activity) getMap())
						.getText(R.string.LayersActivity_1);
				getMap().getMapHandler().sendMessage(m);
				return true;
			}
			getMap().getMapHandler().sendEmptyMessage(
					IMapActivity.GETFEATURE_INITED);
			final MapRenderer mapRenderer = getMap().getMapView()
					.getMRendererInfo();
			if (mapRenderer.getType() == MapRenderer.WMS_RENDERER) {

				final WMSRenderer wmsRenderer = (WMSRenderer) mapRenderer;
				FMapWMSDriver driver = FMapWMSDriverFactory
						.getFMapDriverForURL(new URL(wmsRenderer.getBASEURL()));
				boolean hasConnected = driver.connect(new WMSCancellable());
				if (!hasConnected) {
					getMap().showToast(R.string.error_connecting_server);
					res = TaskHandler.FINISHED;
					return true;
				}
				if (driver.isQueryable()) {
					final WMSStatus status = wmsRenderer.buildWMSStatus();
					status.setWidth(getMap().getMapView().getMapWidth());
					status.setHeight(getMap().getMapView().getMapHeight());
					status.setExtent(getMap()
							.getMapView()
							.getViewPort()
							.calculateExtent(
									getMap().getMapView().getMapWidth(),
									getMap().getMapView().getMapHeight(),
									getMap().getMapView().getMRendererInfo()
											.getCenter()));

					URL url = driver.getClient().getFeatureInfoURL(status,
							getMap().getMapView().getMapWidth() / 2,
							getMap().getMapView().getMapHeight() / 2, 10,
							new WMSCancellable());

					if (url == null)
						getMap().showToast(R.string.error_connecting_server);
					else {

						if (status.getInfoFormat().contains("html")) {
							InvokeIntents.openBrowser((Activity) getMap(),
									url.toString());
							Message m = Message.obtain();
							m.what = IMapActivity.VOID;
							getMap().getMapHandler().sendMessage(m);
						} else {
							String info = driver.getFeatureInfo(status,
									getMap().getMapView().getMapWidth() / 2,
									getMap().getMapView().getMapHeight() / 2,
									10, new WMSCancellable());
							logger.log(Level.FINE, "GetFeatureInfo: " + info);
							Message m = Message.obtain();
							m.what = IMapActivity.SHOW_OK_DIALOG;
							m.obj = info;
							getMap().getMapHandler().sendMessage(m);
						}
					}
				} else {
					showToastError();
				}
			} else {
				showToastError();
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "", e);
			res = TaskHandler.ERROR;
			super.stop();
		}
		return true;
	}

	private void showToastError() {
		Message m = Message.obtain();
		m.what = IMapActivity.SHOW_TOAST;
		m.obj = ((Activity) getMap()).getResources().getString(
				R.string.GetFeatureInfoFunc_0);
		getMap().getMapHandler().sendMessage(m);
		res = TaskHandler.FINISHED;
	}

	@Override
	public int getMessage() {
		return res;
	}
}
