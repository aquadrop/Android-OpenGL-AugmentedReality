package com.example.ece1778;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
import com.android.ece1778.R;

public class LoadTask extends AsyncTask<Void, Void, Boolean> {
	String TAG = "info";
	/** MAM part **/
	private List<float[]> vertex_cor = new ArrayList<float[]>();
	private List<float[]> texture = new ArrayList<float[]>();
	private List<float[]> normal = new ArrayList<float[]>();
	private List<short[]> vertex_index = new ArrayList<short[]>();

	private int n_tri = 0;
	/***** MAM part ******/

	private VertexPackage mVertexPackage;
	ProgressDialog progressBar;
	Context context = null;
	String filename = null;
	boolean urlmode = true;

	public LoadTask(Context context) {
		this.context = context;
		progressBar = new ProgressDialog(context);
		progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	}

	public LoadTask(String filename, Context context) {
		urlmode = false;
		this.filename = filename;
		this.context = context;
		// LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(300,
		// 10);
		progressBar = new ProgressDialog(context);

		progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);

	}

	@Override
	protected void onPreExecute() {
		this.progressBar.setMessage("Loading and Calculating");
		this.progressBar.show();
	}

	@Override
	protected Boolean doInBackground(Void... NULL) {
		Boolean success = false;
		try {
			mVertexPackage = new VertexPackage();
			URL url = null;
			InputStream in = null;
			InputStreamReader isr = null;
			BufferedReader br = null;
			if (urlmode) {
				url = new URL(Main.urlstring);
				in = url.openStream();
				isr = new InputStreamReader(in);
				br = new BufferedReader(isr);
				success = mVertexPackage.parse_Data(br);
			} else {
				success = mVertexPackage.parse_Data(filename);
			}

			
		} catch (IOException e) {
			return false;
		}
		Log.w(TAG, "load" +" "+ filename);
		Log.w(TAG, "Load Complete");
		return success;
	}

	@Override
	protected void onPostExecute(Boolean success) {
		
		Log.w(TAG, "Transform Complete");

		if (success) {
			Toast.makeText(context, "Load Complete", Toast.LENGTH_LONG).show();
			Intent i = new Intent(context, TranslucentSurface.class);
			i.putExtra("MAM", true);
			System.out.println("1" + filename);
			i.putExtra("filename", filename);
			i.putExtra("Package", mVertexPackage);
			context.startActivity(i);
		} else {
			Toast.makeText(context, "Load Incomplete", Toast.LENGTH_LONG)
					.show();
			Intent i = new Intent(context, TranslucentSurface.class);
			i.putExtra("MAM", false);
			context.startActivity(i);

		}

		if (progressBar.isShowing())
			progressBar.dismiss();
	}

	
}
