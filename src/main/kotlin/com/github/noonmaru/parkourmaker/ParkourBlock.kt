package com.github.noonmaru.parkourmaker

import com.github.noonmaru.tap.fake.FakeArmorStand
import com.github.noonmaru.tap.fake.FakeFallingBlock
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.block.Banner
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.Directional
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent

object ParkourBlocks {

    val SPAWN = SpawnBlock()
    val CHECKPOINT = CheckpointBlock()
    val SWITCH = SwitchBlock()
    val TOGGLE = ToggleBlock()

    fun getBlock(block: Block): ParkourBlock? {
        val state = block.state
        val data = block.blockData
        val type = data.material

        if (type == Material.EMERALD_BLOCK) {
            return SPAWN
        } else if (state is Banner) {
            return CHECKPOINT
        } else if (type == Material.RED_SHULKER_BOX || type == Material.LIGHT_BLUE_SHULKER_BOX) {
            return SWITCH
        } else if (type == Material.RED_CONCRETE || type == Material.LIGHT_BLUE_CONCRETE) {
            return TOGGLE
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

    open fun onPass(challenge: Challenge, event: PlayerMoveEvent) {}

    open fun onInteract(challenge: Challenge, event: PlayerInteractEvent) {}

    open fun destroy() {}
}

class SpawnBlock : ParkourBlock() {
    override fun newBlockData(block: Block): ParkourBlockData {
        return SpawnData()
    }

    class SpawnData : ParkourBlockData() {
        val location: Location
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

class CheckpointBlock : ParkourBlock() {
    override fun newBlockData(block: Block): ParkourBlockData {
        return CheckpointData()
    }

    class CheckpointData : ParkourBlockData() {
        val location
            get() = block.location.add(0.5, 1.0, 0.5)

        override fun onPass(challenge: Challenge, event: PlayerMoveEvent) {
            challenge._spawns[event.player.traceur!!] = location
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

        override fun onInteract(challenge: Challenge, event: PlayerInteractEvent) {
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
                    it.player?.let { p ->
                        p.playSound(loc, Sound.BLOCK_NOTE_BLOCK_BIT, SoundCategory.MASTER, 0.8F, 2.0F)
                    }
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