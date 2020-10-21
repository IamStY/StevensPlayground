package testing.steven.longpressdragdrop

import android.content.ClipData
import android.content.ClipDescription
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.DragEvent
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import kotlinx.android.synthetic.main.activity_drag_drop.*

class DragDropActivity : AppCompatActivity() {
    private val onDragListener = View.OnDragListener { view, dragEvent ->
        (view as? CardView)?.let {
            when (dragEvent.action) {
                // Once the drag event has started, we elevate all the views that are listening.
                // In our case, that's two of the areas.
                DragEvent.ACTION_DRAG_STARTED -> {
                    it.cardElevation = CARD_ELEVATION_DRAG_START_DP.toDp(resources.displayMetrics)
                    return@OnDragListener true
                }
                // Once the drag gesture enters a certain area, we want to elevate it even more.
                DragEvent.ACTION_DRAG_ENTERED -> {
                    it.cardElevation = CARD_ELEVATION_DRAG_ENTER_DP.toDp(resources.displayMetrics)
                    return@OnDragListener true
                }
                // No need to handle this for our use case.
                DragEvent.ACTION_DRAG_LOCATION -> {
                    return@OnDragListener true
                }
                // Once the drag gesture exits the area, we lower the elevation down to the previous one.
                DragEvent.ACTION_DRAG_EXITED -> {
                    it.cardElevation = CARD_ELEVATION_DRAG_START_DP.toDp(resources.displayMetrics)
                    return@OnDragListener true
                }
                // Once the color is dropped on the area, we want to paint it in that color.
                DragEvent.ACTION_DROP -> {
                    // Read color data from the clip data and apply it to the card view background.
                    val item: ClipData.Item = dragEvent.clipData.getItemAt(0)
                    val drawableFileName = item.text
                    val uri =
                        "@drawable/$drawableFileName" // where myresource (without the extension) is the file


                    val imageResource = resources.getIdentifier(uri, null, packageName)
                    iv_large.setImageResource(imageResource)
                    return@OnDragListener true
                }
                // Once the drag has ended, revert card views to the default elevation.
                DragEvent.ACTION_DRAG_ENDED -> {
                    it.cardElevation = CARD_ELEVATION_DEFAULT_DP.toDp(resources.displayMetrics)
                    return@OnDragListener true
                }
                else -> return@OnDragListener false
            }
        }
        false
    }


    private val onLongClickListener = View.OnLongClickListener { view: View ->
        (view as? ImageView)?.let {

            // First we create the `ClipData.Item` that we will need for the `ClipData`.
            // The `ClipData` carries the information of what is being dragged.
            // If you look at the main activity layout XML, you'll see that we've stored
            // color values for each of the FABs as their tags.
            val item = ClipData.Item(it.tag as? CharSequence)

            // We create a `ClipData` for the drag action and save the color as plain
            // text using `ClipDescription.MIMETYPE_TEXT_PLAIN`.
            val dragData = ClipData(
                it.tag as? CharSequence,
                arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN),
                item
            )
            // Instantiates the drag shadow builder, which is the class we will use
            // to draw a shadow of the dragged object. The implementation details
            // are in the rest of the article.
            val myShadow = View.DragShadowBuilder(it)

            // Start the drag. The new method is called `startDragAndDrop()` instead
            // of `startDrag()`, so we'll use it on the newer API.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                it.startDragAndDrop(dragData, myShadow, null, 0)
            } else {
                it.startDrag(dragData, myShadow, null, 0)
            }

            true
        }
        false
    }
    companion object {
        // Default card elevation.
        const val CARD_ELEVATION_DEFAULT_DP = 2F

        // Card elevation once the dragging has started.
        const val CARD_ELEVATION_DRAG_START_DP = 8F

        // Card elevation once the color is dragged over one of the areas.
        const val CARD_ELEVATION_DRAG_ENTER_DP = 16F
    }
        override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drag_drop)
            initView()
    }

    private fun initView() {

        fabRed.setOnLongClickListener(onLongClickListener)
        fabBlue.setOnLongClickListener(onLongClickListener)
        fabGreen.setOnLongClickListener(onLongClickListener)
        fabPurple.setOnLongClickListener(onLongClickListener)
        fabYellow.setOnLongClickListener(onLongClickListener)
        area1.setOnDragListener(onDragListener)

    }
}

private fun Float.toDp(displayMetrics: DisplayMetrics): Float {
    return this*displayMetrics.density
}
