package se.dxapps.beta.timeclock;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import se.dxapps.timeclock.R;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.ArrayWheelAdapter;
import kankan.wheel.widget.adapters.NumericWheelAdapter;
import se.dxapps.beta.timeclock.data.ClockIn;
import se.dxapps.beta.timeclock.data.Constants;
import se.dxapps.beta.timeclock.data.ExportResult;
import se.dxapps.beta.timeclock.data.ExportSetting;
import se.dxapps.beta.timeclock.data.Project;
import se.dxapps.beta.timeclock.provider.DbAdapter;
import se.dxapps.beta.timeclock.provider.MyWriter;
import se.dxapps.beta.timeclock.widget.ClockExportAdapter;
import se.dxapps.beta.timeclock.widget.ClockListAdapter;
import se.dxapps.beta.timeclock.widget.DayArrayAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
/**
 * List activity to list the items in the database.  Can either just list them or list in export mode (checkboxes). 
 * 
 * @author david
 *
 */
public class ListActivity extends Activity{
	/** LinearLayout with progressbar
	 */
	private LinearLayout pbar;
	/** Listview
	 */
	private ListView listview;
	/**Database helper
	 */
	private DbAdapter dbhelper;
	/** Preferences
	 */
	private SharedPreferences sp;
	/** Typefaces 
	 */
	private Typeface abel;
	private Typeface coust;
	/** TextView for empty list 
	 */
	private TextView twEmpty;
	/** Adapter for listing items 
	 */
	private ClockListAdapter mClockAdapter=null;
	/** Adapter for listing items to export
	 */
	private ClockExportAdapter mExportAdapter=null;
	private String TAG ="ListActivity";
	private int SHOW_TYPE;
	
