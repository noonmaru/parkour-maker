package com.github.noonmaru.parkourmaker.plugin

import com.github.noonmaru.parkourmaker.ParkourListener
import com.github.noonmaru.parkourmaker.ParkourMaker
import com.github.noonmaru.parkourmaker.ParkourTask
import com.github.noonmaru.parkourmaker.command.CommandLevel
import com.github.noonmaru.tap.command.command
import org.bukkit.plugin.java.JavaPlugin

/**
 * @author Nemo
 */
class ParkourMakerPlugin : JavaPlugin() {
    override fun onEnable() {
        ParkourMaker.initialize(this)
        server.apply {
            pluginManager.registerEvents(ParkourListener(), this@ParkourMakerPlugin)
            scheduler.runTaskTimer(this@ParkourMakerPlugin, ParkourTask(), 0L, 1L)
        }
    }

    private fun setupCommands() {
        command("parkour") {
            help("help") {
                description = "설명을 확인합니다."
            }
            component("level") {
                usage = "<Name>"
                description = "레벨을 생성합니다."
                CommandLevel()
            }
        }
    }

    override fun onDisable() {
        ParkourMaker.levels.values.forEach {
            it.save()
            it.challenge?.run {
                it.stopChallenge()
            }
        }
    }
}