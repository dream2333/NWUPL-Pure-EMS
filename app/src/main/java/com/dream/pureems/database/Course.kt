package com.dream.pureems.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "course_table")
data class Course(
    val teachersName: String,
    val courseName: String,
    val location: String,
    val weeks: String,
    val day: Int,
    val start: Int,
    val length: Int,
    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0
)