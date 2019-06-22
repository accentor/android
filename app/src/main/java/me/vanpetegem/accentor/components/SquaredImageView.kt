package me.vanpetegem.accentor.components

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView

class SquaredImageView : ImageView {
    constructor(context: Context) : super(context) {
        scaleType = ScaleType.CENTER_CROP
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        scaleType = ScaleType.CENTER_CROP
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(measuredWidth, measuredWidth)
    }
}