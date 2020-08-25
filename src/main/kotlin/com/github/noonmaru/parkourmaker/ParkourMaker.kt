package com.github.noonmaru.parkourmaker

import com.github.noonmaru.parkourmaker.util.toBoundingBox
import com.github.noonmaru.tap.fake.FakeEntityServer
import com.sk89q.worldedit.regions.CuboidRegion
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.util.BoundingBox
import java.io.File
import java.util.*

object ParkourMaker {

    lateinit var coursesFolder: File
        private set

    lateinit var historyFolder: File
        private set

    lateinit var levelFolder: File
        private set

    private lateinit var _levels: MutableMap<String, Level>
    val levels: Map<String, Level>
        get() = _levels

    private lateinit var _traceurs: MutableMap<UUID, Traceur>
    val traceurs: Map<UUID, Traceur>
        get() = _traceurs

    lateinit var fakeEntityServer: FakeEntityServer
        private set

    internal fun initialize(plugin: JavaPlugin) {
        plugin.dataFolder.let { dir ->
            coursesFolder = File(dir, "courses")
            historyFolder = File(dir, "course-history")
            levelFolder = File(dir, "levels")
        }

        _levels = TreeMap<String, Level>(String.CASE_INSENSITIVE_ORDER).apply {
            levelFolder.let { dir ->
                if (dir.exists()) {
                    levelFolder.listFiles { _, name -> name.endsWith(".yml") }?.forEach { file ->
                        val level = Level(file)
                        put(level.name, level)
                    }
                }
            }
        }

        _traceurs = HashMap<UUID, Traceur>().apply {
            Bukkit.getOnlinePlayers().forEach { player ->
                put(player.uniqueId, Traceur(player))
            }
        }

        fakeEntityServer = FakeEntityServer.create(plugin).apply {
            plugin.server.pluginManager.registerEvents(object : Listener {
                @EventHandler
                fun onJoin(event: PlayerJoinEvent) {
                    addPlayer(event.player)
                }
            }, plugin)
            plugin.server.scheduler.runTaskTimer(plugin, this::update, 0L, 1L)
        }
    }

    fun createLevel(name: String, region: CuboidRegion): Level {
        _levels.apply {
            require(name !in this) { "Name is already in use" }
            getOverlappedLevels(region.toBoundingBox()).let { overlaps ->
                require(overlaps.isEmpty()) { "Region overlaps with other levels ${overlaps.joinToString { it.name }}" }
            }

            return Level(name, region).apply {
                copyCourse()
                save()

                _levels[name] = this
            }
        }
    }

    fun getOverlappedLevels(box: BoundingBox): List<Level> {
        return levels.values.filter { box.overlaps(it.region.toBoundingBox()) }
    }

    fun registerPlayer(player: Player) {
        _traceurs.computeIfAbsent(player.uniqueId) { Traceur(player) }
    }

    internal fun removeLevel(level: Level) {
        _levels.remove(level.name)
    }
}

val Player.traceur: Traceur
    get() {
        return requireNotNull(ParkourMaker.traceurs[uniqueId]) { "Not found traceur for ${this.name}(${this.uniqueId})" }
    }