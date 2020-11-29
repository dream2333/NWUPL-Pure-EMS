package com.dream.nwuplems.coursetable

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class CourseTableViewModelFactory(
    private val application: Application
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CourseTableViewModel::class.java)) {
            return CourseTableViewModel(application) as T
        }
        throw IllegalArgumentException("未知ViewModel类")
    }
}