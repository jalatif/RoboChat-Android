package com.jalatif.Chat;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jalatif
 * Date: 4/17/13
 * Time: 10:26 PM
 * To change this template use File | Settings | File Templates.
 */
public class ChatPageAdapter extends FragmentPagerAdapter{
    private List<Fragment> fragments;


    public ChatPageAdapter(FragmentManager fm, List<Fragment> fragments) {

        super(fm);

        this.fragments = fragments;

    }

    @Override

    public Fragment getItem(int position) {

        return this.fragments.get(position);

    }

    public void addItem(Fragment fragment){
        this.fragments.add(fragment);
    }

    @Override
    public int getCount() {

        return this.fragments.size();

    }

    @Override
    public CharSequence getPageTitle(int position) {
         return TabbedChat.titles.get(position % TabbedChat.titles.size());
        //return super.getPageTitle(position);    //To change body of overridden methods use File | Settings | File Templates.
    }
}
