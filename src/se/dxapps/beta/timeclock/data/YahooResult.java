package se.dxapps.beta.timeclock.data;
/**
 * result class from yahoo. 
 * @author david
 *
 */
public class YahooResult {
	private int Error;
	private String ErrorMessage;
	private int Quality;
	private int Found;
	private String Street;
	private String Neighborhood;
	private String City;
	private String Country;
	private String Line1;
	public int getError() {
		return Error;
	}
	public void setError(int error) {
		Error = error;
	}
	public String getErrorMessage() {
		return ErrorMessage;
	}
	public void setErrorMessage(String errorMessage) {
		ErrorMessage = errorMessage;
	}
	public int getQuality() {
		return Quality;
	}
	public void setQuality(int quality) {
		Quality = quality;
	}
	public int getFound() {
		return Found;
	}
	public void setFound(int found) {
		Found = found;
	}
	public String getStreet() {
		return Street;
	}
	public void setStreet(String street) {
		Street = street;
	}
	public String getNeighborhood() {
		return Neighborhood;
	}
	public void setNeighborhood(String neighborhood) {
		Neighborhood = neighborhood;
	}
	public String getCity() {
		return City;
	}
	public void setCity(String city) {
		City = city;
	}
	public void setCountry(String country) {
		Country = country;
	}
	public String getCountry() {
		return Country;
	}
	public void setLine1(String line1) {
		Line1 = line1;
	}
	public String getLine1() {
		return Line1;
	}

}
