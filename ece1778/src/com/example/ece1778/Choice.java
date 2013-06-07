package com.example.ece1778;

import com.android.constants.FileSystem;
import com.android.ece1778.R;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Choice extends Activity {

	private Button networkButton;
	private Button localButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.choice);
		
		addItems();

	}

	private void addItems() {
		localButton = (Button) findViewById(R.id.local);
		networkButton = (Button) findViewById(R.id.network);
		localButtonListener();
	}

	private void localButtonListener() {

		localButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String path = FileSystem.dir;
				Intent i = new Intent(Choice.this, GuestListActivity.class);
				// i.putExtra("filenamelist", filelist.toArray(new String[0]));
				i.putExtra("path", path);
				startActivity(i);

			}
		});
	}
}
