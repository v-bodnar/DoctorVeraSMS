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
 * Templates tab, creates ListView to show template SMS (state 2), it updates data onResume
 * @author      Volodymyr Bodnar
 * @version     %I%, %G%
 * @since       1.0
 */

public class TemplateTab extends Fragment {

	private ListView lvMain;
	CheckboxListAdapter sAdapter;
    private final String LOG_TAG = "MyLogs TemplateTab";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
    	Log.d(LOG_TAG,"onCreateView");

        // Inflate the layout for this fragment
        View V = inflater.inflate(R.layout.template_tab, container, false);

        // Searching View
        lvMain = (ListView) V.findViewById(R.id.template_list);

        // Creating adapter
       sAdapter = new CheckboxListAdapter(getActivity(), new ArrayList<Map<String, Object>>(),R.layout.template_item);

        // Setting Adapter
        lvMain.setOnItemClickListener(sAdapter);
        lvMain.setAdapter(sAdapter);

        //Refreshing menu items
        getActivity().invalidateOptionsMenu();

        return V;
    }


}