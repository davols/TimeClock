package se.dxapps.beta.timeclock.provider;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import se.dxapps.beta.timeclock.data.AppInfo;
import se.dxapps.beta.timeclock.data.OldVersion;
import se.dxapps.beta.timeclock.data.YahooResult;
import android.location.Location;
import android.util.Log;

/**
 * 
 * @author david
 * Gets street information from yahoo and app version from your server. 
 *
 */
public class DataManager {

	private static final String URL_YAH = "http://where.yahooapis.com/geocode?q=";
	private static final String YAHOO_APPID="your_app_id_here";

	public YahooResult getAdressFromLocation(Location location) throws ClientProtocolException, IOException, JSONException {
		YahooResult result = new YahooResult();
		String url = URL_YAH+location.getLatitude()+","+location.getLongitude()+"&gflags=R&appid="+YAHOO_APPID+"&flags=J";
		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(url);
		HttpResponse response = client.execute(request);
		JSONObject jsonobject = new JSONObject(inputStreamToString(response.getEntity().getContent()));
		jsonobject = jsonobject.getJSONObject("ResultSet");
		result.setError(jsonobject.getInt("Error"));
		result.setErrorMessage(jsonobject.getString("ErrorMessage"));
		result.setQuality(jsonobject.getInt("Quality"));
		result.setFound(jsonobject.getInt("Found"));
		JSONArray array = jsonobject.getJSONArray("Results");
		if(array.length()>0) {
			JSONObject tmp = array.getJSONObject(0);
			result.setLine1(tmp.getString("line1"));
			result.setStreet(tmp.getString("street"));
			result.setNeighborhood(tmp.getString("neighborhood"));
			result.setCity(tmp.getString("city"));
			result.setCountry(tmp.getString("country"));
		}
		return result;
	}
	public AppInfo getAppInfo() throws JSONException, ClientProtocolException, IOException {
		
			AppInfo res = new AppInfo();
			String url = "your_url_to_json_here";
			HttpClient client = new DefaultHttpClient();
	
			
			HttpGet request = new HttpGet(url);
			HttpResponse response = client.execute(request);

			JSONObject jsonobject = new JSONObject(inputStreamToString(response.getEntity().getContent()));
			res.setCurrentNumber(jsonobject.getInt("currentVersion"));
			res.setCurrentVersion(jsonobject.getString("currentVersionName"));
			res.setStrict(jsonobject.getInt("strictUpdate")==1);
		
			JSONArray array = jsonobject.getJSONArray("versions");
			Log.d("CC","Max:"+array.length());
			for(int i=0;i<array.length();i++) {
				JSONObject tmp = array.getJSONObject(i);
				OldVersion old = new OldVersion();
				old.setCurrentNumber(tmp.getInt("versionNumber"));
				old.setCurrentVersion(tmp.getString("versionName"));
				
				old.setIsAllowed(tmp.getString("allowed").equalsIgnoreCase("yes"));
				res.addOld(old);
				
			}
			return res;
		
	}
	private String inputStreamToString(InputStream is) throws IOException { 
		final char[] buffer = new char[0x10000];
		StringBuilder out = new StringBuilder();
		Reader in = new InputStreamReader(is, "UTF-8");
		int read;
		do {
			read = in.read(buffer, 0, buffer.length);
			if (read>0) {
				out.append(buffer, 0, read);
			}
		} while (read>=0);
		return out.toString();
	}
	public static String convertURL(String str) {

		String url = str;
		try{
			url = new String(str.trim().replace(" ", "%20").replace("&", "%26")
					.replace(",", "%2c").replace("(", "%28").replace(")", "%29")
					.replace("!", "%21").replace("=", "%3D").replace("<", "%3C")
					.replace(">", "%3E").replace("#", "%23").replace("$", "%24")
					.replace("'", "%27").replace("*", "%2A").replace("-", "%2D")
					.replace(".", "%2E").replace("/", "%2F").replace(":", "%3A")
					.replace(";", "%3B").replace("?", "%3F").replace("@", "%40")
					.replace("[", "%5B").replace("\\", "%5C").replace("]", "%5D")
					.replace("_", "%5F").replace("`", "%60").replace("{", "%7B")
					.replace("|", "%7C").replace("}", "%7D"));
		}catch(Exception e){
			e.printStackTrace();
		}
		return url;
	}
}
