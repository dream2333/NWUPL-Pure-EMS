package com.dream.nwuplems.coursetable

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.transition.TransitionInflater
import com.dream.nwuplems.R
import com.dream.nwuplems.widgets.ViewUtils
import com.dream.nwuplems.database.Course
import com.dream.nwuplems.databinding.FragmentCourseTableBinding
import com.dream.nwuplems.widgets.CourseWidgetProvider
import com.dream.nwuplems.widgets.UpdateService
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_course_table.*
import kotlinx.android.synthetic.main.item_course_card.view.*
import kotlinx.android.synthetic.main.item_course_detail.view.*
import kotlinx.android.synthetic.main.item_course_number.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class CourseTableFragment : Fragment(), AdapterView.OnItemSelectedListener {
    private lateinit var binding: FragmentCourseTableBinding
    private val viewModel: CourseTableViewModel by viewModels(
        factoryProducer = {
            CourseTableViewModelFactory(
                requireActivity().application
            )
        }
    )
    private lateinit var day: Array<FrameLayout>
    private lateinit var title: Array<TextView>
    private var selectedPosition: Int = 0
    private var operationIsDelete = false
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_course_table, container, false)
        binding.lifecycleOwner = this
        binding.courseTableViewModel = viewModel
        viewModel.snackMessage.observe(viewLifecycleOwner, {
            Snackbar.make(requireActivity().findViewById(R.id.rootView), it, Snackbar.LENGTH_LONG)
                .show()
        })
        val transInflater = TransitionInflater.from(requireContext())
        exitTransition = transInflater.inflateTransition(R.transition.fade)
        enterTransition = transInflater.inflateTransition(R.transition.explode)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        day = arrayOf(
            binding.courseColumn0,
            binding.courseColumn1,
            binding.courseColumn2,
            binding.courseColumn3,
            binding.courseColumn4,
            binding.courseColumn5,
            binding.courseColumn6
        )
        title = arrayOf(
            binding.weekTitle1,
            binding.weekTitle2,
            binding.weekTitle3,
            binding.weekTitle4,
            binding.weekTitle5,
            binding.weekTitle6,
            binding.weekTitle7
        )
        //课程列
        val dayOfWeek = viewModel.currentDate[2] - 1
        title[dayOfWeek].setTextColor(Color.WHITE)
        title[dayOfWeek].background =
            ContextCompat.getDrawable(requireContext(), R.drawable.card_shape)
        title[dayOfWeek].typeface = Typeface.DEFAULT_BOLD
        //绘制表头
        val cardHeight = 150
        drawTimeLine(cardHeight, 2, 12)
        //绘制课程格子
        drawerButton.setOnClickListener {
            val drawer = requireActivity().findViewById<DrawerLayout>(R.id.drawer_layout)
            drawer.openDrawer(GravityCompat.START)
        }
        //抽屉按钮
        viewModel.courseList.observe(viewLifecycleOwner, {
            if (!operationIsDelete) {
                //如果是因为删除而更新，则跳过
                clearColumn()
                Handler().postDelayed({ drawCourseTable(it, cardHeight, 2) }, 1000)
                Log.i("课程详情", "$it")
            } else {
                operationIsDelete = false
            }
            updateWidget()
        })
        //数据库更新则课程表同步更新
        viewModel.semesterList.observe(viewLifecycleOwner, {
            semesterImage.adapter = ArrayAdapter(
                requireContext(),
                R.layout.support_simple_spinner_dropdown_item,
                it.map { i -> i.str })
            selectedPosition = semesterImage.selectedItemPosition
            semesterImage.onItemSelectedListener = this
        })
        //学期切换按钮
        updateWidget()
        //更新小部件
    }

    private fun updateWidget() {
        //延时，等待绘制完毕
        Handler().postDelayed({
            val info = ViewUtils.getScreenInfo(requireContext())
            ViewUtils.layoutView(binding.courseLayout, info[0], info[1])
            ViewUtils.scheduleImage =
                Bitmap.createBitmap(ViewUtils.loadBitmapFromView(binding.courseLayout))
            val views = RemoteViews(
                requireContext().packageName,
                R.layout.widget_course_table
            )
            val widgetManager = AppWidgetManager.getInstance(requireContext().applicationContext)
            val appWidgetId = widgetManager.getAppWidgetIds(
                ComponentName(
                    requireContext(),
                    CourseWidgetProvider::class.java
                )
            )
            val svcIntent = Intent(requireContext(), UpdateService::class.java)
            svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            svcIntent.data = Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME))
            views.setRemoteAdapter(R.id.image_list, svcIntent)
            val dayOfWeek = viewModel.currentDate[2] - 1
            val day = arrayOf(
                R.id.widget_day1,
                R.id.widget_day2,
                R.id.widget_day3,
                R.id.widget_day4,
                R.id.widget_day5,
                R.id.widget_day6,
                R.id.widget_day7
            )
            day.forEachIndexed { ind, ele ->
                views.setTextViewText(ele, viewModel.titleArray[ind])
                views.setTextColor(ele, Color.parseColor("#9F9F9F"))
            }
            views.setTextViewText(R.id.widget_month, viewModel.currentDate[0].toString() + "\n月")
            views.setTextColor(day[dayOfWeek], Color.BLACK)
            widgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.image_list)
            widgetManager.updateAppWidget(appWidgetId, views)
        }, 2000)
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        //放空
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if (selectedPosition != position) {
            viewModel.semesterId.value = viewModel.semesterList.value?.get(position)!!.id
            selectedPosition = position
        }
    }

    private fun drawCourseTable(courseList: List<Course>, cardHeight: Int, realMargin: Int) {
        val detailDialog = LayoutInflater.from(context).inflate(R.layout.item_course_detail, null)
        val detailPop = PopupWindow(
            detailDialog,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )
        detailPop.animationStyle = R.style.popwin_anim_style
        //弹出的课程消息
        val colorIterator = getColorIterator()
        val courseColorMap = mutableMapOf<String, Int>()
        //颜色控制，确保同课程名颜色相同
        courseList.forEach { course ->
            val column = course.day
            val row = course.start
            val lessonCardView = LayoutInflater.from(context)
                .inflate(R.layout.item_course_card, courseTable, false)
            val params = FrameLayout.LayoutParams(lessonCardView.layoutParams).apply {
                setMargins(0, row * (cardHeight + realMargin * 2) + 2, 0, 0)
                width = LinearLayout.LayoutParams.MATCH_PARENT
                height = (cardHeight) * course.length
            }
            day[column].addView(lessonCardView, params)
            //课程卡片布局信息
            val color: Int
            if (!courseColorMap.containsKey(course.courseName)) {
                color = colorIterator.next()//获取颜色
                courseColorMap[course.courseName] = color
            } else {
                color = courseColorMap[course.courseName]!!
            }
            lessonCardView.lessonCard.backgroundTintList =
                ColorStateList.valueOf(color)
            //颜色控制，确保同课程名颜色相同
            lessonCardView.lessonCard.text = course.courseName + "\n*" + course.location
            lessonCardView.lessonCard.typeface = Typeface.DEFAULT_BOLD
            val week = viewModel.repository.teachingWeek
            if (week > course.weeks.length || course.weeks[week - 1] == '0') {
                lessonCardView.lessonCard.background.alpha = 60
            }
            //课程卡片属性
            lessonCardView.setOnClickListener { card ->
                detailPop.contentView.card.teacherName.text = course.teachersName
                detailPop.contentView.card.courseName.text = course.courseName
                detailPop.contentView.card.locationName.text = course.location
                val weekmode = when (course.weeks.substring(0..1)) {
                    "01" -> "(双周)"
                    "10" -> "(单周)"
                    else -> "(连续)"
                }
                val firstweek = course.weeks.indexOf("1") + 1
                val lastweek = course.weeks.lastIndexOf("1") + 1
                detailPop.contentView.card.weeksLength.text = "第${firstweek}-${lastweek}周$weekmode"
                val dayOfWeek = when (course.day) {
                    0 -> "周一"
                    1 -> "周二"
                    2 -> "周三"
                    3 -> "周四"
                    4 -> "周五"
                    5 -> "周六"
                    7 -> "周日"
                    else -> "错误"
                }
                detailPop.contentView.card.timeLength.text =
                    "$dayOfWeek${course.start + 1}-${course.start + course.length + 1}节"
                //窗口内显示的课程信息
                detailPop.contentView.deleteButton.setOnClickListener {
                    GlobalScope.launch {
                        viewModel.repository.delete(course)
                    }
                    detailPop.dismiss()
                    day[column].removeView(card)
                    operationIsDelete = true
                }
                //删除键
                detailPop.showAtLocation(view, Gravity.CENTER, 0, 0)
                //设置信息并显示弹窗
                setWindowAlpha(0.6f)
            }
            detailPop.setOnDismissListener {
                setWindowAlpha(1f)
            }
        }
        //绘制周一到周五的课表

    }


    private fun drawTimeLine(cardHeight: Int, realMargin: Int, total: Int) {
        for (i in 0 until total) {
            val number = LayoutInflater.from(context)
                .inflate(R.layout.item_course_number, courseTable, false)
            number.courseNumber.text = (i + 1).toString()
            val params = FrameLayout.LayoutParams(number.layoutParams).apply {
                setMargins(0, realMargin, 0, realMargin)
                height = cardHeight
            }
            periodColumn.addView(number, params)
        }
        //绘制左侧课时的纵向列表（我神经病吗，搞这个干嘛）
    }

    private fun setWindowAlpha(alpha: Float) {
        val lp = activity?.window?.attributes
        lp?.alpha = alpha
        activity?.window?.attributes = lp
        //背景变黑
    }

    private fun clearColumn() {
        day.forEach {
            it.removeAllViews()
        }
    }


    private fun getColorIterator() = iterator {
        val colors = resources.getIntArray(R.array.customizedColors)
        var cur = 0
        while (true) {
            yield(colors[cur])
            cur = (cur + 1) % colors.size
        }
    }
}
