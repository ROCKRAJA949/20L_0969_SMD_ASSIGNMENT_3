package com.example.a20l_0969_smd_assignment_3
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "contacts.db"
        const val DATABASE_VERSION = 1

        const val TABLE_NAME = "contacts"
        const val COLUMN_ID = "_id"
        const val COLUMN_DISPLAY_NAME = "display_name"
        const val COLUMN_PHONE_NUMBER = "phone_number"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_DISPLAY_NAME TEXT,
                $COLUMN_PHONE_NUMBER TEXT
            )
        """.trimIndent()

        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Add upgrade logic if needed
    }

    fun insertContact(displayName: String, phoneNumber: String) {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_DISPLAY_NAME, displayName)
            put(COLUMN_PHONE_NUMBER, phoneNumber)
        }

        db.insert(TABLE_NAME, null, contentValues)
        db.close()
        Log.d("DatabaseHelper", "Insert Contact")
    }

    fun deleteContact(phoneNumber: String) {
        val db = writableDatabase
        db.delete(TABLE_NAME, "$COLUMN_PHONE_NUMBER=?", arrayOf(phoneNumber))
        db.close()
        Log.d("DatabaseHelper", "Successful Delete")
    }
}

