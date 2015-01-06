package ua.kiev.doctorvera;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;


/*
 * <p>Adapter for ListView with checkbox on items</p>
 * <p>It also handles click on item and checking checkbox</p>
 * @author      Volodymyr Bodnar
 * @version     %I%, %G%
 * @since       1.0
 */
public class CheckboxListAdapter extends ArrayAdapter<Map<String, Object>> implements View.OnClickListener, AdapterView.OnItemClickListener {

    private List<Map<String, Object>> data; 
    //private final String LOG_TAG = "myLogs CheckboxListAdapter";
    private Context context;
    private final String LOG_TAG = "myLogs CheckboxListAdapter";
    private int resourceItem;
    
	//Data for creating ListView Adapter
	// Names of Map keys
	private final String SMS_TEXT = "text";
	private final String SMS_DATE = "date";
	private final String SMS_ID = "id";
	private final String SMS_PHONE = "phone";
	private final String SMS_STATE = "state";
    
    public CheckboxListAdapter(Context context, List<Map<String, Object>> data, int resource) {
    	super(context, resource, data);
        this.context = context;
        this.resourceItem = resource;
        getData();
        Log.d(LOG_TAG, "Adapter Created for " + context.getResources().getResourceEntryName(resource));
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Map<String, Object> getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /*
     * (non-Javadoc)
     * @see android.widget.SimpleAdapter#getView(int, android.view.View, android.view.ViewGroup)
     */
    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {

	        // We only create the view if its needed
	        if (view == null) {
	        	LayoutInflater inflater = (LayoutInflater) context
	                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	            view = inflater.inflate(resourceItem, viewGroup, false);
	        }
	        
	        //Depending on which fragment is calling this
	        if (resourceItem == R.layout.archive_item){
	        	
	        	//Searching for elements
	        	CheckBox checkBox = (CheckBox)view.findViewById(R.id.checkBox);
	        	ImageView iv = (ImageView) view.findViewById(R.id.status_icon);
	        	TextView phoneNumber = (TextView) view.findViewById(R.id.phone_number);
	            TextView dateCreated = (TextView) view.findViewById(R.id.date_created);
	            TextView text = (TextView) view.findViewById(R.id.text);
	            
	            //Setting data to elements
	        	phoneNumber.setText((String)data.get(position).get(SMS_PHONE));
	            dateCreated.setText((String)data.get(position).get(SMS_DATE));
	            text.setText((String)data.get(position).get(SMS_TEXT));
	        	
	        	// Setting icon depending on the SMS state
	            if (iv != null)
	                switch ((Byte) data.get(position).get("state")) {
	                    case 3:
	                        iv.setImageResource(R.drawable.status_icon_sent);
	                        break;
	                    case 4:
	                        iv.setImageResource(R.drawable.status_icon_delivered);
	                        break;
	                    case 5:
	                        iv.setImageResource(R.drawable.status_icon_derror);
	                        break;
	                    case 6:
	                        iv.setImageResource(R.drawable.status_icon_error);
	                        break;
	                }
	            // Set the click listener for the checkbox
	            checkBox.setChecked(false);
	            checkBox.setOnClickListener(this);
	        }else{
	        	//Searching for elements
	        	CheckBox checkBox = (CheckBox)view.findViewById(R.id.checkBox);
	            TextView dateCreated = (TextView) view.findViewById(R.id.date_created);
	            TextView text = (TextView) view.findViewById(R.id.text);
	            
	            //Setting data to elements
	            dateCreated.setText((String)data.get(position).get(SMS_DATE));
	            text.setText((String)data.get(position).get(SMS_TEXT));
	        	
	            // Set the click listener for the checkbox
	            // Set the click listener for the checkbox
	            checkBox.setChecked(false);
	            checkBox.setOnClickListener(this);
	            
	        }
        return view;
    }

    /*
     * Fires when Item is clicked. Method fills phone field and text field of the NewSMSTab with data from the clicked SMS
     * (non-Javadoc)
     * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View, int, long)
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        SMS_DAO smsDao = new SMS_DAO(context);
        int smsId = (Integer) data.get(position).get("id");
        //Retrieving NewSMSTab & its fields 
        Fragment newSMSTab = ((MainActivity) context).getSupportFragmentManager().getFragments().get(0);
        AutoCompleteTextView phone = (AutoCompleteTextView) newSMSTab.getView().findViewById(R.id.phoneNumberField);
        EditText text = (EditText) newSMSTab.getView().findViewById(R.id.smsTextField);
        //Filling fields
        text.setText(smsDao.getSMS(smsId).getText());
        if (((MainActivity) context).getActionBar().getSelectedTab().getPosition() == 0)
            phone.setText(smsDao.getSMS(smsId).getPhoneNumber());
        ((MainActivity) context).getSupportActionBar().setSelectedNavigationItem(1);
    }

    @Override
    /** Will be called when a checkbox has been clicked. */
    public void onClick(View view) {
        //We are looking for position of the clicked checkbox
        CheckBox cb = (CheckBox) view;
        ListView lvMain = (ListView) cb.getParent().getParent();
        int position = lvMain.getPositionForView(cb);

        //Marking List item as checked or not
        if (cb.isChecked()) data.get(position).put("checked", true);
        else data.get(position).put("checked", false);
    }
    

	/*
	 * This method updates the state of sent SMS, retrieves SMS data & pack it to the structure for adapter(ArrayList<Map<String, Object>>)
	 * @return ArrayList<Map<String, Object>> SMS data for this tab
 	*/
	public void getData() {
		final  ArrayList<Map<String, String>> contactData = Utils.getContactData((MainActivity)context); //Get device contacts
		    //Updating SMS state
            Log.d(LOG_TAG,"Update SMS state");
            try {
				new SMSGatewayConnect(context).execute("updateSMSState").get();
			} catch (InterruptedException e) {
				Log.e(LOG_TAG, e.getMessage());
			} catch (ExecutionException e) {
				Log.e(LOG_TAG, e.getMessage());
			}
		// Initializing DB Object
		SMS_DAO smsDao = new SMS_DAO(context);

		// Find all SMS needed for this tab
		List<SMS> smsList = new ArrayList<SMS>() ;
		if (resourceItem == R.layout.archive_item){
			smsList.addAll(smsDao.getAllSMS((byte) 3));
			smsList.addAll(smsDao.getAllSMS((byte) 4));
			smsList.addAll(smsDao.getAllSMS((byte) 5));
			smsList.addAll(smsDao.getAllSMS((byte) 6));
		}else{
			smsList.addAll(smsDao.getAllSMS((byte) 2));
		}
		
		// I want SMS to be sorted by date they were sent
		Collections.sort(smsList, new Comparator<SMS>(){
		    @Override
		    public int compare(SMS o1, SMS o2) {
		        return (o1.getDateSent().getTime()>o2.getDateSent().getTime() ? -1 : (o1.getDateSent().getTime()==o2.getDateSent().getTime() ? 0 : 1));
		    }
		});

		// Packing data for adapter
		ArrayList<Map<String, Object>> data = new ArrayList<Map<String, Object>>(smsList.size());
		Map<String, Object> m;
		for (int i = 0; i < smsList.size(); i++) {

			m = new HashMap<String, Object>();
			m.put(SMS_ID, smsList.get(i).getId());
			m.put(SMS_TEXT, smsList.get(i).getText());
			m.put(SMS_STATE, smsList.get(i).getState());
			//I want SMS_PHONE to be formatted like in phonebook in the case that it is already in contacts
			for (Map<String, String> contact : contactData) {
				contact.put("Phone", Utils.formatPhoneNumber(contact.get("Phone")));//just formatting phone, so it could be compared
				if (contact.containsValue(smsList.get(i).getPhoneNumber()))
					m.put(SMS_PHONE, contact.get("Name")); //in the case phone is already in contacts retrieving the Name of the person
				else
					m.put(SMS_PHONE, smsList.get(i).getPhoneNumber()); //else put as is 
			}
			//Different Date formatting depending on the Tab
			if (resourceItem == R.layout.archive_item)
				m.put(SMS_DATE,	Utils.formatDateTwoLines(smsList.get(i).getDateSent(),context.getResources().getConfiguration().locale));
			else
				m.put(SMS_DATE,	Utils.formatDateOneLine(smsList.get(i).getDateSent(),context.getResources().getConfiguration().locale));
			
			data.add(m);
		}
		this.data=data;
		Log.d(LOG_TAG, "Data changed!");
	}


}