package ua.kiev.doctorvera;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.widget.ListView;

/*
 * <p>This class creates confirmation dialog window with the list of SMS marked to deletion. </p>
 * @author      Volodymyr Bodnar
 * @version     %I%, %G%
 * @since       1.0
 */
public class DialogDeleteSMS extends DialogFragment {
    private ArrayList<SMS> smsToDelete;

    private SMS_DAO smsDao;
    private final String LOG_TAG = "myLogs DialogDeleteSMS";
    private ListView fragmentListView;
    
    public DialogDeleteSMS(ListView fragmentListView){
    	super();
    	this.fragmentListView = fragmentListView;
    }
    
    public void setSMStoDelete(ArrayList<SMS> smsToDelete) {
        this.smsToDelete = smsToDelete;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        smsDao = new SMS_DAO(getActivity());
        String dialogText = getString(R.string.delete_sms_text) + "\n";
        if(smsToDelete.size()>10) dialogText =getString(R.string.delete_sms_text) +" "+ smsToDelete.size() +" "+ getString(R.string.delete_sms_text_end); 
        else for (SMS sms : smsToDelete)
            dialogText += sms.getDateSent() + " " + sms.getText() + "\n";
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.delete_sms_header);
        builder.setMessage(dialogText);
        // positive button
        builder.setPositiveButton(R.string.delete_sms_yes, myClickListener);
        // negative button
        builder.setNegativeButton(R.string.delete_sms_no, myClickListener);
        // Create the AlertDialog object and return it
        return builder.create();
    }

    DialogInterface.OnClickListener myClickListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                // positive button
                case Dialog.BUTTON_POSITIVE:
                    for (SMS sms : smsToDelete)
                        smsDao.deleteSMS(sms);
                    Log.d(LOG_TAG, "Deleted "+ smsToDelete.size() + "SMS");
            		//Updating ListView
            		Utils.listUpdate(fragmentListView);
                    break;
                // negative button
                case Dialog.BUTTON_NEGATIVE:
                    DialogDeleteSMS.this.dismiss();
                    break;
            }

        }
    };
}