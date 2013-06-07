package com.example.connection;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;

import com.android.constants.FileSystem;
import com.example.ece1778.GuestListActivity;
import com.example.ece1778.LoginUIActivity;
import com.example.ece1778.TranslucentSurface;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.Toast;

public class ConnectServer extends AsyncTask<Void, Void, Boolean> {

	ProgressDialog progressBar;
	ProgressDialog progressDownloadBar;
	Context context = null;
	private String url = null;
	private String ip = null;
	private String username = null;
	private int progress = 0;
	ArrayList<NameValuePair> postParameters = null;
	int lengthoffiles = 0;

	private List<String> missionnames = new ArrayList<String>();

	// private List<String> status = new ArrayList<String>();

	public ConnectServer(Context context, String ip, String username,
			ArrayList<NameValuePair> postParameters) {
		this.context = context;
		this.ip = ip;
		this.username = username;
		this.url = Network.head + ip + ":" + Network.port + Network.link;
		this.postParameters = postParameters;

		progressBar = new ProgressDialog(context);
		progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		// progressBar.getWindow().setLayout(200, 10);

	}

	@Override
	protected void onPreExecute() {
		this.progressBar.setMessage("Connecting to Server" + " " + " " + " ");
		this.progressBar.show();
	}

	@Override
	protected Boolean doInBackground(Void... arg0) {
		String response = null;
		Boolean success = false;
		try {
			response = HttpClient.executeHttpPost(url, postParameters);
			response = response.toString().replaceAll("\\s+", "");
			// Log.w("info", "response"+" "+response);
			if (response.equals("-1")) {
				success = false;
				// Log.w("info", "out");
			} else {
				success = true;
				storeSession();
				// Log.w("info", response);
				this.jsonListParser(response);

				// for (int i = 0; i<filenames.size(); i++)
				// Log.w("info", filenames.get(i)+" "+status.get(i));
				File folder = new File(FileSystem.dir+this.username);
				DeleteRecursive(folder);
				initPrivateFolder();
				

			}
		} catch (Exception e) {

			e.printStackTrace();
			Log.w("info", "failed");
			success = false;
		}

		return success;
	}

	@Override
	protected void onPostExecute(Boolean success) {
		if (success) {
			Toast.makeText(context, "Successfully Connect to Server",
					Toast.LENGTH_LONG).show();
			Intent i = new Intent(context,
					PrivateListActivity.class);
			//i.putExtra("filenamelist", filelist.toArray(new String[0]));
			i.putExtra("path", FileSystem.dir+this.username);
			i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			context.startActivity(i);

		} else {
			Toast.makeText(context, "Failed to Connect to Server",
					Toast.LENGTH_LONG).show();
			
		}

		if (progressBar.isShowing())
			progressBar.dismiss();
	}

	// @Override
	// protected void onProgressUpdate(Integer... progress) {
	//
	// progressBar.setProgress(progress[0]);
	// }
	
	private void storeSession()
	{
		Session.username = this.username;
		Session.ip = this.ip;
		Session.is_logged = true;
	}
	
	private void jsonListParser(String response) {
		try {
			JSONArray jsonArray = new JSONArray(response);
			for (int i = 0; i < jsonArray.length(); i++) {
				Log.w("info", "missionlist"+" "+Integer.toString(jsonArray.length())+" "+jsonArray.getString(i));
				missionnames.add(jsonArray.getString(i));
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// do refresh, clean all the files
	void DeleteRecursive(File fileOrDirectory) {
	    if (fileOrDirectory.isDirectory())
	        for (File child : fileOrDirectory.listFiles())
	            DeleteRecursive(child);

	    fileOrDirectory.delete();
	}

	private void initPrivateFolder() {
		File file = new File(FileSystem.dir+this.username);
		if (!file.exists())
			file.mkdir();
		for (int i = 0; i < this.missionnames.size(); i++) {
			file = new File(FileSystem.dir + this.username+"/"+this.missionnames.get(i));
			if (!file.exists())
				file.mkdir();
		}
	}
	

}
