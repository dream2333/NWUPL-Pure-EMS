package com.dream.pureems.studentinfo

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dream.pureems.data.ErrorMessage
import com.dream.pureems.data.Success
import com.dream.pureems.login.LoginUtils.getStudentInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StudentInfoViewModel(application: Application) :
    AndroidViewModel(application) {
    var info = MutableLiveData<List<String>>()
    var snackMessage = MutableLiveData<String>()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val temp = getStudentInfo()
            when (temp) {
                is Success -> {
                    info.postValue(temp.data)
                }
                is ErrorMessage -> show(temp.msg)
            }
        }
    }

    private fun show(msg: String) {
        snackMessage.postValue(msg)
    }
}