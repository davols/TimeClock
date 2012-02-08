package se.dxapps.beta.timeclock;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.ArrayWheelAdapter;
import kankan.wheel.widget.adapters.NumericWheelAdapter;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;

import se.dxapps.beta.timeclock.data.AppInfo;
import se.dxapps.beta.timeclock.data.Constants;
import se.dxapps.beta.timeclock.data.OldVersion;
import se.dxapps.beta.timeclock.data.Project;
import se.dxapps.beta.timeclock.data.YahooResult;
import se.dxapps.beta.timeclock.provider.DataManager;
import se.dxapps.beta.timeclock.provider.DbAdapter;
import se.dxapps.beta.timeclock.widget.DayArrayAdapter;
import se.dxapps.timeclock.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class TimeClock extends Activity implements OnClickListener {
	public static final int DIALOG_CHECK_VERSION = 1;
	private static final String TAG="TimeClock";
	private LocationManager locationManager;
	/**
	 * Handler to post runnables 
	 */
	private Handler mHandler = new Handler();
	/**
	 * DbAdapter
	 */
	private DbAdapter dbhelper;
/**
 * Button to clock in
 */
	private Button btnClockIn;
	/**
	 * Button to clock out
	 */	
	private Button btnClockOut;
	/**
	 * Button to list all
	 */
	private Button btnListAll;
	/**
	 * Btn to show settings
	 */
	private Button btnSettings;
	/** 
	 * Button to open export
	 * 
	 */
	private Button btnExport;
	/** Button to show app info
	 * 
	 */
	private Button btnInfo;
	/** Button to show projects and manage them
	 * 
	 */
	private Button btnCabinet;
	
	private Typeface abel;
	private int minDistance=500; //Default.
	
	private SharedPreferences sp;
	/** Thread that  fetches the adress of the user. 
	 * 
	 */
	private GetAdressThread getAdressThread;
	/** Default address and neighborhood
	 * 
	 */
	private String myCurrentAdress="Unknown";
	private String myCurrentNeighborHood="Unknown";
	/** TextView to display the adress
	 * 
	 */
	private TextView twAdress;
	/** Last location of the user
	 * 
	 */
	private Location lastLocation=null;
	/** TextView to show hint
	 * 
	 */
	private TextView twHint;
	/** Asynctask that checks if the app version is new enough. 
	 * 
	 */
	private CheckAppVersionClass cclass;
	/** 
	 * Boolean to see if the user is allowed to use the app (press buttons) depending on app version. 
	 */
	private boolean isAllowedToPress=false;
	/** List of project names
	 * 
	 */
	ArrayList<String> projects = new ArrayList<String>();
	/** 
	 * List of projects 
	 */
	ArrayList<Project> mProjects = new ArrayList<Project>();
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		/**Check appversion is disabled now. 
		 */
		isAllowedToPress=true;
		//cclass  = new CheckAppVersionClass();
		//cclass.execute();
		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		sp = PreferenceManager.getDefaultSharedPreferences(this); 
		sp.registerOnSharedPreferenceChangeListener(prefsListener);
		minDistance = Integer.parseInt(sp.getString(Constants.PREFS_ACCURACY, "500"));
		dbhelper = new DbAdapter(this);
		myCurrentAdress=TimeClock.this.getString(R.string.unknown_address);
		myCurrentNeighborHood=myCurrentAdress;
		getProjects();
	}

	private void getProjects() {
		projects = new ArrayList<String>();
		mProjects = new ArrayList<Project>();
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

	@Override
	protected void onPause() {
		unregisterLocationListener();
		super.onPause();
	}

	@Override
	protected void onResume() {
		getProjects();
		registerLocationlisteners();
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		unregisterLocationListener();
		sp.unregisterOnSharedPreferenceChangeListener(prefsListener);
		locationManager=null;
		sp=null;
		super.onDestroy();
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		initViews();
		checkClockedIn();
	}

	public void initViews() {

		btnClockIn = (Button) findViewById(R.id.btnClockIn);
		btnClockOut = (Button) findViewById(R.id.btnClockOut);
		btnListAll = (Button) findViewById(R.id.btnList);
		btnSettings = (Button) findViewById(R.id.btnSettings);
		btnExport = (Button) findViewById(R.id.btnExport);
		btnInfo = (Button) findViewById(R.id.btnInfo);
		btnCabinet = (Button) findViewById(R.id.btnProjects);
		abel =(Typeface.createFromAsset(getAssets(), "fonts/abel_regular.ttf"));

		btnClockIn.setTypeface(abel);
		btnClockOut.setTypeface(abel);
		btnListAll.setTypeface(abel);
		btnSettings.setTypeface(abel);
		btnExport.setTypeface(abel);
		btnInfo.setTypeface(abel);
		btnCabinet.setTypeface(abel);

		btnClockIn.setOnClickListener(this);
		btnClockOut.setOnClickListener(this);
		btnClockOut.setVisibility(View.GONE);
		btnListAll.setOnClickListener(this);
		btnSettings.setOnClickListener(this);
		btnExport.setOnClickListener(this);
		btnInfo.setOnClickListener(this);
		btnCabinet.setOnClickListener(this);

	}
	/**
	 * Check if user is allowed to press a button (Version checking routine has to be finished). 
	 * 
	 * @return user allowd to press a button
	 */
	public boolean allowedToPress() {
		if(cclass==null) {
			return isAllowedToPress;
		}
		if(cclass.isRunning) {
			return false;
		}
		else {
			return isAllowedToPress;
		}
	}
	/**
	 * Checks if the user has an ongoing clock in. Sets the buttons accordingly. 
	 */
	public void checkClockedIn() {
		if(sp.getBoolean(Constants.KEY_IS_CLOCKED_IN, false)) {
			btnClockOut.setVisibility(View.VISIBLE);
			btnClockIn.setVisibility(View.GONE);
		}
		else {
			btnClockOut.setVisibility(View.GONE);
			btnClockIn.setVisibility(View.VISIBLE);
		}
	}
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.btnClockIn:
			if(allowedToPress()) {
				clickedClockIn();
			}
			else {
				//buildNeedUpdateDialog(); //Disabled, no need for dialog. 
			}
			break;
		case R.id.btnProjects:
			if(allowedToPress()) {
				
				startActivity(new Intent(this,ManageProjectsActivity.class));
			}
			break;
		case R.id.btnClockOut:
			if(allowedToPress()) {
				clickedClockOut();
			}
			break;
		case R.id.btnExport:
			if(allowedToPress()) {
				Intent i2 = new Intent(this,ListActivity.class);
				i2.putExtra(Constants.KEY_SHOW_TYPE, Constants.TYPE_EXPORT);
				startActivity(i2);
			}
			break;
		case R.id.btnInfo:
			String name = null;
			try {
				name = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
			} catch (NameNotFoundException e) {
				e.printStackTrace();
				name=this.getString(R.string.version_name_unable)+e.getMessage();
			}

			final AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
			builder2.setMessage(TimeClock.this.getString(R.string.version_name)+name);
			builder2.setTitle(R.string.app_name)
			.setCancelable(true)
			.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick( final DialogInterface dialog, final int id) {
					dialog.dismiss();
				}

			});

			final AlertDialog alert2 = builder2.create();
			alert2.show();
			break;
		case R.id.btnList:
			if(allowedToPress()) {
				Intent i = new Intent(this,ListActivity.class);
				i.putExtra(Constants.KEY_SHOW_TYPE, Constants.TYPE_LIST);
				startActivity(i);
			}
			break;
		case R.id.btnSettings:
			if(allowedToPress()) {
				startActivity(new Intent(TimeClock.this,MyPrefsActivity.class));
			}
			break;
		}
	}

	/**
	 * User clicked clock in. If the user has setting to auto clock in then just clock in. Otherwise open dialog to choose time. 
	 */
	private void clickedClockIn() {
		if(sp.getBoolean(Constants.PREFS_AUTO_CLOCK_IN, false)) {
			startNewClock(true);
		}
		else {
			final Dialog dialog = new Dialog(this);
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			dialog.setContentView(R.layout.layout_clock_in);
			Button btnCancel = (Button) dialog.findViewById(R.id.btnCancel);
			Button btnNow = (Button) dialog.findViewById(R.id.btnClockInNow);
			Button btnOffset = (Button) dialog.findViewById(R.id.btnClockInOffset);
			TextView adress = (TextView) dialog.findViewById(R.id.twAdress);

			adress.setTypeface(abel);
			btnNow.setTypeface(abel);
			btnOffset.setTypeface(abel);
			btnCancel.setTypeface(abel);
			btnCancel.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					dialog.dismiss();
				}

			});
			btnNow.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					startNewClock(false);
					dialog.dismiss();
				}

			});
			btnOffset.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					startNewClock(true);
					dialog.dismiss();
				}

			});
			twAdress = (TextView) dialog.findViewById(R.id.twClockInAdress);
			twHint = (TextView) dialog.findViewById(R.id.hint);
			twHint.setTypeface(abel);
			twAdress.setTypeface(abel);
			Log.d(TAG,"nhood:"+myCurrentNeighborHood+" and:"+myCurrentAdress);
			if(!myCurrentNeighborHood.equalsIgnoreCase(myCurrentAdress)||myCurrentNeighborHood!=null) {
				twAdress.setText(myCurrentAdress+"\n"+myCurrentNeighborHood);
			}
			else {
				twAdress.setText(myCurrentAdress);
			}
			dialog.setCancelable(false);
			dialog.show();
		}
	}
	/**
	 * Starts a new clock in with an offset if boolean is true, otherwise uses the current system time. 
	 * The offset is X minutes, IE closest 15 minutes or 30, chosen by the user under settings. 
	 * 
	 * @param boolean to start new clock in with user set offset. 
	 */
	protected void startNewClock(boolean offset) {

		Calendar cal = Calendar.getInstance();
		if(offset) {
			cal = roundCalendar(cal);
		}
		String DATE_FORMAT = "dd/MM/yyyy HH:mm";
	
		if(sp.getBoolean("prefs_time_24", true)) {
			DATE_FORMAT = "dd/MM/yyyy HH:mm";
		}
		else {
			DATE_FORMAT = "dd/MM/yyyy hh:mm a";
		}
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
		String name = sdf.format(cal.getTime());
		dbhelper.open();
		long id = dbhelper.createClockIn(name, myCurrentAdress, myCurrentNeighborHood, cal.getTimeInMillis(), lastLocation);
		dbhelper.close();
		Editor edit = sp.edit();
		edit.putBoolean(Constants.KEY_IS_CLOCKED_IN,true);
		edit.putLong(Constants.KEY_CLOCKED_ID, id);
		edit.commit();
		checkClockedIn();
	}
	public Calendar roundCalendar(Calendar cal) {
		int withMod = 15;
		withMod = Integer.parseInt(sp.getString(Constants.PREFS_TIME_OFFSET, "30"));
		if(withMod==0) {
			return cal;
		}
		Log.d(TAG,"withMod:"+withMod);
		int unroundedMinutes = cal.get(Calendar.MINUTE);

		int mod = unroundedMinutes % withMod;

		cal.add(Calendar.MINUTE, withMod-mod);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal;
	}
	public long getClockOutTime() {
		Calendar cal = Calendar.getInstance();
		cal = roundCalendar(cal);
		return cal.getTimeInMillis();
	}
	/** Call a clock out. 
	 * Fetches the latest clock in and sets the clock out time. If the user has chosen to auto save it using the system clock at that time. 
	 * Otherwise it opens a dialog for the user to set the check out time. 
	 */
	private void clickedClockOut() {
		final long id = sp.getLong(Constants.KEY_CLOCKED_ID, -1);
		if(id!=-1) {

			final Dialog dialog = new Dialog(this);
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			dialog.setContentView(R.layout.edit);
			dbhelper.open();
			Cursor c = dbhelper.getClock(id);
			if(c.moveToFirst()) {
				String title = c.getString(c.getColumnIndex(DbAdapter.KEY_TITLE));
				String desc = c.getString(c.getColumnIndex(DbAdapter.KEY_DESCRIPTION));
				final long mStart = c.getLong(c.getColumnIndex(DbAdapter.KEY_START));
				final long mEnd = getClockOutTime();
				String add = c.getString(c.getColumnIndex(DbAdapter.KEY_ADRESS));
				String hood = c.getString(c.getColumnIndex(DbAdapter.KEY_NHOOD));
				final long dur = mEnd-mStart;
				c.close();
				dbhelper.close();
				/** 
				 * If the user has the setting to auto save skip the dialog and just use the time now. 
				 */
				if(sp.getBoolean(Constants.PREFS_AUTO_SAVE, false)) {
					dbhelper.open();
				
					dbhelper.updateClockIn(id,title,-1, desc ,mStart, mEnd, add, hood, null, dur, 0);
					dbhelper.close();
					SPClockOut(false);
				}
				else {
					final Spinner mSpinner = (Spinner) dialog.findViewById(R.id.edit_spinner);
					ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, projects);
					  adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
					mSpinner.setSelection(0);
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
					calStart.setTimeInMillis(mStart);
					final java.text.NumberFormat nf = new java.text.DecimalFormat("00"); 
					int hours   = (int) ((dur / 1000) / 3600);
					int minutes = (int) (((dur / 1000) / 60)-hours*60);
					mDuration.setText(this.getString(R.string.hint_duration)+nf.format(hours)+":"+nf.format(minutes));
					calEnd.setTimeInMillis(mEnd);
			
					String DATE_FORMAT = "dd/MM/yyyy HH:mm";
					final boolean show24 = sp.getBoolean("prefs_time_24", true);
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
					mHood.setTypeface(abel);
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

					mTitle.setText(title);
					mDesc.setText(desc);
					mAdress.setText(add);
					mHood.setText(hood);
					/* Button for saving the clock in. 
					 * 
					 */
					save.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							
							Project chosen = mProjects.get(mSpinner.getSelectedItemPosition());
							chosen.setCount(chosen.getCount()+1);
							mProjects.set(mSpinner.getSelectedItemPosition(), chosen);
							dbhelper.open();
							dbhelper.updateProject(chosen.getName(),chosen.getCount(), chosen.getRowId());
							dbhelper.close();
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
								inTime=mStart;
								e.printStackTrace();
							}
							long outTime;
							try {
								outTime = sdfz.parse(btnEnd.getText().toString()).getTime();
							} catch (ParseException e) {
								outTime=mEnd;
								e.printStackTrace();
							}
							if((outTime-inTime)<0) {
								Toast.makeText(TimeClock.this, R.string.end_time_smaller_than_start, Toast.LENGTH_LONG).show();

								return;
							}
							dbhelper.open();
							dbhelper.updateClockIn(id, mTitle.getText().toString(),chosen.getRowId(), mDesc.getText().toString(), inTime, outTime, mAdress.getText().toString(),mHood.getText().toString(), null, (outTime-inTime), 0);
							dbhelper.close();
							SPClockOut(false);
							dialog.dismiss();
						}
					});
					/* 
					 * Button to cancel the clock in
					 */
					cancel.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							dialog.dismiss();
						}
					});
					/*
					 * Button to delete the clock in 
					 */
					delete.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							dbhelper.open();
							dbhelper.deleteClockIn(id);
							dbhelper.close();
							SPClockOut(true);
							dialog.dismiss();
						}
					});
					/* Button to change the start time. 
					 * 
					 */
					btnStart.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {

							final Dialog d = showDateDialog(TimeClock.this,mStart);
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
										outTime=mEnd;
										e.printStackTrace();
									}
									//Update duration
									long durz = outTime-inTime;
									int mHours   = (int) ((durz / 1000) / 3600);
									int mMinutes = (int) (((durz / 1000) / 60)-mHours*60);
									mDuration.setText(TimeClock.this.getString(R.string.hint_duration)+nf.format(mHours)+":"+nf.format(mMinutes));
									d.dismiss();
								}

							});
							d.show();
						}
					});
					/* End time of clock in
					 * 
					 */
					btnEnd.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
						
							final Dialog d = showDateDialog(TimeClock.this,mEnd);
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

									Log.d(TAG,"Current hours:"+hours.getCurrentItem());
									Log.d(TAG,"Current mins:"+mins.getCurrentItem());
									Log.d(TAG,"Current day:"+day.getCurrentItem());
									Log.d(TAG,"current apm:"+ampm.getCurrentItem());
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
										inTime=mStart;
										e.printStackTrace();
									}
									//Update duration
									long durz = outTime-inTime;
									int mHours   = (int) ((durz / 1000) / 3600);
									int mMinutes = (int) (((durz / 1000) / 60)-mHours*60);
									mDuration.setText(TimeClock.this.getString(R.string.hint_duration)+nf.format(mHours)+":"+nf.format(mMinutes));
									d.dismiss();
								}

							});
							d.show();
						}
					});
					dialog.setCancelable(false);

					mSpinner.setAdapter(adapter);
					dialog.show();
				}

			}else {
				Toast.makeText(this, R.string.error_to_open_clock_in_contact, Toast.LENGTH_SHORT).show();
			}
		}



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
		NumericWheelAdapter hourAdapter = new NumericWheelAdapter(TimeClock.this, startH, nhours);
		hourAdapter.setItemResource(R.layout.wheel_text_item);
		hourAdapter.setItemTextResource(R.id.text);
		hours.setViewAdapter(hourAdapter);


		final WheelView mins = (WheelView) timeDialog.findViewById(R.id.mins);
		NumericWheelAdapter minAdapter = new NumericWheelAdapter(TimeClock.this, 0, 59, "%02d");
		minAdapter.setItemResource(R.layout.wheel_text_item);
		minAdapter.setItemTextResource(R.id.text);
		mins.setViewAdapter(minAdapter);
		mins.setCyclic(true);

		final WheelView ampm = (WheelView) timeDialog.findViewById(R.id.ampm);
		ArrayWheelAdapter<String> ampmAdapter =
			new ArrayWheelAdapter<String>(TimeClock.this, new String[] {"AM", "PM"});
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
		day.setViewAdapter(new DayArrayAdapter(TimeClock.this, calendar));      
		day.setCurrentItem(20);
		return timeDialog;
	}

	/** Functions to either delete the clock in or to save the clock in. Called at clock out
	 * 
	 * @param delete the latest clock in
	 */
	private void SPClockOut(boolean delete) {
		if(delete) {
			Toast.makeText(TimeClock.this, R.string.clocked_out_delete, Toast.LENGTH_SHORT).show();
		}
		else {
			Toast.makeText(TimeClock.this, R.string.clocked_out, Toast.LENGTH_SHORT).show();
		}
		Editor edit = sp.edit();
		edit.putBoolean(Constants.KEY_IS_CLOCKED_IN,false);
		edit.putLong(Constants.KEY_CLOCKED_ID, -1);
		edit.commit();
		checkClockedIn();
	}


	private void registerLocationlisteners() {
		if (locationManager == null) {
			return;
		}
		else {
			long min = Long.parseLong(sp.getString(Constants.PREFS_TIME,"0"));
			int type = Integer.parseInt(sp.getString(Constants.PREFS_PROVIDER,"0"));
			switch(type) {
			case 0:
				locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, min, 0,mLocationListener);
				locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, min, 0,mLocationListener);
				break;
			case 1:
				locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, min, 0,mLocationListener);
				break;
			case 2:
				locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, min, 0,mLocationListener);
				break;
			}
		}

	}
	public void unregisterLocationListener() {
		if (locationManager == null) {
			Log.e(TAG,
			"CheckerService: Do not have any location manager.");
			return;
		}
		locationManager.removeUpdates(mLocationListener);
		locationManager.removeUpdates(mLocationListener);
		Log.d(TAG,
		"Location listener now unregistered w/ CheckerService.");
	}

	private LocationListener  mLocationListener = new  LocationListener(){

		@Override
		public void onLocationChanged(Location inLoc) {
			if(inLoc.getAccuracy()<minDistance) {
				makeUseOfLocation(inLoc);
			}
			if(inLoc.getProvider().equals(LocationManager.GPS_PROVIDER)) {
				if(twHint!=null) {
					twHint.setText(R.string.hint_did_you_know_settings);
				}
			} 
		}
		@Override
		public void onProviderDisabled(String provider) {
			if(provider.equals(LocationManager.GPS_PROVIDER)) {
				if(twHint!=null) {
					twHint.setText(R.string.hint_gps_on);
				}
			}
			else {

			}
		}

		@Override
		public void onProviderEnabled(String provider) {
			if(provider.equals(LocationManager.GPS_PROVIDER)) {
				if(twHint!=null) {
					twHint.setText(R.string.hint_gps_fix);
				}
			}
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			if(provider.equals(LocationManager.GPS_PROVIDER)) {
				if(twHint!=null) {
					twHint.setText(R.string.hint_gps_fix);
				}
			} else { 
				if(twHint!=null) {

					twHint.setText(R.string.hint_did_you_know_settings);
				}
			}
		}

	};
	/**
	 * Fetches the adress 
	 * @param inLoc
	 */
	protected void makeUseOfLocation(Location inLoc) {
		lastLocation = inLoc;

		if(sp.getLong(Constants.KEY_LATEST_ADRESSTHREAD, -1)==-1) {
			sp.edit().putLong(Constants.KEY_LATEST_ADRESSTHREAD, inLoc.getTime()).commit();
			if(getAdressThread!=null) {
				try{
					getAdressThread.stop();
				} catch(Exception e) {
					e.printStackTrace();
				}
				getAdressThread = new GetAdressThread(inLoc);
				getAdressThread.start();
			}
			else {
				getAdressThread = new GetAdressThread(inLoc);
				getAdressThread.start();
			}
		}
		else if(inLoc.getTime()-sp.getLong(Constants.KEY_LATEST_ADRESSTHREAD, -1)>5000) {
			sp.edit().putLong(Constants.KEY_LATEST_ADRESSTHREAD, inLoc.getTime()).commit();
			if(getAdressThread!=null) {
				try{
					getAdressThread.stop();
				} catch(Exception e) {
					e.printStackTrace();
				}
				getAdressThread = new GetAdressThread(inLoc);
				getAdressThread.start();
			}
			else {
				getAdressThread = new GetAdressThread(inLoc);
				getAdressThread.start();
			}
		}
		else {
			//Too soon.
		}

	}


	private OnSharedPreferenceChangeListener  prefsListener = new  OnSharedPreferenceChangeListener(){

		@Override
		public void onSharedPreferenceChanged(
				SharedPreferences sharedPreferences, String key) {
			if(key.equalsIgnoreCase(Constants.PREFS_PROVIDER)) {
				unregisterLocationListener();
				registerLocationlisteners();
			}
			else if(key.equalsIgnoreCase(Constants.PREFS_TIME)) {
				unregisterLocationListener();
				registerLocationlisteners();
			}
			else if(key.equalsIgnoreCase(Constants.PREFS_ACCURACY)) {
				minDistance = Integer.parseInt(sharedPreferences.getString(Constants.PREFS_ACCURACY, "500"));
			}
			sp = sharedPreferences;
		}



	};

	/** Thread to fetch adresses from yahoo 
	 * 
	 * @author david
	 *
	 */
	private class GetAdressThread extends Thread{

		private Location myLoc;

		public GetAdressThread(Location inloc) {	
			myLoc = inloc;
			Log.d(TAG,"new getAdressThread");

		}
		@Override
		public void run() {
			YahooResult result = new YahooResult();
			DataManager dm = new DataManager();
			Log.d(TAG,"getAdressThreadRun");
			try {
				result = dm.getAdressFromLocation(myLoc);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			Log.d(TAG,"Size: "+result.getFound());
			if(result.getFound()>0) {
				mHandler.post(new myRunnable(result));
			}
			else {
				mHandler.post(new myRunnable(null));
			}
		}
	}
	/** Runnable to display the new adress .
	 * 
	 * @author david
	 *
	 */
	private class myRunnable implements Runnable {
		private YahooResult yahoo;
		public myRunnable(YahooResult in) {
			yahoo=in;
		}
		@Override
		public void run() {
			if(yahoo!=null) {
				if(yahoo.getLine1().length()>0) {
					myCurrentAdress=yahoo.getLine1();
					if(yahoo.getNeighborhood().length()>0){
						myCurrentNeighborHood=yahoo.getNeighborhood();
					}
					else if(yahoo.getCity().length()>0) {
						myCurrentNeighborHood=yahoo.getCity();
					}
					else {
						myCurrentNeighborHood=null;
					}
				}
				else if(yahoo.getStreet().length()>0) {
					myCurrentAdress=yahoo.getStreet();
					myCurrentAdress=yahoo.getLine1();
					if(yahoo.getNeighborhood().length()>0){
						myCurrentNeighborHood=yahoo.getNeighborhood();
					}
					else if(yahoo.getCity().length()>0) {
						myCurrentNeighborHood=yahoo.getCity();
					}
					else {
						myCurrentNeighborHood=null;
					}
				}
				else if(yahoo.getNeighborhood().length()>0){
					myCurrentAdress=yahoo.getNeighborhood();
					myCurrentNeighborHood=null;
				}
				else if(yahoo.getCity().length()>0) {
					myCurrentAdress=yahoo.getCity();
					myCurrentNeighborHood=null;
				}
				else if(yahoo.getCountry().length()>0) {
					myCurrentAdress=yahoo.getCountry();
					myCurrentNeighborHood=null;
				}
				else {
					myCurrentAdress=TimeClock.this.getString(R.string.unknown_address);
					myCurrentNeighborHood=TimeClock.this.getString(R.string.unknown_address);

				}
			}
			else {
				myCurrentAdress=TimeClock.this.getString(R.string.unknown_address);
			}
			if(twAdress!=null) {
				twAdress.setText(myCurrentAdress);
			}
		}

	}

/**
 * App version checking class. Used in the beginning of development to force users to upgrade since many users don't. 
 * @author david
 *
 */
	private class CheckAppVersionClass extends AsyncTask<String, String, AppInfo> {
		private boolean isRunning;
		public CheckAppVersionClass() {

		}


		@Override
		protected AppInfo doInBackground(String... params) {
			isRunning = true;
			AppInfo result = new AppInfo();
			DataManager dm = new DataManager();
			try {
				result = dm.getAppInfo();
			} catch (ClientProtocolException e) {
				result.setFailed(true);
				result.setFailedReason(e.toString());
				e.printStackTrace();
			} catch (JSONException e) {
				result.setFailed(true);
				result.setFailedReason(e.toString());
				e.printStackTrace();
			} catch (IOException e) {
				result.setFailed(true);
				result.setFailedReason(e.toString());
				e.printStackTrace();
			}
			return result;
		}


		@Override
		protected void onPostExecute(AppInfo result) {
			isRunning=false;
			super.onPostExecute(result);
			stopPbDialog(DIALOG_CHECK_VERSION);
			if(result!=null) {
				String name = null;
				int number = 0;

				try {
					name = TimeClock.this.getPackageManager().getPackageInfo(TimeClock.this.getPackageName(), 0).versionName;
					number = TimeClock.this.getPackageManager().getPackageInfo(TimeClock.this.getPackageName(), 0).versionCode;
				} catch (NameNotFoundException e) {
					e.printStackTrace();
					isAllowedToPress=false;
					buildNeedUpdateDialog();
				}
				if(name==null&&number==0) {
					isAllowedToPress=false;
					buildNeedUpdateDialog();
				}
				else {
					if(name.equalsIgnoreCase(result.getCurrentVersion())&&number==result.getCurrentNumber()) {
						isAllowedToPress=true;
						return;
					}
					else { 
						if(result.isStrict()) {
							isAllowedToPress=false;
							buildNeedUpdateDialog();
							return;
						}
						else {
							ArrayList<OldVersion> lol = result.getOlds();
							for(int i=0;i<lol.size();i++) {
								OldVersion tmp = lol.get(i);
								if(tmp.getCurrentNumber()==number&&tmp.getCurrentVersion().equalsIgnoreCase(name)) {
									if(tmp.isIsAllowed()) {
										isAllowedToPress=true;
										return;
									}
									else {
										isAllowedToPress=false;
										buildNeedUpdateDialog();
										return;
									}
								}
								else {
									isAllowedToPress=false;
									buildNeedUpdateDialog();
								}
							}
						}
					}
				}
			}
			else {
				isAllowedToPress=false;
				buildNeedUpdateDialog();
			}


		}


		@Override
		protected void onPreExecute() {
			showPbDialog(DIALOG_CHECK_VERSION);

			super.onPreExecute();
		}
	}
	private void buildNeedUpdateDialog() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("App version is too old. You have to update or you cant use the app.")
		.setCancelable(false)
		.setPositiveButton("Download", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, final int id) {
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.dxapps.se/beta/TimeClock.apk"));
				startActivity(browserIntent);
			}


		})
		.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, final int id) {
				dialog.cancel();
				finish();
			}
		});
		if(!isFinishing()) {
			final AlertDialog alert = builder.create();
			alert.show();
		}
	}
	ProgressDialog mDialogApp;
	void showPbDialog(int id) {
		switch (id) {

		case DIALOG_CHECK_VERSION:

			mDialogApp = new ProgressDialog(this);
			mDialogApp.setTitle("Please wait");
			mDialogApp.setMessage("Checking app version");
			if(!isFinishing()) 
				mDialogApp.show();


			break;
		}
	}
	void stopPbDialog(int id) {
		switch (id) {

		case DIALOG_CHECK_VERSION:
			if(mDialogApp!=null) {
				if(!isFinishing()) {
					try {
						mDialogApp.dismiss();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					}
				}

			}



			break;
		}
	}
}