package com.github.noonmaru.parkourmaker

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
            player.traceur?.apply { this.player = null }
        }
    }

    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        event.player.traceur?.challenge?.let { challenge ->
            event.clickedBlock?.let { block ->
                challenge.dataByBlock[block]?.run {
                    event.isCancelled = true
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
                val block = event.to.block
                challenge.dataByBlock[block]?.run {
                    onPass(challenge, event)
                }
            }
        }
    }

}