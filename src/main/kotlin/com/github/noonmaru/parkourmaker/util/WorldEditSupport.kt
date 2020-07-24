package com.github.noonmaru.parkourmaker.util

import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.regions.CuboidRegion
import com.sk89q.worldedit.regions.Region
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.util.BoundingBox
import org.bukkit.util.Vector

object WorldEditSupport {
    val Player.selection: Region?
        get() {
            return try {
                WorldEdit.getInstance().sessionManager[BukkitAdapter.adapt(this)]?.run {
                    getSelection(selectionWorld)
                }
            } catch (e: Exception) {
                null
            }
        }


    fun Block.getBlockVector3(): BlockVector3 {
            return BlockVector3.at(x, y, z)
        }

    fun CuboidRegion.toBoundingBox(): BoundingBox {
        return BoundingBox.of(minimumPoint.toVector(), maximumPoint.toVector().apply {
            x += 1
            y += 1
            z += 1
        })
    }

    private fun BlockVector3.toVector(): Vector {
        return Vector(x, y, z)
    }
}