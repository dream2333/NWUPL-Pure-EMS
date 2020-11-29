package com.dream.nwuplems.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "grade_table")
data class Grade(
    val courseName: String,
    val credit: Int,
    val score: Float,
    val date: Int,
    val GPA: Double = pointOfEachCourse(score.toInt()),
    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0
)

fun pointOfEachCourse(grade: Int): Double { //根据实际成绩判断学分方法
    var point = 0.0
    if (grade in 90..100) point = 4.0
    if (grade in 85..89) point = 3.7
    if (grade in 82..84) point = 3.3
    if (grade in 78..81) point = 3.0
    if (grade in 75..77) point = 2.7
    if (grade in 72..74) point = 2.3
    if (grade in 68..71) point = 2.0
    if (grade in 64..67) point = 1.5
    if (grade in 60..63) point = 1.0
    if (grade < 60) point = 0.0
    return point
}