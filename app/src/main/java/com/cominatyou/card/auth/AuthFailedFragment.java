package com.cominatyou.card.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.cominatyou.card.databinding.FragmentAuthFailedBinding;

public class AuthFailedFragment extends Fragment {
    private FragmentAuthFailedBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAuthFailedBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        binding.retryButton.setOnClickListener(v -> Auth.authenticate(requireActivity()));
    }
}
