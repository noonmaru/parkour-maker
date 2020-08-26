package com.github.noonmaru.parkourmaker

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityExplodeEvent
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

                val box = player.boundingBox
                val y = floor(box.minY)
                val stepY = floor(box.minY - 0.000001)

                if (stepY != y) {
                    val world = player.world
                    val minX: Int = floor(box.minX)
                    val minZ: Int = floor(box.minZ)
                    val maxX: Int = floor(box.maxX)
                    val maxZ: Int = floor(box.maxZ)

                    for (x in minX..maxX) {
                        for (z in minZ..maxZ) {
                            val stepBlock = world.getBlockAt(x, stepY, z)

                            challenge.dataByBlock[stepBlock]?.run {
                                onStep(challenge, traceur, event)
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    fun onExplode(event: EntityExplodeEvent) {
        val blocks = event.blockList().iterator()

        while (blocks.hasNext()) {
            val block = blocks.next()

            for (level in ParkourMaker.levels.values) {
                level.challenge?.let { challenge ->
                    val parkourBlock = challenge.dataByBlock[block]

                    if (parkourBlock != null) {
                        blocks.remove()
                        parkourBlock.onExplode(challenge, event)
                    }
                }
            }
        }
    }

    @EventHandler
    fun onDamage(event: EntityDamageEvent) {
        val entity = event.entity

        if (entity is Player) {
            val traceur = entity.traceur
            traceur.challenge?.let { challenge ->
                val cause = event.cause

                if (cause == EntityDamageEvent.DamageCause.FIRE) {
                    val world = entity.world
                    val box = entity.boundingBox
                    val minX: Int = floor(box.minX)
                    val minZ: Int = floor(box.minZ)
                    val maxX: Int = floor(box.maxX)
                    val maxZ: Int = floor(box.maxZ)
                    val stepY = floor(box.minY)

                    for (x in minX..maxX) {
                        for (z in minZ..maxZ) {
                            val stepBlock = world.getBlockAt(x, stepY, z)

                            challenge.dataByBlock[stepBlock]?.run {
                                onFire(challenge, traceur, event)
                            }
                        }
                    }
                }
            }
        }
    }

    //Anvil 보류
}