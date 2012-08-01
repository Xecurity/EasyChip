package ca.xecure.easychip;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class FlipAdapter extends FragmentPagerAdapter {
    public FlipAdapter(FragmentManager mgr) {
        super(mgr);
    }

    @Override
    public int getCount() {
        return(3);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new SettingFragment();
            case 1:
                return new IntroductionFragment();
            default:
                return new LogFragment();
        }
    }

    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Settings";
            case 1:
                return "EasyChip";
            default:
                return "Logs";
        }
    }
}
