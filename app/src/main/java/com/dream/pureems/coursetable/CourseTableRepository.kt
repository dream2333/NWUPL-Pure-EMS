package com.dream.pureems.coursetable

import android.util.Log
import androidx.lifecycle.LiveData
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.dream.pureems.data.ErrorMessage
import com.dream.pureems.data.Success
import com.dream.pureems.database.Course
import com.dream.pureems.database.CourseDao
import com.dream.pureems.login.LoginUtils
import com.dream.pureems.data.Result
import com.dream.pureems.data.Semester
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.seimicrawler.xpath.JXDocument
import java.lang.Exception

class CourseTableRepository(private val courseDao: CourseDao) {
    private val tableBarUrl = "http://tam.nwupl.edu.cn/eams/courseTableForStd.action"
    private val tableUrl = "http://tam.nwupl.edu.cn/eams/courseTableForStd!courseTable.action"
    private val queryUrl = "http://tam.nwupl.edu.cn/eams/dataQuery.action"
    private val mainUrl = "http://tam.nwupl.edu.cn/eams/homeExt!main.action"
    private var timeout = 5000
    private var semesterId = ""
    var teachingWeek = 1
    private var ids = ""
    private var tagId = ""


    fun initialize(emsCookies: Map<String, String>): Result<String> {
        try {
            //初始化ids、tagId、semesterId
            var res = Jsoup.connect(tableBarUrl).cookies(emsCookies).timeout(timeout)
                .method(Connection.Method.GET).execute()
            ids = Regex("""(?<=form,"ids",")\d+""").find(res.body())!!.value
            tagId = Regex("""semesterBar\d+Semester""").find(res.body())!!.value
            semesterId = res.cookie("semester.id")

            //初始化教学周
            res = Jsoup.connect(mainUrl).cookies(emsCookies).timeout(timeout)
                .method(Connection.Method.GET).execute()
            var jxdoc = JXDocument.create(res.body())
            teachingWeek =
                jxdoc.selOne("//div[@class=\"title\"]/strong/allText()").toString().toInt()
            return Success("初始化成功")
        } catch (e: Exception) {
            return ErrorMessage("初始化课表失败\n" + e.localizedMessage)
        }
    }

    suspend fun updateAllCourses(vararg semester: String): Result<MutableList<Course>> {
        //更新课表
        var result = if (semester.isEmpty()) {
            getCourseList(LoginUtils.emsCookies)
        } else {
            getCourseList(LoginUtils.emsCookies, semester[0])
        }//有指定学期则获取学期
        when (result) {
            is Success -> {
                insertAfterDeleted(result.data)
            }
        }
        return result
    }

    fun getAllCourse(): LiveData<List<Course>> {
        return courseDao.getAllCourseLiveData()
    }

    fun getAllSemester(): Result<MutableList<Semester>> {
        return getSemesterList(LoginUtils.emsCookies)
    }

    suspend fun insert(course: Course) {
        Log.i("log", "插入单项$course")
        courseDao.insert(course)
    }

    suspend fun insert(courseList: List<Course>) {
        Log.i("log", "插入列表$courseList")
        courseDao.insert(courseList)
    }

    suspend fun insertAfterDeleted(courseList: List<Course>) {
        courseDao.insertAfterDeleted(courseList)
    }

    suspend fun updateAll(courseList: List<Course>) {
        Log.i("log", "插入列表$courseList")
        courseDao.insert(courseList)
    }

    suspend fun delete(course: Course) {
        Log.i("log", "删除$course")
        courseDao.delete(course)
    }

    suspend fun deleteAll() {
        Log.i("log", "删除全部")
        courseDao.deleteAll()
    }

    suspend fun isEmpty(): Boolean {
        return courseDao.courseCount() == 0
    }

