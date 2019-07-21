package me.vanpetegem.accentor.components

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.widget.ImageView
import me.vanpetegem.accentor.R

class SquaredImageView : ImageView {
    val direction: String

    constructor(context: Context) : super(context) {
        scaleType = ScaleType.CENTER_CROP
        direction = "2"
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        scaleType = ScaleType.CENTER_CROP

        val a: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.SquaredImageView)

        direction = a.getString(R.styleable.SquaredImageView_direction) ?: "2"

        a.recycle()
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