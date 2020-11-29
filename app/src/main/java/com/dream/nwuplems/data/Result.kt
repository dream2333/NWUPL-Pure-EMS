package com.dream.nwuplems.data

//储存登录状态
sealed class Result<out T>

data class Success<out T>(val data: T) : Result<T>()
data class ErrorMessage(val msg: String) : Result<Nothing>()


