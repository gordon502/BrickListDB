package com.example.bricklistdb

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.widget.Button
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.lang.Math.abs
import java.util.*

class MainActivity : AppCompatActivity() {

    var db: MyDBHandler? = null
    var projects: List<Project>? = null
    var canAccessDB = false

    companion object {
        var settings: AppSettings = AppSettings("http://fcds.cs.put.poznan.pl/MyWeb/BL/", false)
        var projectName = ""
        var projectID: Int? = null
    }

    private val showOptionsDialog = View.OnClickListener { view ->
        projectID = view.id
        val btn = view as Button
        projectName = btn.text.toString()
        showDialog(btn)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!setupPermissions()) {
            loadSettings()
            canAccessDB = true
            db = MyDBHandler(this)
            db!!.openDB()
            projects = db!!.readAllProjects(settings!!.showArchive)
            fillView()
        }
    }

    override fun onResume() {
        super.onResume()
        if (canAccessDB) {
            projects = db!!.readAllProjects(settings!!.showArchive)
            fillView()
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        var inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_items, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.settingsMenu) {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        return super.onOptionsItemSelected(item)
    }

    fun addNewProject(view: View) {
        val intent = Intent(this, NewProjectActivity::class.java)
        startActivity(intent)
    }

    private fun fillView() {
        linearLayout.removeAllViewsInLayout()
        for (project in projects!!) {
            var tv = Button(this)
            tv.text = project.name
            tv.id = project.id
            linearLayout.addView(tv)
            tv.setOnClickListener(showOptionsDialog)
        }
    }

    private fun setupPermissions(): Boolean {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.INTERNET), 0)
            return true
        }
        return false
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 0) {
            if (grantResults.size > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED
                    && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                canAccessDB = true
                db = MyDBHandler(this)
                db!!.openDB()
                loadSettings()
                projects = db!!.readAllProjects(settings!!.showArchive)
                fillView()
            }
        }
    }

    private fun goToInventory(view: View){
        System.out.println(view.id)
    }

    // Method to show an alert dialog with single choice list items
    private fun showDialog(btn: Button){
        // Late initialize an alert dialog object
        lateinit var dialog:AlertDialog

        // Initialize an array of colors
        var archive : String
        var active = db?.checkArchived(projectID!!)
        if (active == 1)
            archive = "ARCHIVE"
        else
            archive = "UNARCHIVE"
        val array = arrayOf("OPEN", archive)
        val builder = AlertDialog.Builder(this)

        builder.setTitle("Action")
        builder.setSingleChoiceItems(array,-1) { _, which->
            val selection = array[which]
            if (selection == "OPEN") {
                db?.updateLastAccessed(projectID!!, Date().time/1000)
                val intent = Intent(this, ProjectInventoryActivity::class.java)
                dialog.dismiss()
                startActivity(intent)
            }
            else if (selection == archive) {
                db?.setArchived(projectID!!, abs(active!! - 1))
                projects = db?.readAllProjects(settings!!.showArchive)
                fillView()
                dialog.dismiss()
            }

            // Dismiss the dialog

        }


        // Initialize the AlertDialog using builder object
        dialog = builder.create()

        // Finally, display the alert dialog
        dialog.show()
    }

    private fun loadSettings() {
        val dir = filesDir.absolutePath + "/data.txt"

        var file = File(dir)
        val isNewFileCreated = file.createNewFile()

        if (!isNewFileCreated) {
            val lines = file.readLines()
            if (lines.size == 2) {
                settings.prefixURL = lines[0]
                settings.showArchive = lines[1].toBoolean()
            }
        }

    }
}
