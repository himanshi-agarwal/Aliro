package com.example.aliro

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DBHelper(context: Context, factory: SQLiteDatabase.CursorFactory?) :
    SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION){

    companion object{
        private const val DATABASE_NAME = "ALIRO"
        private const val DATABASE_VERSION = 4
        const val USER_TABLE_NAME = "users"
        const val USER_ID_COL = "id"
        const val USER_NAME_COL = "name"
        const val USER_PASSWORD_COL = "password"
        const val USER_EMAIL_COL = "email"
        const val USER_PHONE_NO_COL = "phone_no"
        const val USER_DP_COL = "dp"
        const val USER_USER_TYPE_COL = "user_type"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createUserTable = ("CREATE TABLE IF NOT EXISTS " + USER_TABLE_NAME + " ( " +
                                USER_ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                USER_NAME_COL + " TEXT, " +
                                USER_PASSWORD_COL + " TEXT, " +
                                USER_EMAIL_COL + " TEXT, " +
                                USER_PHONE_NO_COL + " TEXT, " +
                                USER_DP_COL + " BLOB, " +
                                USER_USER_TYPE_COL + " TEXT " + " ) ")

        db.execSQL(createUserTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS  $USER_TABLE_NAME");

        onCreate(db)
    }

    fun addUser(name : String, password : String, email : String, phoneNo: String, userType: String){
        Log.d("DBHelper", "Adding user: $name, $email, $phoneNo, $userType")
        val values = ContentValues()

        values.put(USER_NAME_COL, name)
        values.put(USER_PASSWORD_COL, password)
        values.put(USER_EMAIL_COL, email)
        values.put(USER_PHONE_NO_COL, phoneNo)
        values.put(USER_USER_TYPE_COL, userType)

        val db = this.writableDatabase
        db.insert(USER_TABLE_NAME, null, values)

        Log.d("DBHelper", "Data added successfully")
//        db.close()
    }

    fun loginUser(name : String, password : String): ArrayList<String>{
        val userList = ArrayList<String>()
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * FROM $USER_TABLE_NAME WHERE $USER_NAME_COL = $name and $USER_PASSWORD_COL = $password", null)

        if(cursor.moveToFirst()){
            val name = cursor.getString(1)
            val email = cursor.getString(3)
            val contact = cursor.getString(4)
            val user_type = cursor.getString(6)
            userList.add(name)
            userList.add(email)
            userList.add(contact)
            userList.add(user_type)
        }
        cursor.close()
        db.close()

        return userList
    }
}