package se.dxapps.beta.timeclock.widget;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import se.dxapps.timeclock.R;
import kankan.wheel.widget.adapters.AbstractWheelTextAdapter;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
/**
 * Day array adapter to show the selected days for an clock in. 
 * Used by wheel
 *
 */
public class DayArrayAdapter  extends AbstractWheelTextAdapter {
	// Count of days to be shown
	public final int daysCount = 20;

	// Calendar
	Calendar calendar;

	/**
	 * Constructor
	 */
	public DayArrayAdapter(Context context, Calendar calendar) {
		super(context, R.layout.time2_day, NO_RESOURCE);
		this.calendar = calendar;

		setItemTextResource(R.id.time2_monthday);
	}

	@Override
	public View getItem(int index, View cachedView, ViewGroup parent) {
		int day = -daysCount + index;
		Calendar newCalendar = (Calendar) calendar.clone();
		newCalendar.roll(Calendar.DAY_OF_YEAR, day);

		View view = super.getItem(index, cachedView, parent);
		TextView weekday = (TextView) view.findViewById(R.id.time2_weekday);
		if (day == 0) {
			weekday.setText("");
		} else {
			DateFormat format = new SimpleDateFormat("EEE");
			weekday.setText(format.format(newCalendar.getTime()));
		}

		TextView monthday = (TextView) view.findViewById(R.id.time2_monthday);
		if (day == 0) {
			monthday.setText(R.string.today);
			monthday.setTextColor(R.color.buttonTextColor);
		} else {
			DateFormat format = new SimpleDateFormat("MMM d");
			monthday.setText(format.format(newCalendar.getTime()));
			monthday.setTextColor(0xFF111111);
		}

		return view;
	}

	@Override
	public int getItemsCount() {
		return daysCount + 1;
	}

	@Override
	protected CharSequence getItemText(int index) {
		return "";
	}

}
