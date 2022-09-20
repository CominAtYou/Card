package com.cominatyou.card;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cominatyou.card.activityhelpers.WindowUtil;
import com.cominatyou.card.adapters.TimelineAdapter;
import com.cominatyou.card.databinding.FragmentTimelineBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONObject;

public class TimelineFragment extends Fragment {
    private FragmentTimelineBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTimelineBinding.inflate(inflater, container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        WindowUtil.addPaddingForBottom(requireContext(), binding.timelineRecyclerView);

        final String user_id = requireActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE).getString("id", null);
        final String url =  "https://api.twitter.com/2/users/" + user_id + "/timelines/reverse_chronological?tweet.fields=created_at,text,referenced_tweets,entities,public_metrics,id&expansions=entities.mentions.username,author_id,referenced_tweets.id,referenced_tweets.id.author_id,in_reply_to_user_id&user.fields=profile_image_url,name,username,id&media.fields=duration_ms,preview_image_url,type,url";

        new Thread(() -> {
            try {
                final JSONObject response = JsonNetworkRequest.getObject(requireContext(), url);

                requireActivity().runOnUiThread(() -> {
                    binding.timelineRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
                    binding.timelineRecyclerView.setAdapter(new TimelineAdapter(response));
                });
            }
            catch (Exception e) {
                requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "Failed to get timeline", Toast.LENGTH_SHORT).show());
            }
        }).start();

        ((BottomNavigationView) requireActivity().findViewById(R.id.bottom_navigation)).setOnItemReselectedListener(this::recyclerViewScrollToTop);
    }

    private void recyclerViewScrollToTop(MenuItem item) {
        System.out.println("Hit");
        if (item.getItemId() != R.id.timeline) return;

        binding.timelineRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
        });

        binding.timelineRecyclerView.smoothScrollToPosition(0);
    }
}
