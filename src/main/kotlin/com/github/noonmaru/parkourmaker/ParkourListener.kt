package com.github.noonmaru.parkourmaker

import org.bukkit.block.BlockFace
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent

class ParkourListener : Listener {
    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        event.player.let { player ->
            ParkourMaker._traceurs.computeIfAbsent(player.uniqueId) { Traceur(player) }
        }
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        event.player.let { player ->
            ParkourMaker._traceurs[player.uniqueId]?.apply {
                this.player = null
            }
        }
    }

    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        ParkourMaker.traceurs[event.player.uniqueId]?.challenge?.let { challenge ->
            event.clickedBlock?.let { block ->
                challenge.dataByBlock[block]?.run {
                    onInteract(challenge, event)
                }
            }
        }
    }

    @EventHandler
    fun onMove(event: PlayerMoveEvent) {
        val player = event.player

        if (player.isOnGround) {
            ParkourMaker.traceurs[event.player.uniqueId]?.challenge?.let { challenge ->
                val block = event.to.block.getRelative(BlockFace.DOWN)
                challenge.dataByBlock[block]?.run {
                    onStep(challenge, event)
                }
            }
        }
    }
}