package com.wealthfront.magellan.sample.migration.tide

import android.app.Application
import android.content.Context
import android.os.Looper.getMainLooper
import androidx.activity.ComponentActivity
import androidx.test.core.app.ApplicationProvider
import com.wealthfront.magellan.lifecycle.LifecycleState
import com.wealthfront.magellan.lifecycle.transitionToState
import com.wealthfront.magellan.sample.migration.AppComponentContainer
import com.wealthfront.magellan.sample.migration.TestAppComponent
import com.wealthfront.magellan.sample.migration.api.DogApi
import com.wealthfront.magellan.sample.migration.api.DogMessage
import io.reactivex.Observable
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.mockito.quality.Strictness
import org.robolectric.Robolectric.buildActivity
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import javax.inject.Inject

@RunWith(RobolectricTestRunner::class)
class DogDetailsScreenTest {
  private lateinit var screen: DogDetailsScreen
  private val activity = buildActivity(ComponentActivity::class.java).get()
  private val breedData = DogMessage(
    message = "image-url",
    status = "success"
  )
  private val randomBreedData = DogMessage(
    message = "random-image-url",
    status = "success"
  )

  @Inject lateinit var api: DogApi
  @Mock lateinit var dogDetailsView: DogDetailsView
  val specificDogBreed = DogListStep.DogBreed.BEAGLE
  val randomDogBreed = DogListStep.DogBreed.RANDOM

  @Rule @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule().strictness(Strictness.WARN)

  @Before
  fun setup() {
    val context = ApplicationProvider.getApplicationContext<Application>()
    ((context as AppComponentContainer).injector() as TestAppComponent).inject(this)

    `when`(api.getRandomImageForBreed(specificDogBreed.getBreedName())).thenReturn(Observable.just(breedData))
    `when`(api.getRandomImage()).thenReturn(Observable.just(randomBreedData))
  }

  @Test
  fun fetchesDogBreedOnShow() {
    screen = object : DogDetailsScreen(specificDogBreed) {
      override fun createView(context: Context): DogDetailsView {
        super.createView(context)
        return dogDetailsView
      }
    }

    screen.transitionToState(LifecycleState.Shown(activity))
    shadowOf(getMainLooper()).idle()
    verify(dogDetailsView).setDogPic("image-url")
  }

  @Test
  fun fetchesRandomDogOnShow() {
    screen = object : DogDetailsScreen(randomDogBreed) {
      override fun createView(context: Context): DogDetailsView {
        super.createView(context)
        return dogDetailsView
      }
    }

    screen.transitionToState(LifecycleState.Shown(activity))
    shadowOf(getMainLooper()).idle()
    verify(dogDetailsView).setDogPic("random-image-url")
  }
}
