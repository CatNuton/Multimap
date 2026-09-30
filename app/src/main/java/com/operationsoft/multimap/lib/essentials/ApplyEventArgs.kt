package com.operationsoft.multimap.lib.essentials

import android.graphics.drawable.Drawable

class ApplyEventArgs(argTitle: String,
                     argDescription: String, argIcon: Drawable) {
    var title: String = argTitle
    var description: String = argDescription
    var icon: Drawable = argIcon
}