package com.example.customviewclock

import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {

    lateinit var secondCustomViewClock: CustomViewClock

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        secondCustomViewClock = findViewById<CustomViewClock>(R.id.secondCustomViewClock)
        (SimpleAsyncTask()).execute()
    }

    inner class SimpleAsyncTask: AsyncTask<Unit, Unit, Unit>() {
        override fun doInBackground(vararg params: Unit?) {
            while (true) {
                Thread.sleep(1000)
                publishProgress()
            }
        }

        override fun onProgressUpdate(vararg values: Unit?) {
            super.onProgressUpdate(*values)
            secondCustomViewClock.getTime().add(1)
            secondCustomViewClock.invalidate()
        }
    }
}
