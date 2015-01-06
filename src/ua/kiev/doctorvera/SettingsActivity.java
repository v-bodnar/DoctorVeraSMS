package ua.kiev.doctorvera;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

/*
 * Form to set settings
 * @author      Volodymyr Bodnar
 * @version     %I%, %G%
 * @since       1.0
 */
public class SettingsActivity extends ActionBarActivity implements OnClickListener {
	// Declare Variables

	private EditText loginField, passwordField, alphaNameField;
	private Button saveSettingsButton, cancelButton;
	private Spinner languageSpinner;

	private final String PARAM_NAME_LOGIN="LOGIN";
	private final String PARAM_NAME_PASSWORD="PASSWORD";
	private final String PARAM_NAME_ALPHA_NAME="ALPHA_NAME";
	private final String PARAM_NAME_LANGUAGE="LANGUAGE";
	private final String PARAM_NAME_MY_LICENSE="MY LICENSE";
	
	private final String DEFAULT_LOGIN="android";
	private final String DEFAULT_PASSWORD="849Wyn4hdjpcklnqwg";
	private final String DEFAULT_ALPHA_NAME="Doctor Vera";
	private final String DEFAULT_LANGUAGE="English";
	


	private String LAST_LOGIN; 
	private String LAST_PASSWORD;
	private String LAST_ALPHA_NAME; 
	private String LAST_LANGUAGE;

