package ua.kiev.doctorvera;

import java.util.ArrayList;
import java.util.Map;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

/*
 * Archive tab, creates ListView to show archives SMS (states 3-6), it updates data onResume
 * @author      Volodymyr Bodnar
 * @version     %I%, %G%
 * @since       1.0
 */

public class ArchiveTab extends Fragment {
	private ListView lvMain;

	private final String LOG_TAG = "MyLogs ArchiveTab";
    
	/*
	 * (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {		
    	Log.d(LOG_TAG,"onCreateView");
		// Inflate the layout for this fragment
		View V = inflater.inflate(R.layout.archive_tab, container, false);

		// searching List View
		lvMain = (ListView) V.findViewById(R.id.archive_list);
		
		// creating adapter
		CheckboxListAdapter sAdapter = new CheckboxListAdapter(getActivity(),
				new ArrayList<Map<String, Object>>(), R.layout.archive_item);

		// setting adapter
		lvMain.setOnItemClickListener(sAdapter);
		lvMain.setAdapter(sAdapter);

		// Refreshing menu items
		getActivity().invalidateOptionsMenu();

		setRetainInstance(true);
		return V;
	}

}