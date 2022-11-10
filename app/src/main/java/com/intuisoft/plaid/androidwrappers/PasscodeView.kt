package com.intuisoft.plaid.androidwrappers

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.IntDef
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.core.view.isVisible
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.PasscodeView.PasscodeViewType.Companion.TYPE_CHECK_PASSCODE
import com.intuisoft.plaid.androidwrappers.PasscodeView.PasscodeViewType.Companion.TYPE_SET_PASSCODE
import com.intuisoft.plaid.common.local.UserPreferences
import com.intuisoft.plaid.util.entensions.getColorFromAttr
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy


/**
 * PasscodeView
 * Created by hanks on 2017/4/11.
 */
class PasscodeView @JvmOverloads constructor(
    @NonNull context: Context,
    @Nullable attrs: AttributeSet? = null
) : FrameLayout(context, attrs), View.OnClickListener, FingerprintScanResponse, KoinComponent {
    private val prefs: UserPreferences by inject()
    private var secondInput = false
    private var pinValidationSuccess = false
    private var pinAttemptTracking = false
    var localPasscode = ""
        private set
    var listener: PasscodeViewListener? = null
        private set
    private var layout_psd: ViewGroup? = null
    private var tv_input_tip: TextView? = null
    private var number0: TextView? = null
    private var number1: TextView? = null
    private var number2: TextView? = null
    private var number3: TextView? = null
    private var number4: TextView? = null
    private var number5: TextView? = null
    private var number6: TextView? = null
    private var number7: TextView? = null
    private var number8: TextView? = null
    private var number9: TextView? = null
    private var numberB: ImageView? = null
    private var numberOK: ImageView? = null
    private var iv_lock: ImageView? = null
    private var iv_ok: ImageView? = null
    private var iv_fingerprint: ImageView? = null
    private var cursor: View? = null
    var firstInputTip: String? = "Enter a passcode of 4 digits"
        private set
    var secondInputTip: String? = "Re-enter new passcode"
        private set
    var wrongLengthTip: String? = "Enter a passcode of 4 digits"
        private set
    var wrongInputTip: String? = "Passcode do not match"
        private set
    var correctInputTip: String? = "Passcode is correct"
        private set
    var maxAttempts = 0
        private set
    var minPasscodeLength = 4
        private set
    var passcodeLength = 4
        private set
    var correctStatusColor = -0x9e3aa0 //0xFFFF0000
        private set
    var wrongStatusColor = -0xdbfab
        private set
    var normalStatusColor = -0x0
        private set
    var numberTextColor = -0x8b8b8c
        private set
    var tipTextColor = -0x8b8b8c
        private set

    @get:PasscodeViewType
    var passcodeType: Int = TYPE_SET_PASSCODE
        private set

    fun init() {
        if (passcodeLength < minPasscodeLength) {
            throw RuntimeException("must set a passcode of at least $minPasscodeLength numbers")
        }

        if (passcodeType == TYPE_SET_PASSCODE && TextUtils.isEmpty(localPasscode)) {
            prefs.incorrectPinAttempts = 0
        }

        maxAttempts = prefs.maxPinAttempts
        layout_psd = findViewById<View>(R.id.layout_psd) as ViewGroup
        tv_input_tip = findViewById<View>(R.id.tv_input_tip) as TextView
        cursor = findViewById(R.id.cursor)
        iv_lock = findViewById<View>(R.id.iv_lock) as ImageView
        iv_ok = findViewById<View>(R.id.iv_ok) as ImageView
        iv_fingerprint = findViewById<View>(R.id.fingerprintIcon) as ImageView
        tv_input_tip!!.text = firstInputTip
        number0 = findViewById<View>(R.id.number0) as TextView
        number1 = findViewById<View>(R.id.number1) as TextView
        number2 = findViewById<View>(R.id.number2) as TextView
        number3 = findViewById<View>(R.id.number3) as TextView
        number4 = findViewById<View>(R.id.number4) as TextView
        number5 = findViewById<View>(R.id.number5) as TextView
        number6 = findViewById<View>(R.id.number6) as TextView
        number7 = findViewById<View>(R.id.number7) as TextView
        number8 = findViewById<View>(R.id.number8) as TextView
        number9 = findViewById<View>(R.id.number9) as TextView
        numberOK = findViewById<View>(R.id.numberOK) as ImageView
        numberB = findViewById<View>(R.id.numberB) as ImageView
        number0!!.setOnClickListener(this)
        number1!!.setOnClickListener(this)
        number2!!.setOnClickListener(this)
        number3!!.setOnClickListener(this)
        number4!!.setOnClickListener(this)
        number5!!.setOnClickListener(this)
        number6!!.setOnClickListener(this)
        number7!!.setOnClickListener(this)
        number8!!.setOnClickListener(this)
        number9!!.setOnClickListener(this)
        numberB!!.setOnClickListener { deleteChar() }
        numberB!!.setOnLongClickListener {
            deleteAllChars()
            true
        }
        numberOK!!.setOnClickListener { next() }
        iv_fingerprint!!.isVisible = prefs.fingerprintSecurity && passcodeType == TYPE_CHECK_PASSCODE
        iv_fingerprint!!.setOnClickListener { scanFingerprint() }
        tintImageView(numberB, numberTextColor)
        tintImageView(numberOK, numberTextColor)
        number0!!.tag = 0
        number1!!.tag = 1
        number2!!.tag = 2
        number3!!.tag = 3
        number4!!.tag = 4
        number5!!.tag = 5
        number6!!.tag = 6
        number7!!.tag = 7
        number8!!.tag = 8
        number9!!.tag = 9
        number0!!.setTextColor(numberTextColor)
        number1!!.setTextColor(numberTextColor)
        number2!!.setTextColor(numberTextColor)
        number3!!.setTextColor(numberTextColor)
        number4!!.setTextColor(numberTextColor)
        number5!!.setTextColor(numberTextColor)
        number6!!.setTextColor(numberTextColor)
        number7!!.setTextColor(numberTextColor)
        number8!!.setTextColor(numberTextColor)
        number9!!.setTextColor(numberTextColor)
        tv_input_tip!!.setTextColor(tipTextColor)
    }

    fun disableEverything(disable: Boolean) {
        number0!!.isClickable = !disable
        number1!!.isClickable = !disable
        number2!!.isClickable = !disable
        number3!!.isClickable = !disable
        number4!!.isClickable = !disable
        number5!!.isClickable = !disable
        number6!!.isClickable = !disable
        number7!!.isClickable = !disable
        number8!!.isClickable = !disable
        number9!!.isClickable = !disable
        numberOK!!.isClickable = !disable
        numberB!!.isClickable = !disable
        numberB!!.isLongClickable = !disable
    }

    override fun onClick(view: View) {
        val number = view.tag as Int
        addChar(number)
    }

    /**
     * set  localPasscode
     *
     * @param localPasscode the code will to check
     */
    fun setLocalPasscode(localPasscode: String): PasscodeView {
        for (i in 0 until localPasscode.length) {
            val c = localPasscode[i]
            if (c < '0' || c > '9') {
                throw RuntimeException("must be number digit")
            }
        }
        this.localPasscode = localPasscode
        passcodeType = TYPE_CHECK_PASSCODE
        return this
    }

    fun disableFingerprint() {
        iv_fingerprint?.isVisible = false
    }

    fun disablePinAttemptTracking() {
        pinAttemptTracking = false
    }

    fun setListener(listener: PasscodeViewListener?): PasscodeView {
        this.listener = listener
        return this
    }

    fun setFirstInputTip(firstInputTip: String?): PasscodeView {
        this.firstInputTip = firstInputTip
        return this
    }

    fun setSecondInputTip(secondInputTip: String?): PasscodeView {
        this.secondInputTip = secondInputTip
        return this
    }

    fun setWrongLengthTip(wrongLengthTip: String?): PasscodeView {
        this.wrongLengthTip = wrongLengthTip
        return this
    }

    fun setWrongInputTip(wrongInputTip: String?): PasscodeView {
        this.wrongInputTip = wrongInputTip
        return this
    }

    fun setCorrectInputTip(correctInputTip: String?): PasscodeView {
        this.correctInputTip = correctInputTip
        return this
    }

    fun setPasscodeLength(passcodeLength: Int): PasscodeView {
        this.passcodeLength = passcodeLength
        return this
    }

    fun setCorrectStatusColor(correctStatusColor: Int): PasscodeView {
        this.correctStatusColor = correctStatusColor
        return this
    }

    fun setWrongStatusColor(wrongStatusColor: Int): PasscodeView {
        this.wrongStatusColor = wrongStatusColor
        return this
    }

    fun setNormalStatusColor(normalStatusColor: Int): PasscodeView {
        this.normalStatusColor = normalStatusColor
        return this
    }

    fun setNumberTextColor(numberTextColor: Int): PasscodeView {
        this.numberTextColor = numberTextColor
        return this
    }

    fun setPasscodeType(@PasscodeViewType passcodeType: Int): PasscodeView {
        this.passcodeType = passcodeType
        return this
    }

    fun scanFingerprint() {
        this.iv_fingerprint!!.isClickable = false
        this.listener?.onScanFingerprint(this)
    }

    override fun onScanSuccess() {
        this.iv_fingerprint!!.isClickable = true
        this.listener?.onSuccess(null)
    }

    override fun onScanFail() {
        this.iv_fingerprint!!.isClickable = true
    }

    /**
     * <pre>
     * passcodeView.setListener(new PasscodeView.PasscodeViewListener() {
     * public void onFail() {
     * }
     *
     * public void onSuccess(String number) {
     * String encrypted = SecurePreferences.hashPrefKey(raw);
     * SharedPreferences.Editor editor = keys.edit();
     * editor.putString("passcode", encrypted);
     * editor.commit();
     * finish();
     * }
     * });
     * Second, compare using the overridden equals() method:
     *
     * class PView extends PasscodeView {
     * public PView(Context context) {
     * super(context);
     * }
     * protected boolean equals(String psd) {
     * String after = SecurePreferences.hashPrefKey(raw);
     * return after.equals(encrypted_passcode);
     * }
     * }
     * PView passcodeView = new PView(PasscodeActivity.this);
     *
    </pre> *
     * @param val the input number string
     * @return true if val is right passcode
     */
    protected fun equals(`val`: String): Boolean {
        return localPasscode == `val`
    }

    private operator fun next() {
        if (passcodeType == TYPE_CHECK_PASSCODE && TextUtils.isEmpty(localPasscode)) {
            throw RuntimeException("must set localPasscode when type is TYPE_CHECK_PASSCODE")
        }
        val psd = passcodeFromView
        if (psd.length < minPasscodeLength || psd.length > passcodeLength) {
            tv_input_tip!!.text = wrongLengthTip
            runTipTextAnimation()
            return
        }
        if (passcodeType == TYPE_SET_PASSCODE && !secondInput) {
            // second input
            tv_input_tip!!.text = secondInputTip
            localPasscode = psd
            clearChar()
            secondInput = true
            return
        }
        if (equals(psd)) {
            // match
            pinValidationSuccess = true
            runOkAnimation()
        } else {
            runWrongAnimation()
        }
    }

    private fun addChar(number: Int) {
        if (layout_psd!!.childCount >= passcodeLength) {
            return
        }
        val psdView = CircleView(context)
        val size = dpToPx(8f)
        val params = LinearLayout.LayoutParams(size, size)
        params.setMargins(size, 0, size, 0)
        psdView.setLayoutParams(params)
        psdView.setColor(normalStatusColor)
        psdView.setTag(number)
        layout_psd!!.addView(psdView)
    }

    private fun dpToPx(valueInDp: Float): Int {
        val metrics = resources.displayMetrics
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInDp, metrics).toInt()
    }

    private fun tintImageView(imageView: ImageView?, color: Int) {
        imageView!!.tint(color)
    }

    private fun clearChar() {
        layout_psd!!.removeAllViews()
    }

    fun resetView() {
        clearChar()

        iv_ok!!.animate().alpha(0f).scaleX(0f).scaleY(0f).setDuration(500).start()
        iv_lock!!.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(500).start()
        init()
    }

    private fun deleteChar() {
        val childCount = layout_psd!!.childCount
        if (childCount <= 0) {
            return
        }
        layout_psd!!.removeViewAt(childCount - 1)
    }

    private fun deleteAllChars() {
        val childCount = layout_psd!!.childCount
        if (childCount <= 0) {
            return
        }
        layout_psd!!.removeAllViews()
    }

    fun runTipTextAnimation() {
        shakeAnimator(tv_input_tip).start()
    }

    fun runWrongAnimation() {
        cursor!!.translationX = 0f
        cursor!!.visibility = VISIBLE
        cursor!!.animate()
            .translationX(layout_psd!!.width.toFloat())
            .setDuration(600)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    cursor!!.visibility = INVISIBLE
                    disableEverything(true)
                    var limitReached = false

                    if(pinAttemptTracking) {
                        var attempts = prefs.incorrectPinAttempts + 1
                        prefs.incorrectPinAttempts = attempts

                        if(attempts >= maxAttempts) {
                            limitReached = true
                            tv_input_tip!!.text = "Pin entry limit reached"
                        } else {
                            var errorMessage = "You have ${maxAttempts - attempts} attempt"

                            if((maxAttempts - attempts) > 1) {
                                errorMessage += "s"
                            }

                            errorMessage += " left"
                            tv_input_tip!!.text = errorMessage
                        }
                    } else {
                        tv_input_tip!!.text = wrongInputTip
                    }

                    setPSDViewBackgroundResource(wrongStatusColor)
                    val animator = shakeAnimator(layout_psd)
                    animator.addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            super.onAnimationEnd(animation)
                            setPSDViewBackgroundResource(normalStatusColor)
                            if (listener != null) {
                                if(limitReached) {
                                    listener!!.onMaxAttempts()
                                } else {
                                    disableEverything(false)
                                    listener!!.onFail(passcodeFromView)
                                }
                            }
                        }
                    })
                    animator.start()
                }
            })
            .start()
    }

    private fun shakeAnimator(view: View?): Animator {
        return ObjectAnimator
            .ofFloat(view, "translationX", 0f, 25f, -25f, 25f, -25f, 15f, -15f, 6f, -6f, 0f)
            .setDuration(500)
    }

    private fun setPSDViewBackgroundResource(color: Int) {
        val childCount = layout_psd!!.childCount
        for (i in 0 until childCount) {
            (layout_psd!!.getChildAt(i) as CircleView).setColor(color)
        }
    }

    fun runOkAnimation() {
        cursor!!.translationX = 0f
        cursor!!.visibility = VISIBLE
        cursor!!.animate()
            .setDuration(600)
            .translationX(layout_psd!!.width.toFloat())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    cursor!!.visibility = INVISIBLE
                    setPSDViewBackgroundResource(correctStatusColor)
                    tv_input_tip!!.text = correctInputTip
                    disableEverything(true)
                    iv_lock!!.animate().alpha(0f).scaleX(0f).scaleY(0f).setDuration(500).start()
                    iv_ok!!.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(500)
                        .setListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                super.onAnimationEnd(animation)
                                prefs.incorrectPinAttempts = 0
                                if (listener != null && pinValidationSuccess) {
                                    pinValidationSuccess = false
                                    listener!!.onSuccess(passcodeFromView)
                                }
                            }
                        }).start()
                }
            })
            .start()
    }

    private val passcodeFromView: String
        private get() {
            val sb = StringBuilder()
            val childCount = layout_psd!!.childCount
            for (i in 0 until childCount) {
                val child = layout_psd!!.getChildAt(i)
                val num = child.tag as Int
                sb.append(num)
            }
            return sb.toString()
        }

    /**
     * The type for this passcodeView
     */
    @IntDef(TYPE_SET_PASSCODE, TYPE_CHECK_PASSCODE)
    @Retention(RetentionPolicy.SOURCE)
    annotation class PasscodeViewType {
        companion object {
            /**
             * set passcode, with twice input
             */
            const val TYPE_SET_PASSCODE = 0

            /**
             * check passcode, must pass the result as parameter [PasscodeView.setLocalPasscode]
             */
            const val TYPE_CHECK_PASSCODE = 1
        }
    }

    interface PasscodeViewListener {
        fun onFail(wrongNumber: String?)
        fun onSuccess(number: String?)
        fun onMaxAttempts()
        fun onScanFingerprint(listener: FingerprintScanResponse)
    }

    init {
        inflate(getContext(), R.layout.custom_view_passcode_view, this)
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.PasscodeView)
        try {
            passcodeType =
                typedArray.getInt(R.styleable.PasscodeView_passcodeViewType, passcodeType)
            pinAttemptTracking =
                typedArray.getBoolean(R.styleable.PasscodeView_trackIncorrectAttempts, false)
            passcodeLength =
                typedArray.getInt(R.styleable.PasscodeView_passcodeLength, passcodeLength)
            normalStatusColor =
                typedArray.getColor(R.styleable.PasscodeView_normalStateColor, context.getColorFromAttr(android.R.attr.colorPrimary))
            wrongStatusColor =
                typedArray.getColor(R.styleable.PasscodeView_wrongStateColor, wrongStatusColor)
            correctStatusColor =
                typedArray.getColor(R.styleable.PasscodeView_correctStateColor, correctStatusColor)
            numberTextColor =
                typedArray.getColor(R.styleable.PasscodeView_numberTextColor, numberTextColor)
            tipTextColor =
                typedArray.getColor(R.styleable.PasscodeView_tipTextColor, numberTextColor)
            firstInputTip = typedArray.getString(R.styleable.PasscodeView_firstInputTip)
            secondInputTip = typedArray.getString(R.styleable.PasscodeView_secondInputTip)
            wrongLengthTip = typedArray.getString(R.styleable.PasscodeView_wrongLengthTip)
            wrongInputTip = typedArray.getString(R.styleable.PasscodeView_wrongInputTip)
            correctInputTip = typedArray.getString(R.styleable.PasscodeView_correctInputTip)
        } finally {
            typedArray.recycle()
        }
        firstInputTip = if (firstInputTip == null) "Enter a passcode of 4 digits" else firstInputTip
        secondInputTip = if (secondInputTip == null) "Re-enter new passcode" else secondInputTip
        wrongLengthTip = if (wrongLengthTip == null) firstInputTip else wrongLengthTip
        wrongInputTip = if (wrongInputTip == null) "Passcode do not match" else wrongInputTip
        correctInputTip = if (correctInputTip == null) "Passcode is correct" else correctInputTip
        init()
    }
}
