package ua.kiev.doctorvera;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.SimpleAdapter;

@SuppressLint("DefaultLocale")
public class ContactsAdapter extends SimpleAdapter implements Filterable  {

	private ArrayList<Map<String, String>> mAllData, mDataShown;

	@SuppressWarnings("unchecked")
	public ContactsAdapter(Context context, List<Map<String, String>> data, int resource, String[] from, int[] to) {
		super(context, data, resource, from, to);
		mDataShown = (ArrayList<Map<String, String>>) data;
		mAllData = (ArrayList<Map<String, String>>) mDataShown.clone();
	}

	@Override
	public Filter getFilter() {
		Filter nameFilter = new Filter(){

			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				if(constraint != null)
				{
					ArrayList<Map<String, String>> tmpAllData = mAllData;
					ArrayList<Map<String, String>> tmpDataShown = mDataShown;
					tmpDataShown.clear();  
					for(int i = 0; i < tmpAllData.size(); i++)
					{
						if(tmpAllData.get(i).get("Name").toLowerCase().contains(constraint.toString().toLowerCase()) || tmpAllData.get(i).get("Phone").toLowerCase().replaceAll(" ", "").contains(constraint.toString().toLowerCase()))
						{
							tmpDataShown.add(tmpAllData.get(i));
						}
					}

					FilterResults filterResults = new FilterResults();
					filterResults.values = tmpDataShown;
					filterResults.count = tmpDataShown.size();
					return filterResults;
				}
				else
				{
					return new FilterResults();
				}
			}

			@Override
			protected void publishResults(CharSequence constraint,
					FilterResults results) {
				if(results != null && results.count > 0)
				{
					notifyDataSetChanged();
				}
			}};

			return nameFilter;
	}
}
