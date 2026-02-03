package com.example.mykalgoritmogenetico

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity




class MainListar : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listar)

        val listView: ListView = findViewById(R.id.listaRutas)
        val btnVolver: Button = findViewById(R.id.btnvolver)
        val btnBorrarTodo: Button = findViewById(R.id.btnBorrarTodo)
        val dbHelper = database(this)
        var rutas = dbHelper.obtenerRutas()

        val data = rutas.mapIndexed { index, ruta ->
            "Ruta #${index + 1}: $ruta"
        }

        var adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, data)
        listView.adapter = adapter

        // dentro de onCreate de MainListar, después de setAdapter(...)
        listView.setOnItemClickListener { parent, view, position, id ->
            // Obtén la ruta seleccionada de la lista
            val rutaSeleccionada = rutas[position]

            // volver al MainActivity
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("ruta_seleccionada", rutaSeleccionada)
            intent.putExtra("mostrar_ruta_guardada", true)
            // opcional
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            finish()
        }

        // Acción del botón
        btnVolver.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // opcional: cierra esta actividad
        }

        // Acción del botón borrar todo
        btnBorrarTodo.setOnClickListener {
            if (rutas.isEmpty()) {
                Toast.makeText(this, "No hay rutas para borrar", Toast.LENGTH_SHORT).show()
            } else {
                // Mostrar diálogo de confirmación
                AlertDialog.Builder(this)
                    .setTitle("Confirmar")
                    .setMessage("¿Estás seguro de que quieres borrar todas las rutas guardadas?")
                    .setPositiveButton("Sí") { _, _ ->
                        // Borrar todas las rutas de la base de datos
                        dbHelper.borrarTodasLasRutas()
                        
                        // Actualizar la lista local
                        rutas = dbHelper.obtenerRutas()
                        val nuevaData = rutas.mapIndexed { index, ruta ->
                            "Ruta #${index + 1}: $ruta"
                        }
                        
                        // Actualizar el adaptador
                        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, nuevaData)
                        listView.adapter = adapter
                        
                        Toast.makeText(this, "Todas las rutas han sido borradas", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("No", null)
                    .show()
            }
        }

    }
}

