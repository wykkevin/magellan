package com.wealthfront.magellan.sample.migration.tide

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.wealthfront.magellan.core.Step
import com.wealthfront.magellan.sample.migration.AppComponentContainer
import com.wealthfront.magellan.sample.migration.R
import com.wealthfront.magellan.sample.migration.databinding.DashboardBinding
import com.wealthfront.magellan.sample.migration.toolbar.ToolbarHelper
import java.util.Locale
import javax.inject.Inject

class DogListStep(private val goToDogDetails: (dogBreed: DogBreed) -> Unit) : Step<DashboardBinding>(DashboardBinding::inflate) {

  @Inject lateinit var toolbarHelper: ToolbarHelper

  override fun onCreate(context: Context) {
    (context.applicationContext as AppComponentContainer).injector().inject(this)
  }

  override fun onShow(context: Context, binding: DashboardBinding) {
    toolbarHelper.setTitle(context.getText(R.string.app_name))
    binding.dogItems.adapter = DogListAdapter(context)
  }

  fun onDogSelected(dogBreed: DogBreed) {
    goToDogDetails(dogBreed)
  }

  enum class DogBreed {
    RANDOM, // Make random to be the first item in the list
    AKITA,
    BEAGLE,
    CHOW,
    MIX,
    LABRADOR,
    SHIBA,
    HUSKY,
    SHIHTZU;

    fun getName(): String {
      return name.replace("_", " ").toLowerCase(Locale.getDefault()).capitalize()
    }

    fun getBreedName(): String {
      return name.replace("_", " ").toLowerCase(Locale.getDefault())
    }
  }

  private inner class DogListAdapter(context: Context) : ArrayAdapter<DogBreed>(context, R.layout.dog_item, R.id.dogName, DogBreed.values()) {

    override fun getView(
      position: Int,
      convertView: View?,
      parent: ViewGroup
    ): View {
      var view = convertView
      if (convertView == null) {
        view = View.inflate(context, R.layout.dog_item, null)
      }
      val dogDetail = getItem(position)!!
      val dogDetailTextView = view!!.findViewById<TextView>(R.id.dogName)
      dogDetailTextView.text = dogDetail.getName()
      view.setOnClickListener {
        onDogSelected(dogDetail)
      }
      return view
    }
  }
}
