package com.example.mykalgoritmogenetico

import kotlin.math.sqrt

class Individuo {
    var num_ciudades =0
    var distancia:Int = 0 // aptitud
    var camino:String = ""
    var cromosoma:IntArray = IntArray(num_ciudades) { it }

    constructor(num_ciudades: Int) {
        this.num_ciudades = num_ciudades
        this.cromosoma = IntArray(num_ciudades) { it }
        this.cromosoma.shuffle()
        for (i in (0 until this.cromosoma.size)) {
            this.camino = this.camino + " - " +this.cromosoma[i].toString()
        }
    }
    fun pitagoras(p1:Punto,p2:Punto):Int{
        val x1:Int=p1.x
        val y1:Int=p1.y
        val x2:Int=p2.x
        val y2:Int=p2.y
        return sqrt(((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2)).toDouble()).toInt()
    }
    fun get_distancia(D:Array<Punto>):Int{
        this.distancia = 0
        for(i in (0 until D.size-1)){
            this.distancia = this.distancia + pitagoras(D[cromosoma[i]],D[cromosoma[i+1]])
        }
        return this.distancia
    }
    fun get_camino():String{
        return this.camino//"3-1-4-2-0"
    }
}