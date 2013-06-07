package com.example.connection;

import java.io.File;
import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;
import com.android.ece1778.R;
import com.android.constants.FileSystem;
import com.example.ece1778.GuestList;
import com.example.ece1778.LoadTask;
import com.example.ece1778.LoginUIActivity;

public class PrivateListActivity extends ListActivity {
	String path = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		path = getIntent().getStringExtra("path");
		File file = new File(path);

		String[] filelist = file.list();
		File[] files = file.listFiles();
		for (int i = 0; i < filelist.length; i++)
			filelist[i] = filelist[i].replace(".obj", "");
		setListAdapter(new GuestList(this, files, filelist));

	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {

		// get selected items
		String selectedValue = (String) getListAdapter().getItem(position);
		String filename = selectedValue + ".obj";
		Toast.makeText(this, selectedValue, Toast.LENGTH_SHORT).show();

		String deeperpath = this.path  +"/"+selectedValue;
		String absoluteFilename = this.path  +"/"+filename;
		File file = new File(absoluteFilename);
		File folder = new File(deeperpath);
		Log.w("info", absoluteFilename);
		if (file.exists() && !file.isDirectory()) {

			Log.w("info", "hello");
			new LoadTask(filename, PrivateListActivity.this).execute();

		} else {
			Log.w("info", "load");
			Intent i = new Intent(PrivateListActivity.this,
					PrivateListActivity.class);
			// i.putExtra("filenamelist", filelist.toArray(new String[0]));
			try {
				if (folder.list().length > 0) {
					Log.w("info", "no load");
					i.putExtra("path", deeperpath);
					startActivity(i);
				} else {
					new FileDownloader(PrivateListActivity.this, selectedValue)
							.execute();
				}
			} catch (Exception e) {

				i.putExtra("path", this.path);
			}
		}

	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.menu_private_activity, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.refresh:
//				if (this.path.equals(FileSystem.dir+Session.username))
//				{
//					ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
//					postParameters
//					.add(new BasicNameValuePair("username", Session.username));
//			postParameters
//					.add(new BasicNameValuePair("password", Session.password));
//					new ConnectServer(PrivateListActivity.this, Session.ip, Session.username ,postParameters).execute();
//				}
//				
				return true;
			default: return true;
		}
		
	}
}
