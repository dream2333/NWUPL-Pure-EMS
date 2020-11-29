package com.dream.nwuplems.studentinfo

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dream.nwuplems.data.ErrorMessage
import com.dream.nwuplems.data.Success
import com.dream.nwuplems.login.LoginUtils.getStudentInfo
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