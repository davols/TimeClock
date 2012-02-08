package se.dxapps.beta.timeclock.data;

import java.util.ArrayList;

/**
 * Data class for application info. Used  for checking the latest app version on the server. 
 * @author david
 *
 */
public class AppInfo {
	private boolean Failed;
	public boolean isFailed() {
		return Failed;
	}
	public void setFailed(boolean failed) {
		Failed = failed;
	}
	public String isFailedReason() {
		return FailedReason;
	}
	public void setFailedReason(String failedReason) {
		FailedReason = failedReason;
	}
	private String FailedReason;
	private int currentNumber;
	private String currentVersion;
	/**
	 * Boolean used to force user to update. 
	 */
	private boolean strict;
	private ArrayList<OldVersion> olds;
	public AppInfo() {
		olds = new ArrayList<OldVersion>();
	}
	public int getCurrentNumber() {
		return currentNumber;
	}
	public void setCurrentNumber(int currentNumber) {
		this.currentNumber = currentNumber;
	}
	public String getCurrentVersion() {
		return currentVersion;
	}
	public void setCurrentVersion(String currentVersion) {
		this.currentVersion = currentVersion;
	}
	public boolean isStrict() {
		return strict;
	}
	public void setStrict(boolean strict) {
		this.strict = strict;
	}
	public ArrayList<OldVersion> getOlds() {
		return olds;
	}
	public void addOld(OldVersion oldIn) {
		this.olds.add(oldIn);
	}

}
