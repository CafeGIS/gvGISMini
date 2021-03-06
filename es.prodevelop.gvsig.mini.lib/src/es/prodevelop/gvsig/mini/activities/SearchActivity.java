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

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Spinner;
import es.prodevelop.gvsig.mini.R;
import es.prodevelop.gvsig.mini.activities.adapter.AutoCompleteAdapter;
import es.prodevelop.gvsig.mini.activities.adapter.PinnedHeaderListAdapter;
import es.prodevelop.gvsig.mini.geom.impl.base.Point;
import es.prodevelop.gvsig.mini.search.SearchOptions;
import es.prodevelop.gvsig.mini.util.SpaceTokenizer;


/**
 * Base activity for local pois / streets searches
 * 
 * @author aromeu
 * 
 */
public abstract class SearchActivity extends ListActivity implements
		SearchActivityWrapper/* , ListView.OnScrollListener */{

	public String query;	
	private AutoCompleteAdapter autoCompleteAdapter;
	private ArrayList resultsList;
	private SearchOptions searchOptions = new SearchOptions(this);
	private MultiAutoCompleteTextView autoCompleteTextView;

	public DisplayMetrics metrics = new DisplayMetrics();
	public ListAdapter listAdapter;

	public Spinner spinnerSort;

	public final static String CATEGORY = "category";
	public final static String SUBCATEGORY = "subcategory";

	public final static int SEARCH_DIALOG = 1;
	public final static String HIDE_AUTOTEXTVIEW = "hide_tv";

	public int selectedSpinnerPosition = 0;
	protected ArrayAdapter spinnerArrayAdapter;
	private String text;
	private static boolean reenter = false;

	public final static String QUERY = "query";

	private boolean searchResultsMenuItemAlreadyAdded = false;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			query = getIntent().getStringExtra(QUERY);
			setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);
			getWindowManager().getDefaultDisplay().getMetrics(metrics);

			requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

			getListView().setTextFilterEnabled(true);

			LinearLayout l = ((LinearLayout) getLayoutInflater().inflate(
					R.layout.search_list, null));
			this.setContentView(l);

			autoCompleteTextView = (MultiAutoCompleteTextView) l
					.findViewById(R.id.EditText01);
			this.setAutoCompleteAdapter(new AutoCompleteAdapter(this));

			autoCompleteTextView.setTokenizer(new SpaceTokenizer());
			autoCompleteTextView.setAdapter(getAutoCompleteAdapter());
			autoCompleteTextView.setThreshold(1);

			spinnerSort = ((Spinner) l.findViewById(R.id.SpinnerSort));

			spinnerSort.setOnItemSelectedListener(new OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					try {
						selectedSpinnerPosition = spinnerSort
								.getSelectedItemPosition();
						getSearchOptions().sort = spinnerSort.getSelectedItem()
								.toString();
						// if (selectedSpinnerPosition != 0)
						onTextChanged(autoCompleteTextView.getText(), 0, 0, 0);
					} catch (Exception e) {
						Log.e("", e.getMessage());
					}
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
					// TODO Auto-generated method stub

				}

			});

			spinnerArrayAdapter = new ArrayAdapter(this,
					android.R.layout.simple_spinner_item, getResources()
							.getStringArray(R.array.sort_options));
			spinnerArrayAdapter
					.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			// spinnerSort.setSelection(-1);
			enableSpinner("");
			// getSearchOptions().sort =
			// spinnerSort.getSelectedItem().toString();

			// ((ImageButton) l.findViewById(R.id.search_opts))
			// .setOnClickListener(new OnClickListener() {
			//
			// @Override
			// public void onClick(View arg0) {
			// showDialog(SEARCH_DIALOG);
			// }
			// });

			searchOptions.setCenterMercator(getCenter());
			autoCompleteTextView.addTextChangedListener(this);

			final boolean hide = getIntent().getBooleanExtra(
					SearchActivity.HIDE_AUTOTEXTVIEW, false);
			if (hide)
				autoCompleteTextView.setVisibility(View.GONE);

			// ****************************//
			// mWindowManager = (WindowManager)
			// getSystemService(Context.WINDOW_SERVICE);
			//
			// getListView().setOnScrollListener(this);
			//
			// // LayoutInflater inflate = (LayoutInflater)
			// getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			//
			// mDialogText = new TextView(this);
			// mDialogText.setVisibility(View.INVISIBLE);
			//
			// mHandler.post(new Runnable() {
			//
			// public void run() {
			// mReady = true;
			// WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
			// LayoutParams.WRAP_CONTENT,
			// LayoutParams.WRAP_CONTENT,
			// WindowManager.LayoutParams.TYPE_APPLICATION,
			// WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
			// | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
			// PixelFormat.TRANSLUCENT);
			// mWindowManager.addView(mDialogText, lp);
			// }
			// });
		} catch (Exception e) {
			Log.e("", e.getMessage());
		} finally {
			reenter = true;
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		try {
			if (keyCode == KeyEvent.KEYCODE_SEARCH) {
				return true;
			}
			return super.onKeyDown(keyCode, event);
		} catch (Exception e) {
			return false;
		}
	}

	public abstract void initializeAdapters();	

	public AutoCompleteAdapter getAutoCompleteAdapter() {
		return autoCompleteAdapter;
	}

	public void setAutoCompleteAdapter(AutoCompleteAdapter adapter) {
		this.autoCompleteAdapter = adapter;
	}

	public ArrayList getResultsList() {
		return resultsList;
	}

	public void setResultsList(ArrayList resultsList) {
		this.resultsList = resultsList;
	}

	public SearchOptions getSearchOptions() {
		return searchOptions;
	}

	public void setSearchOptions(SearchOptions searchOptions) {
		this.searchOptions = searchOptions;
	}

	public MultiAutoCompleteTextView getAutoCompleteTextView() {
		return autoCompleteTextView;
	}

	public void setAutoCompleteTextView(
			MultiAutoCompleteTextView autoCompleteTextView) {
		this.autoCompleteTextView = autoCompleteTextView;
	}

	@Override
	public void afterTextChanged(Editable arg0) {
	}

	@Override
	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
			int arg3) {
	}

	public String processNewLine(CharSequence arg0) {
		String query = "";
		try {
			if (arg0 != null)
				query = arg0.toString();
			if (arg0 != null && arg0.toString().endsWith("\n")) {
				query.replaceAll("\n", " ");
				try {
					if (query != null && query.trim().length() <= 0) {
						getAutoCompleteTextView().setText("");
						query = "";
					} else {
						autoCompleteTextView.setText(arg0.subSequence(0,
								arg0.length() - 1));
						autoCompleteTextView.setSelection(autoCompleteTextView
								.length());
					}
				} catch (Exception ignore) {

				}
			}
		} catch (Exception e) {
			if (e != null && e.getMessage() != null)
				Log.e("", e.getMessage());
		}
		return query;
	}
	
	public void processQuery() {
		if (query != null && query.trim().length() > 0) {
			query = query.trim();
			getAutoCompleteTextView().setText(query + " ");
			getAutoCompleteTextView().setSelection(query.length() + 1);
		}
	}

	@Override
	public void onTextChanged(final CharSequence arg0, int arg1, int arg2,
			int arg3) {
		try {
			String query = processNewLine(arg0);			
			((Filterable) getListView().getAdapter()).getFilter().filter(
					query.toString().toLowerCase());
			getAutoCompleteAdapter().getFilter().filter(
					query.toString().toLowerCase());
			Log.d("onTextChanged", query.toString());
			this.setTitle(R.string.please_wait);
			setProgressBarIndeterminateVisibility(true);
			if (query.length() == 0)
				enableSpinner(query.toString());
		} catch (Exception e) {
			if (e != null && e.getMessage() != null)
				Log.e("", e.getMessage());
		}
	}

	public void enableSpinner() {
		this.enableSpinner(autoCompleteTextView.getText().toString());
	}

	protected void enableSpinner(String autoCompleteText) {
		// if (this.resultsList != null && this.resultsList.size() > 1) {
		if (autoCompleteText.length() == 0) {
			spinnerSort.setVisibility(View.GONE);
			searchOptions.sort = "no";
			text = autoCompleteText;
			// ArrayAdapter spinnerArrayAdapter = new ArrayAdapter(this,
			// android.R.layout.simple_spinner_item,
			// new String[] { getResources().getString(
			// R.string.sort_results) });
			// spinnerArrayAdapter
			// .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			// spinnerSort.setAdapter(spinnerArrayAdapter);
		} else {
			if (text.compareTo(autoCompleteText) != 0) {
				spinnerSort.setVisibility(View.VISIBLE);
				spinnerSort.setEnabled(true);
				spinnerSort.setAdapter(spinnerArrayAdapter);
				spinnerSort.setSelection(selectedSpinnerPosition);
				text = autoCompleteText;
			}
		}
		// } else {
		// spinnerSort.setVisibility(View.GONE);
		// searchOptions.sort = "no";
		// }

	}

	public Point getCenter() {
		Intent i = getIntent();
		if (i != null) {
			double lon = i.getDoubleExtra("lon", 0);
			double lat = i.getDoubleExtra("lat", 0);
			return new Point(lon, lat);
		} else {
			return new Point(0, 0);
		}
	}

	public abstract String getQuery();

	public abstract QuickActionContextListener getItemClickListener();

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		try {
			if (resultsList != null
					&& resultsList.size() > 1
					&& getAutoCompleteTextView() != null
					&& (getAutoCompleteTextView().getText().toString().trim()
							.length() > 0 || getAutoCompleteTextView()
							.getVisibility() != View.VISIBLE)) {
				if (!searchResultsMenuItemAlreadyAdded)
					menu.add(0, 1, 1, R.string.show_search_results);
				searchResultsMenuItemAlreadyAdded = true;
			} else {
				menu.removeItem(1);
				searchResultsMenuItemAlreadyAdded = false;
			}
		} catch (Exception ignore) {

		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {		
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		try {
			switch (item.getItemId()) {
			case 0:
//				InvokePOIIntents.launchListBookmarks(this, new double[] {
//						this.getCenter().getX(), this.getCenter().getY() });

				break;
			case 1:								
				finish();
				break;
			}
		} catch (Exception e) {
			Log.e("", e.getMessage());
		}
		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		reenter = false;
		super.onDestroy();
		try {
			// ((PerstOsmPOIClusterProvider)provider).getHelper().close();
			((PinnedHeaderListAdapter) listAdapter).onDestroy();
			listAdapter = null;
			setListAdapter(null);
			// if (query != null && query.length() > 0) {
			//
			// } else {
			// final ArrayList list = resultsList;
			// final int size = list.size();
			//
			// POI p;
			// for (int i = 0; i < size; i++) {
			// try {
			// p = ((POI) list.get(i));
			// if (p != null)
			// p.destroy();
			// } catch (Exception ignore) {
			//
			// }
			// }
			// resultsList = null;
			// ((PerstOsmPOIClusterProvider) POIProviderManager.getInstance()
			// .getPOIProvider()).getHelper().rollback();
			// }
		} catch (Exception e) {
			Log.e("", e.getMessage() != null ? e.getMessage() : "Fail");
		} finally {
			// finish();
		}
	}

	// private final class RemoveWindow implements Runnable {
	// public void run() {
	// removeWindow();
	// }
	// }
	//
	// private RemoveWindow mRemoveWindow = new RemoveWindow();
	// Handler mHandler = new Handler();
	// private WindowManager mWindowManager;
	// private TextView mDialogText;
	// private boolean mShowing;
	// private boolean mReady;
	// private char mPrevLetter = Character.MIN_VALUE;
	//
	// @Override
	// protected void onResume() {
	// super.onResume();
	// mReady = true;
	// }
	//
	// @Override
	// protected void onPause() {
	// super.onPause();
	// removeWindow();
	// mReady = false;
	// }
	//
	// @Override
	// protected void onDestroy() {
	// super.onDestroy();
	// mWindowManager.removeView(mDialogText);
	// mReady = false;
	// }
	//
	// public void onScroll(AbsListView view, int firstVisibleItem,
	// int visibleItemCount, int totalItemCount) {
	// int lastItem = firstVisibleItem + visibleItemCount - 1;
	// if (mReady) {
	// char firstLetter = ((POI) getListAdapter()
	// .getItem(firstVisibleItem)).getDescription().charAt(0);
	//
	// if (!mShowing && firstLetter != mPrevLetter) {
	//
	// mShowing = true;
	// mDialogText.setVisibility(View.VISIBLE);
	//
	// }
	// mDialogText.setText(((Character) firstLetter).toString());
	// mHandler.removeCallbacks(mRemoveWindow);
	// mHandler.postDelayed(mRemoveWindow, 3000);
	// mPrevLetter = firstLetter;
	// }
	// }
	//
	// public void onScrollStateChanged(AbsListView view, int scrollState) {
	// }
	//
	// private void removeWindow() {
	// if (mShowing) {
	// mShowing = false;
	// mDialogText.setVisibility(View.INVISIBLE);
	// }
	// }
}
