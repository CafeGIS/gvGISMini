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

import android.content.DialogInterface.OnCancelListener;
import android.view.LayoutInflater;
import android.view.MenuItem;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.AbstractAction;

public interface IBaseActivity {

	/**
	 * Shows a loading dialog
	 */
	public void showIndeterminateDialog(int title, int message, int icon,
			OnCancelListener cancelListener);

	/**
	 * Hides the loading dialog
	 */
	public void hideIndeterminateDialog();

	/**
	 * Shows a dialog with an OK button
	 * 
	 * @param textBody
	 *            The text of the dialog
	 * @param title
	 *            The title
	 * @param editView
	 *            If the text is editable
	 */
	public void showOKDialog(String textBody, int title, boolean editView);

	/**
	 * Shows a Toast with the text defined by the resource ID passed as a
	 * parameter
	 * 
	 * @param resId
	 *            The id of a text resource
	 */
	public void showToast(int resId);

	/**
	 * Shows an AlertDialog with the content of the assetsFile, that is the name
	 * of a file in the assets folder
	 * 
	 * @param assetsFile
	 *            The file name in the assets folder
	 * @param id
	 *            The title of the AlertDialog
	 */
	public void showDialogFromFile(String assetsFile, int id);

	/**
	 * Enables or disables a MenuItem
	 * 
	 * @param item
	 *            The menuItem
	 * @param enable
	 */
	public void enableMenuItem(MenuItem item, boolean enable);

	public ActionBar getActionbar();

	public void setActionbar(ActionBar actionbar);

	/**
	 * Adds a button to the top action bar
	 * 
	 * @param action
	 */
	public void addActionToActionbar(AbstractAction action);
	
	public void addActionBar(LayoutInflater inflater);

}
