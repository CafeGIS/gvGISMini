package es.prodevelop.gvsig.mini.gpe.activity;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Message;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import es.prodevelop.gvsig.mini.activities.ActivityBundlesManager;
import es.prodevelop.gvsig.mini.activities.TOCActivity;
import es.prodevelop.gvsig.mini.gpe.R;
import es.prodevelop.gvsig.mini.gpe.driver.DriverGPEFactory;
import es.prodevelop.gvsig.mini.gpe.driver.VectorGPEDriver;
import es.prodevelop.gvsig.mini.gpe.tasks.DownloadRemoteGPEFilesTask;

@TargetApi(3)
public class GPETOCActivity extends TOCActivity {

	private String[] mFileList;
	private File mPath = new File(Environment.getExternalStorageDirectory(), "");
	private String mChosenFile;
	private static final String[] FTYPE = new String[] { "gpx", "kml", "gml" };
	private static final int DIALOG_LOAD_FILE = 1000;
	private static final int DIALOG_REMOTE_FILE = 1001;

	@Override
	public boolean onCreateOptionsMenu(Menu pMenu) {
		super.onCreateOptionsMenu(pMenu);

		try {
			pMenu.add(0, 100, 100, "Load GPX, KML from sdcard");
			pMenu.add(0, 101, 101, "Load remote GPX, KML");
		} catch (Exception e) {
			Log.e("", "", e);
		}

		return true;
	}

	public void showLoadVectorFile() {
		loadFileList();
		showDialog(DIALOG_LOAD_FILE);
	}

	public void showLoadRemoteVectorFile() {
		showDialog(DIALOG_REMOTE_FILE);
	}

	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		boolean result = true;
		try {
			result = super.onMenuItemSelected(featureId, item);
			if (result) {
				return true;
			}
			switch (item.getItemId()) {
			case 100:
				showLoadVectorFile();
				result = true;
				break;
			case 101:
				showLoadRemoteVectorFile();
				result = true;
				break;
			}
		} catch (Exception e) {
			Log.e("", "", e);
		}

		return result;
	}

	private void loadFileList() {
		try {
			mPath.mkdirs();
		} catch (SecurityException e) {
			Log.e("", "unable to write on the sd card " + e.toString());
		}
		if (mPath.exists()) {
			FilenameFilter filter = new FilenameFilter() {
				public boolean accept(File dir, String filename) {
					File sel = new File(dir, filename);
					boolean res = false;
					for (int i = 0; i < FTYPE.length; i++) {
						res |= filename.contains(FTYPE[i]);
					}
					return res;
				}
			};
			mFileList = mPath.list(filter);
		} else {
			mFileList = new String[0];
		}
	}

	@Override
	public Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		AlertDialog.Builder builder = new Builder(this);

		switch (id) {
//		case POI_PROXY_DIALOG:
//			builder.setTitle("Choose your file");
//			if (poiProxyServices == null) {
//				Log.e("", "Showing file picker before loading the file list");
//				dialog = builder.create();
//				return dialog;
//			}
//			builder.setItems(poiProxyServices,
//					new DialogInterface.OnClickListener() {
//						public void onClick(DialogInterface dialog, int which) {
//							POIProxyOverlay overlay = new POIProxyOverlay(
//									GPETOCActivity.this, ActivityBundlesManager
//											.getInstance().getMapView(),
//									poiProxyServices[which]);
//							ActivityBundlesManager.getInstance().getMapView()
//									.addOverlay(overlay);
//							Message msg = new Message();
//							msg.what = 0;
//							GPETOCActivity.this.handler.sendMessage(msg);
//						}
//					});
//			break;
		case DIALOG_LOAD_FILE:
			builder.setTitle("Choose your file");
			if (mFileList == null) {
				Log.e("", "Showing file picker before loading the file list");
				dialog = builder.create();
				return dialog;
			}
			builder.setItems(mFileList, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					mChosenFile = mFileList[which];
					new LoadGPEFilesTask()
							.execute(new String[] { mChosenFile });
				}
			});
			break;
		case DIALOG_REMOTE_FILE:
			builder.setIcon(R.drawable.menu02);
			builder.setTitle(R.string.type_url);
			final EditText input = new EditText(this);
			input.setText("http://www.comunitatvalenciana.com/geo/single/spa/playas/layer.kml");

			builder.setView(input);

			builder.setPositiveButton(R.string.load_layer,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							try {
								Editable value = input.getText();
								new DownloadRemoteGPEFilesTask(
										GPETOCActivity.this,
										GPETOCActivity.this.handler)
										.execute(value.toString());
							} catch (Exception e) {
								Log.e("GPETOCActivity", "Remote GPE file", e);
							}
						}
					});

			builder.setNegativeButton(R.string.cancel,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
						}
					});
			break;
		default:
			dialog = super.onCreateDialog(id);
			break;
		}

		dialog = builder.show();

		return dialog;
	}

	private class LoadGPEFilesTask extends AsyncTask<String, Integer, Long> {

		private ProgressDialog progress;

		protected Long doInBackground(String... files) {
			long totalSize = 0;
			try {
				int count = files.length;
				for (int i = 0; i < count; i++) {
					File file = new File(mPath.getPath() + File.separator
							+ files[i]);

					VectorGPEDriver driver = DriverGPEFactory
							.createVectorialDriver(ActivityBundlesManager
									.getInstance().getMapView(),
									GPETOCActivity.this);
					try {
						driver.open(file);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						Message msg = new Message();
						msg.what = 1;
						msg.obj = e.getMessage();
						GPETOCActivity.this.handler.sendMessage(msg);
					}
					driver.initialize();

					if (driver.getErrorHandler().getErrorsSize() > 0) {
						Message msg = new Message();
						msg.what = 1;
						StringBuffer errores = new StringBuffer();
						for (int p = 0; p < driver.getErrorHandler()
								.getErrorsSize(); p++) {
							errores.append(driver.getErrorHandler().getErrorAt(
									p));
						}
						msg.obj = errores.toString();
						GPETOCActivity.this.handler.sendMessage(msg);
					}
				}
			} catch (Exception e) {
				Log.e("GPETOCACtivity", "Error while loading local gpe file", e);
			}

			return totalSize;
		}

		protected void onPostExecute(Long result) {
			Message msg = new Message();
			msg.what = 0;
			GPETOCActivity.this.handler.sendMessage(msg);
			if (progress != null) {
				progress.dismiss();
			}
		}

		protected void onPreExecute() {
			this.progress = ProgressDialog.show(GPETOCActivity.this,
					GPETOCActivity.this.getText(R.string.please_wait), "");
		}
	}

	@Override
	public void onAddLayer(int type) {
		switch (type) {
		case ADD_VECTOR_LAYER:
			this.showLoadVectorFile();
			break;
		case ADD_REMOTE_VECTOR_LAYER:
			this.showLoadRemoteVectorFile();
			break;
		default:
			super.onAddLayer(type);
			break;
		}
	}

}
