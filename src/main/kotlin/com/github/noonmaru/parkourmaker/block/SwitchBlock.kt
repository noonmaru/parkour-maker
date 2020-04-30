package com.github.noonmaru.parkourmaker.block

import com.github.noonmaru.parkourmaker.Challenge
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.block.Block

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

        override fun changeState(challenge: Challenge) {
            challenge.toggle = challenge.toggle.next()

            when (val type = block.type) {
                Toggle.RED.switchType -> Toggle.BLUE
                Toggle.BLUE.switchType -> Toggle.RED
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