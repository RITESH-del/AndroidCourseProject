package com.example.snackstream.utils;

import android.widget.ImageView;
import androidx.databinding.BindingAdapter;

import com.bumptech.glide.Glide;
import com.example.snackstream.R;

import java.util.List;

/* for loading images from url
* Usage add this line to the imageView
* app:imageUrl="@{item.imageURI}"
*
*/
public class BindingUtils {
    @BindingAdapter("imageUrl")
    public static void loadImage(ImageView view, String url) {

        if (url == null || url.isEmpty()) {
            view.setImageResource(R.drawable.sample_user);
            return;
        }

        Glide.with(view.getContext())
                .load(url)
                .placeholder(R.drawable.sample_user)
                .error(R.drawable.sample_user)
                .into(view);
    }

    @BindingAdapter("imageUrl")
    public static void loadImage(ImageView view, List<String> urls) {
        if (urls == null || urls.isEmpty()) {
            loadImage(view, (String) null);
        } else {
            loadImage(view, urls.get(0));
        }
    }
}