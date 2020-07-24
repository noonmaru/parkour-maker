package com.github.noonmaru.parkourmaker.block

import org.bukkit.Material
import org.bukkit.block.Banner
import org.bukkit.block.Block

object ParkourBlocks {
    private val SPAWN = SpawnBlock()
    private val CLEAR = ClearBlock()
    private val CHECKPOINT = CheckpointBlock()
    val SWITCH = SwitchBlock()
    val TOGGLE = ToggleBlock()

    fun getBlock(block: Block): ParkourBlock? {
        val state = block.state
        val data = block.blockData
        val type = data.material

        if (state is Banner) return CHECKPOINT

        // removed unnecessary null return
        return when (type) {
            Material.EMERALD_BLOCK -> SPAWN
            Material.GOLD_BLOCK -> CLEAR
            Material.RED_SHULKER_BOX, Material.LIGHT_BLUE_SHULKER_BOX -> SWITCH
            Material.RED_CONCRETE, Material.LIGHT_BLUE_CONCRETE -> TOGGLE
            else -> null
        }
    }
}