package com.dream.nwuplems.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
abstract class CourseDao {
    @Query("SELECT * from course_table")
    abstract fun getAllCourseLiveData(): LiveData<List<Course>>

    @Query("SELECT * from course_table")
    abstract fun getAllCourse(): List<Course>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insert(course: Course)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insert(courseList: List<Course>)

    @Query("DELETE FROM course_table")
    abstract suspend fun deleteAll()

    @Delete
    abstract suspend fun delete(course: Course)

    @Query("select count(*) from course_table")
    abstract suspend fun courseCount(): Int

    @Transaction
    open suspend fun insertAfterDeleted(courseList: List<Course>) {
        deleteAll()
        insert(courseList)
    }
}