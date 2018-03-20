@file:Suppress("NAME_SHADOWING")

package main.ui


import Item.Companion.order
import com.badlogic.gdx.ApplicationListener
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Buttons.LEFT
import com.badlogic.gdx.Input.Buttons.MIDDLE
import com.badlogic.gdx.Input.Buttons.RIGHT
import com.badlogic.gdx.Input.Keys.*
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Color.*
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.GL20.GL_TEXTURE_2D
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.Texture.TextureFilter.Linear
import com.badlogic.gdx.graphics.Texture.TextureFilter.MipMap
import com.badlogic.gdx.graphics.g2d.*
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.DEFAULT_CHARS
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Line
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import main.*
import main.deserializer.channel.ActorChannel.Companion.actorHasWeapons
import main.deserializer.channel.ActorChannel.Companion.actors
import main.deserializer.channel.ActorChannel.Companion.airDropLocation
import main.deserializer.channel.ActorChannel.Companion.corpseLocation
import main.deserializer.channel.ActorChannel.Companion.droppedItemLocation
import main.deserializer.channel.ActorChannel.Companion.firing
import main.deserializer.channel.ActorChannel.Companion.visualActors
import main.deserializer.channel.ActorChannel.Companion.weapons
import main.struct.Archetype.*
import main.struct.cmd.ActorCMD.actorHealth
import main.struct.cmd.ActorCMD.actorWithPlayerState
import main.struct.cmd.ActorCMD.playerStateToActor
import main.struct.cmd.GameStateCMD.ElapsedWarningDuration
import main.struct.cmd.GameStateCMD.IsTeamMatch
import main.struct.cmd.GameStateCMD.NumAlivePlayers
import main.struct.cmd.GameStateCMD.NumAliveTeams
import main.struct.cmd.GameStateCMD.PoisonGasWarningPosition
import main.struct.cmd.GameStateCMD.PoisonGasWarningRadius
import main.struct.cmd.GameStateCMD.RedZonePosition
import main.struct.cmd.GameStateCMD.RedZoneRadius
import main.struct.cmd.GameStateCMD.SafetyZonePosition
import main.struct.cmd.GameStateCMD.SafetyZoneRadius
import main.struct.cmd.GameStateCMD.TotalWarningDuration
import main.struct.cmd.PlayerStateCMD.attacks
import main.struct.cmd.PlayerStateCMD.selfID
import main.struct.cmd.PlayerStateCMD.selfStateID
import main.deserializer.channel.ActorChannel.Companion.teams
import main.struct.*
import main.struct.PlayerState
import main.struct.Team
import main.struct.Weapon

import main.struct.cmd.selfAttachTo
import main.struct.cmd.selfCoords
import main.struct.cmd.selfDirection
import main.util.tuple3
import main.util.tuple4
import org.lwjgl.opengl.GL11.GL_TEXTURE_BORDER_COLOR
import org.lwjgl.opengl.GL11.glTexParameterfv
import org.lwjgl.opengl.GL13.GL_CLAMP_TO_BORDER
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.pow

val itemIcons = HashMap<String, TextureAtlas.AtlasRegion>()
typealias renderInfo = tuple4<Actor,Float,Float,Float>

class GLMap : InputAdapter(), ApplicationListener, GameListener {
    companion object {
        operator fun Vector3.component1(): Float = x
        operator fun Vector3.component2(): Float = y
        operator fun Vector3.component3(): Float = z
        operator fun Vector2.component1(): Float = x
        operator fun Vector2.component2(): Float = y
    }

    init {
        register(this)
    }


    override fun onGameStart() {
        //preSelfCoords.set(if (isErangel) spawnErangel else spawnDesert)
        // selfCoords.set(preSelfCoords)
        //preDirection.setZero()
        selfCoords.setZero()
        selfAttachTo = null
    }

    override fun onGameOver() {
        camera.zoom = 2 / 4f

        aimStartTime.clear()
        attackLineStartTime.clear()
        pinLocation.setZero()
    }

    fun show() {
        val config = Lwjgl3ApplicationConfiguration()
        config.setTitle("")
        config.useOpenGL3(false, 2, 1)
        config.setWindowedMode(800, 800)
        config.setResizable(true)
        config.useVsync(false)
        config.setIdleFPS(120)
        config.setBackBufferConfig(4, 4, 4, 4, 16, 4, 8)
        Lwjgl3Application(this, config)

    }


    private lateinit var spriteBatch: SpriteBatch
    private lateinit var shapeRenderer: ShapeRenderer
    lateinit var mapErangel: Texture
    lateinit var mapMiramar: Texture

    private lateinit var DaMap: Texture
    private lateinit var iconImages: Icons
    private lateinit var corpseboximage: Texture
    private lateinit var AirDropAllTheColors: Texture
    private lateinit var bgcompass: Texture
    private lateinit var menu: Texture
    private lateinit var largeFont: BitmapFont
    private lateinit var littleFont: BitmapFont
    private lateinit var nameFont: BitmapFont
    private lateinit var itemFont: BitmapFont
    private lateinit var hporange: BitmapFont
    private lateinit var hpred: BitmapFont
    private lateinit var hpgreen: BitmapFont
    private lateinit var menuFont: BitmapFont
    private lateinit var menuFontOn: BitmapFont
    private lateinit var menuFontOFF: BitmapFont
    private lateinit var fontCamera: OrthographicCamera
    private lateinit var itemCamera: OrthographicCamera
    private lateinit var camera: OrthographicCamera
    private lateinit var alarmSound: Sound
    private lateinit var hubpanel: Texture
    private lateinit var hubpanelblank: Texture
    private lateinit var vehicle: Texture
    private lateinit var boato: Texture
    private lateinit var teamarrow: Texture
    lateinit var itemAtlas: TextureAtlas
    lateinit var pawnAtlas:TextureAtlas
    lateinit var markers:Array<TextureRegion>


    private lateinit var vano: Texture
    private lateinit var vehicleo: Texture
    private lateinit var jetskio: Texture
    private lateinit var plane: Texture
    private lateinit var boat: Texture
    private lateinit var BikeBLUE: Texture
    private lateinit var BikeRED: Texture
    private lateinit var Bike3BLUE: Texture
    private lateinit var Bike3RED: Texture
    private lateinit var pickupBLUE: Texture
    private lateinit var pickupRED: Texture
    private lateinit var BuggyBLUE: Texture
    private lateinit var BuggyRED: Texture
    private lateinit var van: Texture
    private lateinit var arrow: Texture
    private lateinit var arrowsight: Texture
    private lateinit var jetski: Texture
    private lateinit var player: Texture
    private lateinit var playersight: Texture
    private lateinit var teamsight: Texture
    private lateinit var parachute: Texture
    private lateinit var grenade: Texture
    private lateinit var hubFont: BitmapFont
    private lateinit var hubFontShadow: BitmapFont
    private lateinit var espFont: BitmapFont
    private lateinit var espFontShadow: BitmapFont
    private lateinit var compaseFont: BitmapFont
    private lateinit var compaseFontShadow: BitmapFont
    private lateinit var littleFontShadow: BitmapFont

    private val layout = GlyphLayout()
    private var windowWidth = initialWindowWidth
    private var windowHeight = initialWindowWidth

    private val aimStartTime = HashMap<NetworkGUID, Long>()
    private val firingStartTime = LinkedList<tuple4<Float,Float,Float,Long>>()
    private val attackLineStartTime = LinkedList<Triple<NetworkGUID, NetworkGUID, Long>>()
    private val pinLocation = Vector2()
    // Menu Settings
    //////////////////////////////
    private var filterWeapon = -1
    private var filterAttach = -1
    private var filterArmorBag = 1
    private var filterLvl2 = -1
    private var filterLvl3 = 1
    private var filterScope = -1
    private var filterHeals = -1
    private var filterAmmo = 1
    private var filterThrow = 1
    private var drawcompass = -1
    private var drawmenu = 1
    private var toggleView = -1
    // private var toggleVehicles = -1
    //  private var toggleVNames = -1
    private var drawgrid = -1
    private var airdroplines = 1
    private var togglesafezone = 1
    private var nameToggles = 4
    private var VehicleInfoToggles = 1
    private var ZoomToggles = 1
    ///////////////////////////
    private var scopesToFilter = arrayListOf("")
    private var weaponsToFilter = arrayListOf("")
    private var attachToFilter = arrayListOf("")
    private var level2Filter = arrayListOf("")
    private var level3Filter = arrayListOf("")
    private var level23Filter = arrayListOf("")
    private var healsToFilter = arrayListOf("")
    private var ammoToFilter = arrayListOf("")
    private var throwToFilter = arrayListOf("")
    private var dragging = false
    private var prevScreenX = -1f
    private var prevScreenY = -1f
    private var screenOffsetX = 0f
    private var screenOffsetY = 0f
	private var menuw = 0f
	private var menuh = 0f


