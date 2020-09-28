package com.dream.pureems.database

data class GradeInfo(
    val date: String,
    val scoreAvg: Float,
    val avgScoreWeighted: Float,
    val creditSum: Float,
    val GPAAvg: Float,
    val avgGPAWeighted: Float
)