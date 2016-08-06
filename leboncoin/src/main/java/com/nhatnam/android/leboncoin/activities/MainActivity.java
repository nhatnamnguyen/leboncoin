package com.nhatnam.android.leboncoin.activities;

import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.nhatnam.android.leboncoin.R;
import com.nhatnam.android.leboncoin.fragments.DashDeposeFragment;
import com.nhatnam.android.leboncoin.fragments.DashFavoriteFragment;
import com.nhatnam.android.leboncoin.fragments.DashOffreFragment;
import com.nhatnam.android.leboncoin.fragments.DashRechercheFragment;
import com.nhatnam.android.leboncoin.utils.Utils;
import com.viewpagerindicator.PageIndicator;

public class MainActivity extends SherlockFragmentActivity implements OnPageChangeListener
{

	private static final int                  TAB_PAGER_OFFSCREEN_LIMIT = 4;
    private ViewPager                         mViewPager;
    private TabsAdapter                       mTabsAdapter;
    
    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        
        this.mViewPager = (ViewPager) findViewById(R.id.pager);

        this.mTabsAdapter = new TabsAdapter(this);
        this.mTabsAdapter.addTab(getString(R.string.title_offre_fragment), DashOffreFragment.class, null);
        this.mTabsAdapter.addTab(getString(R.string.title_recherche_fragment), DashRechercheFragment.class, null);
        this.mTabsAdapter.addTab(getString(R.string.title_favorite_fragment), DashFavoriteFragment.class, null);
        this.mTabsAdapter.addTab(getString(R.string.title_depose_fragment), DashDeposeFragment.class, null);

        this.mViewPager.setAdapter(this.mTabsAdapter);
        // Add this limit in order to avoid bad display
        this.mViewPager.setOffscreenPageLimit(TAB_PAGER_OFFSCREEN_LIMIT);

        // Bind the title indicator to the adapter
        final PageIndicator titleIndicator = (PageIndicator) findViewById(android.R.id.tabs);
        titleIndicator.setViewPager(this.mViewPager);
        
        titleIndicator.setOnPageChangeListener(this);

        if (savedInstanceState == null)
        {
        	//Purge all offres exception favorite offres
	    	Utils.purgeOffres(false);
        } else {
        	this.mViewPager.setCurrentItem(savedInstanceState.getInt("tab", 0), false);        	
        }
    }

    
    @Override
    protected void onSaveInstanceState(final Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putInt("tab", this.mViewPager.getCurrentItem());
    }
    
    
    public static class TabsAdapter extends FragmentPagerAdapter
    {
        private final Context            mContext;
        private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();

        static final class TabInfo
        {
            private final String   tag;
            private final Class<?> clss;
            private final Bundle   args;

            TabInfo(final String _tag, final Class<?> _class, final Bundle _args)
            {
                this.tag = _tag;
                this.clss = _class;
                this.args = _args;
            }
        }

        public TabsAdapter(final FragmentActivity activity)
        {
            super(activity.getSupportFragmentManager());
            this.mContext = activity;
        }

        public void addTab(final String tag, final Class<?> clss, final Bundle args)
        {
            final TabInfo info = new TabInfo(tag, clss, args);
            this.mTabs.add(info);
            notifyDataSetChanged();
        }

        @Override
        public int getCount()
        {
            return this.mTabs.size();
        }

        @Override
        public Fragment getItem(final int position)
        {
            final TabInfo info = this.mTabs.get(position);
            return Fragment.instantiate(this.mContext, info.clss.getName(), info.args);
        }

       
        @Override
        public CharSequence getPageTitle(final int position)
        {
            final TabInfo info = this.mTabs.get(position);
            return info.tag;
        }
    }

    
    @Override
    public void onPageScrollStateChanged(final int arg0)
    {
        // unused
    }

  
    @Override
    public void onPageScrolled(final int arg0, final float arg1, final int arg2)
    {
        // unused
    }

   
    @Override
    public void onPageSelected(final int index)
    {
        invalidateOptionsMenu();
    }

	@Override
	public void onBackPressed() {
		//Favorite
		if (this.mViewPager.getCurrentItem() == 2
				&& ((DashFavoriteFragment) this.mTabsAdapter.getItem(2)).mDeleteMode) 
		{
			//Close menu Cancel-Delete
			((DashFavoriteFragment) this.mTabsAdapter.getItem(2)).animateBottomBar();
		} else {
			super.onBackPressed();
		}
	}
}
