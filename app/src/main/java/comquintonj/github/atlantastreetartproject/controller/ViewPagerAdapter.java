package comquintonj.github.atlantastreetartproject.controller;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

    /*
    * Responsible for creating the fragments for each tab.
    */
public class ViewPagerAdapter extends FragmentPagerAdapter {

    private RecyclerView mRecyclerView;
    private ContentFragment fgmt;
    List<ContentFragment> fragments;


    public ViewPagerAdapter(FragmentManager fm, RecyclerView mRecyclerView) {
        super(fm);
        this.mRecyclerView = mRecyclerView;
        fragments = new ArrayList<>();
    }

    @Override
    public Fragment getItem(int position) {
        fgmt = new ContentFragment();
        fragments.add(fgmt);
        return fgmt;
    }

    public void updateRecyclerView(int index) {
        fragments.get(index).addRecyclerView(mRecyclerView);
    }

    @Override
    public int getCount() {
        return 3;
    }
}