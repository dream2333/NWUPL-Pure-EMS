package com.dream.nwuplems.login

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dream.nwuplems.data.ErrorMessage
import com.dream.nwuplems.data.Success
import kotlinx.coroutines.*

class LoginViewModel : ViewModel() {
    var bitmap = MutableLiveData<Bitmap>()//验证码图片
    var captchaText = MutableLiveData("")//输入的验证码
    var accountText = MutableLiveData("")
    var passwordText = MutableLiveData("")
    var snackMessage = MutableLiveData<String>()
    var emsCookies = MutableLiveData<Map<String, String>>()
    var requestInProgress = MutableLiveData(false)//请求网络状态
    var captchaInProgress = MutableLiveData(false)//请求网络状态
    var loginRetrys = MutableLiveData(0)//登录尝试次数，第三次开始显示验证码
    init {
        viewModelScope.launch(Dispatchers.IO) {
            if (requestInProgress.value == false) {
                requestInProgress.postValue(true)
                initialize()
                requestInProgress.postValue(false)
            }
        }
    }

    fun login() {
        viewModelScope.launch(Dispatchers.IO) {
            requestInProgress.postValue(true)
            loginRetrys.postValue(loginRetrys.value!!+1)
            //尝试次数大于等于2则显示验证码
            val result = LoginUtilsNew.login(
                accountText.value!!,
                passwordText.value!!,
                captchaText.value!!
            )
            when (result) {
                is Success -> {
                    loginRetrys.postValue(0)
                    show("登录成功")
                    emsCookies.postValue(result.data)
                }
                is ErrorMessage -> {
                    captchaText.postValue("")
                    //验证码验证错误则清空验证码框
                    show(result.msg)
                    when (result.msg) {
                        "验证码不正确" -> setCaptchaView()//重新设置验证码
                        "账户已被禁用" -> initialize()//需要重新初始化
                        "网络超时" -> initialize()//此处为timeout，会导致cookies和验证码过期，也需要重新初始化
                        "密码错误" -> {
                            passwordText.postValue("")
                        }
                    }
                }
            }
            requestInProgress.postValue(false)
        }
    }

    private fun setCaptchaView() {
        requestInProgress.postValue(true)
        captchaInProgress.postValue(true)
        val result = LoginUtilsNew.getCaptcha()
        when (result) {
            is Success -> {
                bitmap.postValue(result.data)
            }
            is ErrorMessage -> {
                show(result.msg)
            }
        }
        captchaInProgress.postValue(false)
        requestInProgress.postValue(false)
    }

    fun onCaptchaImageClicked() {
        viewModelScope.launch(Dispatchers.IO) {
            setCaptchaView()
        }
    }

    private fun initialize() {
        when (val result = LoginUtilsNew.casInitialize()) {
            is Success -> setCaptchaView()
            is ErrorMessage -> show(result.msg)
        }
    }

    private fun show(msg: String) {
        snackMessage.postValue(msg)
    }
}