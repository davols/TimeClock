package se.dxapps.beta.timeclock.provider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;
import se.dxapps.timeclock.R;
import se.dxapps.beta.timeclock.data.ClockIn;
import se.dxapps.beta.timeclock.data.ExportSetting;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

/**Writer to write the exported clock ins to user chosen format. 
 * 
 * @author david
 *
 */
public class MyWriter {
	public final String TAG="MyWriter";
	public static final String PATH="TimeClock/";
	public MyWriter() {

	}
	public String write(Context ctx,ArrayList<ClockIn> list,String sFileName,ExportSetting setting) throws IOException,Exception {
		Log.d(TAG,"setting."+setting.getFileFormat());
		if(setting.getFileFormat().equalsIgnoreCase("csv")) {
			//generateCsvFile(ctx,sFileName,setting,list);
			return generateCSVFile(ctx,sFileName,setting,list);
		}
		else if(setting.getFileFormat().equalsIgnoreCase("excel(2003)")) {
			return generateXLSFile(ctx,sFileName,setting,list);
		}
		else if(setting.getFileFormat().equalsIgnoreCase("excel(97)")) {
			return generateXLSFile(ctx,sFileName,setting,list);
		}
		else if(setting.getFileFormat().equalsIgnoreCase("xml")) {
			return generateXMLFile(ctx,sFileName,setting,list);
		}
		else return null;
	}

