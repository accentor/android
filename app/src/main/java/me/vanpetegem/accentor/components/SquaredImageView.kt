package me.vanpetegem.accentor.components

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

class SquaredImageView : AppCompatImageView {
    val direction: String

    constructor(context: Context) : super(context) {
        scaleType = ScaleType.CENTER_CROP
        direction = "2"
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        scaleType = ScaleType.CENTER_CROP
        direction = "2"
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (direction == "2") {
            setMeasuredDimension(measuredWidth, measuredWidth)
        } else {
            setMeasuredDimension(measuredHeight, measuredHeight)
        }
    }
}
