package com.github.noonmaru.parkourmaker.block

import com.github.noonmaru.parkourmaker.Challenge
import com.github.noonmaru.parkourmaker.ParkourMaker
import com.github.noonmaru.tap.fake.FakeArmorStand
import com.github.noonmaru.tap.fake.FakeFallingBlock
import org.bukkit.Material
import org.bukkit.block.Block

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