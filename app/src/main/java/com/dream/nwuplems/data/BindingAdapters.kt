package com.dream.nwuplems.data

import android.graphics.Bitmap
import android.widget.EditText
import android.widget.ImageView
import androidx.lifecycle.MutableLiveData
import androidx.databinding.BindingAdapter

@BindingAdapter("bitmapImage")
fun setImageViewBitmap(imageView: ImageView, bitmap: MutableLiveData<Bitmap>) {
    imageView.setImageBitmap(bitmap.value)
}

@BindingAdapter("android:enabled")
fun setEditTextEnabled(view: EditText, bool: Boolean) {
    view.isEnabled = bool
}