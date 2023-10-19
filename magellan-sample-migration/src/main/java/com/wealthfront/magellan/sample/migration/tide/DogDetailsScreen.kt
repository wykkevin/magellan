package com.wealthfront.magellan.sample.migration.tide

import android.content.Context
import android.view.View
import android.widget.Toast
import com.wealthfront.magellan.OpenForMocking
import com.wealthfront.magellan.Screen
import com.wealthfront.magellan.lifecycle.attachFieldToLifecycle
import com.wealthfront.magellan.rx2.RxDisposer
import com.wealthfront.magellan.sample.migration.AppComponentContainer
import com.wealthfront.magellan.sample.migration.R
import com.wealthfront.magellan.sample.migration.api.DogApi
import com.wealthfront.magellan.sample.migration.toolbar.ToolbarHelper
import com.wealthfront.magellan.transitions.CircularRevealTransition
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import javax.inject.Inject

@OpenForMocking
class DogDetailsScreen(private val dogBreed: DogListStep.DogBreed) : Screen<DogDetailsView>() {

  @Inject lateinit var api: DogApi
  @Inject lateinit var toolbarHelper: ToolbarHelper
  private val rxUnsubscriber by attachFieldToLifecycle(RxDisposer())

  override fun createView(context: Context): DogDetailsView {
    (context.applicationContext as AppComponentContainer).injector().inject(this)
    return DogDetailsView(context)
  }

  override fun onShow(context: Context) {
    toolbarHelper.setTitle("Dog Breed Info")
    toolbarHelper.setMenuIcon(R.drawable.clock_white) {
      Toast.makeText(activity, "Menu - Notifications clicked", Toast.LENGTH_SHORT).show()
    }
    toolbarHelper.setMenuColor(R.color.water)

    rxUnsubscriber.autoDispose(
      if (dogBreed == DogListStep.DogBreed.RANDOM) {
        api.getRandomImage()
          .observeOn(mainThread())
          .subscribe {
            view!!.setDogPic(it.message)
          }
      } else {
        api.getRandomImageForBreed(dogBreed.getBreedName())
          .observeOn(mainThread())
          .subscribe {
            view!!.setDogPic(it.message)
          }
      }
    )
  }

  fun goToHelpScreen(originView: View) {
    navigator.goTo(HelpJourney(), CircularRevealTransition(originView))
  }
}
