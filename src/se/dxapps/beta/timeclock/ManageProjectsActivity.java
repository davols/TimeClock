package se.dxapps.beta.timeclock;

import java.util.ArrayList;

import se.dxapps.beta.timeclock.data.Project;
import se.dxapps.beta.timeclock.provider.DbAdapter;
import se.dxapps.beta.timeclock.widget.ProjectsAdapter;
import se.dxapps.timeclock.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
/**
 * Manages the projects the user has. 
 * @author david
 *
 */
public class ManageProjectsActivity extends Activity implements OnItemClickListener{
	private ArrayList<Project> mList;
	/**
	 * Linearlayout (root) of progressbar. 
	 */
	private LinearLayout pbar;
	private ListView listview;
	private TextView twEmpty;
	private DbAdapter dbhelper;
	private Typeface coust;
	private ProjectsAdapter mAdapter;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.manage_projects);
		mList = new ArrayList<Project>();
		dbhelper = new DbAdapter(this);
		coust =(Typeface.createFromAsset(getAssets(), "fonts/coustard_reg.ttf"));
	}
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		initViews();

		new fetchProjectsClass(dbhelper).execute(0);
		//Add header view. 
		LayoutInflater inflater = getLayoutInflater();
		View v = inflater.inflate(R.layout.list_item_add, null);
		TextView tw = (TextView)  v.findViewById(R.id.item_header);
		tw.setTypeface(coust);
		listview.addHeaderView(v);
		listview.setOnItemClickListener(this);
	}
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		if(arg2==0) {
			final AlertDialog.Builder alert = new AlertDialog.Builder(this);
			final EditText input = new EditText(this);

			input.setHint(R.string.new_project);
			alert.setView(input);
			alert.setPositiveButton("Save", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int whichButton) {
					String value = input.getText().toString().trim();
					if(value.length()>0) {
						dbhelper.open();
						long id = dbhelper.createProject(value);
						Project tmp = new Project();
						tmp.setName(value);
						tmp.setRowId(id);
						tmp.setCount(0);

						mList.add(tmp);
						mAdapter.notifyDataSetChanged();
						dbhelper.close();
					}
					else {
						Toast.makeText(ManageProjectsActivity.this, R.string.rename_value, Toast.LENGTH_SHORT).show();
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
			alert.setTitle(R.string.new_project);
			alert.show();
		}
	}

	public ListAdapter createAdapter() {
		mAdapter = new ProjectsAdapter(this,0,mList);
		return mAdapter;
	}
	public void initViews() {
		pbar = (LinearLayout) findViewById(R.id.root_pb);
		pbar.setVisibility(View.GONE);
		listview = (ListView) findViewById(R.id.listview);
		listview.setCacheColorHint(0);
		twEmpty = (TextView) findViewById(R.id.tw_empty);
		twEmpty.setTypeface(coust);
		twEmpty.setVisibility(View.GONE);

		registerForContextMenu(listview);

	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		int menuItemIndex = item.getItemId();
		if(info.position>0) {
			if(menuItemIndex==0) {
				//Edit.
				editProject(mList.get((info.position-1)),(info.position-1));
			}
			else if(menuItemIndex==1) {
				deleteProject(mList.get(info.position-1),(info.position-1));
			}
		}
		return true;
	}

	private void editProject(final Project project, final int position) {
		final AlertDialog.Builder alert = new AlertDialog.Builder(this);
		final EditText input = new EditText(this);

		input.setText(project.getName());
		alert.setView(input);
		alert.setPositiveButton("Save", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				String value = input.getText().toString().trim();
				if(value.length()>0) {
					dbhelper.open();
					if(dbhelper.updateProject(value, project.getCount(), project.getRowId())) {
						Toast.makeText(ManageProjectsActivity.this, R.string.renamed_completed, Toast.LENGTH_SHORT).show();
					}
					else {
						Toast.makeText(ManageProjectsActivity.this, R.string.failed_rename_project, Toast.LENGTH_SHORT).show();
					}
					Project tmp = new Project();
					tmp.setName(value);
					tmp.setRowId(project.getRowId());
					tmp.setCount(project.getCount());
					mList.set(position, tmp);
					mAdapter.notifyDataSetChanged();
					dbhelper.close();
				}
				else {
					Toast.makeText(ManageProjectsActivity.this, R.string.rename_value, Toast.LENGTH_SHORT).show();
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
		alert.setTitle(R.string.rename);
		alert.show();
	}
	private void deleteProject(Project project,int id) {
		Log.d("del","Delete id:"+id);
		dbhelper.open();
		if(dbhelper.deleteProject(project.getRowId())) {
			Toast.makeText(ManageProjectsActivity.this, R.string.deletion_completed, Toast.LENGTH_SHORT).show();
			mList.remove(id);
			mAdapter.notifyDataSetChanged();
		}
		else {
			Toast.makeText(ManageProjectsActivity.this, R.string.deletion_failed, Toast.LENGTH_SHORT).show();
		}
		dbhelper.close();
	}
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		if (v.getId()==R.id.listview) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
			if(info.position>0) {
				menu.setHeaderTitle(mList.get(info.position-1).getName());
				String[] menuItems = getResources().getStringArray(R.array.item_project_context_menu);
				for (int i = 0; i<menuItems.length; i++) {
					menu.add(Menu.NONE, i, i, menuItems[i]);
				}
			}
		}
	}

	private class fetchProjectsClass extends AsyncTask<Integer, String, ArrayList<Project>> {
		private DbAdapter dbadapter;
		public fetchProjectsClass(DbAdapter in) {
			dbadapter=in;
		}
		@Override
		protected ArrayList<Project> doInBackground(Integer... params) {
			ArrayList<Project> list = new ArrayList<Project>();

			dbadapter.open();
			Cursor c = dbadapter.getAllProjects();
			Project tmp = new Project();
			if(c.moveToFirst()) {
				do
				{
					tmp=new Project();
					tmp.setRowId(c.getLong(c.getColumnIndex(DbAdapter.ROW_ID)));
					tmp.setName(c.getString(c.getColumnIndex(DbAdapter.KEY_PROJECTNAME)));
					tmp.setCount(c.getInt(c.getColumnIndex(DbAdapter.KEY_COUNTER)));

					list.add(tmp);

				}while(c.moveToNext());

			}
			c.close();
			dbadapter.close();
			return list;
		}
		@Override
		protected void onPostExecute(ArrayList<Project> result) {
			super.onPostExecute(result);
			listview.setVisibility(View.VISIBLE);
			pbar.setVisibility(View.GONE);
			mList=result;
			listview.setAdapter(createAdapter());

		}
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			listview.setVisibility(View.GONE);
			pbar.setVisibility(View.VISIBLE);
			twEmpty.setVisibility(View.GONE);
		}
	}


}
