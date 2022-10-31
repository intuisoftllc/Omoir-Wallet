package com.intuisoft.plaid.androidwrappers

import android.content.Context
import android.text.Html
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.Switch
import android.widget.TextView
import androidx.core.view.isVisible
import com.intuisoft.plaid.R

class SettingsItemView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    private var container: View? = null
    private var titleTv: TextView? = null
    private var subtitleTv: TextView? = null
    private var switch: Switch? = null
    private var check: ImageView? = null
    private var chevron: ImageView? = null
    private var radio: RadioButton? = null
    private var copy: ImageView? = null
    private var title: String = ""
    private var subTitle: String = ""
    private var showSwitch: Boolean = false
    private var showCheck: Boolean = false
    private var showChevron: Boolean = false
    private var showCopy: Boolean = false
    private var ellipsizeSubtitle: Boolean = false
    private var showRadio: Boolean = false
    private var subtitleMaxLines: Int = 0
    private var subtitleIcon: Int = 0
    private var titleColor: Int = 0
    private var subtitleColor: Int = 0
    private var settingsVariant: Int = 0

    companion object {
        // button text positions
        const val CARD_VARIANT = 0
        const val NORMAL_VARIANT = 1
        const val RADIO_VARIANT = 2
    }

    init {
        inflate(context, R.layout.custom_view_settings_item, this)
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
            ellipsizeSubtitle = getBoolean(R.styleable.SettingsItemView_elipsize_subtitle, false)
            showRadio = getBoolean(R.styleable.SettingsItemView_show_radio, false)
            subtitleMaxLines = getInteger(R.styleable.SettingsItemView_max_lines_subtitle, -1)
            titleColor = getColor(R.styleable.SettingsItemView_title_text_color, 0)
            subtitleColor = getColor(R.styleable.SettingsItemView_subtitle_text_color, 0)
            subtitleIcon = getResourceId(R.styleable.SettingsItemView_subtitle_icon, 0)
            settingsVariant = getInt(R.styleable.SettingsItemView_settings_variant, CARD_VARIANT)

            setVariant(settingsVariant)
        }
    }

    private fun setVariant(variant: Int) {
        container?.isVisible = false

        when(variant) {
            CARD_VARIANT -> {
                container = findViewById(R.id.variant_card_container)
                titleTv = findViewById(R.id.title)
                subtitleTv = findViewById(R.id.subtitle)
                switch = findViewById(R.id.settingsSwitch)
                check = findViewById(R.id.check)
                chevron = findViewById(R.id.chevron)
                copy = findViewById(R.id.copy)
                radio = null
            }
            NORMAL_VARIANT -> {
                container = findViewById(R.id.variant_normal_container)
                titleTv = findViewById(R.id.variant_normal_title)
                subtitleTv = findViewById(R.id.variant_normal_subtitle)
                switch = findViewById(R.id.variant_normal_settingsSwitch)
                check = findViewById(R.id.variant_normal_check)
                chevron = findViewById(R.id.variant_normal_chevron)
                copy = findViewById(R.id.variant_normal_copy)
                radio = null
            }
            RADIO_VARIANT -> {
                container = findViewById(R.id.variant_radio_container)
                titleTv = findViewById(R.id.variant_radio_title)
                subtitleTv = findViewById(R.id.variant_radio_subtitle)
                switch = null
                check = null
                chevron = null
                copy = null
                radio = findViewById(R.id.radio)
            }
            else -> { // default to cardVariant
                container = findViewById(R.id.variant_card_container)
                titleTv = findViewById(R.id.title)
                subtitleTv = findViewById(R.id.subtitle)
                switch = findViewById(R.id.settingsSwitch)
                check = findViewById(R.id.check)
                chevron = findViewById(R.id.chevron)
                copy = findViewById(R.id.copy)
                radio = null
            }
        }

        container?.isVisible = true
        setupView()
    }

    fun setTitleText(value: String) {
        titleTv?.text = Html.fromHtml(value)
        titleTv?.isVisible = value.isNotEmpty()
    }

    fun setSubTitleText(value: String) {
        subtitleTv?.text = Html.fromHtml(value)
        subtitleTv?.isVisible = value.isNotEmpty()
    }

    fun showSwitch(show: Boolean) {
        switch?.isVisible = show
    }

    fun showRadio(show: Boolean) {
        radio?.isVisible = show
    }

    fun showCopy(show: Boolean) {
        copy?.isVisible = show
    }

    fun checkRadio(checked: Boolean) {
        radio?.isChecked = checked
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

    fun isChecked() = check?.isVisible ?: false

    fun disableView(disable: Boolean) {
        switch?.isClickable = !disable
        copy?.isClickable = !disable
        switch?.isEnabled = !disable
        radio?.isEnabled = !disable
        isClickable = !disable

        if(disable) {
            chevron?.background = context.getDrawable(R.drawable.ic_chevron_right_disabled)
            check?.tint(context.getColor(R.color.light_grey))
            titleTv?.setTextColor(context.getColor(R.color.color_disabled))
            subtitleTv?.setTextColor(context.getColor(R.color.color_disabled))
        } else {
            check?.tint(context.getColor(R.color.black))
            chevron?.background = context.getDrawable(R.drawable.ic_chevron_right)
            titleTv?.setTextColor(context.getColor(if(titleColor != 0) titleColor else R.color.black))
            subtitleTv?.setTextColor(context.getColor(R.color.alt_black))
        }
    }

    fun setTitleColor(color: Int) {
        if(color != 0) {
            titleTv?.setTextColor(color)
        }
    }

    fun setSubTitleColor(color: Int) {
        if(color != 0) {
            subtitleTv?.setTextColor(color)
        }
    }

    fun showSubtitleIcon(drawable: Int) {
        if(drawable != 0) {
            subtitleTv?.leftDrawable(drawable, R.dimen.subtitle_icon_size)
        }
    }

    fun elipsizeSubtitle(elipsize: Boolean) {
        if(elipsize) {
            subtitleTv?.ellipsize = TextUtils.TruncateAt.END
        }
    }

    fun subtitleMaxLines(maxLines: Int) {
        if(maxLines != -1) {
            subtitleTv?.maxLines = maxLines
        }
    }

    private fun setupView() {
        subtitleMaxLines(subtitleMaxLines)
        elipsizeSubtitle(ellipsizeSubtitle)
        setTitleText(title)
        setSubTitleText(subTitle)
        showSwitch(showSwitch)
        showSubtitleIcon(subtitleIcon)
        setTitleColor(titleColor)
        setSubTitleColor(subtitleColor)
        showCheck(showCheck)
        showChevron(showChevron)
        showCopy(showCopy)
        showRadio(showRadio)
    }

    fun onRadioClicked(click: (SettingsItemView, Boolean) -> Unit) {
        this.setOnClickListener {
            radio?.isChecked = !(radio?.isChecked ?: false)
        }

        radio?.setOnCheckedChangeListener { compoundButton, checked ->
            click(this, checked)
        }
    }

    fun onClick(click: (SettingsItemView) -> Unit) {
        this.setOnClickListener {
            click(this)
        }
    }

    fun onLongClick(click: (SettingsItemView) -> Unit) {
        this.setOnLongClickListener {
            click(this)
            true
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
