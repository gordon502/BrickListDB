package com.example.bricklistdb

import android.opengl.Visibility
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_new_project.*
import khttp.get
import khttp.responses.Response
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.lang.Exception
import java.time.LocalDateTime
import java.util.*
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory


class NewProjectActivity : AppCompatActivity() {

    private var lastResponse: Response? = null
    private var dbHandler: MyDBHandler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_project)

        val actionBar = supportActionBar
        actionBar!!.title = "New Project"
        actionBar.setDisplayHomeAsUpEnabled(true)

        progressBar.animate()

        dbHandler = MyDBHandler(this)

        setNumberText.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                addButton.isEnabled = false
            }
        })
        projectNameText.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                addButton.isEnabled = false
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return true
    }

    fun checkIfFileIsAvailable(view: View) {
        StatusCodeTask().execute(setNumberText.text.toString())
    }

    fun addNewProject(view: View) {
        dbHandler?.openDB()
        dbHandler?.insertNewProject(projectNameText.text.toString(), 1, Date().time/1000)
        ProcessXMLTask().execute(dbHandler?.getLastGeneratedProjectId())
    }

    private inner class StatusCodeTask:AsyncTask<String, Void, Boolean>() {

        override fun onPreExecute() {
            progressBar.visibility = View.VISIBLE
            checkButton.isEnabled = false
            addButton.isEnabled = false
        }

        override fun doInBackground(vararg params: String?): Boolean {
            try {
                val r = get(MainActivity.settings!!.prefixURL + params[0] + ".xml", stream = true)
                lastResponse = r
                return r.statusCode == 200
            }
            catch(e: Exception) {
                return false;
            }
        }

        override fun onPostExecute(result: Boolean?){
            progressBar.visibility = View.INVISIBLE
            checkButton.isEnabled = true
            if (projectNameText.text.isEmpty() && !result!!)
                Toast.makeText(this@NewProjectActivity, "Project name is empty and cannot reach this set!", Toast.LENGTH_LONG).show()
            else if (!result!!)
                Toast.makeText(this@NewProjectActivity, "Cannot reach this set!", Toast.LENGTH_LONG).show()
            else if(projectNameText.text.isEmpty())
                Toast.makeText(this@NewProjectActivity, "Project name is empty!", Toast.LENGTH_LONG).show()

            addButton.isEnabled = result!! && !projectNameText.text.isEmpty()
        }
    }

    private inner class ProcessXMLTask:AsyncTask<Int, Void, Boolean>() {
        override fun onPreExecute() {
            checkButton.isEnabled = false
            addButton.isEnabled = false
            progressBar.visibility = View.VISIBLE
            progressBar.animate()
        }

        override fun doInBackground(vararg params: Int?): Boolean {
            val factory = DocumentBuilderFactory.newInstance()
            val builder: DocumentBuilder = factory.newDocumentBuilder()
            val responseCopy = lastResponse
            val xml = builder.parse(responseCopy!!.raw)
            xml.documentElement.normalize()

            val itemList: NodeList = xml.getElementsByTagName("ITEM")

            for (i in 0 until itemList.length) {
                val itemNode = itemList.item(i)
                if (itemNode.nodeType == Node.ELEMENT_NODE) {
                    val element = itemNode as Element
                    val alternate = element.getElementsByTagName("ALTERNATE").item(0).textContent
                    val itemType = element.getElementsByTagName("ITEMTYPE").item(0).textContent
                    if (alternate.equals("N") && itemType.equals("P")) {
                        val code = element.getElementsByTagName("ITEMID").item(0).textContent
                        val qty = element.getElementsByTagName("QTY").item(0).textContent.toInt()
                        val color = element.getElementsByTagName("COLOR").item(0).textContent.toInt()
                        val extra = element.getElementsByTagName("EXTRA").item(0).textContent
                        val typeID = dbHandler?.getTypeIDByCode(code)
                        val itemID = dbHandler?.getItemIDByCode(code)


                        dbHandler?.insertPartIntoInventoriesPart(
                            params[0]!!,
                            typeID!!,
                            itemID!!,
                            qty,
                            0,
                            color,
                            0)
                    }
                }
            }
            return true
        }

        override fun onPostExecute(result: Boolean?) {
            super.onPostExecute(result)
            dbHandler?.closeDB()
            finish()
        }
    }

}
