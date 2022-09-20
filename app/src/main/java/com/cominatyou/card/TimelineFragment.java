package com.cominatyou.card;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.cominatyou.card.activityhelpers.WindowUtil;
import com.cominatyou.card.adapters.TimelineAdapter;
import com.cominatyou.card.databinding.FragmentTimelineBinding;
import com.squareup.picasso.Picasso;

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
    }
}
