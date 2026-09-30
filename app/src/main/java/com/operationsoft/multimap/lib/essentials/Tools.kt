package com.operationsoft.multimap.lib.essentials

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.text.Editable
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.graphics.scale
import kotlin.math.roundToInt

class Tools {
    companion object{
        public fun Bitmap.scaleSavingRatio(maxDP: Int): Bitmap {
            val width = width
            val height = height

            if (width <= maxDP && height <= maxDP) {
                return this
            }

            val scaleFactor = if (width >= height) {
                maxDP.toFloat() / width
            } else {
                maxDP.toFloat() / height
            }

            val newWidth = (width * scaleFactor).roundToInt().coerceAtLeast(1)
            val newHeight = (height * scaleFactor).roundToInt().coerceAtLeast(1)

            return scale(newWidth, newHeight)
        }

        public fun String.addNumber(): String {
            var inputTitle = this
            val regex = Regex("""(\d+)(?!.*\d)""")

            if (!regex.containsMatchIn(inputTitle)) {
                inputTitle += "1"
            }
            else {
                val matches = regex.findAll(inputTitle)
                var result = inputTitle

                for (matchResult in matches) {
                    val digitString = matchResult.value
                    val editedNumber = (digitString.toInt() + 1).toString()

                    val replacement = if (editedNumber.length < digitString.length) {
                        editedNumber.padStart(digitString.length, '0')
                    } else {
                        editedNumber
                    }
                    result = result.replaceFirst(digitString, replacement)
                }

                inputTitle = result
            }
            return inputTitle
        }

        public fun String.toEditable() : Editable {
            return Editable.Factory.getInstance().newEditable(this)
        }

        public fun requestForPermissions(context: AppCompatActivity, permissions: Array<String>) {
            var allGranted = true

            for (permission in permissions){
                if (ActivityCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED){
                    allGranted = false
                    break
                }
            }
            if (!allGranted){
                ActivityCompat.requestPermissions(context, permissions, 100)
            }
        }
    }
}