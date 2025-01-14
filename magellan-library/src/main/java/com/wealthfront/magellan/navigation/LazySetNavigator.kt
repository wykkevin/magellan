package com.wealthfront.magellan.navigation

import android.content.Context
import android.view.View
import androidx.annotation.VisibleForTesting
import com.wealthfront.magellan.Direction
import com.wealthfront.magellan.ScreenContainer
import com.wealthfront.magellan.init.shouldRunAnimations
import com.wealthfront.magellan.lifecycle.LifecycleAwareComponent
import com.wealthfront.magellan.lifecycle.LifecycleLimit
import com.wealthfront.magellan.lifecycle.LifecycleState
import com.wealthfront.magellan.transitions.DefaultTransition
import com.wealthfront.magellan.transitions.MagellanTransition
import com.wealthfront.magellan.transitions.NoAnimationTransition
import com.wealthfront.magellan.view.whenMeasured

public open class LazySetNavigator(
  protected val container: () -> ScreenContainer
) : LifecycleAwareComponent() {

  private var currentNavigableSetup: ((NavigableCompat) -> Unit)? = null
  private val navigationPropagator: NavigationPropagator = NavigationPropagator
  private var ongoingTransition: MagellanTransition? = null

  @VisibleForTesting
  internal var containerView: ScreenContainer? = null
  private var currentNavigable: NavigableCompat? = null

  protected val context: Context?
    get() = currentState.context

  public fun addNavigables(navigables: Set<NavigableCompat>) {
    for (navigable in navigables) {
      addNavigable(navigable)
    }
  }

  public fun addNavigable(navigable: NavigableCompat) {
    lifecycleRegistry.attachToLifecycleWithMaxState(navigable, LifecycleLimit.CREATED)
  }

  override fun onShow(context: Context) {
    containerView = container()
    currentNavigable?.let { currentNavigable ->
      containerView!!.addView(currentNavigable.view!!)
      currentNavigable.transitionStarted()
      currentNavigable.transitionFinished()
    }
  }

  override fun onHide(context: Context) {
    containerView = null
  }

  override fun onDestroy(context: Context) {
    lifecycleRegistry.children.forEach { lifecycleRegistry.removeFromLifecycle(it) }
  }

  public fun replace(
    navigable: NavigableCompat,
    requestedTransition: MagellanTransition = DefaultTransition()
  ) {
    if (currentNavigable == navigable) {
      return
    }

    ongoingTransition?.interrupt()
    val transition = if (currentNavigable == null) {
      NoAnimationTransition()
    } else {
      requestedTransition
    }

    containerView?.setInterceptTouchEvents(true)
    navigationPropagator.beforeNavigation()
    val from = navigateFrom(currentNavigable)
    currentNavigable = navigable
    currentNavigableSetup?.invoke(navigable)
    val to = navigateTo(navigable)
    animateAndRemove(from, to, transition)
  }

  protected open fun animateAndRemove(
    from: View?,
    to: View?,
    magellanTransition: MagellanTransition
  ) {
    if (to != null) {
      currentNavigable!!.transitionStarted()
    }
    to?.whenMeasured {
      val transition = if (shouldRunAnimations()) {
        magellanTransition
      } else {
        NoAnimationTransition()
      }
      ongoingTransition = transition
      transition.animate(from, to, Direction.FORWARD) {
        if (context != null && containerView != null) {
          containerView!!.removeView(from)
          currentNavigable!!.transitionFinished()
          navigationPropagator.afterNavigation()
          containerView!!.setInterceptTouchEvents(false)
        }
        ongoingTransition = null
      }
    }
  }

  protected open fun navigateTo(currentNavigable: NavigableCompat): View? {
    lifecycleRegistry.updateMaxState(currentNavigable, LifecycleLimit.NO_LIMIT)
    navigationPropagator.onNavigatedTo(currentNavigable)
    when (currentState) {
      is LifecycleState.Shown, is LifecycleState.Resumed -> {
        containerView!!.addView(currentNavigable.view!!, 0)
      }
      is LifecycleState.Destroyed, is LifecycleState.Created -> {
      }
    }
    return currentNavigable.view
  }

  protected open fun navigateFrom(currentNavigable: NavigableCompat?): View? {
    return currentNavigable?.let { oldNavigable ->
      val currentView = oldNavigable.view
      lifecycleRegistry.updateMaxState(oldNavigable, LifecycleLimit.SHOWN)
      navigationPropagator.onNavigatedFrom(oldNavigable)
      currentView
    }
  }

  override fun backPressed(): Boolean {
    return currentNavigable?.backPressed() ?: false
  }
}
