package ua.kiev.doctorvera;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
/*
 * This class send data to SMS Gateway. It can send SMS and get SMS state. Also it can check Internet connection
 * @author      Volodymyr Bodnar
 * @version     %I%, %G%
 * @since       1.0
 */
public class SMSGatewayConnect extends AsyncTask<Object, Void, Object> {
	private SharedPreferences  sPref;
    private String LOGIN;
    private String PASS;
    private String FROM;
    private String ID;
    private  final String SMS_SEND_URL = "https://api.life.com.ua/ip2sms/";
    private  final String SMS_SATE_URL = "https://api.life.com.ua/ip2sms-request/";
    
    private final String PARAM_NAME_LOGIN="LOGIN";
    private final String PARAM_NAME_PASSWORD="PASSWORD";
    private final String PARAM_NAME_ALPHA_NAME="ALPHA_NAME";
    private final String PARAM_NAME_ID="ID";
    private Context context; 

    
    private  final String LOG_TAG = "mylogs SMSGatewayConnect";
    
    public SMSGatewayConnect(Context context){
    	this.context = context;
	    //Initializing preferences 
	    sPref = PreferenceManager.getDefaultSharedPreferences(context);
	    LOGIN = sPref.getString(PARAM_NAME_LOGIN, null); 
	    PASS = sPref.getString(PARAM_NAME_PASSWORD, null);
	    FROM = sPref.getString(PARAM_NAME_ALPHA_NAME, null); 
	    ID = sPref.getString(PARAM_NAME_ID, null);     
    }     
    

	@Override
	protected Object doInBackground(Object... params) {

		if (params[0].equals("isNetworkConnected")) return isNetworkConnected(context);
		if (params[0].equals("isInternetAvailable")) return isInternetAvailable();
		if (params[0].equals("checkGatewayAuth")) return checkGatewayAuth((String)params[1], (String)params[2], (String)params[3]);
		if (params[0].equals("sendSMS")) return sendSMS((SMS)params[1]);
		if (params[0].equals("checkState")) return checkState((SMS)params[1]);
		if (params[0].equals("updateSMSState")) return updateSMSState((Activity)context);
		if (params[0].equals("isLicenseActive")) return isLicenseActive((String)params[1]);
		return false;
	}

    
	/*
	 * Checking if any network is connected
	 */
	private boolean isNetworkConnected(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		if (ni != null && ni.isConnected()) {
			Log.d(LOG_TAG, "Are there active networks: "+true);
			return true;
		} else{
			// There are no active networks.
			Log.d(LOG_TAG, "Are there active networks: "+false);
			return false;

		}
	}

	/*
	 * Checking if Internet is reachable
	 */
	private boolean isInternetAvailable() {
		Socket sock = null;

		try {
			InetAddress host = InetAddress.getByName("doctorvera.kiev.ua");
		    SocketAddress sockaddr = new InetSocketAddress(host, 80);
		    // Create an unbound socket
		    sock = new Socket();

		    // This method will block no more than timeoutMs.
		    // If the timeout occurs, SocketTimeoutException is thrown.
		    int timeoutMs = 2000;   // 2 seconds
		    sock.connect(sockaddr, timeoutMs);
		    Log.d(LOG_TAG,"Internet is reachable:  true");
		    return true;
		}catch(Exception e){
			Log.e(LOG_TAG, e.getMessage());
		}finally{
			try {
				if (sock != null)
				sock.close();
			} catch (IOException e) {
				Log.e(LOG_TAG, e.getMessage());
			}
		}
		Log.d(LOG_TAG,"Internet is reachable:  false");
		return false;

	}
	
	
	/*
	 * Checking if Gateway is reachable
	 */
    private  Boolean checkGatewayAuth(String login, String password, String alpha){
    	if(login==null || password==null  || alpha==null) return false;
    	final String MESSAGE = "<message><service id='single' source='"+alpha+"'/><to>1234</to><body content-type='text/plain'>test</body></message>";
        HttpClient httpclient = new DefaultHttpClient();
        try {
            StringEntity entity = new StringEntity(MESSAGE, "UTF-8");
            entity.setContentType("text/xml");
            entity.setChunked(true);
            
            HttpPost httpPost = new HttpPost(SMS_SEND_URL);
            httpPost.setEntity(entity);
            httpPost.addHeader(BasicScheme.authenticate(new UsernamePasswordCredentials(login, password), "UTF-8", false));

            StrictMode.ThreadPolicy policy = new
                    StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
            HttpResponse response = httpclient.execute(httpPost);
            Log.d(LOG_TAG, "Checking if Login, Password, Alpha Name are right:" + (response.getStatusLine().getStatusCode() == 200));
            if(EntityUtils.toString(response.getEntity()).contains("id=")) return true;
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
        } finally {
            httpclient.getConnectionManager().shutdown();
        }
        return false;
    }
    
