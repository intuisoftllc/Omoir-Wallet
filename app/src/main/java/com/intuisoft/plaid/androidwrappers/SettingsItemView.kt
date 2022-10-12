package com.intuisoft.plaid.androidwrappers

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import androidx.core.view.isVisible
import com.intuisoft.plaid.R

class SettingsItemView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    private var titleTv: TextView? = null
    private var subtitleTv: TextView? = null
    private var switch: Switch? = null
    private var check: ImageView? = null
    private var chevron: ImageView? = null
    private var copy: ImageView? = null
    private var title: String = ""
    private var subTitle: String = ""
    private var showSwitch: Boolean = false
    private var showCheck: Boolean = false
    private var showChevron: Boolean = false
    private var showCopy: Boolean = false
    private var subtitleIcon: Int = 0
    private var titleColor: Int = 0

    init {
        inflate(context, R.layout.settings_item, this)
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.SettingsItemView,
            0,
            0
        ).apply {
            title = getString(R.styleable.SettingsItemView_item_title) ?: ""
            subTitle = getString(R.styleable.SettingsItemView_item_subtitle) ?: ""
            showSwitch = getBoolean(R.styleable.SettingsItemView_show_switch, false)
            showCheck = getBoolean(R.styleable.SettingsItemView_show_check, false)
            showChevron = getBoolean(R.styleable.SettingsItemView_show_chevron, false)
            showCopy = getBoolean(R.styleable.SettingsItemView_show_copy, false)
            titleColor = getColor(R.styleable.SettingsItemView_title_text_color, 0)
            subtitleIcon = getResourceId(R.styleable.SettingsItemView_subtitle_icon, 0)

            titleTv = findViewById(R.id.settingsTitle)
            subtitleTv = findViewById(R.id.settingsSubtitle)
            switch = findViewById(R.id.settingsSwitch)
            check = findViewById(R.id.check)
            chevron = findViewById(R.id.chevron)
            copy = findViewById(R.id.copy)

            setupView()
        }
    }

    fun setTitleText(value: String) {
        titleTv?.text = value
        titleTv?.isVisible = value.isNotEmpty()
    }

    fun setSubTitleText(value: String) {
        subtitleTv?.text = value
        subtitleTv?.isVisible = value.isNotEmpty()
    }

    fun showSwitch(show: Boolean) {
        switch?.isVisible = show
    }

    fun showCopy(show: Boolean) {
        copy?.isVisible = show
    }

    fun setSwitchChecked(checked: Boolean) {
        switch?.isChecked = checked
    }

    fun showCheck(show: Boolean) {
        check?.isVisible = show
    }

    fun showChevron(show: Boolean) {
        chevron?.isVisible = show
    }

    fun setLayoutClickTriggersSwitch() {
        switch?.let { s ->
            this.onClick {
                s.performClick()
            }
        }
    }

    fun disableView(disable: Boolean) {
        switch?.isClickable = !disable
        copy?.isClickable = !disable
        switch?.isEnabled = !disable
        isClickable = !disable

        if(disable) {
            chevron?.background = context.getDrawable(R.drawable.ic_white_chevron_right)
            check?.tint(context.getColor(R.color.light_grey))
            titleTv?.setTextColor(context.getColor(R.color.color_disabled))
            subtitleTv?.setTextColor(context.getColor(R.color.color_disabled))
        } else {
            check?.tint(context.getColor(R.color.black))
            chevron?.background = context.getDrawable(R.drawable.ic_black_chevron_right)
            titleTv?.setTextColor(context.getColor(if(titleColor != 0) titleColor else R.color.black))
            subtitleTv?.setTextColor(context.getColor(R.color.alt_black))
        }
    }

    fun setTitleColor(color: Int) {
        if(color != 0) {
            titleTv?.setTextColor(color)
        }
    }

    fun showSubtitleIcon(drawable: Int) {
        if(drawable != 0) {
            subtitleTv?.leftDrawable(drawable, R.dimen.settings_item_subtitle_icon_size)
        }
    }

    private fun setupView() {
        setTitleText(title)
        setSubTitleText(subTitle)
        showSwitch(showSwitch)
        showSubtitleIcon(subtitleIcon)
        setTitleColor(titleColor)
        showCheck(showCheck)
        showChevron(showChevron)
        showCopy(showCopy)
    }

    fun onClick(click: (SettingsItemView) -> Unit) {
        this.setOnClickListener {
            click(this)
        }
    }

    fun onSwitchClicked(click: (Boolean) -> Unit) {
        switch?.setOnCheckedChangeListener { buttonView, isChecked ->
            click(isChecked)
        }
    }

    fun onCopyClicked(click: () -> Unit) {
        copy?.setOnClickListener {
            click()
        }
    }
}
