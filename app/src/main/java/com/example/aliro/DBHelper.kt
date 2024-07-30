package com.example.aliro

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context, factory: SQLiteDatabase.CursorFactory?) :
    SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION){

    override fun onCreate(db: SQLiteDatabase) {
        val query = ("CREATE TABLE " + TABLE_NAME + " ( " +
                ID_COL + "INTEGER PRIMARY KEY AUTO_INCREMENT, " +
                NAME_COL + "TEXT, " +
                PASSWORD_COL + "TEXT" + " ) ")

        db.execSQL(query)
    }

    override fun onUpgrade(db: SQLiteDatabase, p1: Int, p2: Int) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME)
        onCreate(db)
    }

    fun addName(username : String, password : String ){
        val values = ContentValues()

        values.put(NAME_COL, username)
        values.put(PASSWORD_COL, password)

        val db = this.writableDatabase
        db.insert(TABLE_NAME, null, values)

        db.close()
    }

    companion object{
        private val DATABASE_NAME = "ALIRO"
        private val DATABASE_VERSION = 1
        val TABLE_NAME = "login"
        val ID_COL = "id"
        val NAME_COL = "name"
        val PASSWORD_COL = "password"
    }
}