	/*
	 * Send SMS to Gateway
	 */
    private  ArrayList<String> sendSMS(SMS sms) {
        ArrayList<String> result = new ArrayList<String>();
        final String MESSAGE = "<message><service id='single' source='" + FROM + "'/><to>" + sms.getPhoneNumber() + "</to><body content-type='text/plain'>" + sms.getText() + "</body></message>";
        
        HttpClient httpclient = new DefaultHttpClient();
        String xml = null;
        try {
            HttpPost httpPost = new HttpPost(SMS_SEND_URL);

            StringEntity entity = new StringEntity(MESSAGE, "UTF-8");
            entity.setContentType("text/xml");
            entity.setChunked(true);
            httpPost.setEntity(entity);
            httpPost.addHeader(BasicScheme.authenticate(new UsernamePasswordCredentials(LOGIN, PASS), "UTF-8", false));
            HttpResponse response = httpclient.execute(httpPost);
            HttpEntity resEntity = response.getEntity();
            Log.d(LOG_TAG, "Sending SMS: " + (response.getStatusLine().getStatusCode()==200));
            xml = EntityUtils.toString(resEntity);
        } catch (Exception e) {
            Log.e(LOG_TAG, ""+e.getStackTrace());
        } finally {
            httpclient.getConnectionManager().shutdown();
        }

        //parsing xml result
        Document doc = Utils.loadXMLFromString(xml);
        NodeList nl = doc.getElementsByTagName("status");
        Element status = (Element) nl.item(0);

        result.add(0, status.getAttribute("id").toString()); //tracking id at position 0
        result.add(1, status.getAttribute("date").toString()); //date at position 1
        result.add(2, Utils.getElementValue(status.getFirstChild())); //state at position 2
        return result;
    }

	/*
	 * Check SMS State
	 */
    private  SMS checkState(SMS sms) {

        //ArrayList<String> result = new ArrayList<String>();
        final String MESSAGE = "<request id='" + sms.getTrackingId() + "'>status</request>";
        HttpClient httpclient = new DefaultHttpClient();
        String xml = null;
        try {
            HttpPost httpPost = new HttpPost(SMS_SATE_URL);

            StringEntity entity = new StringEntity(MESSAGE, "UTF-8");
            entity.setContentType("text/xml");
            entity.setChunked(true);

            httpPost.setEntity(entity);
            httpPost.addHeader(BasicScheme.authenticate(new UsernamePasswordCredentials(LOGIN, PASS), "UTF-8", false));
            HttpResponse response = httpclient.execute(httpPost);
            HttpEntity resEntity = response.getEntity();
            
            xml = EntityUtils.toString(resEntity);
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
        } finally {
            httpclient.getConnectionManager().shutdown();
        }

        //parsing xml result
        Document doc = Utils.loadXMLFromString(xml);
        NodeList nl = doc.getElementsByTagName("status");
        Element status = (Element) nl.item(0);
        sms.setStateString(Utils.getElementValue(status.getFirstChild())); //state at position 2
        //Log.d(LOG_TAG,Utils.getElementValue(status.getFirstChild()));
        Log.d(LOG_TAG, "Checking SMS id = " + sms.getTrackingId() + " state: " + Utils.getElementValue(status.getFirstChild()));
        return sms;
    }
 
    /*
     * This will update state of each SMS with state - 3
     */
	private  Boolean updateSMSState(Activity activity){
		
		// Initializing DB Object
		SMS_DAO smsDao = new SMS_DAO(activity);

		// Array with Objects
		List<SMS> smsList = smsDao.getAllSMS((byte) 3);
		
		if(smsList.isEmpty()) Log.d(LOG_TAG, "No SMS for status update!");
		else {
	        if(!isNetworkConnected(activity) && !isInternetAvailable())Toast.makeText(activity, R.string.internet_connection_error, Toast.LENGTH_LONG).show(); 
	        else{
				for (SMS sms : smsList)
					smsDao.updateSMS(checkState(sms));
				Log.d(LOG_TAG, "SMS status updated!");
				return true;
	        }
		}
		return false;
		
	}
	
	/*
     * Checks My License from doctorvera.kiev.ua
     */
	private Boolean isLicenseActive(String login){
        HttpClient httpclient = new DefaultHttpClient();
        String xml = null;
        try {
            HttpPost httpPost = new HttpPost("http://www.doctorvera.kiev.ua/DrVeraSMSLic.php");
            // Request parameters and other properties.
            List<NameValuePair> params = new ArrayList<NameValuePair>(2);
            params.add(new BasicNameValuePair("id", ID));
            params.add(new BasicNameValuePair("login", login));
            
            httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
            HttpResponse response = httpclient.execute(httpPost);
            HttpEntity resEntity = response.getEntity();
            
            Log.d(LOG_TAG, "Requesting My License: " + (response.getStatusLine().getStatusCode()==200));
            xml = EntityUtils.toString(resEntity);
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
            return false;
        } finally {
            httpclient.getConnectionManager().shutdown();
        }
        //parsing xml result
        Document doc = Utils.loadXMLFromString(xml);
        NodeList nl = doc.getElementsByTagName("license");
        Element license = (Element) nl.item(0);
        Log.d(LOG_TAG, "My License status is: "+license.getTextContent());
        return Boolean.valueOf(license.getTextContent());
	}


	
}
