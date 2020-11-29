package com.dream.nwuplems.gradetable

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dream.nwuplems.data.ErrorMessage
import com.dream.nwuplems.data.Success
import com.dream.nwuplems.database.AppDataBase
import com.dream.nwuplems.database.Grade
import com.dream.nwuplems.database.GradeInfo
import com.dream.nwuplems.login.LoginUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GradeTableViewModel(application: Application) :
    AndroidViewModel(application) {
    var snackMessage = MutableLiveData<String>()
    private var dao = AppDataBase.getDatabase(application).gradeDao()
    var gradeInfoLiveData = MutableLiveData<GradeInfo>()
    var semesterGradeLiveData = MutableLiveData<List<Grade>>()
    var allGradeInfo = MutableLiveData<List<GradeInfo>>()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            if (dao.count() == 0) {
                when (val grades = LoginUtils.getGrade()) {
                    is Success -> {
                        dao.insert(grades.data)
                        allGradeInfo.postValue(dao.getAllGradeInfo())
                        Log.i("所有课程", grades.data.toString())
                    }
                    is ErrorMessage -> show(grades.msg)
                }
                //数据库空则先联网获取
            }
            allGradeInfo.postValue(dao.getAllGradeInfo())
        }
    }

    private fun show(msg: String) {
        snackMessage.postValue(msg)
    }

    fun refreshList(index: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            gradeInfoLiveData.postValue(allGradeInfo.value!![index])
            //获取学年信息
            semesterGradeLiveData.postValue(dao.getSemesterGradeList(allGradeInfo.value!![index].date))
            //获取学期成绩列表
        }
    }
}