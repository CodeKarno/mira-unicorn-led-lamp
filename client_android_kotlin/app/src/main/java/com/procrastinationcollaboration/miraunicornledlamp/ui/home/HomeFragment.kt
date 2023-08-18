package com.procrastinationcollaboration.miraunicornledlamp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Switch
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.slider.Slider
import com.procrastinationcollaboration.miraunicornledlamp.R
import com.procrastinationcollaboration.miraunicornledlamp.databinding.FragmentHomeBinding
import com.procrastinationcollaboration.miraunicornledlamp.services.Consts
import com.skydoves.colorpickerview.listeners.ColorListener

class HomeFragment : Fragment() {

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_home,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.homeViewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel.getLedLampModes()
        viewModel.getStateFromLamp()

        binding.modeSelector.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, _, _ ->
                val mode = binding.modeSelector.text.toString()
                viewModel.setMode(mode)
                viewModel.updateLampStateOnServer(
                    mode,
                    null,
                    null
                )
            }

        binding.brightnessSlider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {}
            override fun onStopTrackingTouch(slider: Slider) {
                val bright = slider.value.toInt()
                viewModel.setBrightness(bright)
                viewModel.updateLampStateOnServer(
                    null,
                    null,
                    bright.toString()
                )
            }
        })

        binding.colorPicker.setColorListener(object :
            ColorListener {
            override fun onColorSelected(color: Int, fromUser: Boolean) {
                if (fromUser) {
                    viewModel.setColor(color)
                    viewModel.updateLampStateOnServer(
                        null,
                        color.toString(),
                        null
                    )
                }
            }
        })

        binding.lampSwitch.setOnClickListener { v ->
            val isChecked = (v as Switch).isChecked
            if (isChecked) {
                viewModel.getStateFromLamp()
            } else {
                viewModel.setMode(Consts.OFF_MODE)
                viewModel.updateLampStateOnServer(
                    Consts.OFF_MODE,
                    null,
                    null
                )
            }
        }
    }
}
