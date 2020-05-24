package com.example.bricklistdb

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.view.MenuItem
import androidx.appcompat.app.ActionBar
import kotlinx.android.synthetic.main.activity_settings.*
import java.io.File

class SettingsActivity : AppCompatActivity() {

    private var dbHandler: MyDBHandler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        dbHandler = MyDBHandler(this)

        urlPrefixText.setText(MainActivity.settings.prefixURL)
        archiveSwitch.isChecked = MainActivity.settings!!.showArchive
        val actionBar = supportActionBar
        actionBar!!.title = "Settings"

        actionBar.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        MainActivity.settings!!.showArchive = archiveSwitch.isChecked
        MainActivity.settings!!.prefixURL = urlPrefixText.text.toString()
        //if(!MainActivity.settings!!.prefixURL.endsWith("/"))
        //    MainActivity.settings!!.prefixURL += "/"
        saveSettings()
        finish()
        return true
    }

    fun saveSettings() {
        val dir = filesDir.absolutePath + "/data.txt"

        var file = File(dir)

        file.writeText(urlPrefixText.text.toString() + "\n" + archiveSwitch.isChecked.toString())
    }
}
