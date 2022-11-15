package com.intuisoft.plaid.androidwrappers

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import com.intuisoft.plaid.R

class BottomBarView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    private var item_1_text: TextView? = null
    private var item_1_icon: ImageView? = null
    private var item_2_text: TextView? = null
    private var item_2_icon: ImageView? = null
    private var item_3_icon: ImageView? = null
    private var item_4_text: TextView? = null
    private var item_4_icon: ImageView? = null
    private var item_5_text: TextView? = null
    private var item_5_icon: ImageView? = null

    private var item_1_container: LinearLayout? = null
    private var item_2_container: LinearLayout? = null
    private var item_3_container: LinearLayout? = null
    private var item_4_container: LinearLayout? = null
    private var item_5_container: LinearLayout? = null

    private var item1UnselectedIcon: Int = 0
    private var item1SelectedIcon: Int = 0
    private var item2UnselectedIcon: Int = 0
    private var item2SelectedIcon: Int = 0
    private var item3UnselectedIcon: Int = 0
    private var item3SelectedIcon: Int = 0
    private var item4UnselectedIcon: Int = 0
    private var item4SelectedIcon: Int = 0
    private var item5UnselectedIcon: Int = 0
    private var item5SelectedIcon: Int = 0
    private var itemTextColorSelected: Int = 0
    private var itemTextColorUnSelected: Int = 0

    private var item1Text: String = ""
    private var item2Text: String = ""
    private var item4Text: String = ""
    private var item5Text: String = ""

    private var item1Destination: Int = 0
    private var item2Destination: Int = 0
    private var item3Destination: Int = 0
    private var item4Destination: Int = 0
    private var item5Destination: Int = 0

    init {
        inflate(context, R.layout.custom_view_bottom_bar, this)

        item_1_text = findViewById(R.id.bottom_bar_item_1_text)
        item_1_icon = findViewById(R.id.bottom_bar_item_1_icon)
        item_2_text = findViewById(R.id.bottom_bar_item_2_text)
        item_2_icon = findViewById(R.id.bottom_bar_item_2_icon)
        item_3_icon = findViewById(R.id.bottom_bar_item_3_icon)
        item_4_text = findViewById(R.id.bottom_bar_item_4_text)
        item_4_icon = findViewById(R.id.bottom_bar_item_4_icon)
        item_5_text = findViewById(R.id.bottom_bar_item_5_text)
        item_5_icon = findViewById(R.id.bottom_bar_item_5_icon)

        item_1_container = findViewById(R.id.item_1_container)
        item_2_container = findViewById(R.id.item_2_container)
        item_3_container = findViewById(R.id.item_3_container)
        item_4_container = findViewById(R.id.item_4_container)
        item_5_container = findViewById(R.id.item_5_container)

        setConfiguration(
            "", 0, 0, "",
            0, 0, 0, 0, "",
            0, 0, "", 0, 0,
            0, 0
        )
    }

    fun setConfiguration(
        item1Text: String,
        item1IconSelected: Int,
        item1IconUnselected: Int,
        item2Text: String,
        item2IconSelected: Int,
        item2IconUnselected: Int,
        item3IconSelected: Int,
        item3IconUnselected: Int,
        item4Text: String,
        item4IconSelected: Int,
        item4IconUnselected: Int,
        item5Text: String,
        item5IconSelected: Int,
        item5IconUnselected: Int,
        itemTextColorSelected: Int,
        itemTextColorUnselected: Int,
    ) {
        this.item1Text = item1Text
        this.item2Text = item2Text
        this.item4Text = item4Text
        this.item5Text = item5Text

        this.item1SelectedIcon = item1IconSelected
        this.item1UnselectedIcon = item1IconUnselected
        this.item2SelectedIcon = item2IconSelected
        this.item2UnselectedIcon = item2IconUnselected
        this.item3SelectedIcon = item3IconSelected
        this.item3UnselectedIcon = item3IconUnselected
        this.item4SelectedIcon = item4IconSelected
        this.item4UnselectedIcon = item4IconUnselected
        this.item5SelectedIcon = item5IconSelected
        this.item5UnselectedIcon = item5IconUnselected

        this.itemTextColorSelected = itemTextColorSelected
        this.itemTextColorUnSelected = itemTextColorUnselected

        setupView()
    }

    fun setupDestinations(
        dest1: Int,
        dest2: Int,
        dest3: Int,
        dest4: Int,
        dest5: Int,
    ) {
        item1Destination = dest1
        item2Destination = dest2
        item3Destination = dest3
        item4Destination = dest4
        item5Destination = dest5
    }

    fun onDestinationChanged(newDestination: Int) {
        when(newDestination) {
            item1Destination -> {
                selectItem1(true)
            }
            item2Destination -> {
                selectItem2(true)
            }
            item3Destination -> {
                selectItem3(true)
            }
            item4Destination -> {
                selectItem4(true)
            }
            item5Destination -> {
                selectItem5(true)
            }
        }
    }

    private fun setupView() {
        item_1_text!!.text = item1Text
        item_2_text!!.text = item2Text
        item_4_text!!.text = item4Text
        item_5_text!!.text = item5Text

        if(item1SelectedIcon != 0 && item1UnselectedIcon != 0) {
            item_1_container!!.isVisible = true
            selectItem1(false)
        }
        else item_1_container!!.isVisible = false

        if(item2SelectedIcon != 0 && item2UnselectedIcon != 0) {
            item_2_container!!.isVisible = true
            selectItem2(false)
        }
        else item_2_container!!.isVisible = false

        if(item3SelectedIcon != 0 && item3UnselectedIcon != 0) {
            item_3_container!!.isVisible = true
            selectItem3(false)
        }
        else item_3_container!!.isVisible = false

        if(item4SelectedIcon != 0 && item4UnselectedIcon != 0) {
            item_4_container!!.isVisible = true
            selectItem4(false)
        }
        else item_4_container!!.isVisible = false

        if(item5SelectedIcon != 0 && item5UnselectedIcon != 0) {
            item_5_container!!.isVisible = true
            selectItem5(false)
        }
        else item_5_container!!.isVisible = false
    }

    fun selectItem1(select: Boolean) {
        if(select) {
            selectItem2(false)
            selectItem3(false)
            selectItem4(false)
            selectItem5(false)

            item_1_icon!!.setImageResource(item1SelectedIcon)
            item_1_text!!.setTextColor(context.getColor(itemTextColorSelected))
        } else {
            item_1_icon!!.setImageResource(item1UnselectedIcon)
            item_1_text!!.setTextColor(context.getColor(itemTextColorUnSelected))
        }
    }

    fun selectItem2(select: Boolean) {
        if(select) {
            selectItem1(false)
            selectItem3(false)
            selectItem4(false)
            selectItem5(false)

            item_2_icon!!.setImageResource(item2SelectedIcon)
            item_2_text!!.setTextColor(context.getColor(itemTextColorSelected))
        } else {
            item_2_icon!!.setImageResource(item2UnselectedIcon)
            item_2_text!!.setTextColor(context.getColor(itemTextColorUnSelected))
        }
    }

    fun selectItem3(select: Boolean) {
        if(select) {
            selectItem1(false)
            selectItem2(false)
            selectItem4(false)
            selectItem5(false)

            item_3_icon!!.setImageResource(item3SelectedIcon)
        } else {
            item_3_icon!!.setImageResource(item3UnselectedIcon)
        }
    }

    fun selectItem4(select: Boolean) {
        if(select) {
            selectItem1(false)
            selectItem2(false)
            selectItem3(false)
            selectItem5(false)

            item_4_icon!!.setImageResource(item4SelectedIcon)
            item_4_text!!.setTextColor(context.getColor(itemTextColorSelected))
        } else {
            item_4_icon!!.setImageResource(item4UnselectedIcon)
            item_4_text!!.setTextColor(context.getColor(itemTextColorUnSelected))
        }
    }

    fun selectItem5(select: Boolean) {
        if(select) {
            selectItem1(false)
            selectItem2(false)
            selectItem3(false)
            selectItem4(false)

            item_5_icon!!.setImageResource(item5SelectedIcon)
            item_5_text!!.setTextColor(context.getColor(itemTextColorSelected))
        } else {
            item_5_icon!!.setImageResource(item5UnselectedIcon)
            item_5_text!!.setTextColor(context.getColor(itemTextColorUnSelected))
        }
    }

    fun onItemClicked(onClick: (Int) -> Unit) {
        item_1_container!!.setOnClickListener {
            selectItem1(true)
            onClick(item1Destination)
        }
        item_2_container!!.setOnClickListener {
            selectItem2(true)
            onClick(item2Destination)
        }
        item_3_icon!!.setOnClickListener {
            selectItem3(true)
            onClick(item3Destination)
        }
        item_4_container!!.setOnClickListener {
            selectItem4(true)
            onClick(item4Destination)
        }
        item_5_container!!.setOnClickListener {
            selectItem5(true)
            onClick(item5Destination)
        }
    }
}
