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
        ParkourMaker.registerPlayer(event.player)
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        event.player.traceur.apply {
            player = null
        }
    }

    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        event.clickedBlock?.let { block ->
            val traceur = event.player.traceur
            traceur.challenge?.let { challenge ->
                challenge.dataByBlock[block]?.run {
                    event.isCancelled = true
                    onInteract(challenge, traceur, event)
                }
            }
        }
    }

    @EventHandler
    fun onMove(event: PlayerMoveEvent) {
        event.player.let { player ->

            val traceur = player.traceur
            traceur.challenge?.let { challenge ->
                val passBlock = event.to.block
                challenge.dataByBlock[passBlock]?.run {
                    onPass(challenge, traceur, event)
                }
                if (player.isOnGround) {
                    val stepBlock = passBlock.getRelative(BlockFace.DOWN)
                    challenge.dataByBlock[stepBlock]?.run {
                        onStep(challenge, traceur, event)
                    }
                }
            }
        }
    }
}