	private String generateCSVFile(Context ctx, String sFileName,
			ExportSetting setting, ArrayList<ClockIn> list) throws IOException, RowsExceededException, WriteException, BiffException {
		File root = Environment.getExternalStorageDirectory();
		File folder = new File(root,PATH);

		if(!folder.exists()) {
			folder.mkdir();
		}

		File mWork = new File(folder, sFileName+".xls");
		WritableWorkbook workbook = Workbook.createWorkbook( mWork);


		WritableSheet sheet = workbook.createSheet("First Sheet", 0);
		//Generate the labels 
		int start=0;
		String clockFormat;
		if(setting.isT24Clock()) {
			clockFormat = "HH:mm";
		}
		else {
			clockFormat = "hh:mm a";
		}
		String DATE_FORMAT = "dd/MM/yyyy";
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
		SimpleDateFormat cdf = new SimpleDateFormat(clockFormat);
		java.text.NumberFormat nf = new java.text.DecimalFormat("00"); 
		Calendar cal = Calendar.getInstance();
		if(setting.isKeepName()) {
			Label label = new Label(start, 0, ctx.getString(R.string.export_header_name)); 
			sheet.addCell(label); 
			start=start+1;

		}
		if(setting.isKeepProject()) {
			Label label = new Label(start, 0, ctx.getString(R.string.export_header_project)); 
			sheet.addCell(label); 
			start=start+1;

		}
		if(setting.isKeepClockInDay()) {
			Label label = new Label(start, 0, ctx.getString(R.string.export_header_clock_in_date)); 
			sheet.addCell(label); 
			start=start+1;

		}
		if(setting.isKeepClockInTime()) {
			Label label = new Label(start, 0, ctx.getString(R.string.export_header_clock_in_time)); 
			sheet.addCell(label); 
			start=start+1;
		}
		if(setting.isKeepClockOutDay()) {
			Label label = new Label(start, 0, ctx.getString(R.string.export_header_clock_out_date)); 
			sheet.addCell(label); 
			start=start+1;

		}
		if(setting.isKeepClockOutTime()) {
			Label label = new Label(start, 0, ctx.getString(R.string.export_header_clock_out_time)); 
			sheet.addCell(label); 
			start=start+1;

		}
		if(setting.isKeepAdress()) {
			Label label = new Label(start, 0, ctx.getString(R.string.export_header_adress)); 
			sheet.addCell(label); 
			start=start+1;

		}
		if(setting.isKeepAdress()) {
			Label label = new Label(start, 0, ctx.getString(R.string.export_header_neighborhood)); 
			sheet.addCell(label); 
			start=start+1;

		}
		if(setting.isKeepDuration()) {
			Label label = new Label(start, 0, ctx.getString(R.string.export_header_duration)); 
			sheet.addCell(label); 
			start=start+1;

		}
		if(setting.isKeepDescription()) {
			Label label = new Label(start, 0, ctx.getString(R.string.export_header_description)); 
			sheet.addCell(label); 
			start=start+1;
		}
		long totalDuration = 0;
		int allExported=0;
		for(int i=0;i<list.size();i++) {

			Log.d(TAG,"id:"+i);
			ClockIn tmp = list.get(i);
			if(tmp.isExport()) {
				allExported=allExported+1;
				totalDuration = totalDuration+tmp.getDuration();
				//EXPORT
				start=0;
				if(setting.isKeepName()) {
					Label label = new Label(start, (i+1), tmp.getTitle()); 
					sheet.addCell(label); 
					start=start+1;
				}
				if(setting.isKeepProject()) {
					Label label = new Label(start, (i+1), tmp.getProjectName()); 
					sheet.addCell(label); 
					start=start+1;
				}
				if(setting.isKeepClockInDay()) {
					cal.setTimeInMillis(tmp.getStartTime());
					Label label = new Label(start, (i+1), sdf.format(cal.getTime())); 
					sheet.addCell(label); 
					start=start+1;


				}
				if(setting.isKeepClockInTime()) {
					cal.setTimeInMillis(tmp.getStartTime());

					Label label = new Label(start, (i+1), cdf.format(cal.getTime())); 
					sheet.addCell(label); 
					start=start+1;

				}
				if(setting.isKeepClockOutDay()) {
					cal.setTimeInMillis(tmp.getEndTime());
					Label label = new Label(start, (i+1), sdf.format(cal.getTime())); 
					sheet.addCell(label); 
					start=start+1;


				}
				if(setting.isKeepClockOutTime()) {
					cal.setTimeInMillis(tmp.getEndTime());

					Label label = new Label(start, (i+1), cdf.format(cal.getTime())); 
					sheet.addCell(label); 
					start=start+1;

				}
				if(setting.isKeepAdress()) {
					Label label = new Label(start, (i+1),tmp.getAdress()); 
					sheet.addCell(label); 
					start=start+1;

				}
				if(setting.isKeepNeighborhood()) {
					Label label = new Label(start, (i+1),tmp.getHood()); 
					sheet.addCell(label); 
					start=start+1;

				}
				if(setting.isKeepDuration()) {
					long dur = tmp.getDuration();
					int hoursz   = (int) ((dur / 1000) / 3600);
					int minutesz = (int) (((dur / 1000) / 60)-hoursz*60);
					Label label = new Label(start, (i+1), nf.format(hoursz)+":"+nf.format(minutesz)); 
					sheet.addCell(label); 
					start=start+1;

				}
				if(setting.isKeepDescription()) {
					Label label = new Label(start,(i+1),tmp.getDescription()); 
					sheet.addCell(label); 
					start=start+1;

				}
			}
			else {
				//Null
			}
		}
		//Insert total time. 
		if(setting.isKeepTotalDuration()) {
			Label label = new Label(0,(allExported+1),ctx.getString(R.string.export_header_total_time)); 
			sheet.addCell(label);

			int hoursz   = (int) ((totalDuration / 1000) / 3600);
			int minutesz = (int) (((totalDuration / 1000) / 60)-hoursz*60);
			label = new Label(1, (allExported+1), nf.format(hoursz)+":"+nf.format(minutesz)); 

			sheet.addCell(label);


		}

		workbook.write(); 
		workbook.close();
		File csvFile = new File(folder,sFileName+".csv");
		FileOutputStream out = new FileOutputStream(csvFile);
		se.dxapps.beta.timeclock.provider.CSV  c= new se.dxapps.beta.timeclock.provider.CSV(Workbook.getWorkbook(mWork),out,null,true); 

		out.flush();
		out.close();
		mWork.delete();
		return csvFile.getAbsolutePath();
	}
	private String generateXMLFile(Context ctx, String sFileName,
			ExportSetting setting, ArrayList<ClockIn> list) throws IOException, RowsExceededException, WriteException, BiffException {
		File root = Environment.getExternalStorageDirectory();
		File folder = new File(root,PATH);
		Log.d(TAG,"XML!");
		if(!folder.exists()) {
			folder.mkdir();
		}
		File mWork = new File(folder, sFileName+".xls");
		WritableWorkbook workbook = Workbook.createWorkbook( mWork);


		WritableSheet sheet = workbook.createSheet("First Sheet", 0);
		//Generate the labels 
		int start=0;
		String clockFormat;
		if(setting.isT24Clock()) {
			clockFormat = "HH:mm";
		}
		else {
			clockFormat = "hh:mm a";
		}
		String DATE_FORMAT = "dd/MM/yyyy";
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
		SimpleDateFormat cdf = new SimpleDateFormat(clockFormat);
		java.text.NumberFormat nf = new java.text.DecimalFormat("00"); 
		Calendar cal = Calendar.getInstance();
		if(setting.isKeepName()) {
			Label label = new Label(start, 0, ctx.getString(R.string.export_header_name)); 
			sheet.addCell(label); 
			start=start+1;

		}
		if(setting.isKeepProject()) {
			Label label = new Label(start, 0, ctx.getString(R.string.export_header_project)); 
			sheet.addCell(label); 
			start=start+1;

		}
		if(setting.isKeepClockInDay()) {
			Label label = new Label(start, 0, ctx.getString(R.string.export_header_clock_in_date)); 
			sheet.addCell(label); 
			start=start+1;

		}
		if(setting.isKeepClockInTime()) {
			Label label = new Label(start, 0, ctx.getString(R.string.export_header_clock_in_time)); 
			sheet.addCell(label); 
			start=start+1;
		}
		if(setting.isKeepClockOutDay()) {
			Label label = new Label(start, 0, ctx.getString(R.string.export_header_clock_out_date)); 
			sheet.addCell(label); 
			start=start+1;

		}
		if(setting.isKeepClockOutTime()) {
			Label label = new Label(start, 0, ctx.getString(R.string.export_header_clock_out_time)); 
			sheet.addCell(label); 
			start=start+1;

		}
		if(setting.isKeepAdress()) {
			Label label = new Label(start, 0, ctx.getString(R.string.export_header_adress)); 
			sheet.addCell(label); 
			start=start+1;

		}
		if(setting.isKeepAdress()) {
			Label label = new Label(start, 0, ctx.getString(R.string.export_header_neighborhood)); 
			sheet.addCell(label); 
			start=start+1;

		}
		if(setting.isKeepDuration()) {
			Label label = new Label(start, 0, ctx.getString(R.string.export_header_duration)); 
			sheet.addCell(label); 
			start=start+1;

		}
		if(setting.isKeepDescription()) {
			Label label = new Label(start, 0, ctx.getString(R.string.export_header_description)); 
			sheet.addCell(label); 
			start=start+1;
		}
		long totalDuration = 0;
		int allExported=0;
		for(int i=0;i<list.size();i++) {

			Log.d(TAG,"id:"+i);
			ClockIn tmp = list.get(i);
			if(tmp.isExport()) {
				allExported=allExported+1;
				totalDuration = totalDuration+tmp.getDuration();
				//EXPORT
				start=0;
				if(setting.isKeepName()) {
					Label label = new Label(start, (i+1), tmp.getTitle()); 
					sheet.addCell(label); 
					start=start+1;
				}
				if(setting.isKeepProject()) {
					Label label = new Label(start, (i+1), tmp.getProjectName()); 
					sheet.addCell(label); 
					start=start+1;
				}
				if(setting.isKeepClockInDay()) {
					cal.setTimeInMillis(tmp.getStartTime());
					Label label = new Label(start, (i+1), sdf.format(cal.getTime())); 
					sheet.addCell(label); 
					start=start+1;


				}
				if(setting.isKeepClockInTime()) {
					cal.setTimeInMillis(tmp.getStartTime());

					Label label = new Label(start, (i+1), cdf.format(cal.getTime())); 
					sheet.addCell(label); 
					start=start+1;

				}
				if(setting.isKeepClockOutDay()) {
					cal.setTimeInMillis(tmp.getEndTime());
					Label label = new Label(start, (i+1), sdf.format(cal.getTime())); 
					sheet.addCell(label); 
					start=start+1;


				}
				if(setting.isKeepClockOutTime()) {
					cal.setTimeInMillis(tmp.getEndTime());

					Label label = new Label(start, (i+1), cdf.format(cal.getTime())); 
					sheet.addCell(label); 
					start=start+1;

				}
				if(setting.isKeepAdress()) {
					Label label = new Label(start, (i+1),tmp.getAdress()); 
					sheet.addCell(label); 
					start=start+1;

				}
				if(setting.isKeepNeighborhood()) {
					Label label = new Label(start, (i+1),tmp.getHood()); 
					sheet.addCell(label); 
					start=start+1;

				}
				if(setting.isKeepDuration()) {
					long dur = tmp.getDuration();
					int hoursz   = (int) ((dur / 1000) / 3600);
					int minutesz = (int) (((dur / 1000) / 60)-hoursz*60);
					Label label = new Label(start, (i+1), nf.format(hoursz)+":"+nf.format(minutesz)); 
					sheet.addCell(label); 
					start=start+1;

				}
				if(setting.isKeepDescription()) {
					Label label = new Label(start,(i+1),tmp.getDescription()); 
					sheet.addCell(label); 
					start=start+1;

				}
			}
			else {
				//Null
			}
		}
		//Insert total time. 
		if(setting.isKeepTotalDuration()) {
			Label label = new Label(0,(allExported+1),ctx.getString(R.string.export_header_total_time)); 
			sheet.addCell(label);

			int hoursz   = (int) ((totalDuration / 1000) / 3600);
			int minutesz = (int) (((totalDuration / 1000) / 60)-hoursz*60);
			label = new Label(1, (allExported+1), nf.format(hoursz)+":"+nf.format(minutesz)); 

			sheet.addCell(label);


		}

		workbook.write(); 
		workbook.close();
		File xmlFile = new File(folder,sFileName+".xml");
		FileOutputStream out = new FileOutputStream(xmlFile);
		se.dxapps.beta.timeclock.provider.XML  c= new se.dxapps.beta.timeclock.provider.XML(Workbook.getWorkbook(mWork),out,null,false); 
		out.flush();
		out.close();
		mWork.delete();
		return xmlFile.getAbsolutePath();
	}
	private String generateXLSFile(Context ctx, String sFileName,
			ExportSetting setting, ArrayList<ClockIn> list) throws IOException, RowsExceededException, WriteException, BiffException {
		File root = Environment.getExternalStorageDirectory();
		File folder = new File(root,PATH);

		if(!folder.exists()) {
			folder.mkdir();
		}

		File mWork = new File(folder, sFileName+".xls");
		WritableWorkbook workbook = Workbook.createWorkbook( mWork);


		WritableSheet sheet = workbook.createSheet("First Sheet", 0);
		//Generate the labels 
		int start=0;
		String clockFormat;
		if(setting.isT24Clock()) {
			clockFormat = "HH:mm";
		}
		else {
			clockFormat = "hh:mm a";
		}
		String DATE_FORMAT = "dd/MM/yyyy";
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
		SimpleDateFormat cdf = new SimpleDateFormat(clockFormat);
		java.text.NumberFormat nf = new java.text.DecimalFormat("00"); 
		Calendar cal = Calendar.getInstance();
		if(setting.isKeepName()) {
			Label label = new Label(start, 0, ctx.getString(R.string.export_header_name)); 
			sheet.addCell(label); 
			start=start+1;

		}
		if(setting.isKeepProject()) {
			Label label = new Label(start, 0, ctx.getString(R.string.export_header_project)); 
			sheet.addCell(label); 
			start=start+1;

		}
		if(setting.isKeepClockInDay()) {
			Label label = new Label(start, 0, ctx.getString(R.string.export_header_clock_in_date)); 
			sheet.addCell(label); 
			start=start+1;

		}
		if(setting.isKeepClockInTime()) {
			Label label = new Label(start, 0, ctx.getString(R.string.export_header_clock_in_time)); 
			sheet.addCell(label); 
			start=start+1;
		}
		if(setting.isKeepClockOutDay()) {
			Label label = new Label(start, 0, ctx.getString(R.string.export_header_clock_out_date)); 
			sheet.addCell(label); 
			start=start+1;

		}
		if(setting.isKeepClockOutTime()) {
			Label label = new Label(start, 0, ctx.getString(R.string.export_header_clock_out_time)); 
			sheet.addCell(label); 
			start=start+1;

		}
		if(setting.isKeepAdress()) {
			Label label = new Label(start, 0, ctx.getString(R.string.export_header_adress)); 
			sheet.addCell(label); 
			start=start+1;

		}
		if(setting.isKeepAdress()) {
			Label label = new Label(start, 0, ctx.getString(R.string.export_header_neighborhood)); 
			sheet.addCell(label); 
			start=start+1;

		}
		if(setting.isKeepDuration()) {
			Label label = new Label(start, 0, ctx.getString(R.string.export_header_duration)); 
			sheet.addCell(label); 
			start=start+1;

		}
		if(setting.isKeepDescription()) {
			Label label = new Label(start, 0, ctx.getString(R.string.export_header_description)); 
			sheet.addCell(label); 
			start=start+1;
		}
		long totalDuration = 0;
		int allExported=0;
		for(int i=0;i<list.size();i++) {

			Log.d(TAG,"id:"+i);
			ClockIn tmp = list.get(i);
			if(tmp.isExport()) {
				allExported=allExported+1;
				totalDuration = totalDuration+tmp.getDuration();
				//EXPORT
				start=0;
				if(setting.isKeepName()) {
					Label label = new Label(start, (i+1), tmp.getTitle()); 
					sheet.addCell(label); 
					start=start+1;
				}
				if(setting.isKeepProject()) {
					Label label = new Label(start, (i+1), tmp.getProjectName()); 
					sheet.addCell(label); 
					start=start+1;
				}
				if(setting.isKeepClockInDay()) {
					cal.setTimeInMillis(tmp.getStartTime());
					Label label = new Label(start, (i+1), sdf.format(cal.getTime())); 
					sheet.addCell(label); 
					start=start+1;


				}
				if(setting.isKeepClockInTime()) {
					cal.setTimeInMillis(tmp.getStartTime());

					Label label = new Label(start, (i+1), cdf.format(cal.getTime())); 
					sheet.addCell(label); 
					start=start+1;

				}
				if(setting.isKeepClockOutDay()) {
					cal.setTimeInMillis(tmp.getEndTime());
					Label label = new Label(start, (i+1), sdf.format(cal.getTime())); 
					sheet.addCell(label); 
					start=start+1;


				}
				if(setting.isKeepClockOutTime()) {
					cal.setTimeInMillis(tmp.getEndTime());

					Label label = new Label(start, (i+1), cdf.format(cal.getTime())); 
					sheet.addCell(label); 
					start=start+1;

				}
				if(setting.isKeepAdress()) {
					Label label = new Label(start, (i+1),tmp.getAdress()); 
					sheet.addCell(label); 
					start=start+1;

				}
				if(setting.isKeepNeighborhood()) {
					Label label = new Label(start, (i+1),tmp.getHood()); 
					sheet.addCell(label); 
					start=start+1;

				}
				if(setting.isKeepDuration()) {
					long dur = tmp.getDuration();
					int hoursz   = (int) ((dur / 1000) / 3600);
					int minutesz = (int) (((dur / 1000) / 60)-hoursz*60);
					Label label = new Label(start, (i+1), nf.format(hoursz)+":"+nf.format(minutesz)); 
					sheet.addCell(label); 
					start=start+1;

				}
				if(setting.isKeepDescription()) {
					Label label = new Label(start,(i+1),tmp.getDescription()); 
					sheet.addCell(label); 
					start=start+1;

				}
			}
			else {
				//Null
			}
		}
		//Insert total time. 
		if(setting.isKeepTotalDuration()) {
			Label label = new Label(0,(allExported+1),ctx.getString(R.string.export_header_total_time)); 
			sheet.addCell(label);

			int hoursz   = (int) ((totalDuration / 1000) / 3600);
			int minutesz = (int) (((totalDuration / 1000) / 60)-hoursz*60);
			label = new Label(1, (allExported+1), nf.format(hoursz)+":"+nf.format(minutesz)); 

			sheet.addCell(label);


		}

		workbook.write(); 
		workbook.close();

		return mWork.getAbsolutePath();

	}

