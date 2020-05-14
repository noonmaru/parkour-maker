package com.github.noonmaru.parkourmaker.block

import com.github.noonmaru.parkourmaker.Traceur
import org.bukkit.FireworkEffect
import org.bukkit.Location
import org.bukkit.block.Banner
import org.bukkit.block.Block
import org.bukkit.entity.Firework

class CheckpointBlock : ParkourBlock() {
    override fun newBlockData(block: Block): ParkourBlockData {
        return CheckpointData()
    }

    class CheckpointData : ParkourBlockData(), Respawnable {
        override val respawn: Location
            get() = block.location.add(0.5, 1.0, 0.5)

        override fun onPass(traceur: Traceur) {
            if (traceur.challenge?.setSpawn(traceur, this) != this) {
                val loc = respawn.add(0.0, 4.0, 0.0)
                loc.world.spawn(loc, Firework::class.java).apply {
                    fireworkMeta = fireworkMeta.apply {
                        addEffect(FireworkEffect.builder().apply {
                            with(FireworkEffect.Type.BALL)
                            val blockState = block.state
                            if (blockState is Banner) {
                                withColor(blockState.baseColor.fireworkColor)
                            }
                        }.build())
                        power = 1
                    }
                }
            }
        }
    }
}