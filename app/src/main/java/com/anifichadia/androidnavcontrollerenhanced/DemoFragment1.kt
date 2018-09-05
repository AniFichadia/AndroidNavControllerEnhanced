package com.anifichadia.androidnavcontrollerenhanced

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

/**
 * @author Aniruddh Fichadia
 * @date 2018-09-05
 */
class DemoFragment1 : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_demo_fragment_1, container, false).also {
            it.findViewById<View>(R.id.btn_next).setOnClickListener {
                findNavController().navigate(
                        DemoFragment1Directions.actionDemoFragment1ToDemoFragment2()
                )
            }
        }
    }
}