package com.cominatyou.card;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cominatyou.card.activityhelpers.TimelineUtil;
import com.cominatyou.card.activityhelpers.WindowUtil;
import com.cominatyou.card.databinding.FragmentTimelineBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class TimelineFragment extends Fragment {
    public FragmentTimelineBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTimelineBinding.inflate(inflater, container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        WindowUtil.addPaddingForBottom(requireContext(), binding.timelineRecyclerView);

        binding.fragmentTimelineSwipeRefreshLayout.setColorSchemeColors(requireActivity().getColor(R.color.md_theme_light_primary));

        TimelineUtil.getTimeline(this, () -> {
            // hide the loading bar when the RecyclerView is done loading
            binding.timelineRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(globalLayoutListener);
            ((BottomNavigationView) requireActivity().findViewById(R.id.bottom_navigation)).setOnItemReselectedListener(this::recyclerViewScrollToTop);
            binding.fragmentTimelineSwipeRefreshLayout.setOnRefreshListener(() -> TimelineUtil.refreshTimeline(this));
        });
    }

    private void recyclerViewScrollToTop(MenuItem item) {
        if (item.getItemId() != R.id.timeline) return;

        final RecyclerView.OnScrollListener listener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (!recyclerView.canScrollVertically(-1)) { // if the scroll is stopped before it hits the top, do not expand the toolbar
                        binding.timelineFragmentAppBarLayout.setExpanded(true, true);
                    }

                    recyclerView.removeOnScrollListener(this);
                }
            }
        };

        binding.timelineRecyclerView.addOnScrollListener(listener);

        // if the scroll position is far down enough, the scroll to the top can take quite a while.
        // determine if the scroll position is far down enough to warrant just jumping to the top, instead of scrolling to the top
        final float numberOfItems = binding.timelineRecyclerView.getAdapter().getItemCount();
        final LinearLayoutManager layoutManager = (LinearLayoutManager) binding.timelineRecyclerView.getLayoutManager();
        float firstVisiblePosition = layoutManager.findFirstVisibleItemPosition();

        if (firstVisiblePosition / numberOfItems < 0.4f) {
            binding.timelineRecyclerView.smoothScrollToPosition(0);
        }
        else {
            binding.timelineRecyclerView.removeOnScrollListener(listener);
            binding.timelineRecyclerView.scrollToPosition(0);
            binding.timelineFragmentAppBarLayout.setExpanded(true);
        }
    }

    final ViewTreeObserver.OnGlobalLayoutListener globalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            ((MainActivity) getActivity()).binding.mainActivityHorizontalProgressBar.setVisibility(View.GONE);
            binding.timelineRecyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        }
    };
}
