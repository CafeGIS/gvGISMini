/* gvSIG Mini. A free mobile phone viewer of free maps.
 *
 * Copyright (C) 2010 Prodevelop.
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
 */

package es.prodevelop.gvsig.mini.activities;

import java.util.logging.Level;
import java.util.logging.Logger;

import android.content.Context;
import es.prodevelop.gvsig.mini.R;
import es.prodevelop.tilecache.renderer.MapRenderer;
import es.prodevelop.tilecache.renderer.wms.OSRenderer;

public class OSSettingsUpdater {

	private final static Logger log = Logger.getLogger(OSSettingsUpdater.class
			.getName());

	public static void synchronizeRendererWithSettings(OSRenderer osr,
			Context context) {
		try {
			if (osr.getType() != MapRenderer.OS_RENDERER)
				return;
			boolean isCustomEnabled = false;
			try {
				isCustomEnabled = Settings.getInstance().getBooleanValue(
						context.getText(R.string.settings_key_os_custom)
								.toString());
			} catch (NoSuchFieldError ignore) {

			}

			if (isCustomEnabled) {
				osr.setKey(Settings.getInstance().getStringValue(
						context.getText(R.string.settings_key_os_key)
								.toString()));
				osr.setKeyURL(Settings.getInstance().getStringValue(
						context.getText(R.string.settings_key_os_url)
								.toString()));
			} else {
				osr.setDefaultKeysAndURLs();
			}
			// Layers.getInstance().persist();
		} catch (Exception e) {
			log.log(Level.SEVERE, "", e);
		}
	}
}
