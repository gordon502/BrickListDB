package com.example.bricklistdb

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.core.content.FileProvider
import androidx.core.view.marginBottom
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.activity_project_inventory.*
import kotlinx.android.synthetic.main.activity_project_inventory.view.*
import java.io.File
import java.io.InputStream
import java.lang.Exception
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

class ProjectInventoryActivity : AppCompatActivity() {

    private var dbHandler: MyDBHandler? = null
    private var items: List<Item>? = null

    private val incrementQuantity = View.OnClickListener { view ->
        val item = items!![view.id]
        if (item.quantityInStore < item.quantityInSet) {
            item.quantityInStore += 1

            dbHandler?.changeQuantityInStore(item.idInInvParts, item.quantityInStore)
            val tv = findViewById<TextView>(item.idInInvParts)
            tv.text = "%d of %d".format(item.quantityInStore, item.quantityInSet)
        }
    }

    private val decrementQuantity = View.OnClickListener { view ->
        val item = items!![view.id]
        if (item.quantityInStore > 0) {
            item.quantityInStore -= 1

            dbHandler?.changeQuantityInStore(item.idInInvParts, item.quantityInStore)
            val tv = findViewById<TextView>(item.idInInvParts)
            tv.text = "%d of %d".format(item.quantityInStore, item.quantityInSet)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project_inventory)

        val actionBar = supportActionBar
        actionBar!!.title = MainActivity.projectName
        actionBar.setDisplayHomeAsUpEnabled(true)

        dbHandler = MyDBHandler(this)
        dbHandler?.openDB()

        items = dbHandler?.readInventoriesPartsByProjectID(MainActivity.projectID!!)
        fillViewWithItems()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        System.out.println(id)

        if (id == 16908332) {
            dbHandler?.closeDB()
            finish()
        }
        else if (id == R.id.saveXML) {
            exportXML()
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        var inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.xml_save, menu)
        return true
    }

    fun fillViewWithItems() {

        linearLayoutVertical.removeAllViewsInLayout()
        for ((i, item) in items!!.withIndex()) {
            var imageView = ImageView(this)
            var layoutParams = LinearLayout.LayoutParams(200, 200)
            layoutParams.gravity = Gravity.CENTER
            imageView.layoutParams = layoutParams
            linearLayoutVertical.addView(imageView)
            FillImageView().execute(ImageViewWithParams(imageView, item.code, item.colorCode, item.colorID))


            val description = TextView(this)
            description.gravity = Gravity.CENTER
            description.text = "%s\n%s [%s]".format(item.name, item.color, item.code)
            linearLayoutVertical.addView(description)

            val quantity = TextView(this)
            quantity.text = "%d of %d".format(item.quantityInStore, item.quantityInSet)
            quantity.gravity = Gravity.CENTER
            quantity.id = item.idInInvParts
            quantity.setTypeface(Typeface.DEFAULT_BOLD)
            linearLayoutVertical.addView(quantity)

            //val horizontalLayout = LinearLayout(this)
            //horizontalLayout.orientation = LinearLayout.HORIZONTAL
            //val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
            //params.setMargins(0,0,0, 30)
            //params.width = linearLayoutVertical.width
            //horizontalLayout.layoutParams = params
            //linearLayoutVertical.addView(horizontalLayout)

            val incrementButton = Button(this)
            incrementButton.id = i
            incrementButton.text = "INCREMENT"
            incrementButton.width = LinearLayout.LayoutParams.WRAP_CONTENT
            incrementButton.setBackgroundColor(0xFFFFFF)
            incrementButton.setOnClickListener(incrementQuantity)
            linearLayoutVertical.addView(incrementButton)

            val decrementButton = Button(this)
            val btnParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            btnParams.setMargins(0, 0, 0, 40)
            decrementButton.layoutParams = btnParams
            decrementButton.id = i
            decrementButton.text = "DECREMENT"
            decrementButton.width = LinearLayout.LayoutParams.WRAP_CONTENT
            decrementButton.setBackgroundColor(0xFFFFFF)
            decrementButton.setOnClickListener(decrementQuantity)
            decrementButton.setBackgroundResource(R.drawable.cell_shape)
            linearLayoutVertical.addView(decrementButton)

        }
    }

