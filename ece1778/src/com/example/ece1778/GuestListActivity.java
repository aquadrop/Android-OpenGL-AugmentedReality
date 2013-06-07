package com.example.ece1778;

import java.io.File;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;
import com.android.ece1778.R;

public class GuestListActivity extends ListActivity {
	String path = null;
	String[] filelist;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.path = getIntent().getStringExtra("path");
		File file = new File(path);

		filelist = file.list();
		//String []prefixfilelist = new String[filelist.length];//strings
		File[] files = file.listFiles();
		
		setListAdapter(new GuestList(this, files, filelist));

	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {

		// get selected items
		
		
		Toast.makeText(this, this.filelist[position], Toast.LENGTH_SHORT).show();
		
		String entry = this.path + filelist[position];//file or path
		File file = new File(entry);
		if (file.exists()&&!file.isDirectory()) {
			new LoadTask(filelist[position], GuestListActivity.this).execute();
		} else
		{
			Intent i = new Intent(GuestListActivity.this,
					GuestListActivity.class);
			entry = entry + "/";
			i.putExtra("path", entry);
			startActivity(i);
		}
		

	}

}
