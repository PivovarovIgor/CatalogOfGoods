package ru.brauer.catalogofgoods.ui.catalogofgoods.photopager

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_MOVE
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import ru.brauer.catalogofgoods.databinding.ItemPhotoBinding
import ru.brauer.catalogofgoods.extensions.loadFirstImage
import kotlin.math.abs
import kotlin.random.Random

class PhotosOfGoodsAdapter(private val lifecycleCoroutineScope: LifecycleCoroutineScope? = null) :
    RecyclerView.Adapter<PhotosOfGoodsAdapter.PhotoOfGoodsViewHolder>() {

    private var recyclerView: RecyclerView? = null
    private var currentPosition: Int = 0
    private var job: Job? = null
    private var eventTime: Long = 0
    private var diffX: Float = 0.0f
    private var diffY: Float = 0.0f

    var photos: List<String> = listOf()
        set(value) {
            field = value
            notifyDataSetChanged()
            startAutoScrolling()
        }

    @SuppressLint("ClickableViewAccessibility")
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = recyclerView
        recyclerView.setOnTouchListener { v: View?, event: MotionEvent? ->
            job?.run {
                if (event?.action == ACTION_MOVE) {
                    val time = event.eventTime - eventTime
                    if (time <= 150) {
                        diffX = abs(event.rawX - diffX)
                        diffY = abs(event.rawY - diffY)
                        if (diffX > diffY) {
                            cancel()
                            job = null
                        }

                    }
                    diffX = event.rawX
                    diffY = event.rawY
                    eventTime = event.eventTime

                }
            }
            false
        }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        if (this.recyclerView == recyclerView) {
            this.recyclerView = null
        }
    }

    private fun startAutoScrolling() {
        currentPosition = 1
        job?.cancel()
        job = lifecycleCoroutineScope?.launchWhenResumed {
            while (itemCount > 1) {
                val delayMilisec = 1000L * Random.nextLong(3, 10)
                delay(delayMilisec)
                notifyItemChanged(currentPosition)
                delay(1000)

                recyclerView?.setOnScrollChangeListener(null)
                recyclerView?.smoothScrollToPosition(currentPosition++)
                if (currentPosition == itemCount) {
                    currentPosition = 0
                }
                //recyclerView?.setOnScrollChangeListener(::onScrollChangeListener)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoOfGoodsViewHolder =
        ItemPhotoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            .let(::PhotoOfGoodsViewHolder)

    override fun onBindViewHolder(holder: PhotoOfGoodsViewHolder, position: Int) {
        holder.bind(photos[position])
    }

    override fun getItemCount(): Int = photos.count()

    inner class PhotoOfGoodsViewHolder(private val binding: ItemPhotoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(photo: String) {
            binding.goodsImage.loadFirstImage(photo)
        }
    }
}