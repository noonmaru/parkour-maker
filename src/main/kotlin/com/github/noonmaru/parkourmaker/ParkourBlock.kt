package com.github.noonmaru.parkourmaker

import com.github.noonmaru.parkourmaker.task.ParkourTask
import com.github.noonmaru.parkourmaker.util.Tick
import com.github.noonmaru.tap.effect.playFirework
import com.github.noonmaru.tap.fake.FakeEntity
import com.github.noonmaru.tap.fake.invisible
import com.google.common.collect.ImmutableList
import org.bukkit.*
import org.bukkit.attribute.Attribute
import org.bukkit.block.Banner
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.Directional
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Firework
import org.bukkit.entity.Shulker
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.Sound
import org.bukkit.event.Listener
import kotlin.math.min

object ParkourBlocks {
    val SPAWN = SpawnBlock()
    val CLEAR = ClearBlock()
    val CHECKPOINT = CheckpointBlock()
    val SWITCH = SwitchBlock()
    val TOGGLE = ToggleBlock()
    val SAND = SandBlock()
    val SOUL_FIRE = SoulFireBlock()
    val TRAMPOLINE = TrampolineBlock()

    val list = ImmutableList.of(
        SPAWN,
        CLEAR,
        CHECKPOINT,
        SWITCH,
        TOGGLE,
        SAND,
        SOUL_FIRE,
        TRAMPOLINE
    )

    fun getBlock(block: Block): ParkourBlock? {
        val state = block.state
        val data = block.blockData
        val type = data.material

        if (type != Material.AIR) {
            if (type == Material.EMERALD_BLOCK) {
                return SPAWN
            } else if (type == Material.GOLD_BLOCK) {
                return CLEAR
            } else if (state is Banner) {
                return CHECKPOINT
            } else if (type == Material.RED_SHULKER_BOX || type == Material.LIGHT_BLUE_SHULKER_BOX) {
                return SWITCH
            } else if (type == Material.RED_CONCRETE || type == Material.LIGHT_BLUE_CONCRETE) {
                return TOGGLE
            } else if (type == Material.SANDSTONE) {
                return SAND
            } else if (type == Material.SOUL_CAMPFIRE) {
                return SOUL_FIRE
            } else if (type == Material.NOTE_BLOCK) {
                return TRAMPOLINE
            }
        }
        return null
    }
}

abstract class ParkourBlock {
    fun createBlockData(block: Block): ParkourBlockData {
        return newBlockData(block).apply {
            this.block = block
            this.parkoutBlock = this@ParkourBlock
        }
    }

    protected abstract fun newBlockData(block: Block): ParkourBlockData
}

abstract class ParkourBlockData {

    lateinit var block: Block

    lateinit var parkoutBlock: ParkourBlock
        internal set

    open fun onInitialize(challenge: Challenge) {}

    open fun onPass(challenge: Challenge, traceur: Traceur, event: PlayerMoveEvent) {}

    open fun onStep(challenge: Challenge, traceur: Traceur, event: PlayerMoveEvent) {}

    open fun onHit(challenge: Challenge, traceur: Traceur, event: ProjectileHitEvent) {}

    open fun onInteract(challenge: Challenge, traceur: Traceur, event: PlayerInteractEvent) {}

    open fun onExplode(challenge: Challenge, event: EntityExplodeEvent) {}

    open fun onFire(challenge: Challenge, traceur: Traceur, event: EntityDamageEvent) {}

    open fun destroy() {}
}

interface Respawnable {
    val respawn: Location
}

class SpawnBlock : ParkourBlock() {
    override fun newBlockData(block: Block): ParkourBlockData {
        return SpawnData()
    }

    class SpawnData : ParkourBlockData(), Respawnable {
        override val respawn: Location
            get() {
                val loc = block.location.add(0.5, 1.0, 0.5)
                block.getRelative(BlockFace.DOWN).let { down ->
                    if (down.type == Material.MAGENTA_GLAZED_TERRACOTTA) {
                        val blockData = down.blockData
                        blockData as Directional

                        loc.yaw = when (blockData.facing) {
                            BlockFace.EAST -> 90.0F
                            BlockFace.SOUTH -> 180.0F
                            BlockFace.WEST -> 270.0F
                            else -> 0.0F
                        }
                    }
                }
                return loc
            }
    }
}

class ClearBlock : ParkourBlock() {
    override fun newBlockData(block: Block): ParkourBlockData {
        return ClearData()
    }

