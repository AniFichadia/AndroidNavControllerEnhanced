package com.anifichadia.androidnavcontrollerenhanced

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.AttributeSet
import androidx.core.content.res.getIntegerOrThrow
import androidx.core.content.res.use
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.ActivityNavigator
import androidx.navigation.NavController
import androidx.navigation.NavController.OnNavigatedListener
import androidx.navigation.NavDestination
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.NavHostFragment
import java.util.regex.Pattern

/**
 * Custom [NavController] [Navigator] for activities. Allows you to launch an activity with a
 * result.
 *
 * This [Navigator] MUST be registered with the [NavController] BEFORE creating a [NavGraph].
 *
 * Use the [ActivityWithResultNavigator.register] methods to initialise this [Navigator]
 *
 * When a [NavGraph] is initialised, it uses any registered [Navigator]s to create the graph
 * nodes.
 *
 * This has the following limitations:
 *  - Bound to a FragmentActivity or Fragment with a [NavController]
 *  - The requestCode handling may not work with deep links (pending solution)
 *  - Has to observe navigation events
 *
 * @author Aniruddh Fichadia
 * @date 2018-09-05
 */
@Navigator.Name("activity")
open class ActivityWithResultNavigator private constructor(
        private val hostActivity: FragmentActivity
) : ActivityNavigator(hostActivity),
        OnNavigatedListener {

    companion object {
        private const val EXTRA_NAV_SOURCE = "android-support-navigation:ActivityNavigator:source"
        private const val EXTRA_NAV_CURRENT = "android-support-navigation:ActivityNavigator:current"


        fun register(activity: FragmentActivity, navController: NavController) {
            val navigator = ActivityWithResultNavigator(activity)

            navController.navigatorProvider.addNavigator(navigator)
            navController.addOnNavigatedListener(navigator)
        }


        fun register(fragment: Fragment, navController: NavController) {
            register(fragment.activity!!, navController)
        }
    }


    private val context: Context = hostActivity

    private var currentNodeIsFragment: Boolean = false


    override fun createDestination(): Destination {
        return Destination(this)
    }

    override fun navigate(destination: ActivityNavigator.Destination, args: Bundle?, navOptions: NavOptions?) {
        if (destination.intent == null) {
            throw IllegalStateException("Destination ${destination.id} does not have an Intent set.")
        }

        val intent = Intent(destination.intent)
        args?.let {
            intent.putExtras(it)
            val dataPattern = destination.dataPattern
            if (!TextUtils.isEmpty(dataPattern)) {
                // Fill in the data pattern with the it to build a valid URI
                val data = StringBuffer()
                val fillInPattern = Pattern.compile("\\{(.+?)\\}")
                val matcher = fillInPattern.matcher(dataPattern!!)
                while (matcher.find()) {
                    val argName = matcher.group(1)
                    if (it.containsKey(argName)) {
                        matcher.appendReplacement(data, "")
                        data.append(Uri.encode(it.getString(argName)))
                    } else {
                        throw IllegalArgumentException("Could not find $argName in $it to fill data pattern $dataPattern")
                    }
                }
                matcher.appendTail(data)
                intent.data = Uri.parse(data.toString())
            }
        }

        navOptions?.let {
            if (it.shouldClearTask()) {
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }

            if (it.shouldLaunchDocument() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
            } else if (context !is Activity) {
                // If we're not launching from an Activity context we have to launch in a new task.
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            if (it.shouldLaunchSingleTop()) {
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
        }

        val hostIntent = hostActivity.intent
        if (hostIntent != null) {
            val hostCurrentId = hostIntent.getIntExtra(EXTRA_NAV_CURRENT, 0)
            if (hostCurrentId != 0) {
                intent.putExtra(EXTRA_NAV_SOURCE, hostCurrentId)
            }
        }

        val destId = destination.id
        intent.putExtra(EXTRA_NAV_CURRENT, destId)
        NavOptions.addPopAnimationsToIntent(intent, navOptions)

        if (destination is Destination && destination.requestCode != null) {
            val requestCode: Int = destination.requestCode!!

            if (currentNodeIsFragment) {
                // TODO: Manage approach for active fragment handling
//                val activeFragment: Fragment = findActiveFragment(hostActivity) ?: throw
//                NullPointerException("Could not locate an active fragment to launch a new activity from")

                val activeFragment: Fragment? = findActiveFragment(hostActivity)

                if (activeFragment != null) {
                    hostActivity.startActivityFromFragment(activeFragment, intent, requestCode, args)
                } else {
                    hostActivity.startActivity(intent)
                }
            } else {
                hostActivity.startActivityForResult(intent, requestCode, args)
            }
        } else {
            hostActivity.startActivity(intent)
        }

        navOptions?.let {
            var enterAnim = it.enterAnim
            var exitAnim = it.exitAnim
            if (enterAnim != -1 || exitAnim != -1) {
                enterAnim = if (enterAnim != -1) enterAnim else 0
                exitAnim = if (exitAnim != -1) exitAnim else 0
                hostActivity.overridePendingTransition(enterAnim, exitAnim)
            }
        }

        // You can't pop the back stack from the caller of a new Activity,
        // so we don't add this navigator to the controller's back stack
        dispatchOnNavigatorNavigated(destId, Navigator.BACK_STACK_UNCHANGED)
    }


    open fun findActiveFragment(hostActivity: FragmentActivity): Fragment? {
        val navHostFragment: Fragment? = hostActivity.supportFragmentManager.fragments.find { it is NavHostFragment }

        val f: MutableList<Fragment>? = navHostFragment?.childFragmentManager?.fragments

        return if (f != null && f.isNotEmpty()) {
            f.lastOrNull()
        } else {
            navHostFragment
        }
    }


    //region OnNavigatedListener
    override fun onNavigated(controller: NavController, destination: NavDestination) {
        currentNodeIsFragment = destination.navigator is FragmentNavigator
    }
    //endregion


    /**
     * Navigation destination decorated with the [Activity]'s request code.
     *
     * Note: The request code is optional and does not need to be set
     */
    @NavDestination.ClassType(Activity::class)
    class Destination(activityNavigator: ActivityNavigator) : ActivityNavigator.Destination(activityNavigator) {
        private var mRequestCode: Int? = null

        val requestCode: Int?
            get() = mRequestCode


        @SuppressLint("Recycle")
        override fun onInflate(context: Context, attrs: AttributeSet) {
            super.onInflate(context, attrs)

            context.resources.obtainAttributes(attrs, R.styleable.ActivityWithResult).use {
                if (it.hasValue(R.styleable.ActivityWithResult_requestCode)) {
                    setRequestCode(it.getIntegerOrThrow(R.styleable.ActivityWithResult_requestCode))
                }
            }
        }

        fun setRequestCode(requestCode: Int): Destination {
            mRequestCode = requestCode
            return this
        }
    }
}