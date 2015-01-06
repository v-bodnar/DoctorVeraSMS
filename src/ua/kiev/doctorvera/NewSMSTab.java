package ua.kiev.doctorvera;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
/*
 * This class creates tab with form. Two fields and two buttons:
 * <ul>
 * <li><b>phoneNumberField</b> - field to input phone number, with auto complete from phonebook</li>
 * <li><b>smsTextField</b> - textarea to input SMS text</li>
 * <li><b>saveSmsButton</b> - button to save SMS with state template(2)</li>
 * <li><b>sendSmsButton</b> - button to send SMS and save it with state sent(3)</li>
 * </ul>
 * @author      Volodymyr Bodnar
 * @version     %I%, %G%
 * @since       1.0
 */

public class NewSMSTab extends Fragment implements View.OnClickListener, OnItemClickListener, OnItemSelectedListener, TextView.OnEditorActionListener {
	// Declare Variables
	private TextView counter;
	private EditText smsTextField;
	private AutoCompleteTextView phoneNumberField;
	private Button sendSmsButton, saveSmsButton;
	private SMS_DAO SMSDAO;
	final String LOG_TAG = "myLogs NewSMSTab";
	private ContactsAdapter adapter;
	private String currentName; //selected item from auto complete list
	private String currentPhone; //selected item from auto complete list
	private ArrayList<Map<String, String>> contactData;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		Log.d(LOG_TAG,"onCreateView");
		//Inflate the layout for this fragment
		View V = inflater.inflate(R.layout.new_sms_tab, container, false);
		// Get View-elements
		phoneNumberField = (AutoCompleteTextView) V.findViewById(R.id.phoneNumberField);
		smsTextField = (EditText) V.findViewById(R.id.smsTextField);
		sendSmsButton = (Button) V.findViewById(R.id.send_sms_button);
		saveSmsButton = (Button) V.findViewById(R.id.save_sms_button);
		counter = (TextView) V.findViewById(R.id.counter);

		//Setting button listeners
		sendSmsButton.setOnClickListener(this);
		saveSmsButton.setOnClickListener(this);
		
		//Populating contacts
		contactData = Utils.getContactData(getActivity()); //Contacts from the phone book
		//Create adapter
		adapter = new ContactsAdapter(getActivity(), contactData, R.layout.contact_view,
				new String[]{"Name", "Phone"}, new int[]{
			R.id.ccontName, R.id.ccontNo}
				);

		phoneNumberField.setThreshold(1);

		//Set adapter to AutoCompleteTextView
		phoneNumberField.setAdapter(adapter);
		phoneNumberField.setOnItemSelectedListener(this);
		phoneNumberField.setOnItemClickListener(this);
		
