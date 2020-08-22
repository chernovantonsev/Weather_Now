package ru.antonc.weather.binding

import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import ru.antonc.weather.GlideApp
import ru.antonc.weather.R

@BindingAdapter("imageFromUri")
fun bindImageFromUri(view: ImageView, picture: String?) {
    GlideApp.with(view.context)
        .load(if (picture.isNullOrEmpty()) R.drawable.ic_no_image else picture)
        .error(R.drawable.ic_no_image)
        .into(view)
}

@BindingAdapter("android:isGone")
fun bindIsGone(view: View, isGone: Boolean?) {
    isGone?.let {
        view.visibility = if (isGone) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }
}