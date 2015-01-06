package ua.kiev.doctorvera;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.TabListener;
/*
 * Tabs and Page Adapter, handles tabs switching and swiping
 * @author      Volodymyr Bodnar
 * @version     %I%, %G%
 * @since       1.0
 */
import android.util.Log;

public class TabsPagerAdapter extends FragmentPagerAdapter implements TabListener, OnPageChangeListener {
    private Activity activity;
    private final String LOG_TAG = "myLogs " + this.getClass().getSimpleName();
    
    private ArchiveTab archiveTab = new ArchiveTab();
    private TemplateTab templateTab = new TemplateTab();
    private NewSMSTab newSMSTab = new NewSMSTab();
    
    public TabsPagerAdapter(FragmentManager fm, Bundle savedInstanceState, Activity activity) {
        super(fm);
        this.activity = activity;
    }

    @Override
    public Fragment getItem(int index) {
    	
    	Log.d(LOG_TAG,"getItem");
    	
        switch (index) {
            case 0:
                return archiveTab;
            case 2:
                return templateTab;
            case 1:
                return newSMSTab;
        }
        return null;
    }

    @Override
    public int getCount() {
        // get item count - equal to number of tabs
        return 3;
    }
    
    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
    
        ViewPager viewPager = (ViewPager) activity.findViewById(R.id.activity_main);
      //Each tab has different menu items
        activity.invalidateOptionsMenu();

        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
        // TODO Auto-generated method stub

    }
    
    @Override
    public void onPageSelected(int position) {
        // on changing the page
        // make respected tab selected
    	if(activity != null && activity.getActionBar() !=null)
        activity.getActionBar().setSelectedNavigationItem(position);
    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {
    }


}