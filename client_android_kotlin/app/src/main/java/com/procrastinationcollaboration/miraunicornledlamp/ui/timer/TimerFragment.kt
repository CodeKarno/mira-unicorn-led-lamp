package com.procrastinationcollaboration.miraunicornledlamp.ui.timer

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.datastore.preferences.preferencesDataStore
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.procrastinationcollaboration.miraunicornledlamp.R
import com.procrastinationcollaboration.miraunicornledlamp.databinding.FragmentTimerBinding
import com.procrastinationcollaboration.miraunicornledlamp.repositories.DataStoreRepository
import com.procrastinationcollaboration.miraunicornledlamp.services.Consts


val Context.dataStore by preferencesDataStore(
    name = Consts.USER_PREFERENCES_NAME
)

class TimerFragment : Fragment() {
    private lateinit var binding: FragmentTimerBinding
    private lateinit var viewModel: TimerViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_timer,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(
            this,
            TimerViewModelFactory(DataStoreRepository(requireContext().dataStore))
        )[TimerViewModel::class.java]

        binding.timerViewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner


        configureTimePicker()

        binding.setupTimerBtn.setOnClickListener { setupTimer() }

        binding.cancelTimerBtn.setOnClickListener {
            viewModel.cancelLampOff()

            Toast.makeText(
                context,
                getString(R.string.title_timer_canceled_toast),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun setupTimer() {
        val h = binding.hourPicker.value
        val m = binding.minutePicker.value

        viewModel.turnLampOffAfterDelay(h * 60 + m)
    }

    private fun configureTimePicker() {
        binding.hourPicker.minValue = 0
        binding.hourPicker.maxValue = 24
        binding.minutePicker.minValue = 0
        binding.minutePicker.maxValue = 59
    }
}