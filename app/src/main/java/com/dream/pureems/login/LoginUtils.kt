package com.dream.pureems.login

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.dream.pureems.data.ErrorMessage
import com.dream.pureems.data.Result
import com.dream.pureems.data.Success
import com.dream.pureems.database.Grade

import org.jsoup.Connection.Method
import org.jsoup.Connection.Response
import org.jsoup.Jsoup
import org.seimicrawler.xpath.JXDocument
import java.lang.Exception

object LoginUtils {
    private const val ua: String =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.130 Safari/537.36"
    private const val emsUrl =
        "http://ip.nwupl.edu.cn/cas/login?service=http%3A%2F%2Ftam.nwupl.edu.cn%2Feams%2FhomeExt.action"
    private const val captchaUrl = "https://ip.nwupl.edu.cn/cas/kaptcha.jpg"
    private const val gradeUrl =
        "http://tam.nwupl.edu.cn/eams/teach/grade/course/person!historyCourseGrade.action?projectType=MAJOR"
    private const val studentInfoUrl = "http://tam.nwupl.edu.cn/eams/stdDetail.action"
    private lateinit var casResponse: Response
    private lateinit var casCookies: Map<String, String>
    lateinit var emsCookies: Map<String, String>
    lateinit var studentName: String
    lateinit var studentID: String
    private var casForm = mutableMapOf(
        "lt" to "",
        "execution" to "e1s1",
        "_eventId" to "submit",
        "username" to "",
        "password" to "",
        "captcha" to ""
    )
    private var timeout = 5000

    fun casInitialize(): Result<Bitmap> {
        try {
            casResponse = Jsoup.connect(emsUrl).timeout(timeout).userAgent(ua).execute()
            casCookies = casResponse.cookies()
            casForm["lt"] = getLt(casResponse)
            //获取登录页的cookies和lt
            return getCaptcha()
            //返回验证码
        } catch (e: Exception) {
            Log.i("初始化错误", e.localizedMessage)
            return ErrorMessage("初始化网络错误,请重启应用\n错误代码：\n" + e.localizedMessage)

        }
    }

    //门户登录
    fun login(
        captcha: String,
        username: String,
        password: String
    ): Result<Map<String, String>> {
        casForm["captcha"] = captcha
        casForm["username"] = username
        casForm["password"] = password
        val res: Response
        try {
            res = Jsoup.connect(emsUrl).timeout(timeout * 3).data(casForm).cookies(casCookies)
                .method(Method.POST).execute()
        } catch (e: Exception) {
            Log.i("网络错误", e.toString())
            return ErrorMessage("网络超时")
        }

        val jxdoc = JXDocument.create(res.body())
        return when (jxdoc.selNOne("//title/allText()")?.asString()) {
            "西北政法大学本科综合教务管理系统" -> {
                emsCookies = res.cookies()
                studentName = jxdoc.selNOne("//a[@class='personal-name']/allText()").asString()
                val temp = studentName.split("(")
                studentName = temp[0]
                studentID = temp[1].take(temp[1].length - 1)
                Success(emsCookies)
            }
            "登录界面" -> {
                when (jxdoc.selNOne("//*[@id=\"msg\"]/allText()")?.asString()) {
                    "必须输入验证码", "您输入的验证码有误。" -> ErrorMessage("验证码错误")
                    "登录失败–密码不正确" -> ErrorMessage("学号或密码错误")
                    else -> {
                        ErrorMessage("验证码过期")
                    }
                }
            }
            else -> ErrorMessage("错误：无法获取title")
        }
    }

    //获取验证码
    fun getCaptcha(): Result<Bitmap> {
        try {
            val captchaArray =
                Jsoup.connect(captchaUrl).ignoreContentType(true).method(Method.GET)
                    .cookies(casCookies)
                    .execute()
                    .bodyAsBytes()
            return Success(BitmapFactory.decodeByteArray(captchaArray, 0, captchaArray.size))
        } catch (e: Exception) {
            Log.i("log验证码", e.localizedMessage)
            return ErrorMessage("获取验证码出错\n" + e.localizedMessage)
        }
    }

    private fun getLt(response: Response): String {
        val jxdoc: JXDocument = JXDocument.create(response.body())
        return jxdoc.selNOne("//*[@id=\"fm1\"]/input[1]/@value").asString()
    }

    //获取成绩
    fun getGrade(): Result<MutableList<Grade>> {
        try {
            val res =
                Jsoup.connect(gradeUrl).ignoreContentType(true).method(Method.GET)
                    .cookies(emsCookies)
                    .execute()
            val jxdoc: JXDocument = JXDocument.create(res.body())
            val gradesSelList = jxdoc.selN("//tbody[@id]/tr")
            val gradesList = mutableListOf<Grade>()
            gradesSelList.forEach {
                //只要0 3 5 11行
                val td = it.sel("//td/allText()")
                val date = td[0].toString().substring(0, 4) + td[0].toString()
                    .substring(5, 9) + td[0].toString()[10]
                val grade = Grade(
                    courseName = td[3].toString(),
                    credit = td[5].toString().toInt(),
                    score = td.reversed()[1].toString().toFloat(),
                    date = date.toInt()
                )
                gradesList.add(grade)
            }
            return Success(gradesList)
        } catch (e: Exception) {
            return ErrorMessage("获取成绩出错\n" + e.localizedMessage)
        }
    }

    fun getStudentInfo(): Result<List<String>> {
        try {
            val res =
                Jsoup.connect(studentInfoUrl).ignoreContentType(true).method(Method.GET)
                    .cookies(emsCookies)
                    .execute()
            val jxdoc: JXDocument = JXDocument.create(res.body())
            val temp = jxdoc.selN("//table[@id=\"studentInfoTb\"]/tbody/tr/td/allText()")
            val info = listOf(
                temp[2],
                temp[4],
                temp[10],
                temp[11],
                temp[12],
                temp[19],
                temp[20],
                temp[21],
                temp[22],
                temp[32],
                temp[33],
                temp[38],
                temp[39],
                temp[26],
                temp[27],
                temp[28],
                temp[29],
                temp[40],
                temp[41],
            ).map {
                it.toString().replace("：", "")
            }
            return Success(info)
        } catch (e: Exception) {
            Log.i("nmsl", e.toString())
            return ErrorMessage("获取成绩出错\n" + e.localizedMessage)
        }
    }

}

