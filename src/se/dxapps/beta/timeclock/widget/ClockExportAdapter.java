package se.dxapps.beta.timeclock.widget;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import se.dxapps.timeclock.R;
import se.dxapps.beta.timeclock.data.ClockIn;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
/**
 * Adapter to show a list of clock ins for user to select if he/she wants to export them or not. 
 * @author david
 *
 */
public class ClockExportAdapter extends ArrayAdapter<ClockIn>{
	private LayoutInflater mInflater;
	private Typeface abel;
	private Typeface coust;
	private Calendar cal;
	private SimpleDateFormat sdf;
	private java.text.NumberFormat nf;

	ArrayList<ClockIn> myList;
	public ClockExportAdapter(Context context, int textViewResourceId,	ArrayList<ClockIn> objects,boolean uses24) {
		super(context, textViewResourceId, objects);
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		abel =(Typeface.createFromAsset(context.getAssets(), "fonts/abel_regular.ttf"));
		coust =(Typeface.createFromAsset(context.getAssets(), "fonts/coustard_reg.ttf"));
		cal = Calendar.getInstance();
		myList = objects;
		String DATE_FORMAT;
		if(uses24) {
			DATE_FORMAT = "HH:mm";
		}
		else {
			DATE_FORMAT = "hh:mm a";
		}
		nf = new java.text.DecimalFormat("00"); 

		sdf = new SimpleDateFormat(DATE_FORMAT);
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder holder;
		ClockIn cl = myList.get(position);

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.list_item_export, null);
			holder = new ViewHolder();
			TextView start = (TextView) convertView.findViewById(R.id.item_tw_start);
			TextView end = (TextView) convertView.findViewById(R.id.item_tw_end);
			TextView dur = (TextView) convertView.findViewById(R.id.item_tw_duration);
			dur.setTypeface(coust);
			start.setTypeface(coust);
			end.setTypeface(coust);
			holder.header=(TextView) convertView.findViewById(R.id.item_header);
			holder.adress = (TextView) convertView.findViewById(R.id.item_adress);
			holder.start = (TextView) convertView.findViewById(R.id.item_start);
			holder.end=(TextView) convertView.findViewById(R.id.item_end);
			holder.duration=(TextView) convertView.findViewById(R.id.item_duration);
			holder.checkbox=(CheckBox) convertView.findViewById(R.id.item_box);
			holder.duration.setTypeface(abel);
			holder.start.setTypeface(abel);
			holder.end.setTypeface(abel);
			holder.header.setTypeface(coust);
			holder.adress.setTypeface(abel);
			holder.checkbox.bringToFront();
			convertView.setTag(holder);
		} else {

			holder = (ViewHolder) convertView.getTag();
		}
		//BIND
		holder.header.setText(cl.getTitle());
		holder.adress.setText(cl.getAdress());
		cal.setTimeInMillis(cl.getStartTime());
		holder.start.setText(sdf.format(cal.getTime()));
		cal.setTimeInMillis(cl.getEndTime());
		holder.end.setText(sdf.format(cal.getTime()));
		int hoursz   = (int) ((cl.getDuration() / 1000) / 3600);
		int minutesz = (int) (((cl.getDuration() / 1000) / 60)-hoursz*60);
		holder.duration.setText(nf.format(hoursz)+":"+nf.format(minutesz));
		holder.checkbox.setChecked(cl.isExport());

		return convertView;
	}



	static class ViewHolder {
		TextView header;
		TextView duration;
		CheckBox checkbox;
		TextView adress;
		TextView start;
		TextView end;


	}
}
