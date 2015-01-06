package ua.kiev.doctorvera;

import java.io.IOException;
import java.io.StringReader;
import java.sql.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.ListView;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

/*
 * Different static methods that can be used by multiple classes for formatting text, date, getting contacts etc.
 * @author      Volodymyr Bodnar
 * @version     %I%, %G%
 * @since       1.0
 */

public class Utils {
	private static final String LOG_TAG = "MyLogs Utils";

	// Returns current Date Time as sql.Date
	public static Date getDateTime() {
		java.util.Date date = new java.util.Date();
		Date sqlDate = new Date(date.getTime());
		return sqlDate;
	}
	
	// Returns current Date Time as sql.Date
	public static Date getUTCDateTime(String UTCDate) {
        try {
        	SimpleDateFormat simpleFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.US);
            //Fri, 19 Sep 2014 02:20:23 +0300
			java.util.Date date = simpleFormat.parse(UTCDate);
			Date sqlDate = new Date(date.getTime());
			return sqlDate;
		} catch (ParseException e) {
			Log.e(LOG_TAG,e.getMessage());
			return null;
		}
	}

	// Converting phone number to E164 format
	public static String formatPhoneNumber(String phoneNumber) {
		try {
			PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
			Phonenumber.PhoneNumber parsedNumber = phoneUtil.parse(phoneNumber,
					"UA");
			return phoneUtil.format(parsedNumber,
					PhoneNumberUtil.PhoneNumberFormat.E164);
		} catch (NumberParseException e) {
			Log.d("formatPhoneNumber(" + phoneNumber + ")", e.toString());
			return "";
		}
	}

	// Formats Date in two lines dd MMMMM yyyy HH:mm
	public static String formatDateTwoLines(Date date, Locale locale) {
		DateFormat df = new SimpleDateFormat("dd MMMMM yyyy \n HH:mm", locale);
		return df.format(date);
	}

	// Formats Date in one line dd MMMMM yyyy HH:mm
	public static String formatDateOneLine(Date date, Locale locale) {
		DateFormat df = new SimpleDateFormat("dd MMMMM yyyy HH:mm", locale);
		return df.format(date);
	}

	// Method generates XML document
	public static Document loadXMLFromString(String xml) {
		Document doc = null;
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {

			DocumentBuilder db = dbf.newDocumentBuilder();
			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(xml));
			doc = db.parse(is);

		} catch (ParserConfigurationException e) {
			Log.e(LOG_TAG, e.getMessage());
			return null;
		} catch (SAXException e) {
			Log.e(LOG_TAG, e.getMessage());
			return null;
		} catch (IOException e) {
			Log.e(LOG_TAG, e.getMessage());
			return null;
		}
		// return DOM
		return doc;
	}

	// Method retrieves nodes from XML
	public static final String getElementValue(Node elem) {
		Node child;
		if (elem != null) {
			if (elem.hasChildNodes()) {
				for (child = elem.getFirstChild(); child != null; child = child
						.getNextSibling()) {
					if (child.getNodeType() == Node.TEXT_NODE) {
						return child.getNodeValue();
					}
				}
			}
		}
		return "";
	}

	// Read phone contact name and phone numbers
	public static ArrayList<Map<String, String>> getContactData(
			Activity activity) {
		// Store contacts values in these arraylist
		ArrayList<Map<String, String>> peopleList = new ArrayList<Map<String, String>>();

		try {

			/*********** Reading Contacts Name And Number **********/

			String phoneNumber = "";
			ContentResolver cr = activity.getBaseContext().getContentResolver();

			// Query to get contact name

			Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null,
					null, null, null);

			// If data data found in contacts
			if (cur.getCount() > 0) {

				Log.i("AutocompleteContacts", "Reading   contacts........");

				// int k = 0;
				String name = "";

				while (cur.moveToNext()) {

					String id = cur.getString(cur
							.getColumnIndex(ContactsContract.Contacts._ID));
					name = cur
							.getString(cur
									.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

					// Check contact have phone number
					if (Integer
							.parseInt(cur.getString(cur
									.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {

						// Create query to get phone number by contact id
						Cursor pCur = cr
								.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
										null,
										ContactsContract.CommonDataKinds.Phone.CONTACT_ID
												+ " = ?", new String[] { id },
										null);
						int j = 0;

						while (pCur.moveToNext()) {
							// Sometimes get multiple data
							if (j == 0) {
								// Get Phone number
								phoneNumber = ""
										+ pCur.getString(pCur
												.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

								// Add contacts names to adapter
								Map<String, String> NamePhoneType = new HashMap<String, String>();
								NamePhoneType.put("Name", name.toString());
								NamePhoneType.put("Phone",
										phoneNumber.toString());
								peopleList.add(NamePhoneType);

								j++;
							}
						} // End while loop
						pCur.close();

					} // End if

				} // End while loop

			} // End Cursor value check
			cur.close();
			return peopleList;

		} catch (Exception e) {
			Log.i("AutocompleteContacts", "Exception : " + e);
			return null;
		}
	}

	// Changing locale
	public static void setLocale(Context context, String language) {
		Locale locale = null;
		if (language.equals("English")) {
			locale = new Locale("en");
		} else if (language.equals("Русский")) {
			locale = new Locale("ru");
		} else if (language.equals("Українська")) {
			locale = new Locale("uk");
		}
		Locale.setDefault(locale);
		Configuration config = new Configuration();
		config.locale = locale;
		context.getResources().updateConfiguration(config,
				context.getResources().getDisplayMetrics());
	}
	
	public static void listUpdate(ListView lvMain){
		CheckboxListAdapter sAdapter=(CheckboxListAdapter)lvMain.getAdapter();
		sAdapter.getData();
		sAdapter.notifyDataSetChanged();
	}

}