    private inner class FillImageView: AsyncTask<ImageViewWithParams, Void, ImageViewWithParams>() {
        override fun doInBackground(vararg params: ImageViewWithParams?): ImageViewWithParams {
            var url = "https://www.lego.com/service/bricks/5/2/" + params[0]?.colorCode
            var r = khttp.get(url, stream = true)
            if (r.statusCode == 200) {
                params[0]?.inputStream = r.content
                return params[0]!!
            }

            url = "http://img.bricklink.com/P/%d/%s.gif".format(params[0]?.colorID, params[0]?.code)
            r = khttp.get(url, stream = true)
            if (r.statusCode == 200) {
                params[0]?.inputStream = r.content
                return params[0]!!
            }

            url = "http://img.bricklink.com/P/%d/%sold.gif".format(params[0]?.colorID, params[0]?.code)
            r = khttp.get(url, stream = true)
            if (r.statusCode == 200) {
                params[0]?.inputStream = r.content
                return params[0]!!
            }

            url = "https://www.bricklink.com/PL/%s.jpg".format(params[0]?.code)
            r = khttp.get(url, stream = true)
            if (r.statusCode == 200) {
                params[0]?.inputStream = r.content
                return params[0]!!
            }


            System.out.println("Nie udalo sie")
            return params[0]!!
        }

        override fun onPostExecute(result: ImageViewWithParams?) {
            try {
                System.out.println(result!!.inputStream)
                val image = BitmapFactory.decodeByteArray(result!!.inputStream, 0, result!!.inputStream!!.size)
                result!!.imageView.setImageBitmap(image)
            }
            catch (e: Exception) {
                result!!.imageView.setImageResource(R.drawable.ic_launcher_background)
            }
        }
    }

    private inner class ImageViewWithParams {
        val imageView: ImageView
        val code: String
        val colorCode: Int
        val colorID: Int
        var inputStream: ByteArray? = null

        constructor(imageView: ImageView, code: String, colorCode: Int, colorID: Int) {
            this.imageView = imageView
            this.code = code
            this.colorCode = colorCode
            this.colorID = colorID
        }
    }

    private fun exportXML() {
        val docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val document = docBuilder.newDocument()

        val inventory = document.createElement("INVENTORY")

        for (item in items!!) {
            if (item.quantityInStore < item.quantityInSet) {
                val itemNode = document.createElement("ITEM")

                val itemType = document.createElement("ITEMTYPE")
                itemType.appendChild(document.createTextNode("P"))
                itemNode.appendChild(itemType)

                val itemID = document.createElement("ITEMID")
                itemID.appendChild(document.createTextNode(item.itemID.toString()))
                itemNode.appendChild(itemID)

                val color = document.createElement("COLOR")
                color.appendChild(document.createTextNode(item.colorID.toString()))
                itemNode.appendChild(color)

                val qtyfilled = document.createElement("QTYFILLED")
                qtyfilled.appendChild(document.createTextNode((item.quantityInSet - item.quantityInStore).toString()))
                itemNode.appendChild(qtyfilled)

                inventory.appendChild(itemNode)
            }
        }

        val transformer = TransformerFactory.newInstance().newTransformer()

        transformer.setOutputProperty(OutputKeys.INDENT, "yes")
        transformer.setOutputProperty("{http://xml.apache.org/xlst}indent-amount", "2")
        document.appendChild(inventory)

        val path=this.filesDir
        val outDir = File(path, "xml_output")
        outDir.mkdir()

        val file = File(outDir, MainActivity.projectName.replace("\\s".toRegex(), "_") + ".xml")

        transformer.transform(DOMSource(document), StreamResult(file))

        val authority = this.packageName + ".provider"
        val contentSource = FileProvider.getUriForFile(applicationContext, authority, file)

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/xml"
        intent.putExtra(
            Intent.EXTRA_STREAM,
            contentSource
        )
        startActivity(Intent.createChooser(intent, "Send XML"))
    }
}
