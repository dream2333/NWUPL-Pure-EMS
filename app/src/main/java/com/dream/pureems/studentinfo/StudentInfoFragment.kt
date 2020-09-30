package com.dream.pureems.studentinfo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.transition.TransitionInflater
import com.dream.pureems.R
import com.dream.pureems.databinding.FragmentStudentInfoBinding
import com.ethanhua.skeleton.Skeleton
import com.google.android.material.snackbar.Snackbar

class StudentInfoFragment : Fragment() {
    private lateinit var binding: FragmentStudentInfoBinding
    private val viewModel: StudentInfoViewModel by viewModels(
        factoryProducer = {
            StudentInfoViewModelFactory(
                requireActivity().application
            )
        }
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val transInflater = TransitionInflater.from(requireContext())
        exitTransition = transInflater.inflateTransition(R.transition.fade)
        enterTransition = transInflater.inflateTransition(R.transition.explode)
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_student_info, container, false)
        binding.lifecycleOwner = this
        binding.gradeTableViewModel = viewModel
        val studentInfoSkeleton1 = Skeleton.bind(binding.studentInfo1)
            .load(R.layout.skeleton_student_info1)
            .show()

        val studentInfoSkeleton2 = Skeleton.bind(binding.studentInfo2)
            .load(R.layout.skeleton_student_info2)
            .show()

        viewModel.info.observe(viewLifecycleOwner, {
            studentInfoSkeleton1.hide()
            studentInfoSkeleton2.hide()
            binding.nameText.text = it[3]
            binding.numberText.text = it[1]
            if (it[5] == "男") {
                binding.genderImage.setImageResource(R.drawable.ic_male)
            } else {
                binding.genderImage.setImageResource(R.drawable.ic_female)
            }
            binding.include1.findViewById<TextView>(R.id.title_text).text = it[6]
            binding.include1.findViewById<TextView>(R.id.info_text).text = it[7]
            binding.include2.findViewById<TextView>(R.id.title_text).text = it[8]
            binding.include2.findViewById<TextView>(R.id.info_text).text = it[9]
            binding.include3.findViewById<TextView>(R.id.title_text).text = it[10]
            binding.include3.findViewById<TextView>(R.id.info_text).text = it[11]
            binding.include4.findViewById<TextView>(R.id.title_text).text = it[12]
            binding.include4.findViewById<TextView>(R.id.info_text).text = it[13]
            binding.include5.findViewById<TextView>(R.id.title_text).text = it[14]
            binding.include5.findViewById<TextView>(R.id.info_text).text = it[15]
            binding.include6.findViewById<TextView>(R.id.title_text).text = it[16]
            binding.include6.findViewById<TextView>(R.id.info_text).text = it[17]
            binding.include7.findViewById<TextView>(R.id.title_text).text = it[18]
            binding.include7.findViewById<TextView>(R.id.info_text).text = it[19]
            binding.include8.findViewById<TextView>(R.id.title_text).text = it[20]
            binding.include8.findViewById<TextView>(R.id.info_text).text = it[21]
        })
        viewModel.snackMessage.observe(viewLifecycleOwner, {
            Snackbar.make(requireActivity().findViewById(R.id.rootView), it, Snackbar.LENGTH_LONG)
                .show()
        })
        binding.drawerButton3.setOnClickListener {
            val drawer = requireActivity().findViewById<DrawerLayout>(R.id.drawer_layout)
            drawer.openDrawer(GravityCompat.START)
        }
        return binding.root
    }

}