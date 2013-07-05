package com.buildallthethings.doglog.ui;

import java.util.Locale;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;

import com.buildallthethings.doglog.R;
import com.buildallthethings.doglog.ui.DummyFragment;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one
 * of the sections/tabs/pages.
 */
public class MainActivityFragmentPagerAdapter extends FragmentPagerAdapter {
	protected FragmentActivity fragmentActivity;
	
	public MainActivityFragmentPagerAdapter(FragmentActivity fragmentActivity) {
		super(fragmentActivity.getSupportFragmentManager());
		this.fragmentActivity = fragmentActivity;
	}
	
	@Override
	public Fragment getItem(int position) {
		// getItem is called to instantiate the fragment for the given page.
		// We can pass args to the fragment in the bundle if needed.
		Bundle args = new Bundle();
		switch (position) {
			case 0:
				Fragment overviewFragment = new OverviewFragment();
				return overviewFragment;
			case 1:
				Fragment historyFragment = new HistoryFragment();
				return historyFragment;
			case 2:
				Fragment mapInteractionFragment = new MapInteractionFragment();
				mapInteractionFragment.setArguments(args);
				return mapInteractionFragment;
			default:
				// Return a DummySectionFragment (defined as a static inner
				// class
				// below) with the page number as its lone argument.
				Fragment dummyFragment = new DummyFragment();
				args.putInt(DummyFragment.ARG_SECTION_NUMBER, position + 1);
				dummyFragment.setArguments(args);
				return dummyFragment;
		}
	}
	
	@Override
	public int getCount() {
		return 3;
	}
	
	@Override
	public CharSequence getPageTitle(int position) {
		Locale l = Locale.getDefault();
		switch (position) {
			case 0:
				return this.fragmentActivity.getString(R.string.title_fragment_overview).toUpperCase(l);
			case 1:
				return this.fragmentActivity.getString(R.string.title_fragment_history).toUpperCase(l);
			case 2:
				return this.fragmentActivity.getString(R.string.title_fragment_location).toUpperCase(l);
		}
		return null;
	}
}