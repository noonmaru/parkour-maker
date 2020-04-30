package com.github.noonmaru.parkourmaker.block

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Directional

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
                        val directional = down.blockData as Directional

                        loc.yaw = when (directional.facing) {
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