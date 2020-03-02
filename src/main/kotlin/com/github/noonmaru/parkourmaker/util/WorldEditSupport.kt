package com.github.noonmaru.parkourmaker.util

import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.regions.CuboidRegion
import com.sk89q.worldedit.regions.Region
import org.bukkit.entity.Player
import org.bukkit.util.BoundingBox
import org.bukkit.util.Vector

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

fun CuboidRegion.toBoundingBox(): BoundingBox {
    return BoundingBox.of(minimumPoint.toVector(), maximumPoint.toVector().apply {
        x += 1
        y += 1
        z += 1
    })
}

fun BlockVector3.toVector(): Vector {
    return Vector(x, y, z)
}