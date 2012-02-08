package se.dxapps.beta.timeclock.widget;

import java.util.ArrayList;

import se.dxapps.beta.timeclock.data.Project;
import se.dxapps.timeclock.R;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
/** Adapter to display the projects and number of clock ins in each project. 
 * @author david
 *
 */
public class ProjectsAdapter extends ArrayAdapter<Project>{
	private LayoutInflater mInflater;
	private Typeface abel;
	private Typeface coust;

	ArrayList<Project> myList;
	public ProjectsAdapter(Context context, int textViewResourceId,	ArrayList<Project> objects) {
		super(context, textViewResourceId, objects);
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		abel =(Typeface.createFromAsset(context.getAssets(), "fonts/abel_regular.ttf"));
		coust =(Typeface.createFromAsset(context.getAssets(), "fonts/coustard_reg.ttf"));

		myList = objects;


	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder holder;
		Project cl = myList.get(position);

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.list_item_project, null);

			holder = new ViewHolder();

			holder.header=(TextView) convertView.findViewById(R.id.item_header);
			holder.counter = (TextView) convertView.findViewById(R.id.item_counter);

			holder.counter.setTypeface(abel);

			holder.header.setTypeface(coust);



			convertView.setTag(holder);
		} else {

			holder = (ViewHolder) convertView.getTag();
		}
		//BIND
		holder.header.setText(cl.getName());
		holder.counter.setText("("+cl.getCount()+")");



		return convertView;
	}



	static class ViewHolder {
		TextView header;

		TextView counter;


	}
}
