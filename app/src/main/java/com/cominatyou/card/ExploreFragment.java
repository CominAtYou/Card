package com.cominatyou.card;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.cominatyou.card.databinding.FragmentExploreBinding;

public class ExploreFragment extends Fragment {
    private FragmentExploreBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentExploreBinding.inflate(inflater,container,false);
        requireActivity().getWindow().setSharedElementExitTransition(null);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        binding.searchActionBar.setOnClickListener(v -> {
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(getActivity(), binding.searchBarLayout, "search");
            startActivity(new Intent(getContext(), SearchActivity.class), options.toBundle());
        });
    }
}
