package com.example.ece1778;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import com.android.constants.FileSystem;
import com.android.ece1778.R;

public class DatabaseHelper extends SQLiteOpenHelper{
	 
	
	private static final int SCHEMA=1;

	Context context = null;
	public DatabaseHelper(Context context) {
		super(context, "MAMlocal.sqlite", null, SCHEMA);
		this.context = context;
		burn_Files();
	}

	@Override
	public void onCreate(SQLiteDatabase db) 
	{
//		db.execSQL("drop table if exists defaultuser;");
//		db.execSQL("drop table if exists images");
		db.execSQL("CREATE TABLE if not exists defaultuser (_id INTEGER PRIMARY KEY AUTOINCREMENT,login TEXT, password TEXT);");
		db.execSQL("CREATE TABLE if not exists images (_id INTEGER PRIMARY KEY AUTOINCREMENT,key TEXT);");
		//Log.w("info","oncreate");
		
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldversion, int newversion) 
	{
		
		throw new RuntimeException("Database RuntimeException");
	}

	public void burn_Files()
	{
		File direct = new File(Environment.getExternalStorageDirectory() + "/3D");
		if (!direct.exists())
			direct.mkdir();
		copyAssets();
		//cleanDatabase();
	}
	
	
	private void copyAssets() {
	    AssetManager assetManager = context.getAssets();
	    String[] files = null;
	    try {
	        files = assetManager.list("");
	    } catch (IOException e) {
	        Log.w("info", "Failed to get asset file list.", e);
	    }
	    for(String filename : files) {
	    	if (!filename.endsWith(".obj")&&!filename.endsWith(".x3d"))
	    		continue;
	    	File file = new File(FileSystem.dir+ filename);
	    	if (file.exists())
	    		file.delete();
	        InputStream in = null;
	        OutputStream out = null;
	        Log.w("info", "assets"+" "+filename);
	        try {
	          in = assetManager.open(filename);
	          out = new FileOutputStream(FileSystem.dir+ filename);
	          copyFile(in, out);
	          in.close();
	          in = null;
	          out.flush();
	          out.close();
	          out = null;
//	          SQLiteDatabase write = this.getWritableDatabase();
//	          write.execSQL("insert into images(key) values("+"'"+filename+"');");
	        } catch(IOException e) {
	            Log.w("info", "Failed to copy asset file: " +" "+Environment.getExternalStorageDirectory().toString()+" " +filename+" "+e);
	        }       
	    }
	}
	
	//eliminate non-existed files from database
	private void cleanDatabase()
	{
		SQLiteDatabase read = this.getReadableDatabase();
		SQLiteDatabase write = this.getWritableDatabase();
		Cursor cursor = read.rawQuery("select key from images",null);
		while(cursor.moveToNext())
		{
			String filename = cursor.getString(0);
			File file = new File(FileSystem.dir+ filename);
	    	if (!file.exists())
	    	{
	    		write.execSQL("delete from images where key = " + "'" + filename + "';");
	    	}
		}
	}
	
	private void copyFile(InputStream in, OutputStream out) throws IOException {
	    byte[] buffer = new byte[1024];
	    int read;
	    while((read = in.read(buffer)) != -1){
	      out.write(buffer, 0, read);
	    }
	}
	/*
	 * 
	 * 
	 * File file = new File(Environment.getExternalStorageDirectory().toString()+"/3D/");
		File list[] = file.listFiles();
		SQLiteDatabase write = this.getWritableDatabase();
		for (int i = 0; i<list.length; i++)
		{
			Log.w("info", list[i].getName());
			//write.execSQL("insert into images(key)"+"values("+"'"+list[i].getName()+";");
			
		}
		if (file.exists())
		{
			Log.w("info", "directory exists");
			return;
		}
		Log.w("info", "directory not exists");
	 * 
	 * 
	 * */
	
//    //The Android's default system path of your application database.
//    private static String DB_PATH = "/data/data/com.example.d/databases/";
// 
//    private static String DB_NAME = "MAMlocal";
// 
//    private SQLiteDatabase myDatabase; 
// 
//    private final Context myContext;
// 
//    /**
//     * Constructor
//     * Takes and keeps a reference of the passed context in order to access to the application assets and resources.
//     * @param context
//     */
//    public DatabaseHelper(Context context) {
// 
//    	super(context, DB_NAME, null, 1);
//        this.myContext = context;
//    }	
// 
//  /**
//     * Creates a empty database on the system and rewrites it with your own database.
//     * */
//    public void createDatabase() throws IOException{
// 
//    	boolean dbExist = checkDatabase();
//    	SQLiteDatabase db_read = null;
//    	if(dbExist){
//    		
//     	
//    	}else{
// 
//    		//By calling this method and empty database will be created into the default system path
//               //of your application so we are gonna be able to overwrite that database with our database.
//        	db_read = this.getReadableDatabase();
//        	db_read.close();
//        	try {
// 
//    			copyDatabase();
// 
//    		} catch (IOException e) {
// 
//        		throw new Error("Error copying database");
// 
//        	}
//    	}
// 
//    }
// 
//    /**
//     * Check if the database already exist to avoid re-copying the file each time you open the application.
//     * @return true if it exists, false if it doesn't
//     */
//    private boolean checkDatabase(){
// 
//    	SQLiteDatabase checkDB = null;
// 
//    	try{
//    		String myPath = DB_PATH + DB_NAME;
//    		checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
// 
//    	}catch(SQLiteException e){
// 
//    		//database does't exist yet.
// 
//    	}
// 
//    	if(checkDB != null){
// 
//    		checkDB.close();
// 
//    	}
// 
//    	return checkDB != null ? true : false;
//    }
// 
//    /**
//     * Copies your database from your local assets-folder to the just created empty database in the
//     * system folder, from where it can be accessed and handled.
//     * This is done by transfering bytestream.
//     * */
//    private void copyDatabase() throws IOException{
// 
//    	//Open your local db as the input stream
//    	InputStream myInput = myContext.getAssets().open(DB_NAME);
// 
//    	// Path to the just created empty db
//    	String outFileName = DB_PATH + DB_NAME;
// 
//    	//Open the empty db as the output stream
//    	OutputStream myOutput = new FileOutputStream(outFileName);
// 
//    	//transfer bytes from the inputfile to the outputfile
//    	byte[] buffer = new byte[1024];
//    	int length = 0;
//    	
//    	while ((length = myInput.read(buffer))>0){
//    		//Log.w("info", Integer.toString(length));
//    		myOutput.write(buffer, 0, length);
//    	}
// 
//    	//Close the streams
//    	myOutput.flush();
//    	myOutput.close();
//    	myInput.close();
// 
//    }
// 
//    public void openDatabase() throws SQLException{
// 
//    	//Open the database
//        String myPath = DB_PATH + DB_NAME;
//    	myDatabase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
// 
//    }
// 
//    @Override
//	public synchronized void close() {
// 
//    	    if(myDatabase != null)
//    		    myDatabase.close();
// 
//    	    super.close();
// 
//	}
// 
//	@Override
//	public void onCreate(SQLiteDatabase db) {
// 
//	}
// 
//	@Override
//	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
// 
//	}
// 
//        // Add your public helper methods to access and get content from the database.
//       // You could return cursors by doing "return myDatabase.query(....)" so it'd be easy
//       // to you to create adapters for your views.
//	public Cursor get_Cursor(String query)
//	{
//		return myDatabase.rawQuery(query, null);
//	}
	
}
