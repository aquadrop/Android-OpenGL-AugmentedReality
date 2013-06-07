package com.example.ece1778;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import com.android.ece1778.R;

public class Main extends Activity {

	String TAG = "info";
	static boolean is_startmission = false;
	static boolean MAM = true;
	//private static String urlstring = "http://individual.utoronto.ca/CloudCliff/Mobile/cube";
	protected static String urlstring = "http://individual.utoronto.ca/CloudCliff/Mobile/monkey";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		new Task().execute();
	}

	
	private class Task extends AsyncTask<Void, Void, Void>
	{
		

		@Override
		protected void onPreExecute()
		{
			prepareMission();
		}
		@Override
		protected Void doInBackground(Void... params) {
			
			return null;
		}
		
		@Override
		protected void onPostExecute(Void arg)
		{
			startLogin();
		}
		
	}
	
	private void prepareMission()
	{
		new PrepareTask(Main.this).execute();
//		PrepareTask preparetask = new PrepareTask(MainActivity.this);
//		preparetask.execute();
//		try {
//			preparetask.get();
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (ExecutionException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
    }
	
	private void startMission(boolean is)
	{
		if (!is)
		{
			Intent i = new Intent(this, TranslucentSurface.class);
			i.putExtra("MAM", is);
			startActivity(i);
		}
		else
		{
			new LoadTask(Main.this).execute();
		}
	}

	private void startLogin()
	{
		Intent i = new Intent(this, Choice.class);
		startActivity(i);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
}