    class ClearData : ParkourBlockData() {
        override fun onStep(challenge: Challenge, traceur: Traceur, event: PlayerMoveEvent) {
            val level = challenge.level
            level.stopChallenge()

            Bukkit.getOnlinePlayers().forEach {
                it.sendTitle(
                    "${ChatColor.AQUA}${ChatColor.BOLD}COURSE CLEAR",
                    "${ChatColor.RED}${ChatColor.BOLD}${traceur.name}${ChatColor.RESET}님이 ${ChatColor.GOLD}${level.name} ${ChatColor.RESET}레벨을 클리어!",
                    5,
                    90,
                    5
                )
            }

            val loc = block.location.add(0.5, 1.0, 0.5)
            loc.world.spawn(loc, Firework::class.java).apply {
                fireworkMeta = fireworkMeta.apply {
                    addEffect(FireworkEffect.builder().apply {
                        flicker(true)
                        trail(true)
                        with(FireworkEffect.Type.STAR)
                        withColor(Color.AQUA)
                        withColor(Color.RED)
                        withColor(Color.GREEN)
                        withColor(Color.YELLOW)
                        withFade(Color.WHITE)
                    }.build())
                    power = 1
                }
            }
        }
    }
}

class CheckpointBlock : ParkourBlock() {
    override fun newBlockData(block: Block): ParkourBlockData {
        return CheckpointData()
    }

    class CheckpointData : ParkourBlockData(), Respawnable {
        override val respawn: Location
            get() = block.location.add(0.5, 1.0, 0.5)

        override fun onPass(challenge: Challenge, traceur: Traceur, event: PlayerMoveEvent) {
            if (challenge.setSpawn(traceur, this) != this) {
                val loc = respawn.add(0.0, 4.0, 0.0)
                loc.world.spawn(loc, Firework::class.java).apply {
                    fireworkMeta = fireworkMeta.apply {
                        addEffect(FireworkEffect.builder().apply {
                            with(FireworkEffect.Type.BALL)
                            val blockState = block.state
                            if (blockState is Banner) {
                                withColor(blockState.baseColor.fireworkColor)
                            }
                        }.build())
                        power = 1
                    }
                }
            }
        }
    }
}

enum class Toggle(val blockType: Material, val fakeData: BlockData, val switchType: Material, val next: () -> Toggle) {
    RED(Material.RED_CONCRETE,
        Material.RED_STAINED_GLASS.createBlockData(),
        Material.RED_SHULKER_BOX,
        { BLUE }),
    BLUE(
        Material.LIGHT_BLUE_CONCRETE,
        Material.LIGHT_BLUE_STAINED_GLASS.createBlockData(),
        Material.LIGHT_BLUE_SHULKER_BOX,
        { RED });
}

class SwitchBlock : ParkourBlock() {
    override fun newBlockData(block: Block): ParkourBlockData {
        return SwitchData()
    }

    class SwitchData : ParkourBlockData() {

        override fun onInitialize(challenge: Challenge) {
            update(challenge.toggle)
        }

        private fun update(toggle: Toggle) {
            block.type = toggle.switchType
        }

        override fun onInteract(challenge: Challenge, traceur: Traceur, event: PlayerInteractEvent) {
            changeState(challenge)
        }

        override fun onHit(challenge: Challenge, traceur: Traceur, event: ProjectileHitEvent) {
            changeState(challenge)
        }

        override fun onExplode(challenge: Challenge, event: EntityExplodeEvent) {
            println("EXPLODE")

            changeState(challenge)
        }

        internal fun changeState(challenge: Challenge) {
            val currentTicks = Tick.currentTicks

            if (challenge.toggleDelayTicks > currentTicks)
                return

            challenge.toggleDelayTicks = currentTicks + 10L
            challenge.toggle = challenge.toggle.next()

            when (val type = block.type) {
                Toggle.RED.switchType -> {
                    Toggle.BLUE
                }
                Toggle.BLUE.switchType -> {
                    Toggle.RED
                }
                else -> error("Non switch type $type")
            }.let { toggle ->

                val loc = block.location.add(0.5, 0.0, 0.5)

                challenge.traceurs.forEach {
                    it.player?.playSound(loc, Sound.BLOCK_NOTE_BLOCK_BIT, SoundCategory.MASTER, 0.8F, 2.0F)
                }

                block.type = toggle.switchType
                challenge.dataMap[ParkourBlocks.SWITCH]?.forEach {
                    it as SwitchData
                    it.update(toggle)
                }
                challenge.dataMap[ParkourBlocks.TOGGLE]?.forEach {
                    it as ToggleBlock.ToggleData
                    it.update(toggle)
                }
            }
        }
    }
}

class ToggleBlock : ParkourBlock() {
    override fun newBlockData(block: Block): ParkourBlockData {
        return ToggleData()
    }

