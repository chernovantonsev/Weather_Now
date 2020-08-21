package ru.antonc.weather.ui.base

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import ru.antonc.weather.di.Injectable
import javax.inject.Inject

open class BaseFragment : Fragment(), Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
}

