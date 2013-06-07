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

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;

import com.android.constants.FileSystem;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class FileDownloader extends AsyncTask<Void, Integer, Boolean> {

	String missionname = null;
	Context context = null;
	ProgressDialog progressBar = null;

	int lengthoffiles = 0;
	int progress = 0;

	ArrayList<String> filenames = new ArrayList<String>();

	public FileDownloader(Context context, String missionname) {
		this.context = context;
		this.missionname = missionname;

		progressBar = new ProgressDialog(context);
		progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	}

	@Override
	protected void onPreExecute() {
		this.progressBar.setMessage("Downloading..." + " " + " " + " ");
		this.progressBar.show();
	}

	@Override
	protected void onProgressUpdate(Integer... progress) {
		super.onProgressUpdate(progress);
		progressBar.setProgress(progress[0]);
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		boolean success = false;
		String response = null;
		String url = Network.head + Session.ip + ":" + Network.port + Network.querylink;
		ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
		postParameters
				.add(new BasicNameValuePair("queryimages", this.missionname));
		try
		{
			Log.w("info", "querylink"+" "+url);
			response = HttpClient.executeHttpPost(url, postParameters);
			response = response.toString().replaceAll("\\s+", "");
			// Log.w("info", "response"+" "+response);
			if (response.equals("-1")) {
				success = false;
				// Log.w("info", "out");
			} else {
				success = true;
				
				// Log.w("info", response);
				this.jsonListParser(response);
				this.packageSize();
				this.downloadPackage();
			}
			
		}catch(Exception e)
		{
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
			i.putExtra("path", FileSystem.dir+Session.username+"/"+this.missionname);
			context.startActivity(i);

		} else {
			Toast.makeText(context, "Failed to Connect to Server",
					Toast.LENGTH_LONG).show();
			
		}

		if (progressBar.isShowing())
			progressBar.dismiss();
	}
	
	private void jsonListParser(String response) {
		try {
			JSONArray jsonArray = new JSONArray(response);
			for (int i = 0; i < jsonArray.length(); i++) {
				Log.w("info", "filelist"+" "+Integer.toString(jsonArray.length()) + " "
						+ jsonArray.getString(i));
				filenames.add(jsonArray.getString(i));
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void packageSize() {
		this.lengthoffiles = 0;
		for (String filename : filenames.toArray(new String[0])) {

			try {
				String urlstring = Network.head + Session.ip + ":"
						+ Network.port + Network.datalink + filename;
				URL url = new URL(urlstring);
				URLConnection connection = url.openConnection();
				connection.connect();
				lengthoffiles = lengthoffiles + connection.getContentLength();

			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		Log.w("info", "length" + " " + Integer.toString(lengthoffiles));
	}

	private void downloadPackage() {
		

		this.progress = 0;
		File direct = new File(FileSystem.dir + Session.username + "/"
				+ this.missionname);
		if (!direct.exists())
			direct.mkdir();
		for (String filename : filenames) {

			downloadFile(filename);
		}
	}

	private void downloadFile(String filename) {
		try {
			String surl = Network.head + Session.ip + ":" + Network.port
					+ Network.datalink + filename;
			URL url = new URL(surl);
			URLConnection connection = url.openConnection();
			connection.connect();
			// this will be useful so that you can show a typical 0-100%
			// progress bar

			// download the file
			InputStream input = new BufferedInputStream(url.openStream());
			OutputStream output = new FileOutputStream(FileSystem.dir
					+ Session.username + "/" + this.missionname + "/"
					+ filename);

			byte data[] = new byte[1024];
			int count = 0;
			Log.w("info", "hello");
			while ((count = input.read(data)) != -1) {
				this.progress += count;
				// publishing the progress....
				publishProgress((int) (this.progress * 100 / lengthoffiles));

				Log.w("info", Integer.toString((int) (this.progress * 100 / lengthoffiles)));
				output.write(data, 0, count);
			}

			output.flush();
			output.close();
			input.close();
		} catch (Exception e) {
		}
	}

}
