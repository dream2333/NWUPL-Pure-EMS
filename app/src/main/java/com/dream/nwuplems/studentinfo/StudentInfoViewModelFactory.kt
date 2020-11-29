package com.dream.nwuplems.studentinfo

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class StudentInfoViewModelFactory(
    private val application: Application
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StudentInfoViewModel::class.java)) {
            return StudentInfoViewModel(application) as T
        }
        throw IllegalArgumentException("未知ViewModel类")
    }
}
