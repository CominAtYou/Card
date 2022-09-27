package com.cominatyou.card.activityhelpers;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cominatyou.card.JsonNetworkRequest;
import com.cominatyou.card.R;
import com.cominatyou.card.data.Tweet;
import com.cominatyou.card.databinding.BottomSheetReplyBinding;
import com.cominatyou.card.util.RelativeTimestamp;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.squareup.picasso.Picasso;

import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONObject;

public class ReplyBottomSheet extends BottomSheetDialogFragment {
    public static String TAG = "ReplyBottomSheet";

    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final BottomSheetReplyBinding binding = BottomSheetReplyBinding.inflate(inflater);

       Tweet tweet = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ? getArguments().getSerializable("tweet", Tweet.class) : (Tweet) getArguments().getSerializable("tweet");

       binding.replyingToTweet.tweetTextContent.setText(StringEscapeUtils.unescapeHtml4(tweet.getText()));
       binding.replyingToTweet.tweetAuthorName.setText(tweet.getAuthor().getName());
       binding.replyingToTweet.tweetAuthorHandle.setText("@" + tweet.getAuthor().getUsername() + " • ");
       binding.replyingToTweet.tweetTimestamp.setText(RelativeTimestamp.get(tweet.getCreation()));
       Picasso.get().load(tweet.getAuthor().getProfileImageUrl()).into(binding.replyingToTweet.tweetAuthorAvatar);

       binding.replyEditText.setOnTouchListener((v, event) -> {
           v.getParent().requestDisallowInterceptTouchEvent(true);
           if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
               v.getParent().requestDisallowInterceptTouchEvent(false);
           }
           return false;
       });

       binding.replyEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.replyLayout.setError(null);
            }

            public void afterTextChanged(Editable s) {}
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
       });

        binding.replyingToTweet.getRoot().setClickable(false);
        binding.replyingToTweet.tweetAuthorAvatar.setClickable(false);

        binding.bottomSheetCancelButton.setOnClickListener(l -> dismiss());
        binding.bottomSheetReplyButton.setOnClickListener(l -> {
            if (binding.replyEditText.getText().toString().isEmpty()) {
                binding.replyLayout.setError("Reply cannot be empty");
                return;
            }

            try {
                final JSONObject replyObject = new JSONObject();
                replyObject.put("in_reply_to_tweet_id", tweet.getId());

                final JSONObject tweetObject = new JSONObject();
                tweetObject.put("text", binding.replyEditText.getText().toString());
                tweetObject.put("reply", replyObject);

                JsonNetworkRequest.postObject(getContext(), "https://api.twitter.com/2/tweets", tweetObject, response -> {
                    if (response.has("errors")) {
                        Toast.makeText(getContext(), "Something happened while trying to send your reply.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Toast.makeText(getContext(), "Reply sent!", Toast.LENGTH_SHORT).show();
                    dismiss();
                });
            }
            catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Something happened while trying to send your reply.", Toast.LENGTH_SHORT).show();
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogThemeNoFloating);
        super.onCreate(savedInstanceState);
    }
}
