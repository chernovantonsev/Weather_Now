package ru.antonc.weather.binding

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