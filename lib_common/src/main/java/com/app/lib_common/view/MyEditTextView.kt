package com.app.lib_common.view

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import androidx.appcompat.widget.AppCompatEditText


class MyEditTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatEditText(context, attrs, defStyleAttr) {
    private var keyEvent: OnBackKeyEvent? = null

    override fun dispatchKeyEventPreIme(event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_BACK) {
            if (keyEvent?.onKeyEvent() == true) {
                return true
            }
        }
        return super.dispatchKeyEventPreIme(event)
    }

    fun setOnBackKeyEventListener(event: OnBackKeyEvent?) {
        keyEvent = event
    }

    interface OnBackKeyEvent {
        fun onKeyEvent(): Boolean
    }
}