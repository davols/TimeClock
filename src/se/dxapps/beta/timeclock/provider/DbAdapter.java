package se.dxapps.beta.timeclock.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;

/***
 * 
 * 
 * @author david
 * Database helper to do anything to the database for the different tracks. Contains constructor for the database itself. 
 * Database version needs to be increased if the fields changes, if the application is already installed on users' phones. 
 */

public class DbAdapter {
	//Static fields. 
	private static final String DB_NAME = "timeclock"; 
	private static final String DB_TABLE_CLOCK="clock";
	private static final String DB_TABLE_PROJECTS="projects";

	private static final int DATABASE_VERSION = 2;

	public static final String ROW_ID="_id";
	public static final String KEY_TITLE = "title";
	public static final String KEY_DESCRIPTION = "description";
	public static final String KEY_START = "start";
	public static final String KEY_END = "end";
	public static final String KEY_ADRESS = "adress";
	public static final String KEY_LONG ="long";
	public static final String KEY_LAT = "lat";
	public static final String KEY_DURATION = "duration";
	public static final String KEY_EXPORTED = "exported";
	public static final String KEY_PROJECTID="projectId";
	public static final String KEY_NHOOD = "neighborhood";

	public static final String KEY_PROJECTNAME = "projectName";
	public static final String KEY_COUNTER="counter";

	//Create. 
	private static final String DB_CREATE=
		"create table clock (_id integer primary key autoincrement," +
		"title text ," +
		"description text ," +
		"start integer,"+
		"end integer,"+
		"adress text ," +
		"long float,"+
		"lat float,"+
		"duration integer,"+
		"neighborhood text,"+
		"projectId integer,"+
	
		"exported integer)";

	private static final String DB_CREATE_PROJECTS=
		"create table projects (_id integer primary key autoincrement," +
		"projectName text ," +
		"description text ," +
		"counter integer)";


	Context ctx; 

	private SQLiteDatabase mDb;

	DBHelperAdapter mDbHelper; 
	public DbAdapter(Context ctx) {

		this.ctx = ctx; 

	}

	private class DBHelperAdapter extends SQLiteOpenHelper {

