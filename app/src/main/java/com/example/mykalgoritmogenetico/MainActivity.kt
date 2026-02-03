package com.example.mykalgoritmogenetico

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Bundle
import android.widget.Toast
import android.graphics.Color
import android.graphics.Path
import android.graphics.PathMeasure

import androidx.appcompat.app.AppCompatActivity
import com.example.mykalgoritmogenetico.databinding.ActivityMainBinding
import java.util.concurrent.ThreadLocalRandom
import androidx.core.graphics.createBitmap


class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private var rutaAnimator: ValueAnimator? = null
    private var currentMejorCamino: String? = null

    private var currentMejorRuta: IntArray? = null
    private var currentCoordenadas: Array<Punto>? = null

    // Lista donde se guardan los puntos de control
    private val puntosTactiles = mutableListOf<Punto>()

    @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Verificar si se recibió una ruta desde MainListar
        if (intent.getBooleanExtra("mostrar_ruta_guardada", false)) {
            cargarRutaDesdeMainListar()
        }

        // Captura del toque táctil
        binding.myImg.setOnTouchListener { _, event ->
            // Si estamos en modo "mostrar ruta guardada", resetear al modo normal
            if (intent.getBooleanExtra("mostrar_ruta_guardada", false)) {
                resetearModoNormal()
            }
            
            if (event.action == android.view.MotionEvent.ACTION_DOWN) {
                val x = event.x.toInt()
                val y = event.y.toInt()
                puntosTactiles.add(Punto(x, y))

                // Dibujar el punto
                val bitmap = createBitmap(binding.myImg.width, binding.myImg.height)
                val canvas = Canvas(bitmap)
                
                // Fondo blanco
                val paintFondo = Paint().apply { color = Color.WHITE }
                canvas.drawRect(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat(), paintFondo)
                
                val paint = Paint().apply {
                    color = Color.BLUE
                    style = Paint.Style.FILL
                }
                for (p in puntosTactiles) {
                    canvas.drawCircle(p.x.toFloat(), p.y.toFloat(), 12f, paint)
                }
                binding.myImg.setImageBitmap(bitmap)
            }
            true
        }

        // Botón de acción
        binding.btnaccion.setOnClickListener {
            if (puntosTactiles.size <= 10) {
                Toast.makeText(this, "Debes ingresar al menos 11 puntos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val numciudades = puntosTactiles.size
            val tampoblacion = binding.txttampoblacion.text.toString().toInt()
            val probMutacion = binding.txtprobabilidadMutacion.text.toString().toDouble()
            val numGeneraciones = binding.txtnumgenraciones.text.toString().toInt()

            // Algoritmo genético con los puntos tocados
            var poblacion = Array(tampoblacion) { Individuo(numciudades) }
            val coordenadas = puntosTactiles.toTypedArray()

            poblacion = calcular_aptitud(poblacion, coordenadas)

            for (t in 0..numGeneraciones) {
                val seleccionados = seleccion_torneo(poblacion)
                for (i in 0 until seleccionados.size step 2) {
                    val padre1 = seleccionados[i]
                    val padre2 = seleccionados[i + 1]
                    val hijo1 = mutar(cruzar(padre1, padre2), probMutacion)
                    val hijo2 = mutar(cruzar(padre2, padre1), probMutacion)
                    poblacion[i] = hijo1
                    poblacion[i + 1] = hijo2
                }
                poblacion = calcular_aptitud(poblacion, coordenadas)
            }

            val mejor = poblacion[0]
            binding.lblmejordistancia.text =
                "${mejor.distancia} => ${mejor.get_camino()}"

            currentMejorRuta = mejor.cromosoma
            currentCoordenadas = coordenadas
            val mejorIndividuo = poblacion[0]

            rutaAnimator?.cancel()
            rutaAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
                duration = 2000
                addUpdateListener { anim ->
                    val progress = anim.animatedValue as Float
                    mostrarRutaBezier(currentMejorRuta, currentCoordenadas, progress)
                }
                start()
            }

            currentMejorCamino = mejorIndividuo.get_camino()
        }

        binding.btnborrar.setOnClickListener {
            // Vaciar la lista de puntos
            puntosTactiles.clear()

            // Crear un bitmap vacío del tamaño de tu ImageView
            val bitmap = createBitmap(binding.myImg.width, binding.myImg.height)
            val canvas = Canvas(bitmap)

            // pintar fondo de blanco
            val paintFondo = Paint().apply { color = Color.WHITE }
            canvas.drawRect(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat(), paintFondo)

            // Asignar el bitmap limpio al ImageView
            binding.myImg.setImageBitmap(bitmap)

            // Resetear ruta actual
            currentMejorRuta = null
            currentCoordenadas = null
        }

        binding.btnguardar.setOnClickListener {
            if (currentMejorCamino == null) {
                Toast.makeText(this, "Primero genera una ruta", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val dbHelper = database(this)
            dbHelper.guardarRuta(currentMejorCamino!!)

            Toast.makeText(this, "Ruta guardada en SQLite", Toast.LENGTH_SHORT).show()
        }

        binding.btnopciones.setOnClickListener {
            val intent = Intent(this, MainListar::class.java)
            startActivity(intent)
        }


    }

    fun mutar(Hijo:Individuo,Pm:Double):Individuo{
        val aleatorio:Double = ThreadLocalRandom.current().nextDouble()
        if (aleatorio<Pm){
            val indice1:Int = ThreadLocalRandom.current().nextInt(Hijo.cromosoma.size)
            var indice2:Int = indice1
            while(indice1==indice2){
                indice2 = ThreadLocalRandom.current().nextInt(Hijo.cromosoma.size)
            }
            val t = Hijo.cromosoma[indice1]
            Hijo.cromosoma[indice1] = Hijo.cromosoma[indice2]
            Hijo.cromosoma[indice2] = t
        }
        return  Hijo
    }
    fun cruzar(Padre1:Individuo,Padre2:Individuo):Individuo{
        val punto_cruce:Int = ThreadLocalRandom.current().nextInt(Padre1.cromosoma.size-1)
        val Hijo:Individuo = Individuo(Padre1.num_ciudades)
        for (i in 0..punto_cruce-1){
            Hijo.cromosoma[i] = Padre1.cromosoma[i]
        }
        for (i in punto_cruce..Padre1.num_ciudades-1){
            Hijo.cromosoma[i] = Padre2.cromosoma[i]
        }
        return Hijo
    }
    fun seleccion_torneo(pobla:Array<Individuo>):Array<Individuo>{
        val seleccionados:Array<Individuo> = Array(pobla.size){Individuo(pobla[0].cromosoma.size)}
        for (i in 0..pobla.size-1){
            val indice1:Int = ThreadLocalRandom.current().nextInt(pobla.size)
            var indice2:Int = indice1
            while(indice1==indice2){
                indice2 = ThreadLocalRandom.current().nextInt(pobla.size)
            }
            val competidor1:Individuo = pobla[indice1]
            val competidor2:Individuo = pobla[indice2]
            if(competidor1.distancia<competidor2.distancia){
                seleccionados[i] = competidor1
            }else{
                seleccionados[i] = competidor2
            }
        }
        return seleccionados
    }
    fun calcular_aptitud(pobla:Array<Individuo>,D:Array<Punto>):Array<Individuo>{
        val ordenado:Array<Individuo> = Array(pobla.size){Individuo(pobla[0].cromosoma.size)}
        val aptitud:IntArray=IntArray(pobla.size){0}
        for(i in 0..pobla.size-1){
            aptitud[i] = pobla[i].get_distancia(D)
        }
        aptitud.sort()// ordena de menor a mayor
        for(i in 0..pobla.size-1){
            for(j in 0..pobla.size-1){
                if(aptitud[i] == pobla[j].distancia){
                    ordenado[i] = pobla[j]
                    break
                }
            }
        }
        return ordenado
    }

    fun mostrarRutaBezier(ruta: IntArray?, ciudades: Array<Punto>?, progress: Float) {
        if (ruta == null || ciudades == null) return

        val bitmap = createBitmap(binding.myImg.width, binding.myImg.height)
        val canvas = Canvas(bitmap)

        val paintRuta = Paint().apply {
            color = Color.RED
            strokeWidth = 6f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }

        val paintPuntos = Paint().apply {
            color = Color.BLUE
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val paintTexto = Paint().apply {
            color = Color.BLACK
            textSize = 40f
        }

        // Dibujar las ciudades
        for (i in ciudades.indices) {
            val p = ciudades[i]
            canvas.drawCircle(p.x.toFloat(), p.y.toFloat(), 12f, paintPuntos)
            canvas.drawText(i.toString(), p.x.toFloat() + 16f, p.y.toFloat() - 8f, paintTexto)
        }

        if (ruta.size >= 3) {
            val path = Path()
            val total = ruta.size

            // Punto inicial
            val p0 = ciudades[ruta[0]]
            path.moveTo(p0.x.toFloat(), p0.y.toFloat())

            // Bézier cuadráticas
            for (i in 0 until total) {
                val p1 = ciudades[ruta[i % total]]
                val p2 = ciudades[(i + 1) % total]
                val p3 = ciudades[(i + 2) % total]

                // El control es el punto intermedio, el destino es p2
                path.quadTo(
                    p2.x.toFloat(), p2.y.toFloat(),
                    p3.x.toFloat(), p3.y.toFloat()
                )
            }

            // Animación
            if (progress in 0f..1f) {
                val pm = PathMeasure(path, false)
                val animatedPath = Path()
                pm.getSegment(0f, pm.length * progress, animatedPath, true)
                canvas.drawPath(animatedPath, paintRuta)
            } else {
                // Dibujo completo
                canvas.drawPath(path, paintRuta)
            }
        }

        binding.myImg.setImageBitmap(bitmap)
    }

    private fun parseRuta(ruta: String?): IntArray? {
        if (ruta.isNullOrBlank()) return null
        val parts = ruta.split(",").mapNotNull {
            val s = it.trim()
            if (s.isEmpty()) null else s.toIntOrNull()
        }
        if (parts.isEmpty()) return null
        return parts.toIntArray()
    }

    private fun cargarRutaDesdeMainListar() {
        val rutaSeleccionada = intent.getStringExtra("ruta_seleccionada")
        
        if (rutaSeleccionada != null) {
            // Parsear la ruta para extraer distancia y orden de ciudades
            val rutaParseada = parseRuta(rutaSeleccionada)
            
            if (rutaParseada != null) {
                // Esperar a que el ImageView tenga dimensiones válidas
                binding.myImg.post {
                    // Crear puntos de ejemplo en posiciones distribuidas en la pantalla
                    puntosTactiles.clear()
                    val numPuntos = rutaParseada.size
                    
                    // Usar las dimensiones reales del ImageView
                    val imgWidth = binding.myImg.width
                    val imgHeight = binding.myImg.height
                    
                    if (imgWidth > 0 && imgHeight > 0) {
                        // Generar puntos distribuidos en un círculo centrado en el ImageView
                        val centerX = imgWidth / 2f
                        val centerY = imgHeight / 2f
                        val radius = Math.min(imgWidth, imgHeight) / 3f
                        
                        for (i in 0 until numPuntos) {
                            val angle = (2 * Math.PI * i / numPuntos).toFloat()
                            val x = (centerX + radius * Math.cos(angle.toDouble())).toInt()
                            val y = (centerY + radius * Math.sin(angle.toDouble())).toInt()
                            puntosTactiles.add(Punto(x, y))
                        }
                        
                        // Configurar la ruta actual
                        currentMejorRuta = rutaParseada
                        currentCoordenadas = puntosTactiles.toTypedArray()
                        currentMejorCamino = rutaSeleccionada
                        
                        // Mostrar la distancia
                        val distancia = calcularDistanciaTotal(rutaParseada, puntosTactiles.toTypedArray())
                        binding.lblmejordistancia.text = String.format("Distancia: %.2f", distancia)
                        
                        // Dibujar los puntos inicialmente
                        dibujarPuntosIniciales()
                        
                        // Cambiar el texto del botón y su funcionalidad
                        binding.btnaccion.text = "Crear Curva Bézier"
                        binding.btnaccion.setOnClickListener {
                            crearCurvaBezierParaRutaGuardada()
                        }
                    }
                }
            }
        }
    }
    
    private fun dibujarPuntosIniciales() {
        val bitmap = createBitmap(binding.myImg.width, binding.myImg.height)
        val canvas = Canvas(bitmap)
        
        // Fondo blanco en lugar de verde
        val paintFondo = Paint().apply { color = Color.WHITE }
        canvas.drawRect(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat(), paintFondo)
        
        val paintPuntos = Paint().apply {
            color = Color.BLUE
            style = Paint.Style.FILL
        }
        
        val paintTexto = Paint().apply {
            color = Color.BLACK
            textSize = 30f
            isAntiAlias = true
        }
        
        // Dibujar los puntos numerados
        for (i in puntosTactiles.indices) {
            val p = puntosTactiles[i]
            canvas.drawCircle(p.x.toFloat(), p.y.toFloat(), 12f, paintPuntos)
            canvas.drawText(i.toString(), p.x.toFloat() + 16f, p.y.toFloat() - 8f, paintTexto)
        }
        
        binding.myImg.setImageBitmap(bitmap)
    }
    
    private fun crearCurvaBezierParaRutaGuardada() {
        if (currentMejorRuta != null && currentCoordenadas != null) {
            // Cancelar animación anterior si existe
            rutaAnimator?.cancel()
            
            // Crear nueva animación
            rutaAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
                duration = 2000
                addUpdateListener { anim ->
                    val progress = anim.animatedValue as Float
                    mostrarRutaBezier(currentMejorRuta, currentCoordenadas, progress)
                }
                start()
            }
        }
    }
    
    private fun calcularDistanciaTotal(ruta: IntArray, coordenadas: Array<Punto>): Double {
        var distanciaTotal = 0.0
        for (i in ruta.indices) {
            val actual = coordenadas[ruta[i]]
            val siguiente = coordenadas[ruta[(i + 1) % ruta.size]]
            distanciaTotal += Math.sqrt(
                Math.pow((siguiente.x - actual.x).toDouble(), 2.0) +
                Math.pow((siguiente.y - actual.y).toDouble(), 2.0)
            )
        }
        return distanciaTotal
    }
    
    private fun resetearModoNormal() {
        // Limpiar datos de ruta guardada
        currentMejorRuta = null
        currentCoordenadas = null
        currentMejorCamino = null
        
        // Limpiar el Intent para evitar conflictos
        intent.removeExtra("mostrar_ruta_guardada")
        intent.removeExtra("ruta_seleccionada")
        
        // Resetear el botón al modo normal
        binding.btnaccion.text = "Crear"
        binding.btnaccion.setOnClickListener {
            if (puntosTactiles.size <= 10) {
                Toast.makeText(this, "Debes ingresar al menos 11 puntos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val numciudades = puntosTactiles.size
            val tampoblacion = binding.txttampoblacion.text.toString().toInt()
            val probMutacion = binding.txtprobabilidadMutacion.text.toString().toDouble()
            val numGeneraciones = binding.txtnumgenraciones.text.toString().toInt()

            // Algoritmo genético con los puntos tocados
            var poblacion = Array(tampoblacion) { Individuo(numciudades) }
            val coordenadas = puntosTactiles.toTypedArray()

            poblacion = calcular_aptitud(poblacion, coordenadas)

            for (t in 0..numGeneraciones) {
                val seleccionados = seleccion_torneo(poblacion)
                for (i in 0 until seleccionados.size step 2) {
                    val padre1 = seleccionados[i]
                    val padre2 = seleccionados[i + 1]
                    val hijo1 = mutar(cruzar(padre1, padre2), probMutacion)
                    val hijo2 = mutar(cruzar(padre2, padre1), probMutacion)
                    poblacion[i] = hijo1
                    poblacion[i + 1] = hijo2
                }
                poblacion = calcular_aptitud(poblacion, coordenadas)
            }

            val mejor = poblacion[0]
            binding.lblmejordistancia.text =
                "${mejor.distancia} => ${mejor.get_camino()}"

            currentMejorRuta = mejor.cromosoma
            currentCoordenadas = coordenadas
            val mejorIndividuo = poblacion[0]

            rutaAnimator?.cancel()
            rutaAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
                duration = 2000
                addUpdateListener { anim ->
                    val progress = anim.animatedValue as Float
                    mostrarRutaBezier(currentMejorRuta, currentCoordenadas, progress)
                }
                start()
            }

            currentMejorCamino = mejorIndividuo.get_camino()
        }
        
        // Limpiar el label de resultado
        binding.lblmejordistancia.text = ""
    }






}
