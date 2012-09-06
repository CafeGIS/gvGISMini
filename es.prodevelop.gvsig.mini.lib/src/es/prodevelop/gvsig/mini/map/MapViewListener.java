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
 *   author Fernando González fernando.gonzalez@geomati.co 
 */

package es.prodevelop.gvsig.mini.map;

import es.prodevelop.gvsig.mini.context.ItemContext;

/**
 * Listener of {@link MapView} events
 * 
 * @author fergonco
 */
public interface MapViewListener {

	/**
	 * Called when the zoom level has changed in the MapView
	 */
	void zoomLevelChanged();

	/**
	 * Called when the specified overlay context should be shown
	 * 
	 * @param itemContext
	 * @param x
	 *            ordinate of the point where the ItemContext should be shown
	 * @param y
	 *            ordinate of the point where the ItemContext should be shown
	 */
	void overlayContextToShow(ItemContext itemContext, double x, double y);

	/**
	 * Called when the default context should be shown
	 * 
	 * @param x
	 *            ordinate of the point where the ItemContext should be shown
	 * @param y
	 *            ordinate of the point where the ItemContext should be shown
	 */
	void defaultContextToShow(double x, double y);

	/**
	 * Called when the excluive control has changed
	 */
	void exclusiveControlChanged();

}