		//Set SMSText change listener  
		counter.setText("0/0");
		smsTextField.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				Log.d(LOG_TAG, ""+smsTextField.getText().length());
				Pattern pattern = Pattern.compile("\\p{Cyrillic}");
				Matcher matcher = pattern.matcher(smsTextField.getText());
				int chars = smsTextField.getText().length();
				if (chars==0)counter.setText("0/0");
				else if (matcher.find()){
					int num = (chars/66)+1;
					counter.setText("" + num + "/" + chars);
				}else{
					int num = (chars/152)+1;
					counter.setText("" + num + "/" + chars);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				
			}
		});

		return V;
	}
	
	
	/*
	 * Calls to the proper method when IME is pressed
	 * (non-Javadoc)
	 * @see android.widget.TextView.OnEditorActionListener#onEditorAction(android.widget.TextView, int, android.view.KeyEvent)
	 */
	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		boolean handled = false;
		if (actionId == EditorInfo.IME_ACTION_DONE) {
			onClick(sendSmsButton);
			handled = true;
		} else if (actionId == EditorInfo.IME_ACTION_NEXT) {
			smsTextField.requestFocus();
			handled = true;
		}
		return handled;
	}

	@Override
	public void onClick(View v) {
		// Object to manipulate SMS objects inside the database
		SMSDAO = new SMS_DAO(getActivity());

		//Checking fields data
		if (phoneNumberField.getText().toString().trim().equalsIgnoreCase("")) {
			phoneNumberField.setError(getString(R.string.field_empty_error));
			return;
		} else if (smsTextField.getText().toString().trim().equalsIgnoreCase("")) {
			smsTextField.setError(getString(R.string.field_empty_error));
			return;
		} else if (getPhoneNumber().equalsIgnoreCase("")) {
			phoneNumberField.setError(getString(R.string.phone_number_format_error));
			return;
		} else {
			phoneNumberField.setError(null);
			smsTextField.setError(null);
		}

		// Get data from fields
		SMS newSMS = new SMS();
		newSMS.setPhoneNumber(getPhoneNumber());
		newSMS.setText(smsTextField.getText().toString());
		newSMS.setDateSent(Utils.getDateTime());

		//Sending or Saving SMS, depends on Button pressed
		switch (v.getId()) {
		case R.id.save_sms_button:
			newSMS.setState((byte) 2);
			long rowID = SMSDAO.addSMS(newSMS);
			Log.d(LOG_TAG, "New SMS saved, ID = " + rowID);
			Toast.makeText(getActivity(), R.string.saved_notify, Toast.LENGTH_LONG).show();
			cleanFields();
			//Updating TemplateTab List
			Utils.listUpdate((ListView) getActivity().findViewById(R.id.template_list));
			break;
		case R.id.send_sms_button:
			sendSMS(newSMS);
			//Updating ArchiveTab List
			Utils.listUpdate((ListView) getActivity().findViewById(R.id.archive_list));
			break;
		}
		// closing DB connection
		SMSDAO.close();
	}

	@SuppressWarnings("unchecked")
	private static <T extends ArrayList<?>> T cast(Object obj) {
		return (T) obj;
	}

	private void sendSMS(final SMS newSMS){

		Boolean isNetworkConnected = false;
		Boolean isInternetAvailable = false;
		try {
			isNetworkConnected = (Boolean)new SMSGatewayConnect(getActivity()).execute("isNetworkConnected").get();
			isInternetAvailable  = (Boolean)new SMSGatewayConnect(getActivity()).execute("isInternetAvailable").get();

		} catch (InterruptedException e) {
			Log.e(LOG_TAG, e.getMessage());
		} catch (ExecutionException e) {
			Log.e(LOG_TAG, e.getMessage());
		}
		long rowID; 
		SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
		Boolean isInternetActive = (isNetworkConnected && isInternetAvailable);

		//If Internet is connected, gateway is reachable, and license is active Send SMS
		Log.d(LOG_TAG,"Internet status: " + isInternetActive);
		Log.d(LOG_TAG,"Google license status: " + sPref.getString("MY LICENSE", "false"));
		Log.d(LOG_TAG,"DrVera license status: " + sPref.getString("GOOGLE LICENSE", "false"));
		if(!isInternetActive) Toast.makeText(getActivity(), R.string.internet_connection_error, Toast.LENGTH_LONG).show();
		else if(!(Boolean.parseBoolean(sPref.getString("MY LICENSE", "false")) && Boolean.parseBoolean(sPref.getString("GOOGLE LICENSE", "false"))))Toast.makeText(getActivity(), R.string.unlicensed_dialog_body, Toast.LENGTH_LONG).show();
		else{
			//Sending SMS and saving answer
			ArrayList<String> status = new ArrayList<String>();
			try {
				status = cast(new SMSGatewayConnect(getActivity()).execute("sendSMS",newSMS).get());

			} catch (InterruptedException e) {
				Log.e(LOG_TAG, e.getMessage());
			} catch (ExecutionException e) {
				Log.e(LOG_TAG, e.getMessage());
			}

			//Setting SMS params
			newSMS.setTrackingId(Long.parseLong(status.get(0)));
			newSMS.setDateChanged(Utils.getUTCDateTime(status.get(1)));
			newSMS.setStateString(status.get(2));
			//Saving SMS to DB
			rowID = SMSDAO.addSMS(newSMS);

			Log.d(LOG_TAG, "New SMS sent, ID = " + rowID);
			Toast.makeText(getActivity(), R.string.sent_notify, Toast.LENGTH_LONG).show();

			cleanFields();

		}
	}


	//Cleaning fields
	private void cleanFields() {
		smsTextField.setText("");
		smsTextField.setError(null);
		phoneNumberField.setText("");
		smsTextField.setError(null);
	}

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub

		getActivity();
		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
				Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);

	}
	/*
	 * When AutoComplete list item is selected we save name and phone of the contact and set text to phoneNumberField
	 * (non-Javadoc)
	 * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View, int, long)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void onItemClick(AdapterView<?> av, View arg1, int index,
			long arg3) {
		Map<String, String> map = (Map<String, String>) av.getItemAtPosition(index);
		currentName = map.get("Name");
		currentPhone = map.get("Phone");
		phoneNumberField.setText("" + currentName + "<" + currentPhone + ">");
	}


	/*
	 * Checks phone number length
	 */
	private String checkNumberFormat(String phoneNumber) {
		if (phoneNumber.length() == 13) return phoneNumber;
		else {
			Log.d(LOG_TAG, "phoneNumber.length()!=14");
			return "";
		}
	}

	/*
	 * Returns internationally formatted phone number 
	 */
	private String getPhoneNumber() {
		String phoneNumber = phoneNumberField.getText().toString();
		if (phoneNumber.contains("<")) {
			String formattedPhone = Utils.formatPhoneNumber(currentPhone);
			return checkNumberFormat(formattedPhone);
		} else {
			String formattedPhone = Utils.formatPhoneNumber(phoneNumber);
			return checkNumberFormat(formattedPhone);
		}
	}
}

