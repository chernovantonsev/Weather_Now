package ru.antonc.weather.ui.weather

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.observe
import ru.antonc.weather.R
import ru.antonc.weather.databinding.FragmentWeatherBinding
import ru.antonc.weather.di.Injectable
import ru.antonc.weather.ui.base.BaseFragment

class WeatherFragment : BaseFragment(), Injectable, Toolbar.OnMenuItemClickListener {

    private lateinit var viewModel: WeatherViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel =
            ViewModelProviders.of(this, viewModelFactory).get(WeatherViewModel::class.java)

        val binding = FragmentWeatherBinding.inflate(inflater, container, false)
            .apply {
                viewModel = this@WeatherFragment.viewModel
                lifecycleOwner = viewLifecycleOwner
            }

        binding.toolbar.apply {
            inflateMenu(R.menu.menu_update)
            setOnMenuItemClickListener(this@WeatherFragment)
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { messageEvent ->
            messageEvent.getContentIfNotHandled()?.let { messageResource ->
                Toast.makeText(requireContext(), getText(messageResource), Toast.LENGTH_SHORT)
                    .show()
            }
        }

        viewModel.isNeedUpdateLocationSetting.observe(viewLifecycleOwner) { isLocationUpdateOn ->
            binding.toolbar.menu.findItem(R.id.action_is_need_update_location)?.let { menuItem ->
                menuItem.icon = ContextCompat.getDrawable(
                    requireContext(),
                    if (isLocationUpdateOn) R.drawable.ic_location_on else R.drawable.ic_location_off
                )
            }
        }

        return binding.root
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_update -> {
                viewModel.updateData()
                true
            }
            R.id.action_is_need_update_location -> {
                viewModel.inverseLocationPreferenceValue()
                true
            }
            else -> super.onOptionsItemSelected(item)

        }
    }

}