	private ArrayList<ClockIn> mList = new ArrayList<ClockIn>();
	private ArrayList<String> projects = new ArrayList<String>();
	private ArrayList<Project> mProjects = new ArrayList<Project>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list);
		sp = PreferenceManager.getDefaultSharedPreferences(this); 
		dbhelper = new DbAdapter(this);
		abel =(Typeface.createFromAsset(getAssets(), "fonts/abel_regular.ttf"));
		coust =(Typeface.createFromAsset(getAssets(), "fonts/coustard_reg.ttf"));
		SHOW_TYPE = getIntent().getIntExtra(Constants.KEY_SHOW_TYPE, Constants.TYPE_LIST);
		Log.d(TAG,"ListActivity:"+SHOW_TYPE);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		initViews();

		new fetchClockInsClass(dbhelper).execute(0);

	}
	public void initViews() {
		pbar = (LinearLayout) findViewById(R.id.root_pb);
		pbar.setVisibility(View.GONE);
		listview = (ListView) findViewById(R.id.listview);
		listview.setCacheColorHint(0);
		twEmpty = (TextView) findViewById(R.id.tw_empty);
		twEmpty.setTypeface(coust);
		twEmpty.setVisibility(View.GONE);
		/**Are we showing for export? 
		 */
		if(SHOW_TYPE==Constants.TYPE_EXPORT) {
			listview.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
			listview.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					mList.get(arg2).setExport(!mList.get(arg2).isExport());
					if(mExportAdapter!=null) {
						mExportAdapter.notifyDataSetChanged();
					}
				}

			});
		}
		else {
			registerForContextMenu(listview);
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		if (v.getId()==R.id.listview) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
			menu.setHeaderTitle(mList.get(info.position).getTitle());
			String[] menuItems = getResources().getStringArray(R.array.item_context_menu);
			for (int i = 0; i<menuItems.length; i++) {
				menu.add(Menu.NONE, i, i, menuItems[i]);
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		if(SHOW_TYPE==0) {
			inflater.inflate(R.menu.export_menu, menu);
		}
		else {
			inflater.inflate(R.menu.list_menu, menu);
		}
		return true;
	}
	/** If any of the items are chosen to be exported. 
	 *  @return at least one to export 
	 */
	boolean isSomeOneTrue() {
		for(int i=0;i<mList.size();i++) {
			if(mList.get(i).isExport()) {
				return true;
			}
		}
		return false;
	}
	public static boolean checkSdCardStatus(Context mContext){

		String auxSDCardStatus = Environment.getExternalStorageState();

		if (auxSDCardStatus.equals(Environment.MEDIA_MOUNTED))
			return true;
		else if (auxSDCardStatus.equals(Environment.MEDIA_MOUNTED_READ_ONLY)){
			Toast.makeText(mContext, R.string.sdcard_read_only, Toast.LENGTH_LONG).show();
			return true;
		}
		else if(auxSDCardStatus.equals(Environment.MEDIA_NOFS)){
			Toast.makeText(mContext, R.string.sdcard_nofs, Toast.LENGTH_LONG).show();
			return false;
		}

		else if(auxSDCardStatus.equals(Environment.MEDIA_REMOVED)){
			Toast.makeText(mContext, R.string.sdcard_removed, Toast.LENGTH_LONG).show();
			return false;
		}
		else if(auxSDCardStatus.equals(Environment.MEDIA_SHARED)){
			Toast.makeText(mContext, R.string.sdcard_shared, Toast.LENGTH_LONG).show();
			return false;
		}
		else if (auxSDCardStatus.equals(Environment.MEDIA_UNMOUNTABLE)){
			Toast.makeText(mContext, R.string.sdcard_unmountable, Toast.LENGTH_LONG).show();
			return false;
		}
		else if (auxSDCardStatus.equals(Environment.MEDIA_UNMOUNTED)){
			Toast.makeText(mContext, R.string.sdcard_unmounted, Toast.LENGTH_LONG).show();
			return false;
		}


		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.export_selected:

			if(isSomeOneTrue()) {
				if(checkSdCardStatus(this)) {
					export();
				}
			}
			else {
				Toast.makeText(this, R.string.export_toast_choose_something, Toast.LENGTH_LONG).show();
			}

			return true;
		case R.id.mark_all:
			for(int i=0;i<mList.size();i++) {
				mList.get(i).setExport(true);
			}
			if(mExportAdapter!=null) {
				mExportAdapter.notifyDataSetChanged();
			}

			return true;
		case R.id.unmark_all:
			for(int i=0;i<mList.size();i++) {
				mList.get(i).setExport(false);
			}
			if(mExportAdapter!=null) {
				mExportAdapter.notifyDataSetChanged();
			}
			return true;
		case R.id.delete_all:

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.confirm_want_to_delete_all)
			.setCancelable(false)
			.setPositiveButton(R.string.confirm_yes, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int id) {
					deleteAll();
				}
			})
			.setNegativeButton(R.string.confirm_no, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			});
			AlertDialog alert = builder.create();
			alert.show();


			return true;
		case R.id.export:
			Intent i2 = new Intent(this,ListActivity.class);
			i2.putExtra(Constants.KEY_SHOW_TYPE, Constants.TYPE_EXPORT);
			startActivity(i2);
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	/** 
	 * User wants to export. Prompts with alertDialog so the user can choose the name. Auto filled in with todays date and time. 
	 */
	private void export() {
		final AlertDialog.Builder alert = new AlertDialog.Builder(this);
		final EditText input = new EditText(this);
		String DATE_FORMAT = "yyyy_MM_dd_HH_mm";

		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
		input.setText(this.getString(R.string.export_fileName_default)+sdf.format(Calendar.getInstance().getTime()));
		alert.setView(input);
		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				String value = input.getText().toString().trim();
				if(value.length()>0) {
					value = value.replace(" ", "_");
					new exportClockClass(ListActivity.this).execute(value);
				}
				else {
					Toast.makeText(ListActivity.this, R.string.export_toast_choose_name, Toast.LENGTH_SHORT).show();
				}
			}
		});

		alert.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				dialog.cancel();
			}
		});
		alert.setTitle(R.string.export_dialog_title);
		alert.show();
	}

	public void deleteAll() {
		dbhelper.open();
		for(int i=0;i<mList.size();i++) {
			ClockIn cl = mList.get(i);

			dbhelper.deleteClockIn(cl.getRowId());

		}
		do{
			mList.remove(0);
		}while(mList.size()>0);

		if(mClockAdapter!=null) {
			mClockAdapter.notifyDataSetChanged();
		}
		dbhelper.close();
	}
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		int menuItemIndex = item.getItemId();
		if(menuItemIndex==0) {
			//Edit.
			editNote(mList.get(info.position),info.position);
		}
		else if(menuItemIndex==1) {
			//Delete
			deleteNote(mList.get(info.position));
			Toast.makeText(this, "Successfully deleted!",Toast.LENGTH_SHORT).show();
		}
		return true;
	}
	private void deleteNote(ClockIn clockIn) {
		dbhelper.open();
		dbhelper.deleteClockIn(clockIn.getRowId());
		dbhelper.close();
		mList.remove(clockIn);
		if(mClockAdapter!=null) {
			mClockAdapter.notifyDataSetChanged();
		}
		if(mList.size()==0) {
			listview.setVisibility(View.GONE);
			twEmpty.setVisibility(View.VISIBLE);
		}
	}

	/** 
	 * Edit note opens dialog so the user can change any of the data of an item. 
	 */
	private void editNote(final ClockIn clockIn,final int index) {
		final Dialog dialog = new Dialog(this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.edit);
		final Project myOldProject = getProject(clockIn.getRowId());
		final Spinner spinner = (Spinner) dialog.findViewById(R.id.edit_spinner);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, projects);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setSelection(projects.indexOf(myOldProject.getName()));
		final TextView mDuration = (TextView) dialog.findViewById(R.id.duration);
		final EditText mTitle = (EditText) dialog.findViewById(R.id.edit_title);
		final EditText mDesc = (EditText) dialog.findViewById(R.id.edit_description);
		final EditText mAdress = (EditText) dialog.findViewById(R.id.edit_address);
		final EditText mHood = (EditText) dialog.findViewById(R.id.edit_nbhood);
		TextView twStart = (TextView) dialog.findViewById(R.id.twStart);
		TextView twEnd = (TextView) dialog.findViewById(R.id.twEnd);
		final Button btnStart = (Button) dialog.findViewById(R.id.btnStart);
		final Button btnEnd = (Button) dialog.findViewById(R.id.btnEnd);
		Button save = (Button) dialog.findViewById(R.id.btnSave);
		Button delete = (Button) dialog.findViewById(R.id.btnDelete);
		Button cancel = (Button) dialog.findViewById(R.id.btnCancel);
		Calendar calStart = Calendar.getInstance();
		Calendar calEnd = Calendar.getInstance();
		calStart.setTimeInMillis(clockIn.getStartTime());
		long dur = clockIn.getDuration();
		final java.text.NumberFormat nf = new java.text.DecimalFormat("00"); 
		int hoursz   = (int) ((dur / 1000) / 3600);
		int minutesz = (int) (((dur / 1000) / 60)-hoursz*60);
		mDuration.setText(this.getString(R.string.hint_duration)+nf.format(hoursz)+":"+nf.format(minutesz));
		calEnd.setTimeInMillis(clockIn.getEndTime());
		String DATE_FORMAT = "dd/MM/yyyy HH:mm";
		final boolean show24=sp.getBoolean("prefs_time_24", true);
		if(show24) {
			DATE_FORMAT = "dd/MM/yyyy HH:mm";
		}
		else {
			DATE_FORMAT = "dd/MM/yyyy hh:mm a";
		}

		final SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
		btnStart.setText(sdf.format(calStart.getTime()));
		btnEnd.setText(sdf.format(calEnd.getTime()));

		twStart.setTypeface(abel);
		twEnd.setTypeface(abel);
		btnStart.setTypeface(abel);
		btnEnd.setTypeface(abel);
		mTitle.setTypeface(abel);
		mDesc.setTypeface(abel);
		mAdress.setTypeface(abel);
		mDuration.setTypeface(abel);
		save.setTypeface(abel);
		delete.setTypeface(abel);
		cancel.setTypeface(abel);
		mHood.setTypeface(abel);
	
		mTitle.setText(clockIn.getTitle());
		mDesc.setText(clockIn.getDescription());
		mAdress.setText(clockIn.getAdress());
		mHood.setText(clockIn.getHood());
		/** Saves the note and dismisses the dialog. 
		 */
		save.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Project chosen = mProjects.get(spinner.getSelectedItemPosition());
				if(chosen.getRowId()!=myOldProject.getRowId()) {
					dbhelper.open();
					dbhelper.updateProject(( chosen.getCount()+1), chosen.getRowId());
					if(myOldProject.getRowId()!=-1) {
						dbhelper.updateProject(myOldProject.getCount()-1,myOldProject.getRowId());
					}
					dbhelper.close();
				}

				String myDate;
				if(show24) {
					myDate = "dd/MM/yyyy HH:mm";
				}
				else {
					myDate = "dd/MM/yyyy hh:mm a";
				}
				SimpleDateFormat sdfz = new SimpleDateFormat(myDate);
				long inTime;
				try {
					inTime = sdfz.parse(btnStart.getText().toString()).getTime();
				} catch (ParseException e) {
					inTime=clockIn.getStartTime();
					e.printStackTrace();
				}
				long outTime;
				try {
					outTime = sdfz.parse(btnEnd.getText().toString()).getTime();
				} catch (ParseException e) {
					outTime=clockIn.getEndTime();
					e.printStackTrace();
				}
				if((outTime-inTime)<0) {
					Toast.makeText(ListActivity.this, R.string.end_time_smaller_than_start, Toast.LENGTH_LONG).show();
					return;
				}
				ClockIn newClock = new ClockIn();
				newClock.setAdress(mAdress.getText().toString());
				newClock.setTitle(mTitle.getText().toString());
				newClock.setDescription(mDesc.getText().toString());
				newClock.setRowId(clockIn.getRowId());
				newClock.setStartTime(inTime);
				newClock.setEndTime(outTime);
				newClock.setDuration((outTime-inTime));
				newClock.setHood(mHood.getText().toString());
				mList.set(index, newClock);
				if(mClockAdapter!=null) {
					mClockAdapter.notifyDataSetChanged();
				}
				dbhelper.open();
				dbhelper.updateClockIn(newClock.getRowId(), newClock.getTitle(),chosen.getRowId(), newClock.getDescription(), newClock.getStartTime(), newClock.getEndTime(), newClock.getAdress(),newClock.getHood(), null, newClock.getDuration(), 0);
				dbhelper.close();
				dialog.dismiss();
			}
		});
		cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		delete.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				deleteNote(clockIn);
				dialog.dismiss();
			}
		});
		/** change start time. Opens dialog. 
		 */
		btnStart.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				final Dialog d = showDateDialog(ListActivity.this,clockIn.getStartTime());
				Button cancel = (Button) d.findViewById(R.id.btnCancel);
				cancel.setTypeface(abel);
				Button save = (Button) d.findViewById(R.id.btnSave);
				save.setTypeface(abel);
				cancel.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						d.dismiss();
					}

				});
				save.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						WheelView hours = (WheelView) d.findViewById(R.id.hour);
						final WheelView mins = (WheelView) d.findViewById(R.id.mins);
						final WheelView ampm = (WheelView) d.findViewById(R.id.ampm);
						final WheelView day = (WheelView) d.findViewById(R.id.day);


						Calendar calendar = Calendar.getInstance();
						calendar.set(Calendar.MILLISECOND, 0);
						calendar.set(Calendar.SECOND, 0);
						calendar.add(Calendar.DATE, -(20-day.getCurrentItem()));
						calendar.set(Calendar.MINUTE,mins.getCurrentItem());
						if(show24) {
							calendar.set(Calendar.HOUR_OF_DAY, hours.getCurrentItem());
						}
						else {
							calendar.set(Calendar.HOUR, hours.getCurrentItem());
							calendar.set(Calendar.AM_PM, ampm.getCurrentItem()==0 ? Calendar.AM:Calendar.PM);
						}
						btnStart.setText(sdf.format(calendar.getTime()));
						String myDate;
						if(show24) {
							myDate = "dd/MM/yyyy HH:mm";
						}
						else {
							myDate = "dd/MM/yyyy hh:mm a";
						}
						SimpleDateFormat sdfz = new SimpleDateFormat(myDate);
						long inTime= calendar.getTimeInMillis();
						long outTime;
						try {
							outTime = sdfz.parse(btnEnd.getText().toString()).getTime();
						} catch (ParseException e) {
							outTime=clockIn.getEndTime();
							e.printStackTrace();
						}
						//Update duration
						long durz = outTime-inTime;

						int mHours   = (int) ((durz / 1000) / 3600);
						int mMinutes = (int) (((durz / 1000) / 60)-mHours*60);
						mDuration.setText(ListActivity.this.getString(R.string.hint_duration)+nf.format(mHours)+":"+nf.format(mMinutes));
						d.dismiss();
					}

				});
				d.show();
			}
		});
		/** change the end time. Opens a dialog. 
		 */
		btnEnd.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				final Dialog d = showDateDialog(ListActivity.this,clockIn.getEndTime());
				Button cancel = (Button) d.findViewById(R.id.btnCancel);
				cancel.setTypeface(abel);
				Button save = (Button) d.findViewById(R.id.btnSave);
				save.setTypeface(abel);
				cancel.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						d.dismiss();
					}

				});
				save.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						WheelView hours = (WheelView) d.findViewById(R.id.hour);
						final WheelView mins = (WheelView) d.findViewById(R.id.mins);
						final WheelView ampm = (WheelView) d.findViewById(R.id.ampm);
						final WheelView day = (WheelView) d.findViewById(R.id.day);


						Calendar calendar = Calendar.getInstance();
						calendar.set(Calendar.MILLISECOND, 0);
						calendar.set(Calendar.SECOND, 0);
						calendar.add(Calendar.DATE, -(20-day.getCurrentItem()));
						calendar.set(Calendar.MINUTE,mins.getCurrentItem());
						if(show24) {
							calendar.set(Calendar.HOUR_OF_DAY, hours.getCurrentItem());
						}
						else {
							calendar.set(Calendar.HOUR, hours.getCurrentItem());
							calendar.set(Calendar.AM_PM, ampm.getCurrentItem()==0 ? Calendar.AM:Calendar.PM);
						}
						btnEnd.setText(sdf.format(calendar.getTime()));
						String myDate;
						if(show24) {
							myDate = "dd/MM/yyyy HH:mm";
						}
						else {
							myDate = "dd/MM/yyyy hh:mm a";
						}
						SimpleDateFormat sdfz = new SimpleDateFormat(myDate);
						long outTime= calendar.getTimeInMillis();
						long inTime;
						try {
							inTime = sdfz.parse(btnStart.getText().toString()).getTime();
						} catch (ParseException e) {
							inTime=clockIn.getStartTime();
							e.printStackTrace();
						}
						//Update duration
						long durz = outTime-inTime;
						int mHours   = (int) ((durz / 1000) / 3600);
						int mMinutes = (int) (((durz / 1000) / 60)-mHours*60);
						mDuration.setText(ListActivity.this.getString(R.string.hint_duration)+nf.format(mHours)+":"+nf.format(mMinutes));
						d.dismiss();
					}

				});
				d.show();
			}
		});
		dialog.setCancelable(false);
		dialog.show();
	}
	/**
	 * 
	 * @param id of project to fetch
	 * @return project with specified id
	 */
	private Project getProject(long id) {
		Project project = new Project();
		dbhelper.open();
		Cursor c = dbhelper.getProjectById(id);
		if(c.moveToFirst()) {
			project.setCount(c.getInt(c.getColumnIndex(DbAdapter.KEY_COUNTER)));
			project.setRowId(c.getLong(c.getColumnIndex(DbAdapter.ROW_ID)));
			project.setName(c.getString(c.getColumnIndex(DbAdapter.KEY_PROJECTNAME)));
		}
		else {
			project.setCount(0);
			project.setRowId(-1);
			project.setName("Default");
		}
		c.close();
		dbhelper.close();
		return project;
	}

	/**
	 * Creates an wheelView (Dialog) so the user can set the date and time. 
	 * Requires wheel_src library project. 
	 * 
	 * @param Context to show in
	 * @param Time to show in milliseconds
	 * @return Dialog
	 */
	public Dialog showDateDialog(Context in,long timeinms) {
		Dialog timeDialog = new Dialog(in);
		timeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		timeDialog.setContentView(R.layout.time2_layout);
		LayoutParams params = timeDialog.getWindow().getAttributes(); 
		params.width = LayoutParams.FILL_PARENT; 
		timeDialog.getWindow().setAttributes((android.view.WindowManager.LayoutParams) params); 
		boolean tf=false;
		tf=sp.getBoolean("prefs_time_24", true);

		int nhours = 23;
		int startH=0;
		if(tf) {
			nhours = 23;
			startH=0;
		}
		else {
			startH=1;
			nhours = 12;
		}
		final WheelView hours = (WheelView) timeDialog.findViewById(R.id.hour);
		NumericWheelAdapter hourAdapter = new NumericWheelAdapter(ListActivity.this, startH, nhours);
		hourAdapter.setItemResource(R.layout.wheel_text_item);
		hourAdapter.setItemTextResource(R.id.text);
		hours.setViewAdapter(hourAdapter);
		final WheelView mins = (WheelView) timeDialog.findViewById(R.id.mins);
		NumericWheelAdapter minAdapter = new NumericWheelAdapter(ListActivity.this, 0, 59, "%02d");
		minAdapter.setItemResource(R.layout.wheel_text_item);
		minAdapter.setItemTextResource(R.id.text);
		mins.setViewAdapter(minAdapter);
		mins.setCyclic(true);

		final WheelView ampm = (WheelView) timeDialog.findViewById(R.id.ampm);
		ArrayWheelAdapter<String> ampmAdapter =
			new ArrayWheelAdapter<String>(ListActivity.this, new String[] {"AM", "PM"});
		ampmAdapter.setItemResource(R.layout.wheel_text_item);
		ampmAdapter.setItemTextResource(R.id.text);
		ampm.setViewAdapter(ampmAdapter);

		// set current time
		Calendar calendar = Calendar.getInstance(Locale.US);
		calendar.setTimeInMillis(timeinms);

		mins.setCurrentItem(calendar.get(Calendar.MINUTE));

		ampm.setCurrentItem(calendar.get(Calendar.AM_PM));
		if(tf) {
			hours.setCurrentItem(calendar.get(Calendar.HOUR_OF_DAY));
			ampm.setVisibility(View.GONE);
		}
		else {
			hours.setCurrentItem(calendar.get(Calendar.HOUR)-1);
			ampm.setVisibility(View.VISIBLE);
		}




		final WheelView day = (WheelView) timeDialog.findViewById(R.id.day);
		day.setViewAdapter(new DayArrayAdapter(ListActivity.this, calendar));      
		day.setCurrentItem(10);
		return timeDialog;
	}



	/** Creates the correct adapter for show type. 
	 */
	public ListAdapter createAdapter() {
		if(SHOW_TYPE==Constants.TYPE_LIST) {
			mClockAdapter = new ClockListAdapter(this,0,mList,sp.getBoolean("prefs_time_24", true));
			return mClockAdapter;
		}
		else {

			mExportAdapter = new ClockExportAdapter(this,0,mList,sp.getBoolean("prefs_time_24", true));
			return mExportAdapter;
		}

	}

	/** Fetches the clock ins (items) from database. 
	 */
	private class fetchClockInsClass extends AsyncTask<Integer, String, ArrayList<ClockIn>> {
		private DbAdapter dbadapter;
		public fetchClockInsClass(DbAdapter in) {
			dbadapter=in;
		}
		@Override
		protected ArrayList<ClockIn> doInBackground(Integer... params) {
			ArrayList<ClockIn> list = new ArrayList<ClockIn>();
			getProjects();
			dbadapter.open();
			Cursor c = dbadapter.getAllClocks();
			ClockIn tmp = new ClockIn();
			if(c.moveToFirst()) {
				do
				{
					tmp=new ClockIn();
					tmp.setRowId(c.getLong(c.getColumnIndex(DbAdapter.ROW_ID)));
					try{
						tmp.setProjectID(c.getLong(c.getColumnIndex(DbAdapter.KEY_PROJECTID)));
						tmp.setProjectName(dbadapter.getProjectName(tmp.getProjectID()));
					}
					catch(Exception e) {
						tmp.setProjectID(-1);
						tmp.setProjectName("Default");
					}
					tmp.setTitle(c.getString(c.getColumnIndex(DbAdapter.KEY_TITLE)));
					tmp.setAdress(c.getString(c.getColumnIndex(DbAdapter.KEY_ADRESS)));
					tmp.setDescription(c.getString(c.getColumnIndex(DbAdapter.KEY_DESCRIPTION)));
					tmp.setDuration(c.getLong(c.getColumnIndex(DbAdapter.KEY_DURATION)));
					tmp.setEndTime(c.getLong(c.getColumnIndex(DbAdapter.KEY_END)));
					tmp.setStartTime(c.getLong(c.getColumnIndex(DbAdapter.KEY_START)));
					tmp.setHood(c.getString(c.getColumnIndex(DbAdapter.KEY_NHOOD)));
					list.add(tmp);

				}while(c.moveToNext());

			}
			c.close();
			dbadapter.close();
			return list;
		}
		@Override
		protected void onPostExecute(ArrayList<ClockIn> result) {
			super.onPostExecute(result);
			listview.setVisibility(View.VISIBLE);
			pbar.setVisibility(View.GONE);

			mList=result;
			listview.setAdapter(createAdapter());
			if(result.size()==0) {
				twEmpty.setVisibility(View.VISIBLE);
				listview.setVisibility(View.GONE);
			}
		}
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			twEmpty.setText(R.string.empty);
			listview.setVisibility(View.GONE);
			pbar.setVisibility(View.VISIBLE);
			twEmpty.setVisibility(View.GONE);
		}
	}

