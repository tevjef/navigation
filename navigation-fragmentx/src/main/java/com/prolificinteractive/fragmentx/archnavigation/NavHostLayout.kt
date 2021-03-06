package com.prolificinteractive.fragmentx.archnavigation

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.*
import com.ncapdevi.fragnav.FragNavController
import com.prolificinteractive.conductor.archnavigation.R


/**
 * NavHostController provides an area within your layout for self-contained navigation to occur.
 *
 * NavHostController is intended to be used as the content area within a layout resource
 * defining your app's chrome around it, e.g.:
 *
 * Each NavHostController has a [NavController] that defines valid navigation within
 * the navigation host. This includes the [navigation graph][NavGraph] as well as navigation
 * state such as current location and back stack that will be saved and restored along with the
 * NavHostController itself.
 *
 * NavHostControllers register their navigation controller at the root of their view subtree
 * such that any descendant can obtain the controller instance through the [Navigation]
 * helper class's methods such as [Navigation.findNavController]. View event listener
 * implementations such as [View.OnClickListener] within navigation destination
 * controllers can use these helpers to navigate based on user interaction without creating a tight
 * coupling to the navigation host.
 */
class NavHostLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), NavHost, LifecycleObserver {

  private var graphId: Int = 0
  private var defaultHost: Boolean = false
  private var viewModel: StateViewModel
  private var navigationController: NavController
  private var controller: FragNavController

  private var fragmentXNavigator: FragmentXNavigator

  init {
    val a = context.obtainStyledAttributes(attrs, R.styleable.NavHostLayout)
    graphId = a.getResourceId(R.styleable.NavHostLayout_navGraph, 0)
    defaultHost = a.getBoolean(R.styleable.NavHostLayout_defaultNavHost, false)
    a.recycle()

    viewModel = ViewModelProviders.of(context as AppCompatActivity).get(StateViewModel::class.java)
    val savedInstanceState = viewModel.state

    controller = FragNavController((this.context as AppCompatActivity).supportFragmentManager, id)
    navigationController = NavController(context)

    fragmentXNavigator = FragmentXNavigator(controller)
    navigationController.navigatorProvider += fragmentXNavigator

    // Inflate the nav graph to find the rootFragments
    val navGraph = NavInflater(context, navigationController.navigatorProvider).inflate(graphId)
    controller.rootFragments = navGraph.mapNotNull {
      if (it is FragmentXNavigator.Destination && it.isRootFragment) {
        it.createFragment(null)
      } else {
        null
      }
    }

    Navigation.setViewNavController(this, navigationController)

    var navState: Bundle? = null
    if (savedInstanceState != null) {
      navState = savedInstanceState.getBundle(KEY_NAV_CONTROLLER_STATE)

      if (savedInstanceState.getBoolean(KEY_DEFAULT_NAV_HOST, false)) {
        defaultHost = true
      }
    }

    if (navState != null) {
      navigationController.restoreState(navState)
    } else {
      if (defaultHost) {
        if (graphId != 0) {
          navigationController.setGraph(graphId)
        } else {
          navigationController.setMetadataGraph()
        }
      }
    }
  }

  override fun getNavController(): NavController = navigationController

  override fun onSaveInstanceState(): Parcelable? {
    val navState = navigationController.saveState()
    if (navState != null) {
      viewModel.state.putBundle(KEY_NAV_CONTROLLER_STATE, navState)
    }
    if (defaultHost) {
      viewModel.state.putBoolean(KEY_DEFAULT_NAV_HOST, true)
    }
    return super.onSaveInstanceState()
  }

  class StateViewModel : ViewModel() {
    internal val state: Bundle = Bundle()
  }

  companion object {
    private const val KEY_NAV_CONTROLLER_STATE = "navControllerState"
    private const val KEY_DEFAULT_NAV_HOST = "defaultHost"
  }
}