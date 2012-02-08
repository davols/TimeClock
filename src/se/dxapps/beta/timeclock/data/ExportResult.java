package se.dxapps.beta.timeclock.data;

public class ExportResult {
private boolean Failed;
private String FailedReason;
private String pathName;
public boolean isFailed() {
	return Failed;
}
public void setFailed(boolean failed) {
	Failed = failed;
}
public String getFailedReason() {
	return FailedReason;
}
public void setFailedReason(String failedReason) {
	FailedReason = failedReason;
}
public String getPathName() {
	return pathName;
}
public void setPathName(String pathName) {
	this.pathName = pathName;
}
}
