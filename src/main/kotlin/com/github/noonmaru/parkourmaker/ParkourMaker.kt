package com.github.noonmaru.parkourmaker

import com.github.noonmaru.tap.fake.FakeManager
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.Plugin
import java.io.File
import java.util.*

object ParkourMaker {

    lateinit var coursesFolder: File

    lateinit var historyFolder: File

    lateinit var levelFolder: File

    internal lateinit var _levels: MutableMap<String, Level>
    val levels: Map<String, Level>
        get() = _levels

    internal lateinit var _traceurs: MutableMap<UUID, Traceur>
    val traceurs: Map<UUID, Traceur>
        get() = _traceurs

    lateinit var fakeManager: FakeManager
        private set

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

        _traceurs = HashMap<UUID, Traceur>().apply {
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
}

val Player.traceur: Traceur?
    get() = ParkourMaker.traceurs[uniqueId]