package com.dream.pureems.login

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dream.pureems.data.ErrorMessage
import com.dream.pureems.data.Success
import kotlinx.coroutines.*

class LoginViewModel : ViewModel() {
    var bitmap = MutableLiveData<Bitmap>()//验证码图片
    var captchaText = MutableLiveData("")//输入的验证码
    var accountText = MutableLiveData("")
    var passwordText = MutableLiveData("")
    var snackMessage = MutableLiveData<String>()
    var emsCookies = MutableLiveData<Map<String, String>>()
    var requestInProgress = MutableLiveData(false)//请求网络状态
    var captchaInProgress = MutableLiveData(true)//请求网络状态

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
            val result = LoginUtils.login(
                captchaText.value!!,
                accountText.value!!,
                passwordText.value!!
            )
            when (result) {
                is Success -> {
                    show("登录成功")
                    emsCookies.postValue(result.data)
                }
                is ErrorMessage -> {
                    captchaText.postValue("")
                    //验证码验证错误则清空验证码框
                    show(result.msg)
                    when (result.msg) {
                        "验证码错误" -> setCaptchaView()//重新设置验证码
                        "验证码过期" -> initialize()//此处不仅仅为验证码过期，还包含cookies过期，因此需要重新初始化
                        "网络超时" -> initialize()//此处为timeout，会导致cookies和验证码过期，也需要重新初始化
                        "学号或密码错误" -> {
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
        val result = LoginUtils.getCaptcha()
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
        when (val result = LoginUtils.casInitialize()) {
            is Success -> setCaptchaView()
            is ErrorMessage -> show(result.msg)
        }
    }

    private fun show(msg: String) {
        snackMessage.postValue(msg)
    }
}