package com.example.bricklistdb

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.io.File
import java.io.FileOutputStream

class MyDBHandler(context: Context) : SQLiteOpenHelper(context,
                        "BrickList.db", null, 1)    {

    private var mContext: Context? = context
    private var mDatabase: SQLiteDatabase? = null
    val DATABASE_VERSION = 1
    val DATABASE_NAME = "BrickList.db"
    var DATABASE_PATH = ""


    init {
        this.DATABASE_PATH = "/data/data/" + context.packageName + "/databases/"
    }

    fun openDB() {
        if (mDatabase != null && mDatabase!!.isOpen)
            return
        try {
            mDatabase = SQLiteDatabase.openDatabase(DATABASE_PATH + DATABASE_NAME, null, SQLiteDatabase.OPEN_READWRITE)
        }
        catch (e: Exception) {
            val dbFile = mContext!!.assets.open(DATABASE_NAME)

            try {
                FileOutputStream(DATABASE_PATH + DATABASE_NAME)
            }
            catch (e: Exception) {
                val listnames = File(DATABASE_PATH)
                if (!listnames.exists())
                    listnames.mkdirs()
                else if (!listnames.isDirectory && listnames.canWrite()) {
                    listnames.delete()
                    listnames.mkdirs()
                }

                var file = File(DATABASE_PATH + DATABASE_NAME)
                file.createNewFile()
            }

            val output = FileOutputStream(DATABASE_PATH + DATABASE_NAME)

            val buffer = ByteArray(10)

            var length: Int = dbFile.read(buffer)
            while(length > 0) {
                output.write(buffer, 0, length)
                length = dbFile.read(buffer)
            }
            output.flush()
            output.close()
            dbFile.close()

            mDatabase = SQLiteDatabase.openDatabase(DATABASE_PATH + DATABASE_NAME, null, SQLiteDatabase.OPEN_READWRITE)
        }
    }

    fun closeDB() {
        mDatabase?.close()
    }

    fun readAllProjects(showArchived: Boolean): List<Project> {
        var query : String
        if (showArchived) {
            query = "SELECT * FROM Inventories ORDER BY LastAccessed DESC"
        }
        else
           query = "SELECT * FROM Inventories where Active = 1 ORDER BY LastAccessed DESC "
        val cursor = mDatabase?.rawQuery(query, null)

        var projects = ArrayList<Project>()

        while(cursor!!.moveToNext()) {
            projects.add(Project(cursor.getInt(0), cursor.getString(1), cursor.getInt(2), cursor.getLong(3)))
        }
        cursor.close()

        return projects
    }

    fun insertNewProject(name: String, active: Int, lastAccessed: Long) {
        val query = "INSERT INTO Inventories(Name, Active, LastAccessed) VALUES('%s', %d, %d)"
            .format(name, active, lastAccessed)
        mDatabase?.execSQL(query)
    }

    fun getLastGeneratedProjectId(): Int {
        val query = "SELECT ID FROM INVENTORIES ORDER BY ID DESC"
        val cursor = mDatabase?.rawQuery(query, null)
        cursor?.moveToFirst()
        val result = cursor!!.getInt(0)
        cursor.close()
        return result
    }

    fun getTypeIDByCode(code: String): Int {
        val query = "Select TypeID FROM Parts where Code='%s'"
            .format(code)
        val cursor = mDatabase?.rawQuery(query, null)
        cursor?.moveToFirst()
        val result = cursor!!.getInt(0)
        cursor.close()
        return result
    }

    fun getItemIDByCode(code: String) : Int {
        val query = "Select id FROM Parts where Code='%s'"
            .format(code)
        val cursor = mDatabase?.rawQuery(query, null)
        cursor?.moveToFirst()
        val result = cursor!!.getInt(0)
        cursor.close()
        return result
    }

    fun insertPartIntoInventoriesPart(invID: Int, typeID: Int, itemID: Int, quantityInSet: Int, quantityInStore: Int, colorID: Int, extra: Int) {
        val query = "INSERT INTO InventoriesParts(InventoryID, TypeID, ItemID, QuantityInSet, QuantityInStore, ColorId, Extra) " +
                "VALUES(%d, %d, %d, %d, %d, %d, %d)"
                    .format(invID, typeID, itemID, quantityInSet, quantityInStore, colorID, extra)
        mDatabase?.execSQL(query)
    }

    fun showParts() {
        val query = "Select * from inventoriesparts"
        val cursor = mDatabase?.rawQuery(query, null)
        while(cursor!!.moveToNext()) {
            System.out.println(cursor.toString())
        }
    }

    fun readInventoriesPartsByProjectID(id: Int): List<Item> {
        var items = ArrayList<Item>()

        val query = "Select * from InventoriesParts WHERE InventoryID = %d".format(id)
        val cursor = mDatabase?.rawQuery(query, null)
        while (cursor!!.moveToNext()) {
            val id = cursor.getInt(0)
            val typeID = cursor.getInt(2)
            val itemID = cursor.getInt(3)
            val quantityInSet = cursor.getInt(4)
            val quantityInStore = cursor.getInt(5)
            val colorID = cursor.getInt(6)

            val secondQuery = "Select Name from Colors WHERE code = %d".format(colorID)
            var secondCursor = mDatabase?.rawQuery(secondQuery, null)
            secondCursor?.moveToFirst()
            System.out.println(itemID)
            val color = secondCursor?.getString(0)

            secondCursor = mDatabase?.rawQuery("Select Code, Name, id from Parts where id = %d".format(itemID), null)
            secondCursor?.moveToFirst()
            val code = secondCursor?.getString(0)
            var name = secondCursor?.getString(1)
            val idToFindColorCode = secondCursor?.getInt(2)

            secondCursor = mDatabase?.rawQuery("Select Code from Codes where itemID = $idToFindColorCode and colorID = $colorID", null)
            secondCursor?.moveToFirst()
            var colorCode: Int?
            try {
                colorCode = secondCursor?.getInt(0)
            }
            catch (e: java.lang.Exception) {
                colorCode = -1
            }

            items.add(Item(id, itemID, name!!, color!!, colorID, colorCode!!, code!!, quantityInStore, quantityInSet))
            secondCursor?.close()
        }
        cursor.close()
        return items
    }

    fun changeQuantityInStore(id: Int, quantity: Int) {
        val query = "UPDATE InventoriesParts SET QuantityInStore = $quantity where id = $id"
        mDatabase?.execSQL(query)
    }

    fun updateLastAccessed(projectID: Int, time: Long) {
        val query = "UPDATE Inventories SET LastAccessed = $time WHERE id=$projectID"
        mDatabase?.execSQL(query)
    }

    fun checkArchived(id: Int) : Int{
        val query = "SELECT Active FROM Inventories WHERE id = $id"
        val cursor = mDatabase?.rawQuery(query, null)
        cursor?.moveToFirst()
        val result = cursor?.getInt(0)
        cursor?.close()
        return result!!
    }

    fun setArchived(id: Int, active: Int) {
        val query = "UPDATE Inventories SET active = $active where id = $id"
        mDatabase?.execSQL(query)
    }

    fun getSettings() : AppSettings {
        val query = "SELECT * FROM Settings"
        val cursor = mDatabase?.rawQuery(query, null)
        cursor?.moveToFirst()
        var bool : Boolean
        System.out.println(cursor!!.getInt(2))
        bool = cursor!!.getInt(2) == 1
        val settings = AppSettings(cursor!!.getString(1), bool)
        cursor.close()
        return settings
    }

    fun updateSettings(url: String, showArchived: Int) {
        System.out.println(showArchived)
        val query = "UPDATE Settings SET prefix = $url, archive = $showArchived"
        mDatabase?.execSQL(query)
        val query2 = "Select * FROM SETTINGS"
        val cursorrr = mDatabase?.rawQuery(query2, null)
        cursorrr?.moveToFirst()
        System.out.println(cursorrr?.getInt(0))
    }

    override fun onCreate(db: SQLiteDatabase?) { }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) { }
}