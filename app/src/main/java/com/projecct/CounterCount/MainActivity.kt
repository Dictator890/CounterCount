package com.projecct.CounterCount

import android.app.TimePickerDialog
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.NonCancellable.isActive


class MainActivity : AppCompatActivity() {
companion object{
    private const val TAG="MainActivity"

    private var defaultScope= CoroutineScope(Default)              //Used to access a default scope

    private var mainScope= MainScope()                             //Used to access the main scope
    private var isPaused=false                                      //Store if the pause button is played
    private val timeStorage=TimeStorage(0,0)                //Initializes the object for time storage structure

    private val tickPlayer=MediaPlayer()                            //Object to play the tick for each second
    private val endTickPlayer=MediaPlayer()                         //Objcet to play the tick for last

}

    override fun onStart() {
        super.onStart()
        //Assign the media player their respective assets to be played
        var afd=assets.openFd(getString(R.string.tick_Asset_file_name))
        tickPlayer.setDataSource(afd.fileDescriptor,afd.startOffset,afd.length)
        tickPlayer.prepare()

        afd=assets.openFd(getString(R.string.smooth_Asset_file_name))
        endTickPlayer.setDataSource(afd.fileDescriptor,afd.startOffset,afd.length)
        endTickPlayer.prepare()

    }

    @InternalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //Initializations
        val cancel=findViewById<Button>(R.id.cancel)
        val playpauseButton=findViewById<ImageButton>(R.id.playpausebutton)
        val timeSetter=findViewById<TextView>(R.id.timetext)
        val progressBar=findViewById<ProgressBar>(R.id.progressBar)

        //Create a new timepciker dialog to be opened on tap on the progressbar
        val timePickerDialog=TimePickerDialog(this,TimePickerDialog.OnTimeSetListener { tp, min, sec ->
            if(!(min==0)){
                progressBar.max=(min)*60
            }
            else{
                progressBar.max=sec
            }
            progressBar.progress=progressBar.max
            timeStorage.sec = sec
            timeStorage.min = min
            Log.d(TAG," Sec= $sec Min=$min")
            defaultScope.launch {
                startTimer(timeStorage,timeSetter, mainScope,tickPlayer, endTickPlayer,progressBar)
            }
        },0,0,true)

        //Initially hide cancel button
        cancel.visibility=View.GONE
        cancel.setOnClickListener {
               cancelScopes()
               timeStorage.reset()
               timeSetter.text= timeStorage.toString()
               Log.d(TAG,"Cancelled")
            progressBar.progress=progressBar.max

        }

        //Button to play and pause the timer
        playpauseButton.visibility=View.GONE
        playpauseButton.setOnClickListener {
            if(isPaused){
                isPaused=false
                playpauseButton.setImageResource(R.drawable.pause_32)
                defaultScope.launch {
                    startTimer(timeStorage,timeSetter, mainScope, tickPlayer, endTickPlayer,progressBar)
                }
            }
            else
            {
                cancelScopes()
                Log.d(TAG,"Paused")
                playpauseButton.setImageResource(R.drawable.play_32)
                isPaused=true
            }
        }
        //Open the timepickerdialog if no timer is running and progreebar is tapped
        progressBar.setOnClickListener {
            if(timeStorage.isTimeEmpty()){
                timePickerDialog.show()
            }
            cancel.visibility=View.VISIBLE
            playpauseButton.visibility=View.VISIBLE

        }
    }

    @InternalCoroutinesApi
    //Start the timer on an coroutine to run in background
    private suspend fun startTimer(storage:TimeStorage, textView: TextView, mainScope:CoroutineScope,tickSound:MediaPlayer?,endSound:MediaPlayer?,progressBar: ProgressBar?){
        Log.d(TAG,"startTimer() started")
       while (isActive){
           //If time is 0 then exit
           if(storage.isTimeEmpty()){
               endSound?.start()
               break
           }
           tickSound?.start()
            delay(1000)
            if(storage.sec == 0)
            {
                 if(storage.min != 0)
                 {
                     storage.min-=1
                     storage.sec=59
                 }
            }
             else
             {
                    storage.sec=storage.sec-1
             }
           //Change the time in the text view
       mainScope.launch {
            changeText(textView,storage)
           progressBar?.progress= progressBar?.progress?.minus(1)!!
        }

    }
}

    //Coroutine to be launched on Main Thread
    @InternalCoroutinesApi
    private  fun changeText(textView: TextView, storage: TimeStorage){
           textView.text=storage.toString()
    }
    //Cancel the default and main thread coroutines and assign new ones to them for a new restart if required
    private fun cancelScopes(){
        try{
           defaultScope.cancel()
            mainScope.cancel()
            defaultScope= CoroutineScope(Default)
            mainScope= MainScope()

        }catch (ex:Exception){}
    }


}