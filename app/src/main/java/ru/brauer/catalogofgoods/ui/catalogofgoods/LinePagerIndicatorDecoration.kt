package ru.brauer.catalogofgoods.ui.catalogofgoods

import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.max


class LinePagerIndicatorDecoration : RecyclerView.ItemDecoration() {

    companion object {
        private val DP = Resources.getSystem().displayMetrics.density
        private const val DEFAULT_INDICATOR_HEIGHT = 6f
        private const val DEFAULT_INDICATOR_ITEM_LENGTH = 12f
        private const val DEFAULT_INDICATOR_ITEM_PADDING = 5f
        private const val DEFAULT_INDICATOR_STROKE_WIDTH = 4f
        private const val COLOR_ACTIVE: Int = (0xFF00FCB5).toInt()
        private const val COLOR_INACTIVE: Int = 0x66999999
    }

    //Height of the space the indicator takes up at the bottom of the view.
    private val mIndicatorHeight: Int = (DEFAULT_INDICATOR_HEIGHT * DP).toInt()

    private val mIndicatorItemLength: Float = DEFAULT_INDICATOR_ITEM_LENGTH * DP
    private val mIndicatorItemPadding: Float = DEFAULT_INDICATOR_ITEM_PADDING * DP
    private val mIndicatorStrokeWidth = DEFAULT_INDICATOR_STROKE_WIDTH * DP
    private val mInterpolator = AccelerateDecelerateInterpolator()

    private val mPaint = Paint().apply {
        strokeCap = Paint.Cap.ROUND
        strokeWidth = mIndicatorStrokeWidth
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.bottom = mIndicatorHeight
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)

        val itemCount = parent.adapter?.itemCount ?: 0
        if (itemCount == 0) {
            return
        }

        // center horizontally, calculate width and subtract half from center
        val totalLength = mIndicatorItemLength * itemCount
        val paddingBetweenItem = max(0, itemCount - 1) * mIndicatorItemPadding
        val indicatorTotalWidth = totalLength + paddingBetweenItem
        val indicatorStartX = (parent.width - indicatorTotalWidth) / 2f

        // center vertically in the allotted space
        val indicatorPosY = parent.height - mIndicatorHeight / 2f

        drawInactiveIndicator(c, indicatorStartX, indicatorPosY, itemCount)

        // find active page (which should be highlighted)
        val layoutManager = parent.layoutManager as LinearLayoutManager
        val activePosition = layoutManager.findFirstVisibleItemPosition()
        if (activePosition == RecyclerView.NO_POSITION) {
            return
        }

        // find offset of active page (if the user is scrolling)
        val activeChild = layoutManager.findViewByPosition(activePosition) ?: return
        val left = activeChild.left
        val width = activeChild.width

        // on swipe the active item will be positioned from [-width, 0]
        // interpolate offset for smooth animation
        val progress = mInterpolator.getInterpolation(left * -1 / width.toFloat())

        drawHighlights(c, indicatorStartX, indicatorPosY, activePosition, progress, itemCount)
    }

    private fun drawHighlights(
        canvas: Canvas,
        indicatorStartX: Float,
        indicatorPosY: Float,
        activePosition: Int,
        progress: Float,
        itemCount: Int
    ) {
        mPaint.color = COLOR_ACTIVE

        val itemWidth = mIndicatorItemLength + mIndicatorItemPadding

        if (progress == 0f) {
            // no swipe, draw a normal indicator
            val highlightStart = indicatorStartX + itemWidth * activePosition
            canvas.drawLine(
                highlightStart,
                indicatorPosY,
                highlightStart + mIndicatorItemLength,
                indicatorPosY,
                mPaint
            )
        } else {
            val highlightStart = indicatorStartX + itemWidth * activePosition
            val partialLength = mIndicatorItemLength * progress

            // draw the cut off highlight
            canvas.drawLine(
                highlightStart + partialLength,
                indicatorPosY,
                highlightStart + mIndicatorItemLength,
                indicatorPosY,
                mPaint
            )

            // draw the highlight overlapping to the next item as well
            if (activePosition < itemCount - 1) {
                val highlightStartOfNextItem = highlightStart + itemWidth
                canvas.drawLine(
                    highlightStartOfNextItem,
                    indicatorPosY,
                    highlightStartOfNextItem + partialLength,
                    indicatorPosY,
                    mPaint
                )
            }
        }
    }

    private fun drawInactiveIndicator(
        canvas: Canvas,
        indicatorStartX: Float,
        indicatorPosY: Float,
        itemCount: Int
    ) {
        mPaint.color = COLOR_INACTIVE

        val itemWidth = mIndicatorItemLength + mIndicatorItemPadding

        var start = indicatorStartX
        for (i in 0 until itemCount) {
            canvas.drawLine(
                start,
                indicatorPosY,
                start + mIndicatorItemLength,
                indicatorPosY,
                mPaint
            )
            start += itemWidth
        }
    }
}