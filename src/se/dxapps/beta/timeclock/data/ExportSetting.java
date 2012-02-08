package se.dxapps.beta.timeclock.data;

import android.content.SharedPreferences;

public class ExportSetting {
	public ExportSetting(){

	}
	public void GenereteFromPreferences(SharedPreferences sp) {
		this.setKeepClockInDay(sp.getBoolean("prefs_export_clockin_day",true));
		this.setKeepClockInTime(sp.getBoolean("prefs_export_clockin_time",true));
		this.setKeepClockOutDay(sp.getBoolean("prefs_export_clockout_day",true));
		this.setKeepClockOutTime(sp.getBoolean("prefs_export_clockout_time",true));
		this.setKeepAdress(sp.getBoolean("prefs_export_adress",true));
		this.setKeepDuration(sp.getBoolean("prefs_export_duration",true));
		this.setDeleteAfterExport(sp.getBoolean("prefs_export_delete",true));
		this.setKeepName(sp.getBoolean("prefs_export_keep_name", true));
		this.setFileFormat(sp.getString("prefs_export_fileformat", "excel(97)"));
		this.setT24Clock(sp.getBoolean("prefs_time_24",true));
		this.setKeepDescription(sp.getBoolean("prefs_export_description",false));
		this.setKeepTotalDuration(sp.getBoolean("prefs_export_totaltime", true));
		this.setKeepNeighborhood(sp.getBoolean("prefs_export_neighborhood", true));
		this.setKeepProject(sp.getBoolean("prefs_export_keep_name", true));
	}
	private boolean KeepProject;
	private String fileFormat;
	private boolean KeepDescription;
	private boolean T24Clock;
	private boolean KeepName;
	private boolean KeepAdress;
	private boolean KeepClockInDay;
	private boolean KeepClockInTime;
	private boolean KeepClockOutDay;
	private boolean KeepClockOutTime;
	private boolean KeepDuration;
	private boolean KeepTotalDuration;
	private boolean DeleteAfterExport;
	private boolean KeepNeighborhood;
	public boolean isKeepAdress() {
		return KeepAdress;
	}
	public void setKeepAdress(boolean keepAdress) {
		KeepAdress = keepAdress;
	}
	public boolean isKeepClockInDay() {
		return KeepClockInDay;
	}
	public void setKeepClockInDay(boolean keepClockInDay) {
		KeepClockInDay = keepClockInDay;
	}
	public boolean isKeepClockInTime() {
		return KeepClockInTime;
	}
	public void setKeepClockInTime(boolean keepClockInTime) {
		KeepClockInTime = keepClockInTime;
	}
	public boolean isKeepClockOutDay() {
		return KeepClockOutDay;
	}
	public void setKeepClockOutDay(boolean keepClockOutDay) {
		KeepClockOutDay = keepClockOutDay;
	}
	public boolean isKeepClockOutTime() {
		return KeepClockOutTime;
	}
	public void setKeepClockOutTime(boolean keepClockOutTime) {
		KeepClockOutTime = keepClockOutTime;
	}
	public boolean isKeepDuration() {
		return KeepDuration;
	}
	public void setKeepDuration(boolean keepDuration) {
		KeepDuration = keepDuration;
	}
	public boolean isDeleteAfterExport() {
		return DeleteAfterExport;
	}
	public void setDeleteAfterExport(boolean deleteAfterExport) {
		DeleteAfterExport = deleteAfterExport;
	}
	public void setFileFormat(String fileFormat) {
		this.fileFormat = fileFormat;
	}
	public String getFileFormat() {
		return fileFormat;
	}
	public void setKeepName(boolean keepName) {
		KeepName = keepName;
	}
	public boolean isKeepName() {
		return KeepName;
	}
	public void setT24Clock(boolean t24Clock) {
		T24Clock = t24Clock;
	}
	public boolean isT24Clock() {
		return T24Clock;
	}
	public void setKeepDescription(boolean keepDescription) {
		KeepDescription = keepDescription;
	}
	public boolean isKeepDescription() {
		return KeepDescription;
	}
	public void setKeepTotalDuration(boolean keepTotalDuration) {
		KeepTotalDuration = keepTotalDuration;
	}
	public boolean isKeepTotalDuration() {
		return KeepTotalDuration;
	}
	public void setKeepNeighborhood(boolean keepNeighborhood) {
		KeepNeighborhood = keepNeighborhood;
	}
	public boolean isKeepNeighborhood() {
		return KeepNeighborhood;
	}
	public void setKeepProject(boolean keepProject) {
		KeepProject = keepProject;
	}
	public boolean isKeepProject() {
		return KeepProject;
	}
}
