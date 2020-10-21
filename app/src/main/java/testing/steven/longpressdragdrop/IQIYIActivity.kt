package testing.steven.longpressdragdrop

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.provider.Settings.SettingNotFoundException
import android.provider.Settings.System.SCREEN_BRIGHTNESS
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_i_q_i_y_i.*
import kotlin.math.absoluteValue


class IQIYIActivity : AppCompatActivity() {
    companion object {
        const val MINI_VIEW_WIDTH = 500
        const val MINI_VIEW_HEIGHT = 250
        const val DRAG_HORIZONTAL_DISTANCE_RESTRICTION = 150
        const val DRAG_VERTICAL_DISTANCE_RESTRICTION = 30
        const val TOTAL_VIDEO_LENGTH = 60*2 + 35
        const val SECENDS_FOR_FULL_PERCENTAGE = 100
    }
    var currentVideoPositionSec = 0

   lateinit var cResolver : ContentResolver
    lateinit var audioManager: AudioManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_i_q_i_y_i)

        val titleBarHeight = getStatusBarHeight()
        Log.e("titleBarH", titleBarHeight.toString())

        audioManager =  getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(this)) {
                var intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:$packageName"));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }
        cResolver = contentResolver;
        val miniView: View = layoutInflater.inflate(R.layout.mini_drag_view, null)
        val miniPercentage = miniView.findViewById<TextView>(R.id.txt_mini_percentage)

        miniView.layoutParams = RelativeLayout.LayoutParams(MINI_VIEW_WIDTH, MINI_VIEW_HEIGHT)
        miniView.visibility = View.GONE



        moveVideoByDragPercentage(currentVideoPositionSec, txt_main_screen_text)
        var container = screen_touch_area as RelativeLayout
        container.addView(miniView)


        screen_touch_area.setOnTouchListener(object : OnTouchListener {
            var prevX = 0
            var prevY = 0
            var flagBeginHorizontal = false
            var flagBeginVertical = false
            var targetArea = -1
            var currentSeekBarProgress = 0

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                val par = miniView.layoutParams as RelativeLayout.LayoutParams
                when (event.action) {
                    MotionEvent.ACTION_MOVE -> {


                        if (targetArea == 0 || targetArea == 2) {
                            var abs = (prevY - event.rawY).absoluteValue
                            if (abs > DRAG_VERTICAL_DISTANCE_RESTRICTION) {
                                flagBeginVertical = true
                            }
                            if (flagBeginVertical) {
                                if (lin_seek.visibility == View.GONE) {
                                    lin_seek.visibility = View.VISIBLE
                                }

                                if (targetArea == 0) {
                                    iv_seek.setImageResource(R.drawable.bright)
                                    //bright
                                    seek_bar.progress =
                                        currentSeekBarProgress + ((((prevY - event.rawY.toInt()) / 2).toFloat() / v.height) * 250).toInt()
                                } else if (targetArea == 2) {
                                    iv_seek.setImageResource(R.drawable.volumn)
                                    seek_bar.progress =
                                        currentSeekBarProgress + ((((prevY - event.rawY.toInt()) / 2).toFloat() / v.height) * 100).toInt()

                                    // volumn
                                }

                            }
                        } else if (targetArea == 1) {
                            var abs = (prevX - event.rawX).absoluteValue

                            if (abs > DRAG_HORIZONTAL_DISTANCE_RESTRICTION) {
                                flagBeginHorizontal = true
                            }
                            if (flagBeginHorizontal) {
                                if (miniView.visibility == View.GONE) {
                                    miniView.visibility = View.VISIBLE
                                }
                                par.topMargin = prevY - titleBarHeight - (MINI_VIEW_HEIGHT + 20);
                                par.leftMargin = event.rawX.toInt() - (MINI_VIEW_WIDTH + 20)
                                miniView.layoutParams = par
//                            miniPercentage.text = (((event.rawX.toInt() - prevX ).toFloat()/ v.width)*100).toInt().toString()+" %"


                                var movedSeconds =
                                    getMovedSeconds(((event.rawX.toInt() - prevX).toFloat() / v.width))
                                var tmpCurrent = validateCurrent(
                                    currentVideoPositionSec,
                                    movedSeconds
                                )
                                moveVideoByDragPercentage(tmpCurrent, miniPercentage)

                            }
                        }


                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        if (flagBeginHorizontal) {
                            var movedSeconds =
                                getMovedSeconds((((event.rawX.toInt() - prevX).toFloat() / v.width)))
                            currentVideoPositionSec = validateCurrent(
                                currentVideoPositionSec,
                                movedSeconds
                            )
                            moveVideoByDragPercentage(currentVideoPositionSec, txt_main_screen_text)
                        }

                        miniView.visibility = View.GONE

                        lin_seek.visibility = View.GONE
                        flagBeginVertical = false
                        flagBeginHorizontal = false
                        return true
                    }
                    MotionEvent.ACTION_DOWN -> {
                        prevX = event.rawX.toInt()
                        prevY = event.rawY.toInt()
                        if (event.rawX > (v.width * 5 / 6)) {
                            targetArea = 2
                            initVolumnControlls();
                            currentSeekBarProgress = audioManager
                                .getStreamVolume(AudioManager.STREAM_MUSIC)
                        } else if (event.rawX < (v.width * 1 / 6)) {
                            targetArea = 0
                            initBrightnessControlls();
                            currentSeekBarProgress = Settings.System.getInt(
                                cResolver,
                                SCREEN_BRIGHTNESS
                            )
                        } else {
                            targetArea = 1

                        }
                        Log.e("targetArea", targetArea.toString())


//                        v.layoutParams = par
                        return true
                    }
                }
                return false
            }
        })
    }

    private fun initBrightnessControlls() {
        seek_bar.max = 255;
        //set the seek bar progress to 1
        seek_bar.keyProgressIncrement = 1;
        try {
            //Get the current system brightness

            seek_bar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                override fun onStopTrackingTouch(arg0: SeekBar) {}
                override fun onStartTrackingTouch(arg0: SeekBar) {}
                override fun onProgressChanged(arg0: SeekBar, progress: Int, arg2: Boolean) {
                    var brightness = 0
                    if(progress<=20)
                    {
                        //Set the brightness to 20
                        brightness=20;
                    }
                    else //brightness is greater than 20
                    {
                        brightness = progress;
                    }
                    Settings.System.putInt(cResolver, SCREEN_BRIGHTNESS, brightness);

                }
            })
        } catch (e: SettingNotFoundException) {
            //Throw an error case it couldn't be retrieved
            Log.e("Error", "Cannot access system brightness")
            e.printStackTrace()
        }

    }

    private fun initVolumnControlls() {
        try {
           var  audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            seek_bar.setMax(
                audioManager
                    .getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            )

            seek_bar.setProgress(
                audioManager
                    .getStreamVolume(AudioManager.STREAM_MUSIC)
            )
            seek_bar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                override fun onStopTrackingTouch(arg0: SeekBar) {}
                override fun onStartTrackingTouch(arg0: SeekBar) {}
                override fun onProgressChanged(arg0: SeekBar, progress: Int, arg2: Boolean) {
                    audioManager.setStreamVolume(
                        AudioManager.STREAM_MUSIC,
                        progress, 0
                    )
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun validateCurrent(posCurrent: Int, movedSeconds: Int): Int {

        var tmpCurrent =  posCurrent + movedSeconds
        if(tmpCurrent<0){
            return 0
        }else if(tmpCurrent> TOTAL_VIDEO_LENGTH){
            return TOTAL_VIDEO_LENGTH
        }else{
            return tmpCurrent
        }
    }

    private fun getMovedSeconds(percentage: Float) : Int{
        return (percentage*SECENDS_FOR_FULL_PERCENTAGE).toInt()
    }

    private fun moveVideoByDragPercentage(currentVideoPositionSec: Int, textView: TextView) {

        var currentDisplayLengthFormat : String
        if(currentVideoPositionSec/60==0){
              currentDisplayLengthFormat  =  (currentVideoPositionSec%60).toString()
        }else{
            currentDisplayLengthFormat  = (currentVideoPositionSec/60) .toString()+ ":" + currentVideoPositionSec%60
        }
        var totalDisplayLengthFormat = (TOTAL_VIDEO_LENGTH/60) .toString()+ ":" + TOTAL_VIDEO_LENGTH%60
        textView.text = " $currentDisplayLengthFormat / $totalDisplayLengthFormat"
    }

    fun getStatusBarHeight(): Int {
        var result = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        return result
    }
}