    //获取所有学期的课表id
    fun getSemesterList(cookies: Map<String, String>): Result<MutableList<Semester>> {
        try {
            var data =
                mapOf("tagId" to tagId, "dataType" to "semesterCalendar", "empty" to "false")
            var json = Jsoup.connect(queryUrl).cookies(cookies).timeout(timeout).data(data)
                .method(Connection.Method.GET).execute().body()
            var semesterList = mutableListOf<Semester>()
            JSON.parseObject(json).getJSONObject("semesters").forEach { it ->
                var i = it.value as JSONArray
                i.forEach {
                    var k = it as Map<String, String>
                    semesterList.add(
                        Semester(
                            k["id"].toString(),
                            k.getValue("schoolYear"),
                            k.getValue("name"),
                            k.getValue("schoolYear") + "学年度第" + k.getValue("name") + "学期"
                        )
                    )
                }
            }
            semesterList.sortBy { -(it.year.substring(0..3).toInt() * 10 + it.term.toInt()) }
            return Success(semesterList.subList(0, 8))
        } catch (e: Exception) {
            return ErrorMessage("获取学期错误\n" + e.localizedMessage)
        }
    }

    fun getCourseList(
        cookies: Map<String, String>,
        sId: String = semesterId,
        kind: String = "std",
        week: String = ""
    ): Result<MutableList<Course>> {
        try {
            var form = mapOf(
                "ignoreHead" to "1",
                "setting.kind" to kind,
                "semester.id" to sId,
                "ids" to ids,
                "startWeek" to week
            )
            var doc = Jsoup.connect(tableUrl).cookies(cookies).timeout(timeout)
                .data(form).post()
            var jxdoc = JXDocument.create(doc)
            var str = jxdoc.selN("//script[@language]/allText()")[2].asString()
            var courseList = courseTableParser(str)
            return if (courseList.isNotEmpty()) Success(courseList) else ErrorMessage("课表为空")
        } catch (e: Exception) {
            return ErrorMessage("获取课表错误" + e.localizedMessage)
        }
    }

    private fun courseTableParser(str: String): MutableList<Course> {
        //获取开课老师名
        var teachersNameList = mutableListOf<List<String>>()
        Regex("""(?<=var actTeachers = ).+(?=;)""").findAll(str).forEach { it ->
            var teachersName = mutableListOf<String>()
            JSON.parseArray(it.value).forEach {
                var i = it as JSONObject
                teachersName.add(i["name"].toString())
            }
            teachersNameList.add(teachersName)
        }
        //获取课程信息
        var courseInfoList = mutableListOf<MutableList<String>>()
        //[0]课程名 [1]地点 [2]周次
        Regex("""(?<=activity = new TaskActivity.{0,200}\"\,\").+?(?=(0){15})""").findAll(str)
            .forEach {
                var i = it.value.split("\",\"").toMutableList()
                i.removeAt(1)
                i[0] = i[0].replace(Regex("""\(\w+\.\d+\)"""), "")
                i[1] = i[1].replace(Regex("""\(.+\)"""), "")
                i[2] = i[2].removeRange(0..0)
                courseInfoList.add(i)
            }
        var groups =
            Regex("""(index =(\d+)\*unitCount\+(\d+);\s+table0\.activities\[index]\[table0\.activities\[index]\.length]=activity;\s+){1,5}""").findAll(
                str
            )
        //获取时间信息
        var dateInfoList = mutableListOf<List<Int>>()
        groups.forEach {
            var i = mutableListOf<Int>()
            Regex("""index =(\d+)\*unitCount\+(\d+)""").findAll(it.value).forEach {
                i.add(it.groupValues[1].toInt())
                i.add(it.groupValues[2].toInt())
            }
            dateInfoList.add(mutableListOf(i[0], i[1], i[i.lastIndex] - i[1] + 1))
        }
        var courseList = mutableListOf<Course>()
        var courseInfoIterator = courseInfoList.iterator()
        var nameListIterator = teachersNameList.iterator()
        var dateInfoIterator = dateInfoList.iterator()
        while (courseInfoIterator.hasNext()) {
            var courseInfo = courseInfoIterator.next()
            var nameList = nameListIterator.next().toString()
            var dateInfo = dateInfoIterator.next()
            courseList.add(
                Course(
                    nameList,
                    courseInfo[0],
                    courseInfo[1],
                    courseInfo[2],
                    dateInfo[0],
                    dateInfo[1],
                    dateInfo[2]
                )
            )
        }
        courseList.sortBy { it.day * 10 + it.start }
        return courseList
    }
}