    class ToggleData : ParkourBlockData() {
        private lateinit var toggle: Toggle
        private lateinit var fakeStand: FakeEntity
        private lateinit var fakeBlock: FakeEntity

        override fun onInitialize(challenge: Challenge) {
            toggle = when (block.type) {
                Toggle.RED.blockType -> Toggle.RED
                Toggle.BLUE.blockType -> Toggle.BLUE
                else -> error("Unknown toggle block: $block")
            }

            ParkourMaker.fakeEntityServer.run {
                val loc = block.location.add(0.5, 0.0, 0.5)
                fakeStand = spawnEntity(loc, ArmorStand::class.java).apply {
                    updateMetadata<ArmorStand> {
                        invisible = true
                        isMarker = true
                    }
                }
                fakeBlock = spawnFallingBlock(loc, toggle.fakeData).apply {
                    fakeStand.addPassenger(this)
                }
            }

            update(challenge.toggle)
        }

        override fun destroy() {
            fakeBlock.remove()
            fakeStand.remove()
        }

        internal fun update(toggle: Toggle) {
            if (this.toggle === toggle) {
                block.type = toggle.blockType
                fakeBlock.isVisible = false
            } else {
                block.type = Material.AIR
                fakeBlock.isVisible = true
            }
        }
    }
}

class SandBlock : ParkourBlock() {
    override fun newBlockData(block: Block): ParkourBlockData {
        return SandBlockData()
    }

    class SandBlockData : ParkourBlockData(), Runnable {
        private lateinit var stand: FakeEntity
        private lateinit var fallingBlock: FakeEntity
        private lateinit var shulker: FakeEntity
        private var falling = false

        private lateinit var task: ParkourTask
        private var fallingSpeed = 0.05

        override fun onInitialize(challenge: Challenge) {
            val loc = block.location.add(0.5, 0.0, 0.5)
            val fakeServer = ParkourMaker.fakeEntityServer

            stand = fakeServer.spawnEntity(loc, ArmorStand::class.java)
            fallingBlock = fakeServer.spawnFallingBlock(loc, block.blockData)
            shulker = fakeServer.spawnEntity(loc, Shulker::class.java)

            stand.updateMetadata<ArmorStand> {
                isMarker = true
                invisible = true
            }

            shulker.updateMetadata<Shulker> {
                setAI(false)
                invisible = true
            }

            stand.addPassenger(fallingBlock)
            stand.addPassenger(shulker)
        }

        override fun onStep(challenge: Challenge, traceur: Traceur, event: PlayerMoveEvent) {
            if (falling) return

            falling = true
            block.type = Material.AIR
            task = challenge.runTaskTimer(this, 0L, 1L)
        }

        override fun run() {
            stand.move(0.0, -fallingSpeed, 0.0)
            fallingSpeed = min(0.2, fallingSpeed + 0.01)

            val loc = stand.location.add(0.0, 1.0, 0.0)

            if (loc.y < 0 || !loc.block.getRelative(BlockFace.UP).type.isAir) {
                task.cancel()
                shulker.remove()
                fallingBlock.remove()
                stand.remove()
            }
        }

        override fun destroy() {
            shulker.remove()
            fallingBlock.remove()
            stand.remove()
        }
    }
}

class SoulFireBlock : ParkourBlock() {
    companion object {
        val firework = FireworkEffect.builder().with(FireworkEffect.Type.STAR).withColor(Color.AQUA).build()
    }

    override fun newBlockData(block: Block): ParkourBlockData {
        return SoulFireBlockData()
    }

    class SoulFireBlockData : ParkourBlockData() {
        override fun onFire(challenge: Challenge, traceur: Traceur, event: EntityDamageEvent) {
            event.isCancelled = true

            traceur.player?.let { player ->
                val loc = player.location.add(0.0, 0.9, 0.0)
                loc.world.playFirework(loc, firework)

                player.health = player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.value ?: 20.0
                player.foodLevel = 20
                player.saturation = 4.0F
                challenge.respawns[traceur]?.let { player.teleport(it.respawn) }
            }
        }
    }
}

class TrampolineBlock : ParkourBlock() {
    override fun newBlockData(block: Block): ParkourBlockData {
        return TrampolineBlockData()
    }

    class TrampolineBlockData : ParkourBlockData(), Runnable {
        override fun onStep(challenge: Challenge, traceur: Traceur, event: PlayerMoveEvent) {
            
            traceur.player?.let { player ->
                player.addPotionEffect(PotionEffect(PotionEffectType.LEVITATION, 20, 9, true, false, true))
                player.location.world.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_HARP, 1.0F, 1.0F)
                }
                
            }
        }
    }
}

// 공식버전 업데이트 이전까지 파일 하나에서 관리
