package testing.steven.longpressdragdrop

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_play_ground_entrance.*

class PlayGroundEntrance : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_ground_entrance)
        txt_drag.setOnClickListener {
            startActivity(Intent(this,DragDropActivity::class.java))
        }
        txt_video.setOnClickListener {
            startActivity(Intent(this,IQIYIActivity::class.java))
        }
    }
}