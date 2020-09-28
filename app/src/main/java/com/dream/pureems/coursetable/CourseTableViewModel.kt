package com.dream.pureems.coursetable

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dream.pureems.data.*
import com.dream.pureems.database.AppDataBase
import com.dream.pureems.login.LoginUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class CourseTableViewModel(application: Application) :
    AndroidViewModel(application) {
    var repository = CourseTableRepository(AppDataBase.getDatabase(application).courseDao())
    var courseList = repository.getAllCourse()
    var semesterList = MutableLiveData<MutableList<Semester>>()//储存所有学期的清洗数据
    val currentDate = currentDate()
    val titleArray = getTitle()
    val semesterId = MutableLiveData<String>()
    var snackMessage = MutableLiveData<String>()
    var time = currentDate[0].toString() + "月" + currentDate[1] + "日"

    init {
        viewModelScope.launch(Dispatchers.IO) {
            repository.initialize(LoginUtils.emsCookies)
            if (repository.isEmpty()) {
                repository.updateAllCourses()
            }
            val semesterResult = repository.getAllSemester()
            when (semesterResult) {
                is Success -> semesterList.postValue(semesterResult.data)
                is ErrorMessage -> show(semesterResult.msg)
            }
            //获取学期表
        }

        semesterId.observeForever {
            viewModelScope.launch(Dispatchers.IO) {
                val courseResult =
                    repository.updateAllCourses(it)
                //学期数据
                if (courseResult is ErrorMessage) {
                    show(courseResult.msg)
                }
            }
        }
    }

    private fun getTitle(): Array<String> {
        val titleTextArray = arrayOf(
            "一\n", "二\n", "三\n", "四\n", "五\n", "六\n", "日\n"
        )
        val calendar = GregorianCalendar()
        calendar.firstDayOfWeek = Calendar.MONDAY
        for (i in 0..6) {
            calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek + i)
            titleTextArray[i] += calendar.get(Calendar.DAY_OF_MONTH).toString()
        }
        return titleTextArray
    }

    private fun currentDate(): Array<Int> {
        val calendar = GregorianCalendar()
        calendar.firstDayOfWeek = Calendar.MONDAY
        val month = calendar.get(Calendar.MONTH) + 1
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        var dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        dayOfWeek = if (dayOfWeek == 1) 7 else dayOfWeek - 1
        return arrayOf(month, dayOfMonth, dayOfWeek)
    }

    private fun show(msg: String) {
        snackMessage.postValue(msg)
    }
}