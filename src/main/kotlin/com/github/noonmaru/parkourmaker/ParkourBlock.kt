package com.github.noonmaru.parkourmaker

import com.github.noonmaru.tap.fake.FakeArmorStand
import com.github.noonmaru.tap.fake.FakeFallingBlock
import org.bukkit.*
import org.bukkit.block.Banner
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.Directional
import org.bukkit.entity.Firework
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent

object ParkourBlocks {

    val SPAWN = SpawnBlock()
    val CLEAR = ClearBlock()
    val CHECKPOINT = CheckpointBlock()
    val SWITCH = SwitchBlock()
    val TOGGLE = ToggleBlock()

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

    open fun onInteract(challenge: Challenge, traceur: Traceur, event: PlayerInteractEvent) {}

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
        private lateinit var fakeStand: FakeArmorStand
        private lateinit var fakeBlock: FakeFallingBlock

        override fun onInitialize(challenge: Challenge) {
            toggle = when (block.type) {
                Toggle.RED.blockType -> Toggle.RED
                Toggle.BLUE.blockType -> Toggle.BLUE
                else -> error("Unknown toggle block: $block")
            }

            ParkourMaker.fakeManager.run {
                val loc = block.location.add(0.5, 0.0, 0.5)
                fakeStand = createFakeEntity<FakeArmorStand>(loc).apply {
                    invisible = true
                    mark = true
                }
                fakeBlock = createFallingBlock(loc, toggle.fakeData).apply {
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
                fakeBlock.show = false
            } else {
                block.type = Material.AIR
                fakeBlock.show = true
            }
        }
    }
}