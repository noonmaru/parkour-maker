package com.github.noonmaru.parkourmaker

import com.sk89q.worldedit.math.BlockVector3
import org.bukkit.block.BlockFace
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.entity.ProjectileHitEvent
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
                    val stepBlock = passBlock.getRelative(BlockFace.DOWN)
                    challenge.dataByBlock[stepBlock]?.run {
                        onStep(challenge, traceur, event)
                    }
                }
            }
        }
    }

    @EventHandler
    fun onExplode(event: EntityExplodeEvent) {
        ParkourMaker.levels.values.forEach level@{ level ->
            event.blockList().forEach { block ->
                if (level.region.contains(BlockVector3.at(block.x, block.y, block.z)))
                    level.challenge?.let {
                        it.dataByBlock[block]?.run {
                            event.entity.remove()
                            event.isCancelled = true
                            onExplode(it)
                            return@level
                        }
                    }
            }
        }
    }

    @EventHandler
    fun onAnvil(event: EntityChangeBlockEvent) {
        if (event.entityType == EntityType.FALLING_BLOCK) {
            val block = event.block
            val fallPoint = block.getRelative(BlockFace.DOWN)
            ParkourMaker.levels.values.forEach { level ->
                if (level.region.contains(BlockVector3.at(fallPoint.x, fallPoint.y, fallPoint.z)))
                    level.challenge?.let {
                        it.dataByBlock[fallPoint]?.run {
                            event.entity.remove()
                            event.isCancelled = true
                            block.breakNaturally()
                            onAnvil(it)
                        }
                    }
            }
        }
    }
}