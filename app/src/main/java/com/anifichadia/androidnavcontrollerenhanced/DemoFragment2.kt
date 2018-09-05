package com.anifichadia.androidnavcontrollerenhanced

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

/**
 * @author Aniruddh Fichadia
 * @date 2018-09-05
 */
class DemoFragment2 : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_demo_fragment_2, container, false).also {
            it.findViewById<View>(R.id.btn_prev).setOnClickListener {
                findNavController().popBackStack()
            }

            it.findViewById<View>(R.id.btn_next).setOnClickListener {
                // TODO: Navigate to the activity
                findNavController().navigate(
                        DemoFragment2Directions.actionDemoFragment2ToDemoWithResultActivity()
                )
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            // TODO: Handle the result!
            resources.getInteger(R.integer.request_code_demo_with_result) -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        Toast.makeText(context, "Success", Toast.LENGTH_LONG).show()
                    }
                    else -> {
                        Toast.makeText(context, "Cancelled", Toast.LENGTH_LONG).show()
                    }
                }
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }
}