	private  void generateCsvFile(Context ctx,String sFileName,ExportSetting setting,ArrayList<ClockIn> list) throws IOException, Exception
	{

		File root = Environment.getExternalStorageDirectory();
		Log.d(TAG,"Root:"+root.getAbsolutePath());
		File gpxfile = new File(root, sFileName+".csv");

		FileWriter writer = new FileWriter(gpxfile);

		if(setting.isKeepName()) {
			writer.append(ctx.getString(R.string.export_header_name));
			writer.append(',');
		}
		if(setting.isKeepClockInDay()) {
			writer.append(ctx.getString(R.string.export_header_clock_in_date));
			writer.append(',');
		}
		if(setting.isKeepClockInDay()) {
			writer.append(ctx.getString(R.string.export_header_clock_in_time));
			writer.append(',');
		}
		if(setting.isKeepClockOutDay()) {
			writer.append(ctx.getString(R.string.export_header_clock_out_date));
			writer.append(',');
		}
		if(setting.isKeepClockOutTime()) {
			writer.append(ctx.getString(R.string.export_header_clock_out_time));
			writer.append(',');
		}
		if(setting.isKeepAdress()) {
			writer.append(ctx.getString(R.string.export_header_adress));
			writer.append(',');
		}
		if(setting.isKeepDuration()) {
			writer.append(ctx.getString(R.string.export_header_duration));
			writer.append(',');
		}

		writer.append('\n');
		Calendar cal = Calendar.getInstance();
		//TODO date formats. By setting, 


		String clockFormat;
		if(setting.isT24Clock()) {
			clockFormat = "HH:mm";
		}
		else {
			clockFormat = "hh:mm a";
		}
		String DATE_FORMAT = "dd/MM/yyyy";
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
		SimpleDateFormat cdf = new SimpleDateFormat(clockFormat);
		java.text.NumberFormat nf = new java.text.DecimalFormat("00"); 
		for(int i=0;i<list.size();i++) {
			Log.d(TAG,"id:"+i);
			ClockIn tmp = list.get(i);
			if(tmp.isExport()) {
				//EXPORT

				if(setting.isKeepName()) {
					writer.append(tmp.getTitle());
					writer.append(',');
				}
				if(setting.isKeepClockInDay()) {
					cal.setTimeInMillis(tmp.getStartTime());
					writer.append(sdf.format(cal.getTime()));
					writer.append(',');
				}
				if(setting.isKeepClockInDay()) {
					cal.setTimeInMillis(tmp.getStartTime());
					writer.append(cdf.format(cal.getTime()));
					writer.append(',');
				}
				if(setting.isKeepClockOutDay()) {
					cal.setTimeInMillis(tmp.getEndTime());
					writer.append(sdf.format(cal.getTime()));
					writer.append(',');
				}
				if(setting.isKeepClockOutTime()) {
					cal.setTimeInMillis(tmp.getEndTime());
					writer.append(cdf.format(cal.getTime()));
					writer.append(',');
				}
				if(setting.isKeepAdress()) {
					Log.d(TAG,"Adress:"+new String(tmp.getAdress().getBytes("UTF-8"),"UTF-8"));
					writer.append(new String(tmp.getAdress().getBytes("UTF-8"),"UTF-8"));
					writer.append(',');
				}
				if(setting.isKeepDuration()) {
					long dur = tmp.getDuration();
					int hoursz   = (int) ((dur / 1000) / 3600);
					int minutesz = (int) (((dur / 1000) / 60)-hoursz*60);
					writer.append(nf.format(hoursz)+":"+nf.format(minutesz));
					writer.append(',');
				}

				writer.append('\n');
			}
			else {
				//Null
			}
		}
		//generate whatever data you want

		writer.flush();
		writer.close();


	}
}
