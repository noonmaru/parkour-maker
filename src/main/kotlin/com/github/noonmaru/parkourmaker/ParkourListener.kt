package com.github.noonmaru.parkourmaker

import com.github.noonmaru.parkourmaker.ParkourMaker.traceur
import com.github.noonmaru.parkourmaker.block.SwitchBlock
import com.github.noonmaru.parkourmaker.util.WorldEditSupport.getBlockVector3
import com.sk89q.worldedit.math.BlockVector3
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.entity.FallingBlock
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent

class ParkourListener : Listener {
    // style: collect all types of materials used in this class
    companion object {
        private val PROJECTILE_LAUNCHERS = setOf(Material.BOW, Material.CROSSBOW, Material.TRIDENT, Material.FISHING_ROD,
            Material.SNOWBALL, Material.EGG, Material.ENDER_EYE, Material.EXPERIENCE_BOTTLE, Material.FIREWORK_ROCKET,
            Material.POTION, Material.LINGERING_POTION, Material.SPLASH_POTION)
        private val ANVILS = setOf(Material.ANVIL, Material.CHIPPED_ANVIL, Material.DAMAGED_ANVIL)
        private val SHULKER_BOXES = setOf(Material.RED_SHULKER_BOX, Material.LIGHT_BLUE_SHULKER_BOX)
    }

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
        // fix: bow shooting is recognized as PlayerInteractEvent
        val material = event.player.inventory.itemInMainHand.type
        var flag = true
        if (PROJECTILE_LAUNCHERS.contains(material)) {
            flag = false
        }
        event.clickedBlock?.let { block ->
            event.player.traceur.challenge?.let { challenge ->
                challenge.dataByBlock[block]?.run {
                    // fix: some actions being cancelled due to detection of non-switch parkour blocks.
                    if (this is SwitchBlock.SwitchData) {
                        if (flag) {
                            event.isCancelled = true
                        }
                        changeState(challenge)
                    }
                }
            }
        }
    }

    @EventHandler
    fun onHit(event: ProjectileHitEvent) {
        event.hitBlock?.let { block ->
            val projectile = event.entity
            val shooter = projectile.shooter as? Player?: return
            val traceur = shooter.traceur

            traceur.challenge?.let { challenge ->
                challenge.dataByBlock[block]?.run {
                    projectile.remove()
                    changeState(challenge)
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
                    onPass(traceur)
                }
                if (player.isOnGround) {
                    val stepBlock = passBlock.getRelative(BlockFace.DOWN)
                    challenge.dataByBlock[stepBlock]?.run {
                        onStep(traceur)
                    }
                }
            }
        }
    }

    @EventHandler
    fun onExplode(event: EntityExplodeEvent) {
        ParkourMaker.levels.values.forEach level@{ level ->
            event.blockList().forEach { block ->
                if (level.region.contains(BlockVector3.at(block.x, block.y, block.z))) {
                    level.challenge?.let { challenge ->
                        challenge.dataByBlock[block]?.run {
                            event.entity.remove()
                            event.isCancelled = true
                            changeState(challenge)
                            return@level
                        }
                    }
                }
            }
        }
    }

    // style: align variables at the top, and lesser use of return / if
    @EventHandler
    fun onAnvilFall(event: EntityChangeBlockEvent) {
        val entity = event.entity as? FallingBlock?: return
        val material = entity.blockData.material
        val block = event.block
        val fallPoint = block.getRelative(BlockFace.DOWN)
        val fallPointType = fallPoint.type

        if (ANVILS.contains(material) && SHULKER_BOXES.contains(fallPointType)) {
            ParkourMaker.levels.values.forEach { level ->
                if (level.region.contains(fallPoint.getBlockVector3())) {
                    level.challenge?.let { challenge ->
                        challenge.dataByBlock[fallPoint]?.run {
                            entity.remove()
                            event.isCancelled = true
                            block.type = Material.AIR
                            changeState(challenge)
                        }
                    }
                }
            }
        }
    }

    // style: align variables at the top, and lesser use of return / if
    @EventHandler
    fun onAnvilPlace(event: BlockPlaceEvent) {
        val block = event.block
        val blockType = block.type
        val fallPoint = block.getRelative(BlockFace.DOWN)
        val fallPointType = fallPoint.type

        if (ANVILS.contains(blockType) && SHULKER_BOXES.contains(fallPointType)) {
            ParkourMaker.levels.values.forEach { level ->
                if (level.region.contains(fallPoint.getBlockVector3())) {
                    level.challenge?.let { challenge ->
                        challenge.dataByBlock[fallPoint]?.run {
                            event.isCancelled = true
                            block.type = Material.AIR
                            changeState(challenge)
                        }
                    }
                }
            }
        }
    }
}