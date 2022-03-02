package com.dream.nwuplems.login

//新方案，且无需验证码（目测为数维恶性漏洞，不知何时修复）

import android.graphics.Bitmap
import android.graphics.BitmapFactory

import android.util.Log
import com.dream.nwuplems.data.ErrorMessage
import com.dream.nwuplems.data.Result
import com.dream.nwuplems.data.Success
import com.dream.nwuplems.database.Grade
import org.jsoup.Connection.Method
import org.jsoup.Connection.Response
import org.jsoup.Jsoup
import org.seimicrawler.xpath.JXDocument
import java.lang.Exception
import java.security.MessageDigest

object LoginUtilsNew {
    private const val ua: String = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.130 Safari/537.36"
    private const val loginUrl = "http://tam.nwupl.edu.cn/eams/loginExt.action"
    private const val captchaUrl = "http://tam.nwupl.edu.cn/eams/captcha/image.action"
    private const val gradeUrl = "http://tam.nwupl.edu.cn/eams/teach/grade/course/person!historyCourseGrade.action?projectType=MAJOR"
    private const val studentInfoUrl = "http://tam.nwupl.edu.cn/eams/stdDetail.action"
    private lateinit var casResponse: Response
    private lateinit var casCookies: Map<String, String>
    lateinit var emsCookies: Map<String, String>
    lateinit var studentName: String
    lateinit var studentID: String
    private lateinit var salt:String //密码加密密钥
    private var casForm = mutableMapOf(
        "username" to "",
        "password" to "",
        "captcha_response" to ""
    )
    private var timeout = 5000

    fun casInitialize(): Result<Boolean> {
        try {
            casResponse = Jsoup.connect(loginUrl).timeout(timeout).userAgent(ua).execute()
            casCookies = casResponse.cookies()
            salt = Regex("""(?<=SHA1\(').*-""").find(casResponse.body())!!.value
            //获取登录页的cookies和加密密钥
            return Success(true)
        } catch (e: Exception) {
            Log.i("初始化错误", e.localizedMessage)
            return ErrorMessage("初始化网络错误,请重启应用\n错误代码：\n" + e.localizedMessage)
        }
    }

    //门户登录
    fun login(
        username: String,
        password: String,
        captcha:String
    ): Result<Map<String, String>> {
        val res: Response
        try {
            casForm["username"] = username
            casForm["password"] = sha1(salt+password)
            casForm["captcha_response"] = captcha
            res = Jsoup.connect(loginUrl).timeout(timeout * 3).userAgent(ua).data(casForm).cookies(casCookies)
                .method(Method.POST).execute()
        } catch (e: Exception) {
            return ErrorMessage("网络超时")
        }
        val jxdoc = JXDocument.create(res.body())
        return when (jxdoc.selNOne("//title/allText()")?.asString()) {
            "西北政法大学本科综合教务管理系统" -> {
                var tips = jxdoc.selNOne("//div[@class=\"actionError\"]/allText()")
                //如果没找到错误信息,代表登陆成功
                if (tips==null) {
                    emsCookies = casCookies
                    studentName = jxdoc.selNOne("//a[@class='personal-name']/allText()").asString()
                    val temp = studentName.split("(")
                    studentName = temp[0]
                    studentID = temp[1].take(temp[1].length - 1)
                    Success(emsCookies)
                }
                else{
                    //如果找到错误信息
                    salt = Regex("""(?<=SHA1\(').*-""").find(res.body())!!.value
                    ErrorMessage(tips.asString())
                }
            }
            else -> {
                ErrorMessage("错误：无法获取title")
            }
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


    //str转sha1
    fun sha1(input: String): String {
        val digest = MessageDigest.getInstance("SHA-1")
        val result = digest.digest(input.toByteArray())
        return toHex(result)
    }

    fun toHex(byteArray: ByteArray): String {
        //转成16进制
        val result = with(StringBuilder()) {
            byteArray.forEach {
                val value = it
                val hex = value.toInt() and (0xFF)
                val hexStr = Integer.toHexString(hex)
                //println(hexStr)
                if (hexStr.length == 1) {
                    //this.append("0").append(hexStr)
                    append("0").append(hexStr)
                } else {
                    //this.append(hexStr)
                    append(hexStr)
                }
            }
            this.toString()
        }
        return result

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
            val temp = jxdoc.sel("//table[@id=\"studentInfoTb\"]/tbody/tr/td/allText()")
            temp.removeAt(0)//标题
            temp.removeAt(4)//相片
            temp.removeAt(12)//学制
            temp.removeAt(12)
            temp.removeAt(12)
            temp.removeAt(12)
            temp.removeAt(18)
            temp.removeAt(18)//方向
            temp.removeAt(26)//是否在籍在校
            temp.removeAt(26)
            temp.removeAt(26)
            temp.removeAt(26)
            temp.removeAt(32)//其他
            temp.removeAt(32)
            temp.removeAt(32)
            temp.removeAt(32)
            temp.removeAt(32)
            temp.removeAt(32)
            temp.removeAt(32)
            temp.removeAt(32)
            temp.removeAt(4)//英文名
            temp.removeAt(4)
            temp.removeAt(28)//学籍生效日期
            temp.removeAt(28)
            temp.removeAt(6)
            temp.removeAt(6)
            temp.removeAt(6)
            temp.removeAt(6)
            temp.removeAt(6)
            temp.removeAt(6)
            val info = temp.map {
                it.toString().replace("：", "")
            }
            return Success(info)
        } catch (e: Exception) {
            Log.i("nmsl", e.toString())
            return ErrorMessage("获取成绩出错\n" + e.localizedMessage)
        }
    }

}

