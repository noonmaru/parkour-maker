package com.github.noonmaru.parkourmaker.plugin

import com.github.noonmaru.parkourmaker.ParkourListener
import com.github.noonmaru.parkourmaker.ParkourMaker
import com.github.noonmaru.parkourmaker.ParkourTask
import com.github.noonmaru.parkourmaker.command.CommandCreate
import com.github.noonmaru.parkourmaker.command.CommandList
import com.github.noonmaru.parkourmaker.command.CommandStart
import com.github.noonmaru.parkourmaker.command.CommandStop
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
        setupCommands()
    }

    private fun setupCommands() {
        command("parkour") {
            help("help") {
                description = "설명을 확인합니다."
            }
            component("create") {
                usage = "<Name>"
                description = "레벨을 생성합니다."
                CommandCreate()
            }
            component("list") {
                description = "레벨 목록을 확인합니다."
                CommandList()
            }
            component("start") {
                usage = "<Parkour> [Players...]"
                description = "레벨 도전을 시작합니다."
                CommandStart()
            }
            component("stop") {
                usage = "<Parkour>"
                description = "레벨 도전을 종료합니다."
                CommandStop()
            }
        }
    }

    override fun onDisable() {
        ParkourMaker.levels.values.forEach {
            //            it.save()
            it.challenge?.run {
                it.stopChallenge()
            }
        }
    }
}