package com.cominatyou.card;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.app.AppCompatActivity;

import com.cominatyou.card.databinding.ActivitySearchBinding;
import com.google.android.material.color.DynamicColors;

public class SearchActivity extends AppCompatActivity {
    private ActivitySearchBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        DynamicColors.applyToActivityIfAvailable(this);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getWindow().setSharedElementEnterTransition(null);
        getWindow().setSharedElementReturnTransition(null);

        binding.searchField.requestFocus();

        binding.searchActionBar.setNavigationOnClickListener(v -> {
            binding.searchActionBar.setNavigationIcon(R.drawable.ic_dynamictint_search_24);
            binding.searchField.clearFocus();

            TypedValue value = new TypedValue();
            getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnSurfaceVariant, value, true);
            binding.searchField.setHintTextColor(value.data);

            final InputMethodManager inputMethodManager = getSystemService(InputMethodManager.class);
            inputMethodManager.hideSoftInputFromWindow(binding.searchField.getWindowToken(), 0);

            finishAfterTransition();
        });

        binding.searchField.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    binding.activitySearchGoToProfileCard.setVisibility(View.VISIBLE);
                    binding.activitySearchGoToProfileText.setText(getString(R.string.activity_search_go_to_user, s));
                }
                else {
                    binding.activitySearchGoToProfileCard.setVisibility(View.GONE);
                }
            }
        });

        Intent intent = new Intent(this, ProfileActivity.class)
                .putExtra("user_id", binding.searchField.getText().toString().trim());

        binding.activitySearchGoToProfileCard.setOnClickListener(v -> startActivity(intent));
    }
}
