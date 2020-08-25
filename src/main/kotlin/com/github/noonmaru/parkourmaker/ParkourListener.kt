package com.github.noonmaru.parkourmaker

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.util.NumberConversions.floor

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
    fun onHit(event: ProjectileHitEvent) {
        event.hitBlock?.let { block ->
            val projectile = event.entity
            val shooter = projectile.shooter

            if (shooter is Player) {
                val traceur = shooter.traceur
                traceur.challenge?.let { challenge ->
                    challenge.dataByBlock[block]?.run {
                        projectile.remove()
                        onHit(challenge, traceur, event)
                    }
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
                    val world = player.world
                    val box = player.boundingBox
                    val minX: Int = floor(box.minX)
                    val minZ: Int = floor(box.minZ)
                    val maxX: Int = floor(box.maxX)
                    val maxZ: Int = floor(box.maxZ)
                    val y = floor(box.minY - 0.000001)

                    for (x in minX..maxX) {
                        for (z in minZ..maxZ) {
                            val stepBlock = world.getBlockAt(x, y, z)
                            challenge.dataByBlock[stepBlock]?.run {
                                onStep(challenge, traceur, event)
                            }
                        }
                    }
                }
            }
        }
    }
}