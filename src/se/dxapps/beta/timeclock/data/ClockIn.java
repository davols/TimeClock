package se.dxapps.beta.timeclock.data;

import android.location.Location;

public class ClockIn {
	private long RowId;
	private String Title;
	private String Adress;
	private String Description;
	private Location loc;
	private long StartTime;
	private long EndTime;
	private boolean Export;
	private long Duration;
	private String ProjectName;
	private long ProjectID;
	private String Hood;
	public String getTitle() {
		return Title;
	}
	public void setTitle(String title) {
		Title = title;
	}
	
	public String getAdress() {
		return Adress;
	}
	public void setAdress(String adress) {
		Adress = adress;
	}
	public String getDescription() {
		return Description;
	}
	public void setDescription(String description) {
		Description = description;
	}
	public Location getLoc() {
		return loc;
	}
	public void setLoc(Location loc) {
		this.loc = loc;
	}
	public long getStartTime() {
		return StartTime;
	}
	public void setStartTime(long startTime) {
		StartTime = startTime;
	}
	public long getEndTime() {
		return EndTime;
	}
	public void setEndTime(long endTime) {
		EndTime = endTime;
	}
	public long getDuration() {
		return Duration;
	}
	public void setDuration(long duration) {
		Duration = duration;
	}
	public void setRowId(long rowId) {
		RowId = rowId;
	}
	public long getRowId() {
		return RowId;
	}
	public void setExport(boolean export) {
		Export = export;
	}
	public boolean isExport() {
		return Export;
	}
	public void setHood(String hood) {
		Hood = hood;
	}
	public String getHood() {
		return Hood;
	}
	public void setProjectName(String projectName) {
		ProjectName = projectName;
	}
	public String getProjectName() {
		return ProjectName;
	}
	public void setProjectID(long projectID) {
		ProjectID = projectID;
	}
	public long getProjectID() {
		return ProjectID;
	}

}