    private fun windowToMap(x: Float, y: Float) =
            Vector2(selfCoords.x + (x - windowWidth / 2.0f) * camera.zoom * windowToMapUnit + screenOffsetX,
                    selfCoords.y + (y - windowHeight / 2.0f) * camera.zoom * windowToMapUnit + screenOffsetY)

    private fun mapToWindow(x: Float, y: Float) =
            Vector2((x - selfCoords.x - screenOffsetX) / (camera.zoom * windowToMapUnit) + windowWidth / 2.0f,
                    (y - selfCoords.y - screenOffsetY) / (camera.zoom * windowToMapUnit) + windowHeight / 2.0f)

    fun Vector2.mapToWindow() = mapToWindow(x, y)
    fun Vector2.windowToMap() = windowToMap(x, y)


    override fun scrolled(amount: Int): Boolean {

        if (camera.zoom >= 0.01f && camera.zoom <= 1f) {
            camera.zoom *= 1.05f.pow(amount)
        } else {
            if (camera.zoom < 0.01f) {
                camera.zoom = 0.01f
                println("Max Zoom")
            }
            if (camera.zoom > 1f) {
                camera.zoom = 1f
                println("Min Zoom")
            }
        }

        return true
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        when (button) {
            RIGHT -> {
                pinLocation.set(pinLocation.set(screenX.toFloat(), screenY.toFloat()).windowToMap())
                camera.update()
                println(pinLocation)
                return true
            }
            LEFT -> {
                dragging = true
                prevScreenX = screenX.toFloat()
                prevScreenY = screenY.toFloat()
                return true
            }
            MIDDLE -> {
                screenOffsetX = 0f
                screenOffsetY = 0f
            }
        }
        return false
    }

