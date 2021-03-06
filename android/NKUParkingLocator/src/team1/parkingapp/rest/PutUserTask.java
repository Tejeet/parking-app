/*
 * PutUserTask.java
 * 4/22/14
 * Travis Carney
 * 
 * This class contains the task to update a user's information. Before making the request, a ProgressDialog is shown
 * on the UI. The request is made, the results are returned, and the ProgressDialog is dismissed.
 */
package team1.parkingapp.rest;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import org.json.JSONTokener;

import team1.parkingapp.data.User;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class PutUserTask extends AsyncTask<String, Void, User> {
	private Context ctx;				// Context on which to show the ProgressDialog
	private ProgressDialog progress;	// Lets the user know the app is working
	
	/*
	 * Just set the context reference.
	 */
	protected PutUserTask(Context ctx) {
		super();
		this.ctx = ctx;
	}
	
	/*
	 * Just show a ProgressDialog to the user.
	 */
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		progress = new ProgressDialog(ctx);
		progress.setTitle("Updating User Information");
		progress.setMessage("Please wait.");
		progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progress.show();
	}
	
	/*
	 * Makes a PUT request to change the user's information.
	 */
	@Override
	protected User doInBackground(String... params) {
		String currentEmail = params[0];						// Email address of the user to modify
		String currentPwd = params[1];							// Password of the user to modify
		String id = params[2];									// User's ID
		String newEmail = params[3];							// Modified user's new email (if not null)
		String newPwd = params[4];								// Modified user's new pwd (if not null)
		String newName  = params[5];							// Modified user's new name (if not null)
		String newLastname = params[6];							// Modified user's new last name (if not null)
		String role = Session.getInstance().getUser().getRole();// User's role
		User user = null;										// User to be returned
		HttpClient httpClient = new DefaultHttpClient();		// HTTP client used to perform the request;
		HttpPut httpPut = 										// HTTP PUT header
				new HttpPut(RestContract.USERS_API + id);	
	    UsernamePasswordCredentials creds = 					// User's credentials 
	    		new UsernamePasswordCredentials(currentEmail, currentPwd);
		
	    // Add those credentials to the request
	    ((AbstractHttpClient) httpClient).getCredentialsProvider().setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT), creds);
		try {
			List<NameValuePair> args = new ArrayList<NameValuePair>();
			
			// Add any non null parameters to the request.
			if (newEmail != null) {
				args.add(new BasicNameValuePair(RestContract.USER_EMAIL, newEmail));
				Log.i("PUT User", "Email not null");
			}
			if (newPwd != null) {
				args.add(new BasicNameValuePair(RestContract.USER_PASSWORD, newPwd));
				Log.i("PUT User", "Password not null");
			}
			if (newName != null) {
				args.add(new BasicNameValuePair(RestContract.USER_NAME, newName));
				Log.i("PUT User", "Name not null");
			}
			if (newLastname != null) {
				args.add(new BasicNameValuePair(RestContract.USER_LASTNAME, newLastname));
				Log.i("PUT User", "Lastname not null");
			}
			
			// Add the role
			args.add(new BasicNameValuePair(RestContract.USER_ROLE, role));
			
			// Execute the PUT request and get the response
			httpPut.setEntity(new UrlEncodedFormEntity(args));
			HttpResponse response = httpClient.execute(httpPut);
			StatusLine status = response.getStatusLine();
			
			if (status.getStatusCode() == HttpStatus.SC_OK) {
				Log.i("PUT User", Integer.toString(status.getStatusCode()));
				Log.i("PUT User", status.getReasonPhrase());
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				response.getEntity().writeTo(out);
				user = this.parseResults(out.toString());
				out.close();
			}
			else {
				Log.e("PUT User", Integer.toString(status.getStatusCode()));
				Log.e("PUT User", status.getReasonPhrase());
			}
		}
		catch (Exception e) {
			Log.e("PUT User", e.getMessage());
			return null;
		}
		
		return user;
	}
	
	/*
	 * Dismiss the ProgressDialog.
	 */
	@Override
	protected void onPostExecute(User result) {
		super.onPostExecute(result);
		progress.dismiss();
	}
	
	/*
	 * Format the JSON data from the server's response and create a User object from it.
	 */
	private User parseResults(String results) {
		User user = null;
		
		Log.i("PUT User Resuls", results);
		
		JSONTokener tokener = new JSONTokener(results);
		try {
			JSONObject json = new JSONObject(tokener);
			user = User.validateJSONData(json);
		}
		catch (Exception e) {
			Log.e("PUT User", "Error parsing JSON objects");
			return null;
		}
		
		return user;
	}

}