/**
 * Export async task for exporting the chosen list. 
 * 
 *
 */
	private class exportClockClass extends AsyncTask<String, String, ExportResult> {
		private Context ctx;
		public exportClockClass(Context in) {
			ctx=in;
		}
		@Override
		protected ExportResult doInBackground(String... params) {
			ExportResult ans = new ExportResult();
			ans.setFailed(false);
			MyWriter mWriter = new MyWriter();
			ExportSetting export = new ExportSetting();
			export.GenereteFromPreferences(sp);
			try {
				ans.setPathName(mWriter.write(ctx, mList, params[0], export));
			} catch (IOException e) {
				ans.setFailedReason(e.toString());
				ans.setFailed(true);
				e.printStackTrace();
			} catch (Exception e) {
				ans.setFailedReason(e.toString());
				e.printStackTrace();
				ans.setFailed(true);
			}

			return ans;
		}
		@Override
		protected void onPostExecute(ExportResult result) {
			super.onPostExecute(result);

			if(result.isFailed()) {
				Toast.makeText(ListActivity.this, "Failed!"+result.getFailedReason(), Toast.LENGTH_SHORT).show();
			}
			else if(result.getPathName()==null){


			}
			else {
				Toast.makeText(ListActivity.this, "Completed! Saved to:"+result.getPathName(), Toast.LENGTH_LONG).show();
				//Delete the ones. 
				int size=mList.size();
				for(int i=size-1;i>=0;i--) {
					dbhelper.open();
					if(mList.get(i).isExport()) {
						dbhelper.deleteClockIn(mList.get(i).getRowId());
						mList.remove(i);
					}
					dbhelper.close();
					if(mExportAdapter!=null) {
						mExportAdapter.notifyDataSetChanged();
					}
				}
				if(sp.getBoolean("prefs_export_auto_send", true)) {

					Intent sendIntent = new Intent(Intent.ACTION_SEND);
					if(sp.getString("prefs_export_autosend_mail", null)!=null) {
						sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[]
						                                                   {sp.getString("prefs_export_autosend_mail",null)});
					}
					if(result.getPathName().endsWith(".csv")) {
						sendIntent.setType("text/csv");
						sendIntent.putExtra(Intent.EXTRA_STREAM, 
								Uri.fromFile(new
										File(result.getPathName())));
						ListActivity.this.startActivity(Intent.createChooser(sendIntent,
								ListActivity.this.getString(R.string.send)));
					}
					else if(result.getPathName().endsWith(".xls")){
						sendIntent.setType("application/vnd.ms-excel");
						sendIntent.putExtra(Intent.EXTRA_STREAM, 
								Uri.fromFile(new
										File(result.getPathName())));
						ListActivity.this.startActivity(Intent.createChooser(sendIntent,
								ListActivity.this.getString(R.string.send)));
					}
					else if(result.getPathName().endsWith(".xml")) {
						sendIntent.setType("text/xml");
						sendIntent.putExtra(Intent.EXTRA_STREAM, 
								Uri.fromFile(new
										File(result.getPathName())));
						ListActivity.this.startActivity(Intent.createChooser(sendIntent,
								ListActivity.this.getString(R.string.send)));
					}
					else {
						Toast.makeText(ListActivity.this, "Failed to send mail", Toast.LENGTH_SHORT).show();
					}

				}
				twEmpty.setVisibility(View.VISIBLE);
				listview.setVisibility(View.GONE);
				twEmpty.setText(R.string.export_completed);
			}

		}
		@Override
		protected void onPreExecute() {
			super.onPreExecute();


		}
	}
	private void getProjects() {
		dbhelper.open();
		Cursor c = dbhelper.getAllProjects();
		if(c.moveToFirst()) {
			do{
				Project tmp = new Project();
				tmp.setCount(c.getInt(c.getColumnIndex(DbAdapter.KEY_COUNTER)));
				tmp.setRowId(c.getLong(c.getColumnIndex(DbAdapter.ROW_ID)));
				tmp.setName(c.getString(c.getColumnIndex(DbAdapter.KEY_PROJECTNAME)));
				mProjects.add(tmp);
				projects.add(tmp.getName());

			}while(c.moveToNext());
		}
		c.close();
		dbhelper.close();
	}
}
