package com.dream.nwuplems.gradetable

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class GradeTableViewModelFactory(
    private val application: Application
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GradeTableViewModel::class.java)) {
            return GradeTableViewModel(application) as T
        }
        throw IllegalArgumentException("未知ViewModel类")
    }
}