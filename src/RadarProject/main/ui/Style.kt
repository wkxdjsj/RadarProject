package main.ui

import com.badlogic.gdx.graphics.Color
import main.mapWidth

const val initialWindowWidth = 1000f
const val windowToMapUnit = mapWidth / initialWindowWidth
const val runSpeed = 6.3 * 100 //6.3m/s
const val visionRadius = mapWidth / 4
const val directionRadius = 16000f
const val pinRadius = 4000f
const val healthBarWidth = 15000f
const val healthBarHeight = 2000f
const val playerRadius = 4000f
const val mapMarkerScale=300f
const val corpseRadius = 150f
const val itemRadius = 100f
const val itemScale = 30f
const val staticItemScale = 300f

const val aimLineWidth=1000f
const val aimLineRange=50000f
const val aimCircleRadius=200f
const val aimTimeThreshold=1000
const val attackLineDuration=1000
const val attackMeLineDuration=10000
const val firingLineDuration = 500
const val firingLineLength = 20000f


//Aordp
const val airDropRadius = 4000f
const val airDropTimeDuration = 5000f

//Player Edge
val playerEdgeColor = Color(0.9f, 0.1f, 0.1f, 0.8f)
val teamEdgeColor = Color(1f, 1f, 0f, 0.8f)  //yello

val firingLineColor = Color(1.0f,1.0f,1.0f,0.5f)
val safeDirectionColor = Color(0.12f, 0.56f, 1f, 0.5f)
val visionColor = Color(1f, 1f, 1f, 0.1f)
val attackLineColor = Color(1.0f, 0f, 0f, 1f)
val pinColor = Color(1f, 1f, 0f, 1f)
val redZoneColor = Color(1f, 0f, 0f, 0.2f)
val safeZoneColor = Color(1f, 1f, 1f, 0.5f)
val selfColor = Color(0x32cd32ff)
val bgColor = Color(0.417f, 0.417f, 0.417f, 1f)