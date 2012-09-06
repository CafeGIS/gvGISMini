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

package es.prodevelop.gvsig.mini.activities.feature;

import java.util.ArrayList;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Filterable;
import es.prodevelop.gvsig.mini.R;
import es.prodevelop.gvsig.mini.activities.ActivityBundlesManager;
import es.prodevelop.gvsig.mini.activities.FeatureQuickActionListener;
import es.prodevelop.gvsig.mini.activities.QuickActionContextListener;
import es.prodevelop.gvsig.mini.activities.QuickActionEvent;
import es.prodevelop.gvsig.mini.activities.QuickActionObserver;
import es.prodevelop.gvsig.mini.activities.SearchActivity;
import es.prodevelop.gvsig.mini.activities.adapter.FeatureAdapter;
import es.prodevelop.gvsig.mini.activities.adapter.PinnedHeaderListAdapter;
import es.prodevelop.gvsig.mini.geom.impl.jts.JTSFeature;
import es.prodevelop.gvsig.mini.views.PinnedHeaderListView;

public class ResultSearchActivity extends SearchActivity implements
		QuickActionObserver {

	protected QuickActionContextListener listener;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		try {
			query = getIntent().getStringExtra(QUERY);

			getListView().setOnItemClickListener(
					new OpenDetailsItemClickListener(this, this.getCenter()));

			getListView().setOnItemLongClickListener(
					new OnItemLongClickListener() {

						@Override
						public boolean onItemLongClick(AdapterView<?> arg0,
								View arg1, int arg2, long arg3) {
							return getItemClickListener()
									.onClick(
											arg2,
											(JTSFeature) getListAdapter()
													.getItem(arg2), arg1);
						}
					});

			this.getSearchOptions().clearCategories();
			this.getSearchOptions().clearSubcategories();

			initializeAdapters();
			this.attachSectionedAdapter();

			spinnerSort.setOnItemSelectedListener(new OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					try {
						if (spinnerSort.getSelectedItemPosition() == selectedSpinnerPosition)
							return;

						selectedSpinnerPosition = spinnerSort
								.getSelectedItemPosition();
						getSearchOptions().sort = spinnerSort.getSelectedItem()
								.toString();
						// if (selectedSpinnerPosition != 0)
						onTextChanged(query, 0, 0, 0);
					} catch (Exception e) {
						Log.e("", e.getMessage());
					}
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
					// TODO Auto-generated method stub

				}

			});

			populate();
			enableSpinner("");
			onTextChanged(query, 0, 0, 0);
		} catch (Exception e) {
			Log.e("", e.getMessage());
		}
	}

	/**
	 * Initializes the initial result list of the Activity
	 */
	public void populate() {
		this.setResultsList((ArrayList) ActivityBundlesManager.getInstance()
				.getFeatures());
	}

	public void attachSectionedAdapter() {
		this.getListView().setAdapter(listAdapter);
		// ((PinnedHeaderListAdapter) listAdapter).updateIndexer();
		getListView().setOnScrollListener(
				((PinnedHeaderListAdapter) listAdapter));
		this.setListAdapter(listAdapter);
	}

	@Override
	protected void enableSpinner(String autoCompleteText) {
		View pinnedHeader = getLayoutInflater().inflate(R.layout.list_header,
				getListView(), false);
		((PinnedHeaderListView) getListView()).setPinnedHeaderView(pinnedHeader
				.findViewById(R.id.section_text));
		spinnerSort.setVisibility(View.VISIBLE);
		spinnerSort.setEnabled(true);
		spinnerSort.setAdapter(spinnerArrayAdapter);
		if (spinnerSort.getSelectedItemPosition() != selectedSpinnerPosition)
			spinnerSort.setSelection(selectedSpinnerPosition);
	}

	@Override
	public void initializeAdapters() {
		listAdapter = new FeatureAdapter(this);
		this.getListView().setAdapter(this.listAdapter);
		this.setListAdapter(this.listAdapter);
	}

	@Override
	public void onTextChanged(final CharSequence arg0, int arg1, int arg2,
			int arg3) {
		try {
			String query = processNewLine(arg0);
			// if (query.length() > 0) {
			((Filterable) getListView().getAdapter()).getFilter().filter(
					query.toString());
			Log.d("onTextChanged", query.toString());
			this.setTitle(R.string.please_wait);
			setProgressBarIndeterminateVisibility(true);
			// }
			// if (arg0.length() == 0)
			// enableSpinner(arg0.toString());
		} catch (Exception e) {
			if (e != null && e.getMessage() != null)
				Log.e("", e.getMessage());
		}
	}

	@Override
	public QuickActionContextListener getItemClickListener() {
		if (listener == null) {
			listener = new FeatureQuickActionListener(this, R.drawable.pois,
					R.string.NameFinderActivity_0);
			listener.addObserver(this);
		}
		return listener;
	}

	@Override
	public String getQuery() {
		return query;
	}

	@Override
	public void onQuickActionExecuted(QuickActionEvent event) {
		if (event == null) {
			return;
		}

		switch (event.getType()) {
		case QuickActionEvent.TYPE_REMOVE_FEATURE:
			this.populate();
			BaseAdapter a = ((BaseAdapter) this.listAdapter);
			a.notifyDataSetChanged();
			break;
		case QuickActionEvent.TYPE_ZOOM_FEATURE:
			this.finish();
			break;
		}
	}
}
