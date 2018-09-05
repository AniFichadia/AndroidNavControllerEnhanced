package com.anifichadia.androidnavcontrollerenhanced

class MainActivity : NavHostActivity() {
    override val navGraphResId: Int = R.navigation.main_navigation

    override val defaultDefaultDestinationTitle: CharSequence by lazy {
        getString(R.string.app_name)
    }
}
