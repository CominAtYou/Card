package com.cominatyou.card.activityhelpers;

import android.view.MenuItem;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.cominatyou.card.ActivityFragment;
import com.cominatyou.card.ExploreFragment;
import com.cominatyou.card.MainActivity;
import com.cominatyou.card.TimelineFragment;
import com.cominatyou.card.R;

import java.util.Map;

public class BottomNavigationFragmentManager {

    private static final Map<Integer, String> idToTag = Map.of(
            R.id.timeline, "timeline",
            R.id.explore, "explore",
            R.id.mentions, "mentions"
    );

    public static boolean switchFragment(MainActivity activity, MenuItem item) {
        FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();

        if (item.getItemId() != R.id.timeline) {
            activity.binding.bottomNavigation.getMenu().getItem(0).setIcon(R.drawable.ic_home_24);
        }

        Fragment currentlyDisplayed = null;
        for (Fragment fragment : activity.getSupportFragmentManager().getFragments()) {
            if (fragment.isVisible()) {
                currentlyDisplayed = fragment;
                break;
            }
        }

        if (item.getItemId() == R.id.timeline) {
            activity.binding.bottomNavigation.getMenu().getItem(0).setIcon(R.drawable.ic_home_filled_24);
            final Fragment timelineFragment = activity.getSupportFragmentManager().findFragmentByTag("timeline");

            if (timelineFragment != null) {
                transaction.show(timelineFragment);
            }
            else {
                transaction.add(R.id.fragment_container, TimelineFragment.class, null, "timeline");
            }
        }
        else if (item.getItemId() == R.id.explore) {
            final Fragment exploreFragment = activity.getSupportFragmentManager().findFragmentByTag("explore");

            if (exploreFragment != null) {
                transaction.show(exploreFragment);
            }
            else {
                transaction.add(R.id.fragment_container, ExploreFragment.class, null, "explore");
            }
        }
        else if (item.getItemId() == R.id.mentions) {
            final Fragment activityFragment = activity.getSupportFragmentManager().findFragmentByTag("activity");

            if (activityFragment != null) {
                transaction.show(activityFragment);
            }
            else {
                transaction.add(R.id.fragment_container, ActivityFragment.class, null, "activity");
            }
        }

        if (currentlyDisplayed != null) transaction.hide(currentlyDisplayed);
        transaction.commit();
        return true;
    }
}
