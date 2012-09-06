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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ListView;
import android.widget.Toast;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.AbstractAction;

import es.prodevelop.gvsig.mini.R;
import es.prodevelop.gvsig.mini.map.IBaseActivity;
import es.prodevelop.gvsig.mini.views.LongTextAdapter;

public class AboutActivity extends LogFeedbackActivity implements IBaseActivity {

	private final static Logger log = LoggerFactory
			.getLogger(AboutActivity.class);

	private ActionBar actionBar;
	private ProgressDialog indeterminateDialog;

	public void onCreate(Bundle savedInstanceState) {
		try {
			super.onCreate(savedInstanceState);
		} catch (Exception e) {
			log.error("onCreate AboutActivity", e);
		}
	}

	public void addActionBar(LayoutInflater inflater) {
		getWindow().addContentView(
				inflater.inflate(R.layout.actionbars, null),
				new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT));
		actionBar = (ActionBar) findViewById(R.id.actionbar);
	}

	public void showDialogFromFile(String assetsFile, int id) {
		try {
			String license = "";
			// String aFile = "about.txt";
			InputStream is = this.getAssets().open(assetsFile);
			try {
				// Resources resources = getPackageManager()
				// .getResourcesForApplication(packagename);

				// Read in the license file as a big String
				BufferedReader input = new BufferedReader(
						new InputStreamReader(is));
				// BufferedReader in
				// = new BufferedReader(new InputStreamReader(
				// resources.openRawResource(resourceid)));
				String line;
				StringBuilder sb = new StringBuilder();
				try {
					while ((line = input.readLine()) != null) { // Read line per
						// line.
						if (TextUtils.isEmpty(line)) {
							// Empty line: Leave line break
							sb.append("\n\n");
						} else {
							sb.append(line);
							sb.append(" ");
						}
					}
					license = sb.toString();
				} catch (IOException e) {
					// Should not happen.
					e.printStackTrace();
				}

			} catch (Exception e) {
				log.error("showDialogFromFile", e);
			}

			AlertDialog.Builder alert = new AlertDialog.Builder(this);
			alert.setTitle(id);
			ListView l = new ListView(this);
			l.setAdapter(new LongTextAdapter(this, license, false));
			l.setClickable(false);
			l.setLongClickable(false);
			l.setFocusable(false);
			alert.setView(l);

			alert.setPositiveButton(R.string.ok,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
						}
					});

			alert.show();
		} catch (Exception e) {
			log.error("showDialogFromFile", e);
		}
	}

	public void showAboutDialog() {
		try {
			this.showDialogFromFile("about.txt", R.string.Map_28);
		} catch (Exception e) {
			log.error("showAboutDialog", e);
		}
	}

	public void showLicense() {
		try {
			this.showDialogFromFile("license.txt", R.string.Map_29);
		} catch (Exception e) {
			log.error("showLicense", e);
		}
	}

	public void showWhatsNew() {
		try {
			this.showDialogFromFile("whatsnew.txt", R.string.Map_30);
		} catch (Exception e) {
			log.error("showWhatsnew", e);
		}
	}

	@Override
	public void showIndeterminateDialog(int title, int message, int icon,
			OnCancelListener cancelListener) {
		indeterminateDialog = ProgressDialog
				.show(this, this.getResources().getString(title), this
						.getResources().getString(message), true);
		indeterminateDialog.setCancelable(true);
		indeterminateDialog.setCanceledOnTouchOutside(true);
		indeterminateDialog.setIcon(icon);
		indeterminateDialog.setOnCancelListener(cancelListener);
	}

	@Override
	public void hideIndeterminateDialog() {
		if (indeterminateDialog != null)
			indeterminateDialog.dismiss();
	}

	@Override
	public void showOKDialog(String textBody, int title, boolean editView) {
		try {
			AlertDialog.Builder alert = new AlertDialog.Builder(this);

			if (textBody.length() > 1000) {
				Toast.makeText(this, R.string.Map_25, Toast.LENGTH_LONG).show();
				return;
			}

			if (textBody.contains("<html")) {
				try {
					WebView wv = new WebView(this);
					String html = textBody.substring(textBody.indexOf("<html"),
							textBody.indexOf("html>") + 5);

					wv.loadData(html, "text/html", "UTF-8");
					alert.setView(wv);
				} catch (Exception e) {
					log.error("showOKDialog", e);
					ListView l = new ListView(this);
					l.setAdapter(new LongTextAdapter(this, textBody, editView));
					l.setClickable(false);
					l.setLongClickable(false);
					l.setFocusable(false);
					alert.setView(l);
				} catch (OutOfMemoryError oe) {
					onLowMemory();
					log.error("showOKDialog", oe);
					showToast(R.string.MapLocation_3);
				}

			} else {
				ListView l = new ListView(this);
				l.setAdapter(new LongTextAdapter(this, textBody, editView));
				l.setClickable(false);
				l.setLongClickable(false);
				l.setFocusable(false);
				alert.setView(l);
			}

			alert.setTitle(title);

			alert.setPositiveButton(R.string.ok,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							try {

							} catch (Exception e) {
								log.error("showOkDialog click ok", e);
							}
						}
					});

			alert.show();
		} catch (Exception e) {
			log.error("showOKDialog", e);
		} catch (OutOfMemoryError oe) {
			onLowMemory();
			log.error("showOKDialog", oe);
			showToast(R.string.MapLocation_3);
		}
	}

	@Override
	public void showToast(int resId) {
		Toast t = Toast.makeText(this, this.getText(resId), Toast.LENGTH_LONG);
		t.show();
	}

	@Override
	public void enableMenuItem(MenuItem item, boolean enable) {
		if (item != null)
			item.setEnabled(enable);
	}

	@Override
	public ActionBar getActionbar() {
		return this.actionBar;
	}

	@Override
	public void setActionbar(ActionBar actionbar) {
		this.actionBar = actionbar;
	}

	@Override
	public void addActionToActionbar(AbstractAction action) {
		actionBar.addAction(action);
	}

}
