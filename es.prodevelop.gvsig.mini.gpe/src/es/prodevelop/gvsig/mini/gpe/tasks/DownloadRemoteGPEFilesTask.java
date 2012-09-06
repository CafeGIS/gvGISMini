package es.prodevelop.gvsig.mini.gpe.tasks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import es.prodevelop.android.common.download.Downloader;
import es.prodevelop.gvsig.mini.activities.ActivityBundlesManager;
import es.prodevelop.gvsig.mini.gpe.R;
import es.prodevelop.gvsig.mini.gpe.driver.DriverGPEFactory;
import es.prodevelop.gvsig.mini.gpe.driver.VectorGPEDriver;
import es.prodevelop.gvsig.mini.util.Utils;

public class DownloadRemoteGPEFilesTask extends AsyncTask<String, String, Long> {

	private ProgressDialog progress;
	private Activity context;
	private Handler handler;

	public DownloadRemoteGPEFilesTask(Activity context, Handler handler) {
		this.context = context;
		this.handler = handler;
	}

	protected Long doInBackground(String... urls) {
		int count = urls.length;
		long totalSize = 0;
		for (int i = 0; i < count; i++) {
			Downloader d = new Downloader();
			try {
				publishProgress(urls[i]);
				d.downloadFromUrl(urls[i], null);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				Message msg = new Message();
				msg.what = 1;
				msg.obj = e1.getMessage();
				this.handler.sendMessage(msg);
			}

			if (d.getData() != null && d.getData().length > 0) {
				File f = new File(Environment.getExternalStorageDirectory()
						.getPath()
						+ File.separator
						+ Utils.APP_DIR
						+ File.separator + "layer.kml");
				try {
					f.createNewFile();
					FileOutputStream fileout = new FileOutputStream(f);
					fileout.write(d.getData());
					fileout.close();
				} catch (IOException e) {
					Message msg = new Message();
					msg.what = 1;
					msg.obj = e.getMessage();
					this.handler.sendMessage(msg);
				}
			}
			File file = new File(Environment.getExternalStorageDirectory()
					.getPath()
					+ File.separator
					+ Utils.APP_DIR
					+ File.separator + "layer.kml");

			VectorGPEDriver driver = DriverGPEFactory.createVectorialDriver(
					ActivityBundlesManager.getInstance().getMapView(), context);
			try {
				driver.open(file);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Message msg = new Message();
				msg.what = 1;
				msg.obj = e.getMessage();
				this.handler.sendMessage(msg);
			}
			driver.initialize();

			if (driver.getErrorHandler().getErrorsSize() > 0) {
				Message msg = new Message();
				msg.what = 1;
				StringBuffer errores = new StringBuffer();
				for (int p = 0; p < driver.getErrorHandler().getErrorsSize(); p++) {
					errores.append(driver.getErrorHandler().getErrorAt(0));
				}
				msg.obj = errores.toString();
				this.handler.sendMessage(msg);
			}

			Message msg = new Message();
			msg.what = 0;
			this.handler.sendMessage(msg);
		}
		return totalSize;
	}

	protected void onProgressUpdate(String... progress) {
		this.progress.setMessage(progress[0]);
	}

	protected void onPostExecute(Long result) {
		if (this.progress != null) {
			this.progress.dismiss();
		}
	}

	protected void onPreExecute() {
		this.progress = ProgressDialog.show(context,
				context.getText(R.string.please_wait), "");
	}
}