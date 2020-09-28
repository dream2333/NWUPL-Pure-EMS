package com.dream.pureems.gradetable

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat.getColor
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionInflater
import com.dream.pureems.R
import com.dream.pureems.database.Grade
import com.dream.pureems.databinding.FragmentGradeTableBinding
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_grade_table.*
import java.text.DecimalFormat

class GradeTableFragment : Fragment(), AdapterView.OnItemSelectedListener {
    private lateinit var binding: FragmentGradeTableBinding
    private val viewModel: GradeTableViewModel by viewModels(
        factoryProducer = {
            GradeTableViewModelFactory(
                requireActivity().application
            )
        }
    )
    private lateinit var bbehavior: BottomSheetBehavior<RecyclerView>
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.drawerButton2.setOnClickListener {
            val drawer = requireActivity().findViewById<DrawerLayout>(R.id.drawer_layout)
            drawer.openDrawer(GravityCompat.START)
        }
        //抽屉按钮
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val transInflater = TransitionInflater.from(requireContext())
        exitTransition = transInflater.inflateTransition(R.transition.fade)
        enterTransition = transInflater.inflateTransition(R.transition.explode)
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_grade_table, container, false)
        binding.lifecycleOwner = this
        binding.gradeTableViewModel = viewModel
        val adapter = GradeAdapter()
        binding.gradeList.adapter = adapter
        binding.semesterImage3.onItemSelectedListener = this
        bbehavior = BottomSheetBehavior.from(binding.gradeList)
        setChart()
        viewModel.semesterGradeLiveData.observe(viewLifecycleOwner, {
            adapter.data = it
            val barDataSet = BarDataSet(byScore(it), "")
            barDataSet.color = getColor(requireContext(), R.color.design_default_color_primary)
            barDataSet.setDrawValues(true)
            barDataSet.valueTextSize = 12f
            barDataSet.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(v: Float): String {
                    return v.toInt().toString()
                    //设置自己的返回位数
                }
            }
            binding.chartView.data = BarData(barDataSet)
            binding.chartView.invalidate()
        })

        viewModel.allGradeInfo.observe(viewLifecycleOwner, {
            semesterImage3.adapter = ArrayAdapter(
                requireContext(),
                R.layout.support_simple_spinner_dropdown_item,
                it.map { i -> i.date })
        })

        viewModel.gradeInfoLiveData.observe(viewLifecycleOwner, {
            val myFormatter = DecimalFormat("####.00")
            binding.scoreBar.setPercentData(it.scoreAvg, DecelerateInterpolator(), true)
            binding.scoreView.text = myFormatter.format(it.scoreAvg) + "/" + myFormatter.format(
                it.avgScoreWeighted
            )
            binding.gradeBar.setPercentData(it.GPAAvg, DecelerateInterpolator(), false)
            binding.gradeView.text =
                myFormatter.format(it.GPAAvg) + "/" + myFormatter.format(it.avgGPAWeighted)
            binding.titleText.text = it.date.substring(0, 4) + "-" + it.date.substring(4, 8) + "学年度"
            binding.titleText2.text = "第" + it.date[8] + "学期"
        })
        //选学期菜单
        viewModel.snackMessage.observe(viewLifecycleOwner, {
            Snackbar.make(requireActivity().findViewById(R.id.rootView), it, Snackbar.LENGTH_LONG)
                .show()
        })
        return binding.root
    }

    private fun setChart() {
        val section = arrayOf("0-60分", "60-70分", "70-80分", "80-90分", "90-100分")
        binding.chartView.setDrawBarShadow(false)
        binding.chartView.description.isEnabled = false
        binding.chartView.setDrawGridBackground(false)
        binding.chartView.setTouchEnabled(false)
        binding.chartView.animateY(700, Easing.EaseInSine)
        val xaxis = binding.chartView.xAxis
        xaxis.setDrawGridLines(false)
        xaxis.position = XAxis.XAxisPosition.BOTTOM
        xaxis.granularity = 1f
        xaxis.setDrawLabels(true)
        xaxis.setDrawAxisLine(false)
        xaxis.valueFormatter = IndexAxisValueFormatter(section)

        val yAxisLeft = binding.chartView.axisLeft
        yAxisLeft.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
        yAxisLeft.setDrawGridLines(false)
        yAxisLeft.setDrawAxisLine(false)
        yAxisLeft.isEnabled = false

        binding.chartView.axisRight.isEnabled = false
        val legend = binding.chartView.legend
        legend.isEnabled = false
    }

    private fun byScore(list: List<Grade>): ArrayList<BarEntry> {
        //按分数分类
        val valueSet = arrayOf(0, 0, 0, 0, 0)
        list.forEach {
            when {
                it.score < 60 -> {
                    valueSet[0] += 1
                }
                it.score < 70 -> {
                    valueSet[1] += 1
                }
                it.score < 80 -> {
                    valueSet[2] += 1
                }
                it.score < 90 -> {
                    valueSet[3] += 1
                }
                it.score <= 100 -> {
                    valueSet[4] += 1
                }
            }
        }
        return arrayListOf(
            BarEntry(0f, valueSet[0].toFloat()),
            BarEntry(1f, valueSet[1].toFloat()),
            BarEntry(2f, valueSet[2].toFloat()),
            BarEntry(3f, valueSet[3].toFloat()),
            BarEntry(4f, valueSet[4].toFloat())
        )
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        viewModel.refreshList(position)
        binding.chartView.animateY(700, Easing.EaseInSine)
        bbehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        //TODO("Not yet implemented")
    }

}

