package com.example.mykalgoritmogenetico

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class database(context: Context) : SQLiteOpenHelper(context, "rutas.db", null, 1) {
        override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE rutas (id INTEGER PRIMARY KEY AUTOINCREMENT, ruta TEXT)"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            db.execSQL("DROP TABLE IF EXISTS rutas")
            onCreate(db)
        }

        fun guardarRuta(ruta: String) {
        val db = writableDatabase
        val values = ContentValues()
        values.put("ruta", ruta)
        db.insert("rutas", null, values)
        db.close()
        }


    fun obtenerRutas(): List<String> {
        val db = readableDatabase
        val lista = mutableListOf<String>()
        val cursor = db.rawQuery("SELECT ruta FROM rutas", null)
        if (cursor.moveToFirst()) {
            do {
                lista.add(cursor.getString(0)) // solo la ruta
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return lista
    }

    fun borrarTodasLasRutas() {
        val db = writableDatabase
        db.execSQL("DELETE FROM rutas")
        db.close()
    }

}
