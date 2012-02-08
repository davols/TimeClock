package se.dxapps.beta.timeclock.data;

public class OldVersion {
private boolean IsAllowed;
private int currentNumber;
private String currentVersion;
public boolean isIsAllowed() {
	return IsAllowed;
}
public void setIsAllowed(boolean isAllowed) {
	IsAllowed = isAllowed;
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
}
