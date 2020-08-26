package com.github.noonmaru.parkourmaker.plugin

import com.github.noonmaru.kommand.kommand
import com.github.noonmaru.parkourmaker.ParkourListener
import com.github.noonmaru.parkourmaker.ParkourMaker
import com.github.noonmaru.parkourmaker.ParkourPluginScheduler
import com.github.noonmaru.parkourmaker.command.KommandParkour
import org.bukkit.plugin.java.JavaPlugin

/**
 * @author Nemo
 */
class ParkourMakerPlugin : JavaPlugin() {
    override fun onEnable() {
        ParkourMaker.initialize(this)
        server.apply {
            pluginManager.registerEvents(ParkourListener(), this@ParkourMakerPlugin)
            scheduler.runTaskTimer(this@ParkourMakerPlugin, ParkourPluginScheduler(), 0L, 1L)
        }
        kommand {
            register("parkour") {
                KommandParkour.register(this)
            }
        }
    }

    override fun onDisable() {
        ParkourMaker.fakeEntityServer.clear()

        ParkourMaker.levels.values.forEach {
            it.save()
            it.challenge?.run {
                it.stopChallenge()
            }
        }
    }
}