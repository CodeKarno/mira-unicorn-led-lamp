package com.procrastinationcollaboration.miraunicornledlamp.ui.home

import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.annotation.LayoutRes
import androidx.databinding.BindingAdapter
import com.procrastinationcollaboration.miraunicornledlamp.R

@BindingAdapter("dropDownItems", "dropDownItemLayout", "dropDownItemsIncludeEmpty", requireAll = false)
fun AutoCompleteTextView.setItems(items: Array<String>?, @LayoutRes layout: Int?, includeEmpty: Boolean?) =
    setAdapter(
        ArrayAdapter(
            context,
            layout ?: R.layout.list_item,
            (if (includeEmpty == true) arrayOf("") else emptyArray()) + (items ?: emptyArray())
        )
    )