package com.github.noonmaru.parkourmaker.block

import org.bukkit.block.Block

abstract class ParkourBlock {
    fun createBlockData(block: Block): ParkourBlockData {
        return newBlockData(block).apply {
            this.block = block
            parkourBlock = this@ParkourBlock
        }
    }

    protected abstract fun newBlockData(block: Block): ParkourBlockData
}