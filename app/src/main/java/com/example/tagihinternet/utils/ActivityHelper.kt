package com.example.tagihinternet.utils

import android.content.Context
import android.content.ContextWrapper
import androidx.fragment.app.FragmentActivity

object ActivityHelper {
    fun findFragmentActivity(context: Context): FragmentActivity? {
        var currentContext = context
        while (currentContext is ContextWrapper) {
            if (currentContext is FragmentActivity) {
                return currentContext
            }
            currentContext = currentContext.baseContext
        }
        return null
    }
}
