package ru.antonc.weather.ui.weather

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProviders
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


        return binding.root
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_update -> {
                viewModel.updateData()
                true
            }
            else -> super.onOptionsItemSelected(item)

        }
    }

}