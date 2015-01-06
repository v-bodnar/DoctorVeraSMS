package ua.kiev.doctorvera;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;
import android.util.Log;
import android.widget.Toast;

import com.google.android.vending.licensing.AESObfuscator;
import com.google.android.vending.licensing.LicenseChecker;
import com.google.android.vending.licensing.LicenseCheckerCallback;
import com.google.android.vending.licensing.Policy;
import com.google.android.vending.licensing.ServerManagedPolicy;

public class LicensingService {

    private static final String BASE64_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAiWmXBu4fyPI2jz6EuyMHbcZp2K0zJPF3Kvlelz0bpt9jsA7OVOB+Hm6XdeckY1fbba4UDd2r8Vzc2Se3gY+WnFg/664o2b2toat45O6jfYK7h9Ip52VExEeoAKW6U4WbnmSAkIf1I3s0AZfK/KNFiCQyVYEME2lzq6YOKZcs5Hn0qWbDad2bXkiwos51gPIC7LSZ0dgW6sTPAIkkJX9lpfGb0gX/IKb97xbuCsdK6Y0MAQrRNcFH0Ql+/q/2c9GL1KVix0Xh0zWG++NHiglwQjF0Dsv6OkeqOXlvMna+vJE7W9DU+G4OasQmCCzQOm9nJcnFhBKmKSfanyUNEzEKZwIDAQAB";

    // Generate your own 20 random bytes, and put them here.
    private static final byte[] SALT = new byte[] {
        -46, 65, 22, -118, -103, -57, 74, -55, 51, 88, -33, -45, 17, -117, -55, -113, -11, 23, -64,
        89
    };

    private LicenseCheckerCallback mLicenseCheckerCallback;
    private LicenseChecker mChecker;
    // A handler on the UI thread.
    private Handler mHandler;

    private SharedPreferences  sPref;
    private final String LOG_TAG = "myLogs LicensingService";
    private Activity activity;
    
    public LicensingService(Activity activity){
    	this.activity=activity;
    	mHandler = new Handler();
		sPref = PreferenceManager.getDefaultSharedPreferences(activity);
		
        // Try to use more data here. ANDROID_ID is a single point of attack.
        String deviceId = Secure.getString(activity.getApplicationContext().getContentResolver(), Secure.ANDROID_ID) + sPref.getString("ID", null);

        // Library calls this when it's done.
        mLicenseCheckerCallback = new MyLicenseCheckerCallback();
        
        // Construct the LicenseChecker with a policy.
        mChecker = new LicenseChecker(
        		activity.getApplicationContext(), new ServerManagedPolicy(activity.getApplicationContext(),
                new AESObfuscator(SALT, activity.getApplicationContext().getPackageName(), deviceId)),
            BASE64_PUBLIC_KEY);
    }
    
    

    
    /*
	 * Android Market licensing
	 * -------------------------------------------------
	 */
	


    public void checkGoogleLicense() {
    	//((MainActivity) activity).setProgressBarIndeterminateVisibility(true);
    	Log.d(LOG_TAG,"Checking Google license");
    	mChecker.checkAccess(mLicenseCheckerCallback);
    }

    private void displayResult(final String result) {
        mHandler.post(new Runnable() {
            public void run() {
                Toast.makeText(activity.getApplicationContext(), result, Toast.LENGTH_LONG).show();
                //((MainActivity) activity).setProgressBarIndeterminateVisibility(false);
            }
        });
    }
    
    private void displayDialog(final boolean showRetry) {
        mHandler.post(new Runnable() {
			public void run() {
            	RetryDialog retryDialog = new RetryDialog(showRetry);
            	retryDialog.show(activity.getFragmentManager(),"LicenseDialog");
            }
        });
    }    

    private class MyLicenseCheckerCallback implements LicenseCheckerCallback {
        public void allow(int policyReason) {
            if (activity.isFinishing()) {
                // Don't update UI if Activity is finishing.
                return;
            }
            // Should allow user access.
            Editor ed = sPref.edit();
            ed.putString("GOOGLE LICENSE", "" + true);
            ed.commit();
            Log.d(LOG_TAG,"Google license is active");
            displayResult(activity.getApplicationContext().getString(R.string.google_valid));
    	    //Creating Own license

        }

        public void dontAllow(int policyReason) {
            if (activity.isFinishing()) {
                // Don't update UI if Activity is finishing.
                return;
            }
            Log.d(LOG_TAG,"Google license is not active");
            Editor ed = sPref.edit();
            ed.putString("GOOGLE LICENSE", "" + false);
            ed.commit();
            displayResult(activity.getApplicationContext().getString(R.string.google_not_valid));
            // Should not allow access. In most cases, the app should assume
            // the user has access unless it encounters this. If it does,
            // the app should inform the user of their unlicensed ways
            // and then either shut down the app or limit the user to a
            // restricted set of features.
            // In this example, we show a dialog that takes the user to Market.
            // If the reason for the lack of license is that the service is
            // unavailable or there is another problem, we display a
            // retry button on the dialog and a different message.
            displayDialog(policyReason == Policy.RETRY);
        }

        public void applicationError(int errorCode) {
            if (activity.isFinishing()) {
                // Don't update UI if Activity is finishing.
                return;
            }
            // This is a polite way of saying the developer made a mistake
            // while setting up or calling the license checker library.
            // Please examine the error code and fix the error.
            Editor ed = sPref.edit();
            ed.putString("GOOGLE LICENSE", "" + false);
            ed.commit();
            Log.d(LOG_TAG,"Google license error: " + errorCode);
            String result = String.format(activity.getApplicationContext().getString(R.string.application_error), errorCode);
            displayResult(result);
        }
    }

    public void onDestroy() {
        mChecker.onDestroy();
    }

    private class RetryDialog extends DialogFragment {
    	private boolean bRetry;
    	
    	public RetryDialog(boolean bRetry){
    		super();
    		this.bRetry = bRetry;
    	}
    	
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.unlicensed_dialog_title);
            builder.setMessage(bRetry ? R.string.unlicensed_dialog_retry_body : R.string.unlicensed_dialog_body);
            builder.setPositiveButton(bRetry ? R.string.retry_button : R.string.buy_button, new DialogInterface.OnClickListener() {
                boolean mRetry = bRetry;
                public void onClick(DialogInterface dialog, int which) {
                    if ( mRetry ) {
                    	checkGoogleLicense();
                    } else {
                        Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                                "http://market.android.com/details?id=" + activity.getApplicationContext().getPackageName()));
                        activity.getApplicationContext().startActivity(marketIntent);                        
                    }
                }
            });
            builder.setNegativeButton(R.string.quit_button, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    System.exit(0);;
                }
            });
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }
}