	private SharedPreferences  sPref;
	private final String LOG_TAG = "myLogs Settings";
	private LicensingService licenseService;
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v7.app.ActionBarActivity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings_layout);
	    licenseService = new LicensingService(this);
		// Get View-elements
		loginField = (EditText) findViewById(R.id.login_field);
		passwordField = (EditText) findViewById(R.id.password_field);
		alphaNameField = (EditText) findViewById(R.id.alpha_name_field);
		languageSpinner = (Spinner)findViewById(R.id.language_spinner);
		saveSettingsButton = (Button) findViewById(R.id.save_settings_button);
		cancelButton = (Button) findViewById(R.id.cancel_button);

		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
				R.array.languages, android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		languageSpinner.setAdapter(adapter);
		//languageSpinner.setOnItemSelectedListener(this);

		//Setting button listeners
		saveSettingsButton.setOnClickListener(this);
		cancelButton.setOnClickListener(this);

		//Displaying back button
		getActionBar().setDisplayHomeAsUpEnabled(true);

		loadPreferences();
		fillFields();
	} // onCreate

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.cancel_button:
			loadPreferences();
			NavUtils.navigateUpFromSameTask(this);
			break;
		case R.id.save_settings_button:
			String login = loginField.getText().toString();
			String password = passwordField.getText().toString();
			String alphaName = alphaNameField.getText().toString();
			String language = languageSpinner.getSelectedItem().toString();
			if(checkFieldsNotNull()){
				checkAuth(login, password, alphaName, language);
				checkMyLicense(login);
			    //Check Google License
				licenseService.checkGoogleLicense();
			}
			break;
		}

	}

	//Checks if the fields are filled
	private Boolean checkFieldsNotNull(){
		//Checking fields data
		if (loginField.getText().toString().trim().equalsIgnoreCase("")) {
			loginField.setError(getString(R.string.field_empty_error));
			Log.d(LOG_TAG,"Settings changing failed: loginField is empty");
			return false;
		} else if (passwordField.getText().toString().trim().equalsIgnoreCase("")) {
			passwordField.setError(getString(R.string.field_empty_error));
			Log.d(LOG_TAG,"Settings changing failed: passwordField is empty");
			return false;
		} else if (alphaNameField.getText().toString().trim().equalsIgnoreCase("")) {
			alphaNameField.setError(getString(R.string.field_empty_error));
			Log.d(LOG_TAG,"Settings changing failed: alphaNameField is empty");
			return false;

		} else {
			loginField.setError(null);
			passwordField.setError(null);
			alphaNameField.setError(null);
			return true;
		}
	}

	private void checkAuth(final String login, final String password, final String alphaName, final String language){
		Boolean isNetworkConnected = false;
		Boolean isInternetAvailable = false;
		try {
			isNetworkConnected = (Boolean)new SMSGatewayConnect(SettingsActivity.this).execute("isNetworkConnected").get();
			isInternetAvailable  = (Boolean)new SMSGatewayConnect(SettingsActivity.this).execute("isInternetAvailable").get();

		} catch (InterruptedException e) {
			Log.e(LOG_TAG, e.getMessage());
		} catch (ExecutionException e) {
			Log.e(LOG_TAG, e.getMessage());
		}
		
		//Checking Internet connection
		if(isNetworkConnected && isInternetAvailable){
			Boolean checkGatewayAuth = false;
			try {
				checkGatewayAuth = (Boolean)new SMSGatewayConnect(SettingsActivity.this).execute("checkGatewayAuth",login, password,alphaName).get();
			} catch (InterruptedException e) {
				Log.e(LOG_TAG, e.getMessage());
			} catch (ExecutionException e) {
				Log.e(LOG_TAG, e.getMessage());
			}
			//Trying to connect to Gateway
			if(checkGatewayAuth){
				//Auth is successful Saving Params  
				saveSettings(login,password,alphaName,language);
			}else{
				//If Connection check failed set errors
				loginField.setError(getString(R.string.login_password_error));
				passwordField.setError(getString(R.string.login_password_error));
				alphaNameField.setError(getString(R.string.login_password_error));
				Log.d(LOG_TAG,"Authentification failed");
			}
		}else Toast.makeText(getApplicationContext(), R.string.internet_connection_error, Toast.LENGTH_LONG).show();

	}

	private void saveSettings(final String login, final String password, final String alphaName, final String language){
		Editor ed = sPref.edit();
		ed.putString(PARAM_NAME_LOGIN, login);
		ed.putString(PARAM_NAME_PASSWORD, password);
		ed.putString(PARAM_NAME_ALPHA_NAME, alphaName);
		ed.putString(PARAM_NAME_LANGUAGE, language);
		ed.commit();
		Log.d(LOG_TAG,"Settings changed: ");
		Log.d(LOG_TAG,"	Login=" + login);
		Log.d(LOG_TAG,"	Password=" + password);
		Log.d(LOG_TAG,"	Alpha Name=" + alphaName);
		Log.d(LOG_TAG,"	Language=" + language);
		Toast.makeText(getApplicationContext(), R.string.settings_saved_notice, Toast.LENGTH_LONG).show();
	}

	private void checkMyLicense(final String login){
				setProgressBarIndeterminateVisibility(true);
				Boolean isNetworkConnected = false;
				Boolean isInternetAvailable = false;
				try {
					isNetworkConnected = (Boolean)new SMSGatewayConnect(this).execute("isNetworkConnected").get();
					isInternetAvailable  = (Boolean)new SMSGatewayConnect(this).execute("isInternetAvailable").get();

				} catch (InterruptedException e) {
					Log.e(LOG_TAG, e.getMessage());
				} catch (ExecutionException e) {
					Log.e(LOG_TAG, e.getMessage());
				}

				//Checking Internet connection
				if(isNetworkConnected && isInternetAvailable) {
					Boolean isLicenseActive = false;
					try {
						isLicenseActive = (Boolean)new SMSGatewayConnect(this).execute("isLicenseActive",login).get();     
					} catch (InterruptedException e) {
						Log.e(LOG_TAG, e.getMessage());
					} catch (ExecutionException e) {
						Log.e(LOG_TAG, e.getMessage());
					}
					//Getting My License
					if (isLicenseActive){
						Editor ed = sPref.edit();
						ed.putString(PARAM_NAME_MY_LICENSE, "" + true);
						ed.commit();
						Log.d(LOG_TAG,"My License is Active");
						Toast.makeText(getApplicationContext(), R.string.drvera_valid, Toast.LENGTH_LONG).show();
					}  	else {
						Editor ed = sPref.edit();
						ed.putString(PARAM_NAME_MY_LICENSE, "" + false);
						ed.commit();
						Log.d(LOG_TAG,"My License is NOT Active");
						Toast.makeText(getApplicationContext(), R.string.license_error, Toast.LENGTH_LONG).show();		    	
					}
				} else Toast.makeText(getApplicationContext(), R.string.internet_connection_error, Toast.LENGTH_LONG).show();
				setProgressBarIndeterminateVisibility(false);
	}
	private void loadPreferences(){
		//Initializing preferences 
		sPref = PreferenceManager.getDefaultSharedPreferences(this);
		LAST_LOGIN = sPref.getString(PARAM_NAME_LOGIN, DEFAULT_LOGIN); 
		LAST_PASSWORD = sPref.getString(PARAM_NAME_PASSWORD, DEFAULT_PASSWORD);
		LAST_ALPHA_NAME = sPref.getString(PARAM_NAME_ALPHA_NAME, DEFAULT_ALPHA_NAME); 
		LAST_LANGUAGE = sPref.getString(PARAM_NAME_LANGUAGE, DEFAULT_LANGUAGE);
	}

	private void fillFields(){
		if(LAST_LOGIN!=null && LAST_PASSWORD!=null && LAST_ALPHA_NAME!=null && LAST_LANGUAGE!=null){
			loginField.setText(LAST_LOGIN);
			passwordField.setText(LAST_PASSWORD);
			alphaNameField.setText(LAST_ALPHA_NAME);
			ArrayList<String> languages = new ArrayList<String>();
			languages.addAll(Arrays.asList(getResources().getStringArray(R.array.languages)));
			for(int i=0; i<languages.size();i++)
				if (languages.get(i).equals(LAST_LANGUAGE)) languageSpinner.setSelection(i);
		}
	}
    @Override
    protected void onDestroy() {
        super.onDestroy();
        licenseService.onDestroy();
    }
}
