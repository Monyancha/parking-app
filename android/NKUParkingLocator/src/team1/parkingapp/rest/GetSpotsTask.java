/*
 * GetSpotsTask.java
 * 4/16/14
 * Travis Carney
 * 
 * This class contains the implementation for the GET spots request.
 */
package team1.parkingapp.rest;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import java.util.Vector;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import team1.parkingapp.data.Spot;

public class GetSpotsTask extends AsyncTask<String, Void, Vector<Spot> > {
	private ProgressDialog progress;	// ProgressDialog letting the user know the app is working on their request
	private Context ctx;				// Context on which to show the ProgressDialog

	/*
	 * Assign the context field.
	 */
	protected GetSpotsTask(Context ctx) {
		super();
		this.ctx = ctx;
	}
	
	/*
	 * Setup a ProgressDialog and show it.
	 */
	@Override
	protected void onPreExecute() {
		progress = new ProgressDialog(ctx);
		progress.setTitle("Creating User");
		progress.setMessage("Please wait.");
		progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progress.show();
		super.onPreExecute();
	}
	
	@Override
	protected Vector<Spot> doInBackground(String... params) {
		String getParams = "";								// Contains the GET parameters to send
		Vector<Spot> spots = new Vector<Spot>();			// List of spots to be returned
		HttpClient httpclient = new DefaultHttpClient();	// HTTP client used to execute the request
		HttpGet httpGet;									// HTTP Get header
		 
		try {	
			// If one parameter was passed in, it should be the spot ID
			// If four parameters were passed in, it should be lat & long data
			if (params.length != 1 || params.length != 4) {
				Log.e("GET spots", "Too few arguments.");
				return null;
			}
			else {
				getParams = buildGetParameterString(params);
			}

	        // Execute HTTP Get request & get the response
			httpGet = new HttpGet(RestContract.SPOTS_API + getParams);
	        HttpResponse response = httpclient.execute(httpGet);
	        
	        StatusLine status = response.getStatusLine();
	        
	        if(status.getStatusCode() == HttpStatus.SC_OK) {
	        	Log.i("GET Lot", Integer.toString(status.getStatusCode()));
	        	Log.i("GET Lot", status.getReasonPhrase());
	        	ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                spots = GetSpotsTask.parseResults(out.toString());
                out.close();
	        }
	        
		}
		catch (Exception e) {
			Log.e("GET spots", e.getMessage());
		}
		 
		return spots;
	}
	
	/*
	 * Dismiss the ProgressDialog.
	 */
	@Override
	protected void onPostExecute(Vector<Spot> spots) {
		progress.dismiss();
		super.onPostExecute(spots);
	}
	
	/*
	 * Builds the parameter string to append to the URL to perform a GET request.
	 * This string is returned.
	 */
	private String buildGetParameterString(String[] params) {
		String paramString = "";
		String[] getParams = {RestContract.LAT1, RestContract.LONG1, RestContract.LAT2, RestContract.LONG2};
		
		// To request a spot by ID, it simply append the ID to the end of the URL
		if (params.length == 1) {
			return paramString += params[1];
		}
		// Assuming that there were 4 parameters passed in (should be guaranteed by doInBackground)
		else {
			paramString += "?";
			for (int i = 0; i < getParams.length; i++) {
				paramString += getParams[i] + params[i];
			}
		}
		return paramString;
	}
	
	/*
	 * Takes a string containing JSON data, parses it out, and creates a vector of spot objects.
	 * This vector is returned.
	 */
	private static Vector<Spot> parseResults(String results)
	{
		Vector<Spot> spots = new Vector<Spot>();

		JSONTokener tokener = new JSONTokener(results);
		try {
			JSONArray arr = new JSONArray(tokener);
			for(int i = 0 ; i < arr.length() ; ++i) {
				JSONObject json = arr.getJSONObject(i);
				spots.add(GetSpotsTask.validateData(json));
			}
		}
		catch(Exception e) {
			Log.e("GET Spots", "Error parsing JSON objects");
			return null;
		}
		
		return spots;
	}
	
	/*
	 * Pulls out the data from each JSON object and returns a SPOT object from that data.
	 * If any fields in the database are null, they are set to default invalid values.
	 */
	private static Spot validateData(JSONObject json) {
		int id, lot_id;
		double lat, lng;
		String status;
		
		// Begin the disgusting wall of try-catches
		try {
			id = json.getInt(RestContract.SPOT_ID);
		}
		catch (Exception e) {
			id = -1;
		}
		try {
			lot_id = json.getInt(RestContract.SPOT_LOT_ID);
		}
		catch (Exception e) {
			lot_id = -1;
		}
		try {
			lat = json.getDouble(RestContract.SPOT_LAT);
		}
		catch (Exception e) {
			lat = 0;
		}
		try {
			lng = json.getDouble(RestContract.SPOT_LONG);
		}
		catch (Exception e) {
			lng = 0;
		}
		try {
			status = json.getString(RestContract.SPOT_STATUS);
		}
		catch (Exception e) {
			status = "";
		}
		
		return new Spot(id, lot_id, lat, lng, status);
	}
}
