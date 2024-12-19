package io.github.alxiw.simplesearchview.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.ColorInt
import io.github.alxiw.simplesearchview.R

object ContextUtils {

    @JvmStatic
    fun scanForActivity(context: Context): Activity? {
        when (context) {
            is Activity -> return context
            is ContextWrapper -> return scanForActivity(context.baseContext)
        }
        return null
    }

    @JvmStatic
    @ColorInt
    fun getPrimaryColor(context: Context): Int {
        val value = TypedValue()
        context.theme.resolveAttribute(R.color.colorPrimary, value, true)
        return value.data
    }

    @JvmStatic
    @ColorInt
    fun getAccentColor(context: Context): Int {
        val value = TypedValue()
        context.theme.resolveAttribute(R.color.colorAccent, value, true)
        return value.data
    }

    @JvmStatic
    fun showKeyboard(view: View) {
        view.requestFocus()
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    @JvmStatic
    fun hideKeyboard(view: View) {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