    override fun keyDown(keycode: Int): Boolean {

        when (keycode) {

        // Change Player Info
            F1 -> {
                if (nameToggles < 5) {
                    nameToggles += 1
                }
                if (nameToggles == 5) {
                    nameToggles = 0
                }
            }
			
        // Other Filter Keybinds
            F2 -> drawcompass = drawcompass * -1
            F3 -> drawgrid = drawgrid * -1
        
		// Toggle View Line
            F4 -> toggleView = toggleView * -1
        
		// Toggle Vehicle Info
			F5 -> {
                if (VehicleInfoToggles <= 4) {
                    VehicleInfoToggles += 1
                }
                if (VehicleInfoToggles == 4) {
                    VehicleInfoToggles = 1
                }
            }
			// Line
			F6 -> togglesafezone = togglesafezone * -1
            F7 -> airdroplines = airdroplines * -1

        // Toggle Menu5
            F12 -> drawmenu = drawmenu * -1

        // Icon Filter Keybinds
            NUMPAD_1 -> filterWeapon = filterWeapon * -1
            NUMPAD_2 -> filterLvl2 = filterLvl2 * -1
            NUMPAD_3 -> filterHeals = filterHeals * -1
            NUMPAD_4 -> filterAttach = filterAttach * -1
            NUMPAD_5 -> filterScope = filterScope * -1
            NUMPAD_6 -> filterAmmo = filterAmmo * -1
            NUMPAD_0 -> filterThrow = filterThrow * -1

        // Zoom (Loot, Combat, Scout)
            NUMPAD_8 -> {
                if (ZoomToggles <= 4) {
                    ZoomToggles += 1
                }
                if (ZoomToggles == 4) {
                    ZoomToggles = 1
                }
                // then
                if (ZoomToggles == 1) {
                    camera.zoom = 1 / 8f
                }
                // or
                if (ZoomToggles == 2) {
                    camera.zoom = 1 / 12f
                }
                // or
                if (ZoomToggles == 3) {
                    camera.zoom = 1 / 24f
                }
            }
			
        // Level 2 & 3 Toggle
/*
           F9 -> {
                    if (filterArmorBag <= 4) {
                        filterArmorBag += 1
                    }
                    if (filterArmorBag == 4) {
                        filterArmorBag = 1
                    }
                    // then
                    if (filterArmorBag == 1) {
                        filterLvl3 = 1
                    }
                    // or
                    if (filterArmorBag == 2) {
                        filterLvl2 = 1
                    }
                    // or
                    if (filterArmorBag == 3) {
                        //both?
                        filterLvl23 = 1
                    }
           }
*/

        // Zoom In/Out || Overrides Max/Min Zoom
            MINUS -> camera.zoom = camera.zoom + 0.00525f
            PLUS -> camera.zoom = camera.zoom - 0.00525f
        // lol
				
		// Move the menu
			A -> menuw = menuw - 10f
			D -> menuw = menuw + 10f
			W -> menuh = menuh + 10f
			S -> menuh = menuh - 10f
        }
        return false
   }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        if (!dragging) return false
        with(camera) {
            screenOffsetX += (prevScreenX - screenX.toFloat()) * camera.zoom * 500
            screenOffsetY += (prevScreenY - screenY.toFloat()) * camera.zoom * 500
            prevScreenX = screenX.toFloat()
            prevScreenY = screenY.toFloat()
        }
        return true
    }


    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        if (button == LEFT) {
            dragging = false
            return true
        }
        return false
    }

    override fun create() {
        spriteBatch = SpriteBatch()
        shapeRenderer = ShapeRenderer()
        Gdx.input.inputProcessor = this
        camera = OrthographicCamera(windowWidth, windowHeight)
        with(camera) {
            setToOrtho(true, windowWidth * windowToMapUnit, windowHeight * windowToMapUnit)
            zoom = 1 / 4f
            update()
            position.set(mapWidth / 2, mapWidth / 2, 0f)
            update()
        }

        itemCamera = OrthographicCamera(initialWindowWidth, initialWindowWidth)
        fontCamera = OrthographicCamera(initialWindowWidth, initialWindowWidth)
        alarmSound = Gdx.audio.newSound(Gdx.files.internal("sounds/Alarm.wav"))
        hubpanel = Texture(Gdx.files.internal("images/hub_panel.png"))
        bgcompass = Texture(Gdx.files.internal("images/bg_compass.png"))
        menu = Texture(Gdx.files.internal("images/menu.png"))
        hubpanelblank = Texture(Gdx.files.internal("images/hub_panel_blank_long.png"))
        corpseboximage = Texture(Gdx.files.internal("icons/box.png"))
        AirDropAllTheColors = Texture(Gdx.files.internal("icons/AirDropAllTheColors.png"))
        vehicle = Texture(Gdx.files.internal("images/vehicle.png"))
        vehicleo = Texture(Gdx.files.internal("images/vehicleo.png"))
        arrow = Texture(Gdx.files.internal("images/arrow.png"))
        plane = Texture(Gdx.files.internal("images/plane.png"))
        player = Texture(Gdx.files.internal("images/player.png"))
        playersight = Texture(Gdx.files.internal("images/green_view_line.png"))
        teamsight = Texture(Gdx.files.internal("images/teamsight.png"))
        arrowsight = Texture(Gdx.files.internal("images/red_view_line.png"))
        teamarrow = Texture(Gdx.files.internal("images/team.png"))
        parachute = Texture(Gdx.files.internal("images/parachute.png"))
        boat = Texture(Gdx.files.internal("images/boat.png"))
        boato = Texture(Gdx.files.internal("images/boato.png"))
        BikeBLUE = Texture(Gdx.files.internal("images/BikeBLUE.png"))
        BikeRED = Texture(Gdx.files.internal("images/BikeRED.png"))
        Bike3BLUE = Texture(Gdx.files.internal("images/Bike3BLUE.png"))
        Bike3RED = Texture(Gdx.files.internal("images/Bike3RED.png"))
        pickupBLUE = Texture(Gdx.files.internal("images/pickupBLUE.png"))
        pickupRED = Texture(Gdx.files.internal("images/pickupRED.png"))
        BuggyBLUE = Texture(Gdx.files.internal("images/BuggyBLUE.png"))
        BuggyRED = Texture(Gdx.files.internal("images/BuggyRED.png"))

        jetski = Texture(Gdx.files.internal("images/jetski.png"))
        jetskio = Texture(Gdx.files.internal("images/jetskio.png"))
        van = Texture(Gdx.files.internal("images/van.png"))
        vano = Texture(Gdx.files.internal("images/vano.png"))

        grenade = Texture(Gdx.files.internal("images/grenade.png"))
        iconImages = Icons(Texture(Gdx.files.internal("images/item-sprites.png")), 64)


        itemAtlas = TextureAtlas(Gdx.files.internal("icons/itemIcons.txt"))
        for (region in itemAtlas.regions)
            itemIcons[region.name] = region.apply { flip(false, false) }

        pawnAtlas = TextureAtlas(Gdx.files.internal("icons/Markers.txt"))
        for (region in pawnAtlas.regions)
            region.flip(false,true)

        markers = arrayOf(pawnAtlas.findRegion("marker1"),pawnAtlas.findRegion("marker2"),
                pawnAtlas.findRegion("marker3"),pawnAtlas.findRegion("marker4"))



        glTexParameterfv(GL_TEXTURE_2D, GL_TEXTURE_BORDER_COLOR, floatArrayOf(bgColor.r, bgColor.g, bgColor.b, bgColor.a))
        mapErangel = Texture(Gdx.files.internal("maps/Erangel.png"), null, true).apply {
            setFilter(MipMap, Linear)
            Gdx.gl.glTexParameterf(glTarget, GL20.GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER.toFloat())
            Gdx.gl.glTexParameterf(glTarget, GL20.GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER.toFloat())
        }
        mapMiramar = Texture(Gdx.files.internal("maps/Miramar.png"), null, true).apply {
            setFilter(MipMap, Linear)
            Gdx.gl.glTexParameterf(glTarget, GL20.GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER.toFloat())
            Gdx.gl.glTexParameterf(glTarget, GL20.GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER.toFloat())
        }

        val generatorHub = FreeTypeFontGenerator(Gdx.files.internal("font/AGENCYFB.TTF"))
        val paramHub = FreeTypeFontParameter()
        paramHub.characters = DEFAULT_CHARS
        paramHub.size = 30
        paramHub.color = WHITE
        hubFont = generatorHub.generateFont(paramHub)
        paramHub.color = Color(1f, 1f, 1f, 0.4f)
        hubFontShadow = generatorHub.generateFont(paramHub)
        paramHub.size = 16
        paramHub.color = WHITE
        espFont = generatorHub.generateFont(paramHub)
        paramHub.color = Color(1f, 1f, 1f, 0.2f)
        espFontShadow = generatorHub.generateFont(paramHub)
        val generatorNumber = FreeTypeFontGenerator(Gdx.files.internal("font/NUMBER.TTF"))
        val paramNumber = FreeTypeFontParameter()
        paramNumber.characters = DEFAULT_CHARS
        paramNumber.size = 24
        paramNumber.color = WHITE
        largeFont = generatorNumber.generateFont(paramNumber)
        val generator = FreeTypeFontGenerator(Gdx.files.internal("font/GOTHICB.TTF"))
        val param = FreeTypeFontParameter()
        param.characters = DEFAULT_CHARS
        param.size = 38
        param.color = WHITE
        largeFont = generator.generateFont(param)
        param.size = 15
        param.color = WHITE
        littleFont = generator.generateFont(param)
        param.color = BLACK
        param.size = 10
        nameFont = generator.generateFont(param)
        param.color = WHITE
        param.size = 6
        itemFont = generator.generateFont(param)
        val compaseColor = Color(0f, 0.95f, 1f, 1f)  //Turquoise1
        param.color = compaseColor
        param.size = 10
        compaseFont = generator.generateFont(param)
        param.color = Color(0f, 0f, 0f, 0.5f)
        compaseFontShadow = generator.generateFont(param)
        param.characters = DEFAULT_CHARS
        param.size = 20
        param.color = WHITE
        littleFont = generator.generateFont(param)
        param.color = Color(0f, 0f, 0f, 0.5f)
        littleFontShadow = generator.generateFont(param)
        param.color = WHITE
        param.size = 12
        menuFont = generator.generateFont(param)
        param.color = GREEN
        param.size = 12
        menuFontOn = generator.generateFont(param)
        param.color = RED
        param.size = 12
        menuFontOFF = generator.generateFont(param)
        param.color = ORANGE
        param.size = 10
        hporange = generator.generateFont(param)
        param.color = GREEN
        param.size = 10
        hpgreen = generator.generateFont(param)
        param.color = RED
        param.size = 10
        hpred = generator.generateFont(param)


        generatorHub.dispose()
        generatorNumber.dispose()
        generator.dispose()
    }
    private val dirUnitVector = Vector2(1f,0f)
    override fun render() {
        Gdx.gl.glClearColor(bgColor.r, bgColor.g, bgColor.b, bgColor.a)
        Gdx.gl.glClearColor(0.417f, 0.417f, 0.417f, 0f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        if (gameStarted)
            DaMap = if (isErangel) mapErangel else mapMiramar
        else return
        val currentTime = System.currentTimeMillis()
        // Maybe not needed, could be draw error

        actors[selfID]?.apply {
            actors[attachParent ?: return@apply]?.apply {
                selfCoords.set(location.x, location.y)
                selfDirection = rotation.y
            }
        }

        val (selfX, selfY) = selfCoords

        //val selfDir = Vector2(selfX, selfY).sub(preSelfCoords)
        //if (selfDir.len() < 1e-8)
        //  selfDir.set(preDirection)

        //move camera
        camera.position.set(selfX + screenOffsetX, selfY + screenOffsetY, 0f)
        camera.update()




        paint(camera.combined) {

            draw(DaMap, 0f, 0f, mapWidth, mapWidth,
                    0, 0, DaMap.width, DaMap.height,
                    false, true)

        }

        shapeRenderer.projectionMatrix = camera.combined
        Gdx.gl.glEnable(GL20.GL_BLEND)

        drawCircles()
        drawAttackLine(currentTime)

        val typeLocation = EnumMap<Archetype, MutableList<renderInfo>>(Archetype::class.java)
        for ((_, actor) in visualActors)
            typeLocation.compute(actor.type) { _, v ->
                val list = v ?: ArrayList()
                val (centerX, centerY) = actor.location
                val direction = actor.rotation.y
                list.add(tuple4(actor, centerX, centerY, direction))
                list
            }


        // val zero = numKills.toString()
        paint(fontCamera.combined) {

            // NUMBER PANEL
            val numText = "$NumAlivePlayers"
            layout.setText(hubFont, numText)
            spriteBatch.draw(hubpanel, windowWidth - 130f, windowHeight - 60f)
            hubFontShadow.draw(spriteBatch, "ALIVE", windowWidth - 85f, windowHeight - 29f)
            hubFont.draw(spriteBatch, "$NumAlivePlayers", windowWidth - 110f - layout.width / 2, windowHeight - 29f)
            val teamText = "$NumAliveTeams"


            if (IsTeamMatch) {
                layout.setText(hubFont, teamText)
                spriteBatch.draw(hubpanel, windowWidth - 260f, windowHeight - 60f)
                hubFontShadow.draw(spriteBatch, "TEAM", windowWidth - 215f, windowHeight - 29f)
                hubFont.draw(spriteBatch, "$NumAliveTeams", windowWidth - 240f - layout.width / 2, windowHeight - 29f)
            }

            /*
            if (IsTeamMatch) {

                layout.setText(hubFont, zero)
                spriteBatch.draw(hubpanel, windowWidth - 390f, windowHeight - 60f)
                hubFontShadow.draw(spriteBatch, "KILLS", windowWidth - 345f, windowHeight - 29f)
                hubFont.draw(spriteBatch, "$zero", windowWidth - 370f - layout.width / 2, windowHeight - 29f)
            } else {
                spriteBatch.draw(hubpanel, windowWidth - 390f + 130f, windowHeight - 60f)
                hubFontShadow.draw(spriteBatch, "KILLS", windowWidth - 345f + 128f, windowHeight - 29f)
                hubFont.draw(spriteBatch, "$zero", windowWidth - 370f + 128f - layout.width / 2, windowHeight - 29f)

            }
            */

            // ITEM ESP FILTER PANEL
            spriteBatch.draw(hubpanelblank, 30f, windowHeight - 107f)

            // This is what you were trying to do
            if (filterWeapon != 1)
                espFont.draw(spriteBatch, "WEAPON", 40f, windowHeight - 25f)
            else
                espFontShadow.draw(spriteBatch, "WEAPON", 39f, windowHeight - 25f)

            if (filterAttach != 1)
                espFont.draw(spriteBatch, "ATTACH", 40f, windowHeight - 42f)
            else
                espFontShadow.draw(spriteBatch, "ATTACH", 40f, windowHeight - 42f)

            if (filterLvl2 != 1)
                espFont.draw(spriteBatch, "EQUIP", 100f, windowHeight - 25f)
            else
                espFontShadow.draw(spriteBatch, "EQUIP", 100f, windowHeight - 25f)

            if (filterScope != 1)
                espFont.draw(spriteBatch, "SCOPE", 98f, windowHeight - 42f)
            else
                espFontShadow.draw(spriteBatch, "SCOPE", 98f, windowHeight - 42f)

            if (filterHeals != 1)
                espFont.draw(spriteBatch, "MEDS", 150f, windowHeight - 25f)
            else
                espFontShadow.draw(spriteBatch, "MEDS", 150f, windowHeight - 25f)

            if (filterAmmo != 1)
                espFont.draw(spriteBatch, "AMMO", 150f, windowHeight - 42f)
            else
                espFontShadow.draw(spriteBatch, "AMMO", 150f, windowHeight - 42f)
            if (drawcompass == 1)
                espFont.draw(spriteBatch, "COMPASS", 200f, windowHeight - 42f)
            else
                espFontShadow.draw(spriteBatch, "COMPASS", 200f, windowHeight - 42f)
            if (filterThrow != 1)
                espFont.draw(spriteBatch, "THROW", 200f, windowHeight - 25f)
            else
                espFontShadow.draw(spriteBatch, "THROW", 200f, windowHeight - 25f)

            if (drawmenu == 1)
                espFont.draw(spriteBatch, "[F12] Menu ON", 270f, windowHeight - 25f)
            else
                espFontShadow.draw(spriteBatch, "[F12] Menu OFF", 270f, windowHeight - 25f)

            val num = nameToggles
            espFontShadow.draw(spriteBatch, "[F1] Player Info: $num", 270f, windowHeight - 42f)

            val znum = ZoomToggles
            espFontShadow.draw(spriteBatch, "[Num8] Zoom Toggle: $znum", 40f, windowHeight - 68f)

            val vnum = VehicleInfoToggles
            espFontShadow.draw(spriteBatch, "[F5] Vehicle Toggles: $vnum", 40f, windowHeight - 85f)

             val mnum = filterArmorBag
            espFontShadow.draw(spriteBatch, "[F9] Item Armor Toggle: $mnum", 35f, windowHeight - 115f)


            val pinDistance = (pinLocation.cpy().sub(selfX, selfY).len() / 100).toInt()
            val (x, y) = pinLocation.mapToWindow()

            safeZoneHint()
            drawPlayerNames(typeLocation[Player])

            val camnum = camera.zoom

            if (drawmenu == 1) {
                spriteBatch.draw(menu, menuw + 20f, menuh + windowHeight / 2 - 235f)

                // Filters
                if (filterWeapon != 1)
                    menuFontOn.draw(spriteBatch, "Enabled", menuw + 187f, menuh + windowHeight / 2 + 180f)
                else
                    menuFontOFF.draw(spriteBatch, "Disabled", menuw + 187f, menuh + windowHeight / 2 + 180f)

                if (filterLvl2 != 1)
                    menuFontOn.draw(spriteBatch, "Enabled", menuw + 187f, menuh + windowHeight / 2 + 162f)
                else
                    menuFontOFF.draw(spriteBatch, "Disabled", menuw + 187f, menuh + windowHeight / 2 + 162f)

                if (filterHeals != 1)
                    menuFontOn.draw(spriteBatch, "Enabled", menuw + 187f, menuh + windowHeight / 2 + 144f)
                else
                    menuFontOFF.draw(spriteBatch, "Disabled", menuw + 187f, menuh + windowHeight / 2 + 144f)

                if (filterAttach != 1)
                    menuFontOn.draw(spriteBatch, "Enabled", menuw + 187f, menuh + windowHeight / 2 + 126f)
                else
                    menuFontOFF.draw(spriteBatch, "Disabled", menuw + 187f, menuh + windowHeight / 2 + 126f)

                if (filterScope != 1)
                    menuFontOn.draw(spriteBatch, "Enabled", menuw + 187f, menuh + windowHeight / 2 + 108f)
                else
                    menuFontOFF.draw(spriteBatch, "Disabled", menuw + 187f, menuh + windowHeight / 2 + 108f)

                if (filterAmmo != 1)
                    menuFontOn.draw(spriteBatch, "Enabled", menuw + 187f, menuh + windowHeight / 2 + 90f)
                else
                    menuFontOFF.draw(spriteBatch, "Disabled", menuw + 187f, menuh + windowHeight / 2 + 90f)

                 if (filterThrow != 1)
                    menuFontOn.draw(spriteBatch, "Enabled", menuw + 187f, menuh + windowHeight / 2 + 72f)
                else
                    menuFontOFF.draw(spriteBatch, "Disabled", menuw + 187f, menuh + windowHeight / 2 + 72f)
					
					//   Future Function               187f, menuh + windowHeight / 2 + 54f)
					//   Future Function               187f, menuh + windowHeight / 2 + 36f)
					//   Future Function               187f, menuh + windowHeight / 2 + 18f)
					
				val camvalue = camera.zoom
                when {
                    camvalue <= 0.0100f -> menuFontOFF.draw(spriteBatch, "Max Zoom", menuw + 187f, menuh + windowHeight / 2 + 0f)
                    camvalue >= 1f -> menuFontOFF.draw(spriteBatch, "Min Zoom", menuw + 187f, menuh + windowHeight / 2 + 0f)
                    camvalue == 0.2500f -> menuFont.draw(spriteBatch, "Default", menuw + 187f, menuh + windowHeight / 2 + 0f)
                    camvalue == 0.1250f -> menuFont.draw(spriteBatch, "Scouting", menuw + 187f, menuh + windowHeight / 2 + 0f)
                    camvalue == 0.0833f -> menuFont.draw(spriteBatch, "Combat", menuw + 187f, menuh + windowHeight / 2 + 0f)
                    camvalue == 0.0417f -> menuFont.draw(spriteBatch, "Looting", menuw + 187f, menuh + windowHeight / 2 + 0f)

                    else -> menuFont.draw(spriteBatch, ("%.4f").format(camnum), menuw + menuh + 187f, windowHeight / 2 + 0f)
                }

//   Zoom + Variable               menuw + 187f, menuh + windowHeight / 2 + -18f)
//   Zoom - Variable               menuw + 187f, menuh + windowHeight / 2 + -36f)

//   Future Function                 menuw + 187f, menuh + windowHeight / 2 + -54f)
//   Future Function                 menuw + 187f, menuh + windowHeight / 2 + -72f)
				
                // Name Toggles
                val togs = nameToggles
                if (nameToggles != 1)

                    menuFontOn.draw(spriteBatch, "Enabled: $togs", menuw + 187f, menuh + windowHeight / 2 + -90f)
                else
                    menuFontOFF.draw(spriteBatch, "Disabled", menuw + 187f, menuh + windowHeight / 2 + -90f)


                // Compass
                if (drawcompass != 1)

                    menuFontOFF.draw(spriteBatch, "Disabled", menuw + 187f, menuh + windowHeight / 2 + -108f)
                else
                    menuFontOn.draw(spriteBatch, "Enabled", menuw + 187f, menuh + windowHeight / 2 + -108f)

                // Grid
                if (drawgrid == 1)

                    menuFontOn.draw(spriteBatch, "Enabled", menuw + 187f, menuh + windowHeight / 2 + -126f)
                else
                    menuFontOFF.draw(spriteBatch, "Disabled", menuw + 187f, menuh + windowHeight / 2 + -126f)

                if (toggleView == 1)
                    menuFontOn.draw(spriteBatch, "Enabled", menuw + 187f, menuh + windowHeight / 2 + -144f)
                else
                    menuFontOFF.draw(spriteBatch, "Disabled", menuw + 187f, menuh + windowHeight / 2 + -144f)

                if (VehicleInfoToggles < 3)
                    menuFontOn.draw(spriteBatch, "Enabled", menuw + 187f, menuh + windowHeight / 2 + -162f)
                if (VehicleInfoToggles == 3)
                    menuFontOFF.draw(spriteBatch, "Disabled", menuw + 187f, menuh + windowHeight / 2 + -162f)

                if (togglesafezone == 1)

                    menuFontOn.draw(spriteBatch, "Enabled", menuw + 187f, menuh + windowHeight / 2 + -180f)
                else
                    menuFontOFF.draw(spriteBatch, "Disabled", menuw + 187f, menuh + windowHeight / 2 + -180f)

                if (airdroplines == 1)

                    menuFontOn.draw(spriteBatch, "Enabled", menuw + 187f, menuh + windowHeight / 2 + -198f)
                else
                    menuFontOFF.draw(spriteBatch, "Disabled", menuw + 187f, menuh + windowHeight / 2 + -198f)

                // DrawMenu == 1 already
                menuFontOn.draw(spriteBatch, "Enabled", menuw + 187f, menuh + windowHeight / 2 + -216f)
            
			}



            if (drawcompass == 1) {

                spriteBatch.draw(bgcompass, windowWidth / 2 - 168f, windowHeight / 2 - 168f)

                layout.setText(compaseFont, "0")
                compaseFont.draw(spriteBatch, "0", windowWidth / 2 - layout.width / 2, windowHeight / 2 + layout.height + 150)                  // N
                layout.setText(compaseFont, "45")
                compaseFont.draw(spriteBatch, "45", windowWidth / 2 - layout.width / 2 + 104, windowHeight / 2 + layout.height / 2 + 104)          // NE
                layout.setText(compaseFont, "90")
                compaseFont.draw(spriteBatch, "90", windowWidth / 2 - layout.width / 2 + 147, windowHeight / 2 + layout.height / 2)                // E
                layout.setText(compaseFont, "135")
                compaseFont.draw(spriteBatch, "135", windowWidth / 2 - layout.width / 2 + 106, windowHeight / 2 + layout.height / 2 - 106)          // SE
                layout.setText(compaseFont, "180")
                compaseFont.draw(spriteBatch, "180", windowWidth / 2 - layout.width / 2, windowHeight / 2 + layout.height / 2 - 151)                // S
                layout.setText(compaseFont, "225")
                compaseFont.draw(spriteBatch, "225", windowWidth / 2 - layout.width / 2 - 109, windowHeight / 2 + layout.height / 2 - 109)          // SW
                layout.setText(compaseFont, "270")
                compaseFont.draw(spriteBatch, "270", windowWidth / 2 - layout.width / 2 - 153, windowHeight / 2 + layout.height / 2)                // W
                layout.setText(compaseFont, "315")
                compaseFont.draw(spriteBatch, "315", windowWidth / 2 - layout.width / 2 - 106, windowHeight / 2 + layout.height / 2 + 106)          // NW
            }
            littleFont.draw(spriteBatch, "$pinDistance", x, windowHeight - y)


        }

        if (drawgrid == 1) {
            drawGrid()

        }

        drawMapMarkers()

        // This makes the array empty if the filter is off for performance with an inverted function since arrays are expensive
        scopesToFilter = if (filterScope != 1) {
            arrayListOf("")
        } else {
            arrayListOf(
                    "Item_Attach_Weapon_Upper_Holosight_C",
                    "Item_Attach_Weapon_Upper_DotSight_01_C",
                    "Item_Attach_Weapon_Upper_Aimpoint_C",
                    "Item_Attach_Weapon_Upper_CQBSS_C",
                    "Item_Attach_Weapon_Upper_ACOG_01_C")
        }


        attachToFilter = if (filterAttach != 1) {
            arrayListOf("")
        } else {
            arrayListOf(
                    "Item_Attach_Weapon_Magazine_ExtendedQuickDraw_SniperRifle_C",
                    "Item_Attach_Weapon_Magazine_Extended_SniperRifle_C",
                    "Item_Attach_Weapon_Magazine_ExtendedQuickDraw_Large_C",
                    "Item_Attach_Weapon_Magazine_Extended_Large_C",
                    "Item_Attach_Weapon_Stock_SniperRifle_CheekPad_C",
                    "Item_Attach_Weapon_Stock_SniperRifle_BulletLoops_C",
                    "Item_Attach_Weapon_Stock_AR_Composite_C",
                    "Item_Attach_Weapon_Muzzle_Suppressor_SniperRifle_C",
                    "Item_Attach_Weapon_Muzzle_Suppressor_Large_C",
                    "Item_Attach_Weapon_Muzzle_Suppressor_Medium_C",
                    "Item_Attach_Weapon_Muzzle_FlashHider_Medium_C",
                    "Item_Attach_Weapon_Magazine_ExtendedQuickDraw_Medium_C",
                    "Item_Attach_Weapon_Magazine_Extended_Medium_C",
                    "Item_Attach_Weapon_Muzzle_FlashHider_Large_C",
                    "Item_Attach_Weapon_Muzzle_Compensator_Medium_C",
                    "Item_Attach_Weapon_Lower_Foregrip_C",
                    "Item_Attach_Weapon_Lower_AngledForeGrip_C")
        }

        weaponsToFilter = if (filterWeapon != 1) {
            arrayListOf("")
        } else {
            arrayListOf(
                    "Item_Weapon_AWM_C",
                    "Item_Weapon_M24_C",
                    "Item_Weapon_Kar98k_C",
                    "Item_Weapon_AUG_C",
                    "Item_Weapon_M249_C",
                    "Item_Weapon_Mk14_C",
                    "Item_Weapon_Groza_C",
                    "Item_Weapon_HK416_C",
                    "Item_Weapon_SCAR-L_C",
                    "Item_Weapon_Mini14_C",
                    "Item_Weapon_M16A4_C",
                    "Item_Weapon_SKS_C",
                    "Item_Weapon_AK47_C",
                    "Item_Weapon_DP28_C",
                    "Item_Weapon_UMP_C")
        }

        healsToFilter = if (filterHeals != 1) {
            arrayListOf("")
        } else {
            arrayListOf(
                    "Item_Heal_Bandage_C",
                    "Item_Heal_MedKit_C",
                    "Item_Heal_FirstAid_C",
                    "Item_Boost_PainKiller_C",
                    "Item_Boost_EnergyDrink_C",
                    "Item_Boost_AdrenalineSyringe_C")
        }

        ammoToFilter = if (filterAmmo != 1) {
            arrayListOf("")
        } else {
            arrayListOf(
                    "Item_Ammo_762mm_C",
                    "Item_Ammo_556mm_C",
                    "Item_Ammo_300Magnum_C",
                    "Item_Weapon_Pan_C",
                    "Item_Ammo_9mm_C",
                    "Item_Ammo_45ACP_C",
                    "Item_Ammo_12Guage_C")
        }

        throwToFilter = if (filterThrow != 1) {
            arrayListOf("")
        } else {
            arrayListOf(
                    "Item_Weapon_Grenade_C",
                    "Item_Weapon_FlashBang_C",
                    "Item_Weapon_SmokeBomb_C",
                    "Item_Weapon_Molotov_C")
        }

        level2Filter = if (filterLvl2 != 1) {
            arrayListOf("")
        } else {
            arrayListOf(
                    "Item_Armor_D_01_Lv2_C",
                    "Item_Armor_C_01_Lv3_C",
                    "Item_Head_G_01_Lv3_C",
                    "Item_Head_F_02_Lv2_C",
                    "Item_Head_F_01_Lv2_C",
                    "Item_Back_C_02_Lv3_C",
                    "Item_Back_C_01_Lv3_C",
                    "Item_Back_F_01_Lv2_C",
                    "Item_Back_F_02_Lv2_C",
                    "Item_Back_E_01_Lv1_C",
                    "Item_Armor_E_01_Lv1_C",
                    "Item_Head_E_01_Lv1_C",
                    "Item_Back_E_02_Lv1_C",
                    "Item_Head_E_02_Lv1_C")
        }

        val Crateitems = arrayListOf("Item_Weapon_AUG_C",
                "Item_Weapon_M24_C",
                "Item_Weapon_M249_C",
                "Item_Weapon_Groza_C",
                "Item_Weapon_AWM_C")

        paint(itemCamera.combined) {
            //Draw Corpse Icon
            corpseLocation.values.forEach {
                val (x, y) = it
                val (sx, sy) = Vector2(x + 16, y - 16).mapToWindow()
                val syFix = windowHeight - sy
                val iconScale = 2f / camera.zoom
                spriteBatch.draw(corpseboximage, sx - iconScale / 2, syFix + iconScale / 2, iconScale, -iconScale,
                        0, 0, 128, 128,
                        false, true)
            }
            //Draw Airdrop Icon
            airDropLocation.values.forEach {

                val (x, y) = it
                val (sx, sy) = Vector2(x, y).mapToWindow()
                val syFix = windowHeight - sy
                val iconScale = 4f / camera.zoom
                spriteBatch.draw(AirDropAllTheColors, sx - iconScale / 2, syFix + iconScale / 2, iconScale, -iconScale,
                        0, 0, 64, 64,
                        false, true)
            }

            val iconScale = 2f / camera.zoom

            val sorted = ArrayList(droppedItemLocation.values)
            sorted.sortBy {
                order[it._2]
            }
            sorted.forEach {
                val (x, y) = it._1
                val items = it._2
                val icon = itemIcons[items]
                val (sx, sy) = Vector2(x, y).mapToWindow()
                val syFix = windowHeight - sy


                items.forEach {
                    if (items in Crateitems) {
                        //println("Items: ${items}")
                        hpgreen.draw(spriteBatch,"$items", sx - itemScale / 2, syFix - itemScale / 2)
                        draw(icon, sx - itemScale / 2, syFix - itemScale / 2, itemScale, itemScale)
                    }

                    if ((items !in weaponsToFilter && items !in scopesToFilter && items !in attachToFilter && items !in level2Filter
                                    && items !in ammoToFilter && items !in healsToFilter) && items !in throwToFilter
                            && iconScale > 20 && sx > 0 && sx < windowWidth && syFix > 0 && syFix < windowHeight) {

                        draw(icon, sx - itemScale / 2, syFix - itemScale / 2, itemScale, itemScale)
                        //println("Items: ${items}")
                    }

              }
          }

            drawMyself(tuple4(null, selfX, selfY, selfDirection))
            drawPawns(typeLocation)

        }

        Gdx.gl.glEnable(GL20.GL_BLEND)
        if (airdroplines == 1) {
            draw(Line) {
                airDropLocation.values.forEach {
                    val (x, y) = it
                    val airdropcoords = (Vector2(x, y))
                    color = YELLOW

                    line(selfCoords, airdropcoords)


                }
            }
            Gdx.gl.glDisable(GL20.GL_BLEND)
        }


        val zoom = camera.zoom
        Gdx.gl.glEnable(GL20.GL_BLEND)
        draw(Filled) {
            color = redZoneColor
            circle(RedZonePosition, RedZoneRadius, 100)

            color = visionColor
            circle(selfX, selfY, visionRadius, 100)

            color = pinColor
            circle(pinLocation, pinRadius * zoom, 10)

        }


        Gdx.gl.glDisable(GL20.GL_BLEND)

    }

    private fun drawMyself(actorInfo: renderInfo) {
        val (actor, x, y, dir) = actorInfo
        if (actor?.netGUID == selfID) return
        val (sx, sy) = Vector2(x, y).mapToWindow()
        if (toggleView == 1) {
            // Just draw them both at the same time to avoid player not drawing ¯\_(ツ)_/¯
            spriteBatch.draw(
                    player,
                    sx, windowHeight - sy - 2, 4.toFloat() / 2,
                    4.toFloat() / 2, 4.toFloat(), 4.toFloat(), 5f, 5f,
                    dir * -1, 0, 0, 64, 64, true, false)

            spriteBatch.draw(
                    playersight,
                    sx + 1, windowHeight - sy - 2,
                    2.toFloat() / 2,
                    2.toFloat() / 2,
                    12.toFloat(), 2.toFloat(),
                    10f, 10f,
                    dir * -1, 0, 0, 512, 64, true, false)
        } else {

            spriteBatch.draw(
                    player,
                    sx, windowHeight - sy - 2, 4.toFloat() / 2,
                    4.toFloat() / 2, 4.toFloat(), 4.toFloat(), 5f, 5f,
                    dir * -1, 0, 0, 64, 64, true, false)
        }
    }

    private fun drawCircles() {
        Gdx.gl.glLineWidth(2f)
        draw(Line) {
            //vision circle

            color = safeZoneColor
            circle(PoisonGasWarningPosition, PoisonGasWarningRadius, 100)

            color = BLUE
            circle(SafetyZonePosition, SafetyZoneRadius, 100)

            if (togglesafezone == 1) {
                if (PoisonGasWarningPosition.len() > 0) {
                    color = safeDirectionColor
                    line(selfCoords, PoisonGasWarningPosition)
                }

            }
        }

        Gdx.gl.glLineWidth(1f)
    }


    private  fun drawAttackLine(currentTime: Long) {
        while (attacks.isNotEmpty()) {
            val (A, B) = attacks.poll()
            attackLineStartTime.add(Triple(A, B, currentTime))
        }
        if (attackLineStartTime.isEmpty()) return
        draw(Line) {
            val iter = attackLineStartTime.iterator()
            while (iter.hasNext()) {
                val (A, B, st) = iter.next()
                if (A == selfStateID || B == selfStateID) {
                    if (A != B) {
                        val otherGUID = playerStateToActor[if (A == selfStateID) B else A]
                        if (otherGUID == null) {
                            iter.remove()
                            continue
                        }
                        val other = actors[otherGUID]
                        if (other == null || currentTime - st > attackLineDuration) {
                            iter.remove()
                            continue
                        }
                        color = attackLineColor
                        val (xA, yA) = other.location
                        val (xB, yB) = selfCoords
                        line(xA, yA, xB, yB)
                    }
                } else {
                    val actorAID = playerStateToActor[A]
                    val actorBID = playerStateToActor[B]
                    if (actorAID == null || actorBID == null) {
                        iter.remove()
                        continue
                    }
                    val actorA = actors[actorAID]
                    val actorB = actors[actorBID]
                    if (actorA == null || actorB == null || currentTime - st > attackLineDuration) {
                        iter.remove()
                        continue
                    }
                    color = attackLineColor
                    val (xA, yA) = actorA.location
                    val (xB, yB) = actorB.location
                    line(xA, yA, xB, yB)
                }
            }
        }
    }

    private fun drawPawns(typeLocation: EnumMap<Archetype, MutableList<renderInfo>>) {
        val iconScale = 2f / camera.zoom
        for ((type, actorInfos) in typeLocation) {

            when (type) {
                TwoSeatBoat -> actorInfos?.forEach {

                    if (VehicleInfoToggles < 3) {

                        val (actor, x, y, dir) = it
                        val (sx, sy) = Vector2(x, y).mapToWindow()

                        if (VehicleInfoToggles == 2) compaseFont.draw(spriteBatch, "JSKI", sx + 15, windowHeight - sy - 2)

                        spriteBatch.draw(
                                jetski,
                                sx + 2, windowHeight - sy - 2, 4.toFloat() / 2,
                                4.toFloat() / 2, 4.toFloat(), 4.toFloat(), iconScale / 2, iconScale / 2,
                                dir * -1, 0, 0, 64, 64, true, false
                        )
                        val v_x = actor!!.velocity.x
                        val v_y = actor.velocity.y
                        if (actor.attachChildren.isNotEmpty() || v_x * v_x + v_y * v_y > 40) {
                            spriteBatch.draw(
                                    jetskio,
                                    sx + 2, windowHeight - sy - 2, 4.toFloat() / 2,
                                    4.toFloat() / 2, 4.toFloat(), 4.toFloat(), iconScale / 2, iconScale / 2,
                                    dir * -1, 0, 0, 64, 64, true, false
                            )
                        }
                    }
                }
                SixSeatBoat -> actorInfos?.forEach {
                    if (VehicleInfoToggles < 3) {
                        val (actor, x, y, dir) = it
                        val (sx, sy) = Vector2(x, y).mapToWindow()

                        if (VehicleInfoToggles == 2) compaseFont.draw(spriteBatch, "BOAT", sx + 15, windowHeight - sy - 2)

                        spriteBatch.draw(
                                boat,
                                sx + 2, windowHeight - sy - 2, 4.toFloat() / 2,
                                4.toFloat() / 2, 4.toFloat(), 4.toFloat(), iconScale / 2, iconScale / 2,
                                dir * -1, 0, 0, 64, 64, true, false
                        )
                        val v_x = actor!!.velocity.x
                        val v_y = actor.velocity.y
                        if (actor.attachChildren.isNotEmpty() || v_x * v_x + v_y * v_y > 40) {
                            spriteBatch.draw(
                                    boato,
                                    sx + 2, windowHeight - sy - 2, 4.toFloat() / 2,
                                    4.toFloat() / 2, 4.toFloat(), 4.toFloat(), iconScale / 2, iconScale / 2,
                                    dir * -1, 0, 0, 64, 64, true, false
                            )
                        }
                    }
                }
                TwoSeatBike -> actorInfos?.forEach {
                    if (VehicleInfoToggles < 3) {
                        val (actor, x, y, dir) = it
                        val (sx, sy) = Vector2(x, y).mapToWindow()


                        if (VehicleInfoToggles == 2) compaseFont.draw(spriteBatch, "BIKE", sx + 15, windowHeight - sy - 2)

                        spriteBatch.draw(
                                BikeBLUE,
                                sx + 2, windowHeight - sy - 2, 4.toFloat() / 2,
                                4.toFloat() / 2, 4.toFloat(), 4.toFloat(), iconScale / 3, iconScale / 3,
                                dir * -1, 0, 0, 64, 64, true, false
                        )
                        val v_x = actor!!.velocity.x
                        val v_y = actor.velocity.y
                        if (actor.attachChildren.isNotEmpty() || v_x * v_x + v_y * v_y > 40) {
                            spriteBatch.draw(BikeRED,
                                    sx + 2, windowHeight - sy - 2, 4.toFloat() / 2,
                                    4.toFloat() / 2, 4.toFloat(), 4.toFloat(), iconScale / 3, iconScale / 3,
                                    dir * -1, 0, 0, 64, 64, true, false
                            )
                        }
                    }
                }
                TwoSeatCar -> actorInfos?.forEach {
                    if (VehicleInfoToggles < 3) {
                        val (actor, x, y, dir) = it
                        val (sx, sy) = Vector2(x, y).mapToWindow()

                        if (VehicleInfoToggles == 2) compaseFont.draw(spriteBatch, "BUGGY", sx + 15, windowHeight - sy - 2)

                        spriteBatch.draw(
                                BuggyBLUE,
                                sx + 2, windowHeight - sy - 2,
                                2.toFloat() / 2, 2.toFloat() / 2,
                                2.toFloat(), 2.toFloat(),
                                iconScale / 2, iconScale / 2,
                                dir * -1, 0, 0, 64, 64, false, false
                        )
                        val v_x = actor!!.velocity.x
                        val v_y = actor.velocity.y
                        if (actor.attachChildren.isNotEmpty() || v_x * v_x + v_y * v_y > 40) {
                            spriteBatch.draw(
                                    BuggyRED,
                                    sx + 2, windowHeight - sy - 2,
                                    2.toFloat() / 2, 2.toFloat() / 2,
                                    2.toFloat(), 2.toFloat(),
                                    iconScale / 2, iconScale / 2,
                                    dir * -1, 0, 0, 64, 64, false, false
                            )
                        }
                    }
                }
                ThreeSeatCar -> actorInfos?.forEach {
                    if (VehicleInfoToggles < 3) {
                        val (actor, x, y, dir) = it
                        val (sx, sy) = Vector2(x, y).mapToWindow()

                        if (VehicleInfoToggles == 2) compaseFont.draw(spriteBatch, "BIKE", sx + 15, windowHeight - sy - 2)

                        spriteBatch.draw(
                                Bike3BLUE,
                                sx + 2, windowHeight - sy - 2, 4.toFloat() / 2, 4.toFloat() / 2,
                                4.toFloat(), 4.toFloat(), iconScale / 2, iconScale / 2,
                                dir * -1, 0, 0, 64, 64, true, false
                        )
                        val v_x = actor!!.velocity.x
                        val v_y = actor.velocity.y
                        if (actor.attachChildren.isNotEmpty() || v_x * v_x + v_y * v_y > 40) {
                            spriteBatch.draw(
                                    Bike3RED,
                                    sx + 2, windowHeight - sy - 2, 4.toFloat() / 2, 4.toFloat() / 2,
                                    4.toFloat(), 4.toFloat(), iconScale / 2, iconScale / 2,
                                    dir * -1, 0, 0, 64, 64, true, false
                            )
                        }
                    }

                }
                FourSeatDU -> actorInfos?.forEach {
                    if (VehicleInfoToggles < 3) {
                        val (actor, x, y, dir) = it
                        val (sx, sy) = Vector2(x, y).mapToWindow()

                        if (VehicleInfoToggles == 2) compaseFont.draw(spriteBatch, "CAR", sx + 15, windowHeight - sy - 2)

                        spriteBatch.draw(
                                vehicle,
                                sx + 2, windowHeight - sy - 2,
                                2.toFloat() / 2, 2.toFloat() / 2,
                                2.toFloat(), 2.toFloat(),
                                iconScale / 2, iconScale / 2,
                                dir * -1, 0, 0, 128, 128, false, false
                        )
                        // Draw over top whenever some is in car
                        val v_x = actor!!.velocity.x
                        val v_y = actor.velocity.y
                        if (actor.attachChildren.isNotEmpty() || v_x * v_x + v_y * v_y > 40) {
                            spriteBatch.draw(
                                    vehicleo,
                                    sx + 2, windowHeight - sy - 2,
                                    2.toFloat() / 2, 2.toFloat() / 2,
                                    2.toFloat(), 2.toFloat(),
                                    iconScale / 2, iconScale / 2,
                                    dir * -1, 0, 0, 128, 128, false, false
                            )
                        }
                    }

                }
                FourSeatP -> actorInfos?.forEach {
                    if (VehicleInfoToggles < 3) {
                        val (actor, x, y, dir) = it
                        val (sx, sy) = Vector2(x, y).mapToWindow()

                        if (VehicleInfoToggles == 2) compaseFont.draw(spriteBatch, "PICKUP", sx + 15, windowHeight - sy - 2)

                        spriteBatch.draw(
                                pickupBLUE,
                                sx + 2, windowHeight - sy - 2,
                                2.toFloat() / 2, 2.toFloat() / 2,
                                2.toFloat(), 2.toFloat(),
                                iconScale / 2, iconScale / 2,
                                dir * -1, 0, 0, 64, 64, true, false
                        )

                        val v_x = actor!!.velocity.x
                        val v_y = actor.velocity.y
                        if (actor.attachChildren.isNotEmpty() || v_x * v_x + v_y * v_y > 40) {
                            spriteBatch.draw(
                                    pickupRED,
                                    sx + 2, windowHeight - sy - 2,
                                    2.toFloat() / 2, 2.toFloat() / 2,
                                    2.toFloat(), 2.toFloat(),
                                    iconScale / 2, iconScale / 2,
                                    dir * -1, 0, 0, 64, 64, true, false
                            )
                        }
                    }
                }
                SixSeatCar -> actorInfos?.forEach {
                    if (VehicleInfoToggles < 3) {
                        val (actor, x, y, dir) = it
                        val (sx, sy) = Vector2(x, y).mapToWindow()

                        if (VehicleInfoToggles == 2) compaseFont.draw(spriteBatch, "VAN", sx + 15, windowHeight - sy - 2)

                        spriteBatch.draw(
                                van,
                                sx + 2, windowHeight - sy - 2,
                                2.toFloat() / 2, 2.toFloat() / 2,
                                2.toFloat(), 2.toFloat(),
                                iconScale / 2, iconScale / 2,
                                dir * -1, 0, 0, 64, 64, false, false
                        )

                        val v_x = actor!!.velocity.x
                        val v_y = actor.velocity.y
                        if (actor.attachChildren.isNotEmpty() || v_x * v_x + v_y * v_y > 40) {
                            spriteBatch.draw(
                                    vano,
                                    sx + 2, windowHeight - sy - 2,
                                    2.toFloat() / 2, 2.toFloat() / 2,
                                    2.toFloat(), 2.toFloat(),
                                    iconScale / 2, iconScale / 2,
                                    dir * -1, 0, 0, 64, 64, false, false
                            )
                        }
                    }
                }
                Player -> actorInfos?.forEach {

                    for ((_, _) in typeLocation) {
                        val (actor, x, y, dir) = it
                        val (sx, sy) = Vector2(x, y).mapToWindow()
                        val playerStateGUID = actorWithPlayerState[actor!!.netGUID] ?: return@forEach
                        val PlayerState = actors[playerStateGUID] as? PlayerState ?: return@forEach
                        val selfStateGUID = actorWithPlayerState[selfID] ?: return@forEach
                        val selfState = actors[selfStateGUID] as? PlayerState ?: return@forEach

                        val name = PlayerState.name

                        if (PlayerState.teamNumber == selfState.teamNumber) { // new IF statement

                            // Can't wait for the "Omg Players don't draw issues
                            spriteBatch.draw(
                                    teamarrow,
                                    sx, windowHeight - sy - 2, 4.toFloat() / 2,
                                    4.toFloat() / 2, 4.toFloat(), 4.toFloat(), 5f, 5f,
                                    dir * -1, 0, 0, 64, 64, true, false)

                            if (toggleView == 1) {
                                spriteBatch.draw(
                                        teamsight,
                                        sx + 1, windowHeight - sy - 2,
                                        2.toFloat() / 2,
                                        2.toFloat() / 2,
                                        12.toFloat(), 2.toFloat(),
                                        10f, 10f,
                                        dir * -1, 0, 0, 512, 64, true, false)
                            }

                        } else {

                            spriteBatch.draw(
                                    arrow,
                                    sx, windowHeight - sy - 2, 4.toFloat() / 2,
                                    4.toFloat() / 2, 4.toFloat(), 4.toFloat(), 5f, 5f,
                                    dir * -1, 0, 0, 64, 64, true, false)

                            if (toggleView == 1) {
                                spriteBatch.draw(
                                        arrowsight,
                                        sx + 1, windowHeight - sy - 2,
                                        2.toFloat() / 2,
                                        2.toFloat() / 2,
                                        12.toFloat(), 2.toFloat(),
                                        10f, 10f,
                                        dir * -1, 0, 0, 512, 64, true, false)
                            }
                        }
                    }

                }
                Parachute -> actorInfos?.forEach {
                    for ((_, _) in typeLocation) {

                        val (_, x, y, dir) = it
                        val (sx, sy) = Vector2(x, y).mapToWindow()

                        spriteBatch.draw(
                                parachute,
                                sx + 2, windowHeight - sy - 2, 4.toFloat() / 2,
                                4.toFloat() / 2, 4.toFloat(), 4.toFloat(), 8f, 8f,
                                dir * -1, 0, 0, 128, 128, true, false)

                    }
                }
                Plane -> actorInfos?.forEach {
                    for ((_, _) in typeLocation) {

                        val (_, x, y, dir) = it
                        val (sx, sy) = Vector2(x, y).mapToWindow()

                        spriteBatch.draw(
                                plane,
                                sx + 2, windowHeight - sy - 2, 4.toFloat() / 2,
                                4.toFloat() / 2, 5.toFloat(), 5.toFloat(), 10f, 10f,
                                dir * -1, 0, 0, 64, 64, true, false)

                    }
                }
                Grenade -> actorInfos?.forEach {
                    val (_, x, y, dir) = it
                    val (sx, sy) = Vector2(x, y).mapToWindow()
                    spriteBatch.draw(
                            grenade,
                            sx + 2, windowHeight - sy - 2, 4.toFloat() / 2,
                            4.toFloat() / 2, 4.toFloat(), 4.toFloat(), 5f, 5f,
                            dir * -1, 0, 0, 16, 16, true, false)
                }

                else -> {
                    //nothing
                }
            }

        }
    }

    private fun drawPlayerNames(players: MutableList<renderInfo>?) {
        players?.forEach {

            val zoom = camera.zoom
            val (actor, x, y, _) = it
            if (actor != null && actor.isACharacter) {
                // actor!!
                val dir = Vector2(x - selfCoords.x, y - selfCoords.y)
                val (sx, sy) = mapToWindow(x, y)
                val distance = (dir.len() / 100).toInt()
                val angle = ((dir.angle() + 90) % 360).toInt()
                val playerStateGUID = actorWithPlayerState[actor.netGUID] ?: return@forEach
                val PlayerState = actors[playerStateGUID] as? PlayerState ?: return@forEach
                val name = PlayerState.name
                val teamNumber = PlayerState.teamNumber
                val numKills = PlayerState.numKills
                val health = actorHealth[actor.netGUID] ?: 100f
                val equippedWeapons = actorHasWeapons[actor.netGUID]
                val df = DecimalFormat("###.#")
                var weapon = ""
                val width = healthBarWidth * zoom
                val height = healthBarHeight * zoom
                val backgroundRadius = (playerRadius + 2000f) * zoom
                val hpY = y + backgroundRadius + height / 2


//      color = WHITE
//      rectLine(x - width / 2, y, x + width / 2, y, height+50f*zoom)
                draw(Filled) {
                    val healthWidth = (health / 100.0 * width).toFloat()
                    color = when {
                        health > 80f -> GREEN
                        health > 33f -> ORANGE
                        else -> RED
                    }
                    // rectLine(x - width / 2, hpY, x - width / 2 + healthWidth, hpY, height)

                    if (equippedWeapons != null) {
                        for (w in equippedWeapons) {
                            val weap = weapons[w ?: continue] as? Weapon ?: continue
                            val result = weap.typeName.split("_")
                            weapon += "<${result[2].substring(4)}>->${weap.currentAmmoInClip}\n"
                        }
                    }
                    var items = ""
                    for (element in PlayerState.equipableItems) {
                        if (element == null || element._1.isBlank()) continue
                        items += "${element._1}->${element._2.toInt()}\n"
                    }
                    for (element in PlayerState.castableItems) {
                        if (element == null || element._1.isBlank()) continue
                        items += "${element._1}->${element._2}\n"
                    }



                    when (nameToggles) {

                        1 -> {
                            if (actor is Character) {
                                val health = if (actor.health <= 0f) actor.groggyHealth else actor.health

                                nameFont.draw(spriteBatch,
                                        "$angle°${distance}m\n" +
                                                "|N: $name\n" +
                                                "|H: \n" +
                                                "|K: ($numKills)\nTN.($teamNumber)\n" +
                                                "|S: \n" +
                                                "|W: $weapon\n" +
                                                "|I: $items"

                                        , sx + 20, windowHeight - sy + 20)

                                val healthText = health
                                when {
                                    healthText > 80f -> hpgreen.draw(spriteBatch, "\n${df.format(health)}", sx + 40, windowHeight - sy + 9)
                                    healthText > 33f -> hporange.draw(spriteBatch, "\n${df.format(health)}", sx + 40, windowHeight - sy + 9)
                                    else -> hpred.draw(spriteBatch, "\n${df.format(health)}", sx + 40, windowHeight - sy + 9)
                                }

                                    when {
                                        actor.isGroggying -> {
                                            hpred.draw(spriteBatch, "DOWNED", sx + 40, windowHeight - sy + -42)
                                        }
                                        actor.isReviving -> {
                                            hporange.draw(spriteBatch, "GETTING REVIVED", sx + 40, windowHeight - sy + -42)
                                        }
                                        else -> hpgreen.draw(spriteBatch, "Alive", sx + 40, windowHeight - sy + -42)
                                    }


                            }

                        }
                        2 -> {
                            nameFont.draw(spriteBatch, "${distance}m\n" +
                                    "|N: $name\n" +
                                    "|H: ${df.format(health)}\n" +
                                    "|W: $weapon",
                                    sx + 20, windowHeight - sy + 20)
                        }
                        3 -> {
                            nameFont.draw(spriteBatch, "|N: $name\n|D: ${distance}m", sx + 20, windowHeight - sy + 20)
                            // rectLine(x - width / 2, hpY, x - width / 2 + healthWidth, hpY, height)
                        }
                        4 -> {

                            // Change color of hp
                            val healthText = health
                            when {
                                healthText > 80f -> hpgreen.draw(spriteBatch, "\n${df.format(health)}", sx + 40, windowHeight - sy + 8)
                                healthText > 33f -> hporange.draw(spriteBatch, "\n${df.format(health)}", sx + 40, windowHeight - sy + 8)
                                else -> hpred.draw(spriteBatch, "\n${df.format(health)}", sx + 40, windowHeight - sy + 8)

                            }
                            nameFont.draw(spriteBatch, "|N: $name\n|D: ${distance}m $angle°\n" +
                                    "|H:\n" +
                                    "|S:\n" +
                                    "|W: $weapon",
                                    sx + 20, windowHeight - sy + 20)

                            if (actor is Character)
                                when {
                                    actor.isGroggying -> {
                                        hpred.draw(spriteBatch, "DOWNED", sx + 40, windowHeight - sy + -16)
                                    }
                                    actor.isReviving -> {
                                        hporange.draw(spriteBatch, "GETTING REVIVED", sx + 40, windowHeight - sy + -16)
                                    }
                                    else -> hpgreen.draw(spriteBatch, "Alive", sx + 40, windowHeight - sy + -16)
                                }
                        }
                    }

                }
            }
        }
    }


        fun drawMapMarkers() {
            paint (camera.combined) {
                for (team in teams.values) {
                    if (team.showMapMarker) {
                        //println(team.mapMarkerPosition)
                        val icon = markers[team.memberNumber]
                        val (x, y) = team.mapMarkerPosition
                        draw(icon, x, y, 0f, mapMarkerScale, false)
                    }
                }
            }
        }



    fun SpriteBatch.draw(texture:TextureRegion,x:Float,y:Float,yaw:Float,scale:Float,zoom:Boolean = true) {
        val w = texture.regionWidth.toFloat()
        val h = texture.regionHeight.toFloat()
        val scale = if (zoom) scale else scale*camera.zoom
        draw(texture,x-w/2,
                y-h/2,
                w/2,h/2,
                w,h,
                scale,scale,
                yaw)
    }

private fun drawGrid() {
        draw(Filled) {
            val unit = gridWidth / 8
            val unit2 = unit / 10
            color = BLACK
            //thin grid
            for (i in 0..7)
                for (j in 0..9) {
                    rectLine(0f, i * unit + j * unit2, gridWidth, i * unit + j * unit2, 100f)
                   rectLine(i * unit + j * unit2, 0f, i * unit + j * unit2, gridWidth, 100f)
                }
            color = GRAY
            //thick grid
           for (i in 0..7) {
               rectLine(0f, i * unit, gridWidth, i * unit, 500f)
                rectLine(i * unit, 0f, i * unit, gridWidth, 500f)
            }
        }
    }


    private var lastPlayTime = System.currentTimeMillis()
    private fun safeZoneHint() {

            if (PoisonGasWarningPosition.len() > 0) {
                val dir = PoisonGasWarningPosition.cpy().sub(selfCoords)
                val road = dir.len() - PoisonGasWarningRadius
                if (road > 0) {
                    val runningTime = (road / runSpeed).toInt()
                    val (x, y) = dir.nor().scl(road).add(selfCoords).mapToWindow()
                    littleFont.draw(spriteBatch, "$runningTime", x, windowHeight - y)
                    val remainingTime = (TotalWarningDuration - ElapsedWarningDuration).toInt()
                    if (remainingTime == 60 && runningTime > remainingTime) {
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastPlayTime > 10000) {
                            lastPlayTime = currentTime
                            alarmSound.play()
                        }
                    }
                }
            }
        }


    private inline fun draw(type:ShapeType,draw:ShapeRenderer.() -> Unit) {
        shapeRenderer.apply {
            begin(type)
            draw()
            end()
        }
    }

    private inline fun paint(matrix: Matrix4, paint: SpriteBatch.() -> Unit) {
        spriteBatch.apply {
            projectionMatrix = matrix
            begin()
            paint()
            end()
        }
    }

    private fun ShapeRenderer.circle(loc: Vector2, radius: Float, segments: Int) {
        circle(loc.x, loc.y, radius, segments)
    }


    private fun isTeamMate(actor:Actor?):Int {
        val teamID = (actor as? Character)?.teamID ?: return -1
        val team = actors[teamID] as? Team ?: return -1
        return team.memberNumber
    }

    override fun resize(width: Int, height: Int) {
        windowWidth = width.toFloat()
        windowHeight = height.toFloat()
        camera.setToOrtho(true, windowWidth * windowToMapUnit, windowHeight * windowToMapUnit)
        itemCamera.setToOrtho(false, windowWidth, windowHeight)
        fontCamera.setToOrtho(false, windowWidth, windowHeight)
    }

    override fun pause() {
    }

    override fun resume() {
    }

    override fun dispose() {
        deregister(this)
        alarmSound.dispose()
        nameFont.dispose()
        largeFont.dispose()
        littleFont.dispose()
        menuFont.dispose()
        menuFontOn.dispose()
        menuFontOFF.dispose()
        hporange.dispose()
        hpgreen.dispose()
        itemAtlas.dispose()
        hpred.dispose()
        corpseboximage.dispose()
        AirDropAllTheColors.dispose()
        vehicle.dispose()
        iconImages.iconSheet.dispose()
        compaseFont.dispose()
        compaseFontShadow.dispose()
        pawnAtlas.dispose()

        var cur = 0
        spriteBatch.dispose()
        shapeRenderer.dispose()
    }
}
