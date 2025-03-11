package com.my.raido.ui.home.bottomsheet_fragments.driver

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.my.raido.databinding.FragmentDriverInfoBinding

class DriverInfoFragment : BottomSheetDialogFragment() {

    companion object{
        private const val TAG = "Driver Info Fragment"
    }

    private lateinit var binding: FragmentDriverInfoBinding

    private lateinit var myContext: Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        myContext = context
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDriverInfoBinding.inflate(layoutInflater)



        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.alertdialogCloseBtn.setOnClickListener {
            dialog?.dismiss()
        }

        binding.backHomeBtn.setOnClickListener {
            activity?.finish()
        }

    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        dialog.setOnShowListener {
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.layoutParams?.height = ViewGroup.LayoutParams.MATCH_PARENT

//            This is for avoid bottomsheet dismiss on dragging
            if (bottomSheet != null) {
                val behavior = BottomSheetBehavior.from(bottomSheet)
                // Disable dragging
//                behavior.isDraggable = false
            }
        }

        return dialog
    }

    override fun onStart() {
        super.onStart()

        // Set the height of the bottom sheet to cover 3/4 of the screen
        val dialog = dialog
        dialog?.let {
            val bottomSheet = it.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            val layoutParams = bottomSheet.layoutParams
            layoutParams.height = (resources.displayMetrics.heightPixels * 0.9).toInt() // 75% of screen height
            bottomSheet.layoutParams = layoutParams
        }

    }

}