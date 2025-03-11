package com.my.raido.ui.home.bottomsheet_fragments.drawers.claims.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.my.raido.databinding.FragmentClaimAutoBinding


class ClaimAutoFragment : Fragment() {

    companion object{
        private const val TAG = "Claim Auto Fragment"
    }

    private lateinit var binding:FragmentClaimAutoBinding

    private lateinit var myContext: Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        myContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentClaimAutoBinding.inflate(inflater, container, false)





        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }

}