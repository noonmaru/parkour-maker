package com.github.noonmaru.parkourmaker.block

import org.bukkit.Material
import org.bukkit.block.data.BlockData

enum class Toggle(val blockType: Material, val fakeData: BlockData, val switchType: Material, val next: () -> Toggle) {
    RED(Material.RED_CONCRETE, Material.RED_STAINED_GLASS.createBlockData(), Material.RED_SHULKER_BOX, { BLUE }),
    BLUE(Material.LIGHT_BLUE_CONCRETE, Material.LIGHT_BLUE_STAINED_GLASS.createBlockData(), Material.LIGHT_BLUE_SHULKER_BOX, { RED });
}