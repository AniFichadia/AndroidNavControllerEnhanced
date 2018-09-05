package com.anifichadia.androidnavcontrollerenhanced

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_demo_with_result.*

/**
 * @author Aniruddh Fichadia
 * @date 2018-09-05
 */
class DemoWithResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_demo_with_result)

        btn_result_ok.setOnClickListener {
            setResult(Activity.RESULT_OK)
            finish()
        }

        btn_result_cancelled.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }
}