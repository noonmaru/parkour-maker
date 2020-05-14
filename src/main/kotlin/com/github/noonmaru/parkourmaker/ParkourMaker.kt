package com.github.noonmaru.parkourmaker

import com.github.noonmaru.parkourmaker.util.WorldEditSupport.toBoundingBox
import com.github.noonmaru.tap.fake.FakeManager
import com.sk89q.worldedit.regions.CuboidRegion
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.Plugin
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

    private lateinit var traceurs: MutableMap<UUID, Traceur>

    @Suppress("ObjectPropertyName")
    private lateinit var _levels: MutableMap<String, Level>

    val levels: Map<String, Level>
        get() = _levels

    lateinit var fakeManager: FakeManager
        private set

    fun createLevel(name: String, region: CuboidRegion): Level {
        _levels.apply {
            require(name !in this) { "이미 사용중인 이름입니다." }
            getOverlappedLevels(region.toBoundingBox()).let { overlaps ->
                require(overlaps.isEmpty()) { "${overlaps.joinToString { it.name }} 레벨과 구역이 겹칩니다." }
            }

            return Level(name, region).apply {
                copyCourse()
                save()
                _levels[name] = this
            }
        }
    }

    private fun getOverlappedLevels(box: BoundingBox): List<Level> {
        return levels.values.filter { box.overlaps(it.region.toBoundingBox()) }
    }

    internal fun initialize(plugin: Plugin) {
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

        traceurs = HashMap<UUID, Traceur>().apply {
            Bukkit.getOnlinePlayers().forEach { player ->
                put(player.uniqueId, Traceur(player))
            }
        }

        fakeManager = FakeManager().apply {
            plugin.server.pluginManager.registerEvents(object : Listener {
                @EventHandler
                fun onJoin(event: PlayerJoinEvent) {
                    addPlayer(event.player)
                }

                @EventHandler
                fun onQuit(event: PlayerQuitEvent) {
                    removePlayer(event.player)
                }
            }, plugin)
            plugin.server.scheduler.runTaskTimer(plugin, this, 0L, 1L)
        }
    }

    internal fun registerPlayer(player: Player) {
        traceurs.computeIfAbsent(player.uniqueId) { Traceur(player) }
    }

    internal fun removeLevel(level: Level) {
        _levels.remove(level.name)
    }

    internal val Player.traceur: Traceur
        get() {
            return requireNotNull(traceurs[uniqueId]) { "${this.name}(${this.uniqueId})에 해당하는 트레이서를 찾지 못했습니다." }
        }
}