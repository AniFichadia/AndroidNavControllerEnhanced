package com.anifichadia.androidnavcontrollerenhanced

import android.os.Bundle
import android.view.MenuItem
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavController.OnNavigatedListener
import androidx.navigation.NavDestination
import androidx.navigation.Navigation

/**
 * @author Aniruddh Fichadia
 * @date 2018-09-05
 */
abstract class NavHostActivity : AppCompatActivity(), OnNavigatedListener {

    companion object {
        const val KEY_NAV_CONTROLLER_STATE = "com.anifichadia.androidnavcontrollerenhanced.NavHostActivity.key_nav_controller_state"
    }


    protected val navController: NavController by lazy {
        Navigation.findNavController(this, navHostResId)
    }

    private val _navGraphResId: Int
        get() = navGraphResId.also {
            if (it == 0) {
                throw IllegalStateException("navGraphResId not set")
            }
        }

    protected abstract val navGraphResId: Int


    @LayoutRes
    protected open val layoutRes: Int = R.layout.activity_nav_host

    @IdRes
    protected open val navHostResId: Int = R.id.nav_host


    open fun shouldShowBackButton(destination: NavDestination, startDestination: Boolean): Boolean = !startDestination

    open fun getDestinationTitle(destination: NavDestination): CharSequence =
            destination.label ?: defaultDefaultDestinationTitle

    abstract val defaultDefaultDestinationTitle: CharSequence


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(layoutRes)

        ActivityWithResultNavigator.register(this, navController)

        navController.setGraph(_navGraphResId)

        navController.addOnNavigatedListener(this)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            // Use the same behaviour as onBackPressed for home/back button presses
            onBackPressed()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putBundle(KEY_NAV_CONTROLLER_STATE, navController.saveState())
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        navController.restoreState(savedInstanceState.getBundle(KEY_NAV_CONTROLLER_STATE))
    }


    //region OnNavigatedListener
    override fun onNavigated(controller: NavController, destination: NavDestination) {
        val isStartDestination = destination.parent?.startDestination == destination.id
        val title: CharSequence = getDestinationTitle(destination)

        supportActionBar?.apply {
            val showBackButton = shouldShowBackButton(destination, isStartDestination)

            setHomeButtonEnabled(showBackButton)
            setDisplayHomeAsUpEnabled(showBackButton)

            setTitle(title)
        }

        setTitle(title)
    }
    //endregion
}
