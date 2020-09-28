package com.dream.pureems.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
abstract class GradeDao {
    @Query("SELECT * from (SELECT * from grade_table ORDER BY credit DESC) ORDER BY date DESC")
    abstract fun getAllGradeLivedata(): LiveData<List<Grade>>

    @Query("SELECT * from (SELECT * from grade_table ORDER BY credit DESC) ORDER BY date DESC")
    abstract fun getAllGradeList(): List<Grade>

    @Query("SELECT date,AVG(GPA) AS GPAAvg,SUM(credit) AS creditSum,AVG(score) AS scoreAvg,SUM(scoreWeighted)/SUM(credit) AS avgScoreWeighted,SUM(GPAWeighted)/SUM(credit) AS avgGPAWeighted FROM (SELECT *,score*credit AS scoreWeighted ,GPA*credit AS GPAWeighted FROM grade_table) GROUP BY date ORDER BY date DESC")
    abstract fun getAllGradeInfo(): List<GradeInfo>

    @Query("SELECT * from (SELECT * from grade_table ORDER BY credit DESC) WHERE date = :Smester ORDER BY date DESC")
    abstract fun getSemesterGradeList(Smester: String): List<Grade>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insert(grade: Grade)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insert(gradeList: List<Grade>)

    @Query("DELETE FROM grade_table ")
    abstract suspend fun deleteAll()

    @Delete
    abstract suspend fun delete(grade: Grade)

    @Query("select count(*) from grade_table")
    abstract suspend fun count(): Int

    @Transaction
    open suspend fun insertAfterDeleted(gradeList: List<Grade>) {
        deleteAll()
        insert(gradeList)
    }
}