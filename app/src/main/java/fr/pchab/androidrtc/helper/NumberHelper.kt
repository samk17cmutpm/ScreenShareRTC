package fr.pchab.androidrtc.helper

import java.text.DecimalFormat
import kotlin.math.pow
import kotlin.math.roundToInt

class NumberHelper {
    companion object {
        private const val FORMAT = "#,###"

        fun format(input: Double?) : String? {
            if (input == null) return "0"
            val formatter = DecimalFormat(FORMAT)
            return formatter.format(input)
        }

        fun round(value: Double, precision: Int): Double {
            val scale = 10.0.pow(precision.toDouble()).toInt()
            return (value * scale).roundToInt().toDouble() / scale
        }
    }
}