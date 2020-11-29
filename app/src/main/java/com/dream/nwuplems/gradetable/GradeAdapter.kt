package com.dream.nwuplems.gradetable

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dream.nwuplems.R
import com.dream.nwuplems.database.Grade

class GradeAdapter : RecyclerView.Adapter<GradeAdapter.ViewHolder>() {
    var data = listOf<Grade>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount() = data.size
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        holder.courseName.text = item.courseName
        holder.gradeInfo.text = "GPA" + item.GPA.toString() + "/学分" + item.credit
        holder.courseDate.text = item.date.toString()
        holder.score.text = item.score.toInt().toString()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater
            .inflate(R.layout.item_grade_detail_card, parent, false)
        return ViewHolder(view)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val courseName: TextView = itemView.findViewById(R.id.course_name)
        val gradeInfo: TextView = itemView.findViewById(R.id.grade_info)
        val score: TextView = itemView.findViewById(R.id.score)
        val courseDate: TextView = itemView.findViewById(R.id.course_date)
    }
}