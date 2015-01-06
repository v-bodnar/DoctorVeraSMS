package ua.kiev.doctorvera;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ListView;

/*
 * Main activity creates three tabs
 * <ul>
 * <li>Archive - shows list of sent SMS</li>
 * <li>New SMS - tab where you can create new SMS</li>
 * <li>Templates - shows list of saved SMS templates</li>
 * </ul>
 * <p>Adapters, and listeners are connected to tabs here</p>
 * <p>This class creates menu items and handles menu actions </p>
 * @author      Volodymyr Bodnar
 * @version     %I%, %G%
 * @since       1.0
 */
public class MainActivity extends ActionBarActivity {

    
	// Declare Tab Variable
	private ActionBar.Tab tab1, tab2, tab3;
	private ViewPager viewPager; // Main activity view
	private TabsPagerAdapter mAdapter; // Handling tabs and swipe
	private ActionBar actionBar;
	private SMS_DAO smsDao = new SMS_DAO(this); // Object to manipulate SMS
												// objects inside the database
	private final String LOG_TAG = "MyLogs MainActivity";
	private SharedPreferences sPref;

	
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v7.app.ActionBarActivity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		requestWindowFeature(Window.FEATURE_PROGRESS);

		//Initializing Shared Preferenses
		sPref = PreferenceManager.getDefaultSharedPreferences(this);
		//Setting Locale
	    Utils.setLocale(this, sPref.getString("LANGUAGE", Locale.getDefault().toString())); 
	    
	    //Generating ID for Application
	    if (sPref.getString("ID", null)==null){
	        Editor ed = sPref.edit();
	        Integer id = new Random().nextInt(Integer.MAX_VALUE);
	        ed.putString("ID", "" + id);
	        ed.commit();
	        Log.d(LOG_TAG, "Generated new Program ID: " + id);
	    }
	    
	    //Check Settings, if empty call Settings Activity
	    if (sPref.getString("LOGIN", null)==null && 
	    		sPref.getString("PASSWORD", null)==null &&
	    		sPref.getString("ALPHA NAME", null)==null && 
	    		sPref.getString("MY LICENSE", null)==null){
	    	
	    	Log.d(LOG_TAG, "Authentification settings are empty, calling Settings Activity ");
	    	
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
	    }
	    
		
		// Initilization
		setContentView(R.layout.activity_main);
		
		viewPager = (ViewPager) findViewById(R.id.activity_main);
		actionBar = getSupportActionBar();
		mAdapter = new TabsPagerAdapter(getSupportFragmentManager(),
				savedInstanceState, this);

		viewPager.setAdapter(mAdapter);
		viewPager.setOnPageChangeListener(mAdapter);
		actionBar.setHomeButtonEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Set Tab Titles
		tab1 = actionBar.newTab().setText(R.string.tab1_header);
		tab2 = actionBar.newTab().setText(R.string.tab2_header);
		tab3 = actionBar.newTab().setText(R.string.tab3_header);

		// Set Tab Tag
		tab1.setTag("ArchiveTab");
		tab2.setTag("NewSMSTab");
		tab3.setTag("TemplateTab");

		// Set Tab Listeners
		tab1.setTabListener(mAdapter);
		tab2.setTabListener(mAdapter);
		tab3.setTabListener(mAdapter);

		// Add tabs to actionbar
		actionBar.addTab(tab1);
		actionBar.addTab(tab2, true); // Active Tab
		actionBar.addTab(tab3);

		getActionBar().show();
		

        
	} // onCreate


	/*
	 * Creating menu items
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	/*
	 * Refreshing menu items depending on the active tab
	 * 
	 * @see android.app.Activity#onPrepareOptionsMenu(android.view.Menu)
	 */
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem deleteChecked = (MenuItem) menu.findItem(R.id.delete_checked);
		MenuItem deleteAll = (MenuItem) menu.findItem(R.id.clear_all);
		int tab = getActionBar().getSelectedTab().getPosition();
		// Log.d("tab#",""+tab);
		switch (tab) {
		case 0:
			deleteChecked.setVisible(true);
			deleteAll.setVisible(true);
			break;
		case 1:
			deleteChecked.setVisible(false);
			deleteAll.setVisible(false);
			break;
		case 2:
			deleteChecked.setVisible(true);
			deleteAll.setVisible(true);
			break;
		}
		return super.onPrepareOptionsMenu(menu);
	}

	/*
	 * Handling menu options
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		// Find which Menu Item has been selected
		switch (item.getItemId()) {
		// Check for each known Menu Item
		
		//Settings activity
		case (R.id.action_settings):
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			return true;
		//Quit button
		case (R.id.quit):
			System.exit(0);
			return true;
		// Delete checked SMS
		case (R.id.delete_checked):
			deleteChecked();
			return true;
		//Depending on the tab selected delete Template SMS or Archived SMS
		case (R.id.clear_all):
			int tab = getActionBar().getSelectedTab().getPosition();
			deleteConfirm(tab);
			return true;
			
		// Return false if you have not handled the Menu Item
		default:
			return false;
		}
	}

	/*
	 * Creating confirmation dialog for deleting SMS
	 * @param tab indicates the state of SMS to be deleted: 2 - templates, 0 - all other
	 */
	private void deleteConfirm(int tab) {
		ArrayList<SMS> smsToDelete = new ArrayList<SMS>();
		
		switch (tab){
			case 0:
				smsToDelete.addAll(smsDao.getAllSMS((byte)3));
				smsToDelete.addAll(smsDao.getAllSMS((byte)4));
				smsToDelete.addAll(smsDao.getAllSMS((byte)5));
				smsToDelete.addAll(smsDao.getAllSMS((byte)6));
				if (smsToDelete.size() > 0) {
					DialogDeleteSMS dialog = new DialogDeleteSMS((ListView) findViewById(R.id.archive_list));
					dialog.setSMStoDelete(smsToDelete);
					dialog.show(getSupportFragmentManager(), "delete_all_sms");
				}
				break;
			case 2:
				smsToDelete.addAll(smsDao.getAllSMS((byte)2));
				if (smsToDelete.size() > 0) {
					DialogDeleteSMS dialog = new DialogDeleteSMS((ListView) findViewById(R.id.template_list));
					dialog.setSMStoDelete(smsToDelete);
					dialog.show(getSupportFragmentManager(), "delete_all_sms");
				}
				break;
		}
		

	}

	/*
	 * Searches for checked list items depending on the active tab, and retrieves proper SMS object
	 */
	@SuppressWarnings("unchecked")
	private void deleteChecked() {
		
		ArrayList<SMS> smsToDelete = new ArrayList<SMS>();
		int tab = getActionBar().getSelectedTab().getPosition(); //active tab
		ListView lvMain = null;
		if (tab == 2) lvMain = (ListView) findViewById(R.id.template_list);
		if (tab == 0) lvMain = (ListView) findViewById(R.id.archive_list);
		
		//Searching for checked 
		HashMap<String, Object> item;
		for (int i = 0; i < lvMain.getCount(); i++) {
			item = (HashMap<String, Object>) lvMain.getItemAtPosition(i);
			// Log.d("myLog", "" + item);
			if (item.containsKey("checked") && item.get("checked").equals(true))
				smsToDelete.add(smsDao.getSMS((Integer) item.get("id")));
		}
		
		//Creating confirmation dialog for deleting checked SMS
		if (smsToDelete.size() > 0) {
			DialogDeleteSMS dialog = new DialogDeleteSMS(lvMain);
			dialog.setSMStoDelete(smsToDelete);
			dialog.show(getSupportFragmentManager(), "delete_sms");
		}

	}
	


}