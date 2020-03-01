package com.github.noonmaru.parkourmaker

import com.github.noonmaru.tap.fake.FakeArmorStand
import com.github.noonmaru.tap.fake.FakeFallingBlock
import org.bukkit.DyeColor
import org.bukkit.Material
import org.bukkit.block.Banner
import org.bukkit.block.Block
import org.bukkit.block.data.BlockData
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.material.Colorable

object ParkourBlocks {

    val SPAWN = SpawnBlock()
    val CHECKPOINT = CheckpointBlock()
    val SWITCH = SwitchBlock()
    val TOGGLE = ToggleBlock()

    fun getBlock(block: Block): ParkourBlock? {
        val state = block.state
        val data = block.blockData
        val type = data.material

        if (type == Material.EMERALD) {
            return SPAWN
        }
        else if (state is Banner) {
            return CHECKPOINT
        }
        else if (type == Material.SHULKER_BOX) {
            data as Colorable
            val color = data.color

            if (color == Toggle.RED.switchColor || color == Toggle.BLUE.switchColor) {
                return SWITCH
            }
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

    open fun onStep(challenge: Challenge, event: PlayerMoveEvent) {}

    open fun onInteract(challenge: Challenge, event: PlayerInteractEvent) {}

    open fun destroy() {}
}

class SpawnBlock : ParkourBlock() {
    override fun newBlockData(block: Block): ParkourBlockData {
        return SpawnData()
    }

    class SpawnData : ParkourBlockData() {
        val location
            get() = block.location.add(0.5, 0.0, 0.5)
    }
}

class CheckpointBlock: ParkourBlock() {
    override fun newBlockData(block: Block): ParkourBlockData {
        return CheckpointData()
    }

    class CheckpointData: ParkourBlockData() {
        val location
            get() = block.location.add(0.5, 0.0, 0.5)
    }
}

enum class Toggle(val blockType: Material, val fakeData: BlockData, val switchColor: DyeColor, val next: () -> Toggle) {
    RED(Material.RED_CONCRETE,
        Material.RED_STAINED_GLASS.createBlockData(),
        DyeColor.RED,
        { BLUE }),
    BLUE(
        Material.LIGHT_BLUE_CONCRETE,
        Material.LIGHT_BLUE_STAINED_GLASS.createBlockData(),
        DyeColor.LIGHT_BLUE,
        { RED });

    val switchData: BlockData = Material.SHULKER_BOX.createBlockData {
        it as Colorable
        it.color = switchColor
    }
}

class SwitchBlock : ParkourBlock() {
    override fun newBlockData(block: Block): ParkourBlockData {
        return OnOffData()
    }

    class OnOffData : ParkourBlockData() {
        override fun onInteract(challenge: Challenge, event: PlayerInteractEvent) {
            challenge.toggle = challenge.toggle.next()

            val blockData = block.blockData

            if (blockData.material == Material.SHULKER_BOX) {
                blockData as Colorable

                when (val color = blockData.color) {
                    Toggle.RED.switchColor -> {
                        Toggle.RED
                    }
                    Toggle.BLUE.switchColor -> {
                        Toggle.BLUE
                    }
                    else -> error("Unknown switch color: $color")
                }.let { toggle ->
                    block.blockData = toggle.switchData
                    challenge.dataMap[ParkourBlocks.TOGGLE]?.forEach { data ->
                        data as ToggleBlock.ToggleData
                        data.update(toggle)
                    }
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
                    fakeStand.addPassenger(fakeBlock)
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