		public DBHelperAdapter(Context context, String name, CursorFactory factory, int version) {
			super(context, DB_NAME, factory, DATABASE_VERSION);
		}
		public DBHelperAdapter(Context ctx) {
			super(ctx, DB_NAME, null, DATABASE_VERSION);

		}
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DB_CREATE); 
			db.execSQL(DB_CREATE_PROJECTS);
			createProject(db,"Default");
		}
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			if(oldVersion==1&&newVersion==2) {
				db.execSQL(DB_CREATE_PROJECTS);
				long id = createProject(db,"Default");
				db.execSQL("ALTER TABLE clock ADD COLUMN neighborhood TEXT DEFAULT 'Unknown';");
				
				db.execSQL("ALTER TABLE clock ADD COLUMN projectID integer DEFAULT '"+id+"';");

			}
			else {
				db.execSQL("DROP TABLE IF EXISTS clock");
				db.execSQL("DROP TABLE IF EXISTS projects");

				onCreate(db);
			}
		}
		private long createProject(SQLiteDatabase db, String string) {
			ContentValues values = new ContentValues();
			values.put(KEY_PROJECTNAME,string);
			values.put(KEY_COUNTER, 0);


			return db.insert(DB_TABLE_PROJECTS, null, values);

		}


	}



	public DbAdapter open() throws SQLException {

		mDbHelper = new DBHelperAdapter(ctx);

		mDb = mDbHelper.getWritableDatabase();


		return this;

	}

	public void close() {

		mDbHelper.close();

	}

	public long createClockIn(String title,String adress,String nhood,long startInMs,Location loc) {
		ContentValues values = new ContentValues();
		values.put(KEY_TITLE,title);
		values.put(KEY_START, startInMs);
		values.put(KEY_END, -1);
		values.put(KEY_DURATION, -1);
		values.put(KEY_DESCRIPTION, "");
	
		if(loc!=null) {
			values.put(KEY_LAT, loc.getLatitude());
			values.put(KEY_LONG, loc.getLongitude());
		}
		else {
			values.put(KEY_LAT, 0);
			values.put(KEY_LONG, 0);
		}
		values.put(KEY_ADRESS, adress);
		values.put(KEY_NHOOD, nhood);
		long id = mDb.insert(DB_TABLE_CLOCK, null, values);


		return id;
	}
	public long createProject(String project) {
		ContentValues values = new ContentValues();
		values.put(KEY_PROJECTNAME,project);
		values.put(KEY_COUNTER, 0);


		long id = mDb.insert(DB_TABLE_PROJECTS, null, values);


		return id;
	}
	public boolean deleteClockIn(long id) {
		String selection = ROW_ID+"="+id;
		return mDb.delete(DB_TABLE_CLOCK, selection, null)>0;

	}
	public boolean deleteProject(long id) {
		String selection = ROW_ID+"="+id;
		return mDb.delete(DB_TABLE_PROJECTS, selection, null)>0;

	}

	public Cursor getAllClocks() {

		return mDb.query(true,DB_TABLE_CLOCK, new String[] {ROW_ID, KEY_TITLE,KEY_PROJECTID, KEY_DESCRIPTION,KEY_START,KEY_END,KEY_ADRESS,KEY_LONG,KEY_LAT,KEY_DURATION,KEY_EXPORTED,KEY_NHOOD}, null, null,  null, null, ROW_ID+" DESC", null);

	}

	public Cursor getClock(long rowId) {
		String selection = ROW_ID+"="+rowId;
		return mDb.query(true,DB_TABLE_CLOCK, new String[] {ROW_ID, KEY_TITLE,KEY_PROJECTID, KEY_DESCRIPTION,KEY_START,KEY_END,KEY_ADRESS,KEY_LONG,KEY_LAT,KEY_DURATION,KEY_EXPORTED,KEY_NHOOD}, selection, null,  null, null, ROW_ID+" DESC", null);

	}
	

	public long updateClockIn(long id,String title,long projectID, String description, long startInMs,long endInMs,String adress,String nhood,Location loc,long duration,int exported) {
		ContentValues values = new ContentValues();
		values.put(KEY_TITLE,title);
		values.put(KEY_PROJECTID, projectID);
	
		values.put(KEY_START, startInMs);
		values.put(KEY_END, endInMs);
		values.put(KEY_DURATION,duration);
		values.put(KEY_DESCRIPTION, description);
		values.put(KEY_NHOOD, nhood);
		if(loc!=null) {
			values.put(KEY_LAT, loc.getLatitude());
			values.put(KEY_LONG, loc.getLongitude());
		}
		else {
			values.put(KEY_LAT, 0);
			values.put(KEY_LONG, 0);
		}
		values.put(KEY_ADRESS, adress);

		String selection = ROW_ID+"="+id;
		return mDb.update(DB_TABLE_CLOCK, values, selection,null);

	}


	public boolean updateProject(String project,int count,long id) {
		ContentValues values = new ContentValues();
		values.put(KEY_PROJECTNAME,project);
		values.put(KEY_COUNTER, count);

		String selection = ROW_ID+"="+id;
		return mDb.update(DB_TABLE_PROJECTS, values, selection, null)>0;
	}
	public boolean updateProject(int count,long id) {
		ContentValues values = new ContentValues();
		
		values.put(KEY_COUNTER, count);

		String selection = ROW_ID+"="+id;
		return mDb.update(DB_TABLE_PROJECTS, values, selection, null)>0;
	}
	public Cursor getProjectById(long l) {
		String selection = ROW_ID+"="+l;
		return mDb.query(true, DB_TABLE_PROJECTS, new String[] {ROW_ID,KEY_PROJECTNAME,KEY_COUNTER},selection,null,null,null,null, null);
	}
	public Cursor getAllProjects() {

		return mDb.query(true,DB_TABLE_PROJECTS, new String[] {ROW_ID, KEY_PROJECTNAME, KEY_COUNTER}, null, null,  null, null, ROW_ID+" DESC", null);

	}
	public String getProjectName(long l) {
		String name="Default";
		String selection = ROW_ID+"="+l;
		Cursor c = mDb.query(true, DB_TABLE_PROJECTS, new String[] {ROW_ID,KEY_PROJECTNAME},selection,null,null,null,null, null);
		c.moveToFirst();
		name=c.getString(c.getColumnIndex(KEY_PROJECTNAME));
		c.close();
		
		return name;
		
	}
}
