package com.github.noonmaru.parkourmaker.block

import com.github.noonmaru.parkourmaker.Traceur
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Color
import org.bukkit.FireworkEffect
import org.bukkit.block.Block
import org.bukkit.entity.Firework

class ClearBlock : ParkourBlock() {
    override fun newBlockData(block: Block): ParkourBlockData {
        return ClearData()
    }

    class ClearData : ParkourBlockData() {
        override fun onStep(traceur: Traceur) {
            val level = traceur.challenge?.level?: return
            level.stopChallenge()

            Bukkit.getOnlinePlayers().forEach {
                it.sendTitle(
                    "${ChatColor.AQUA}${ChatColor.BOLD}COURSE CLEAR",
                    "${ChatColor.RED}${ChatColor.BOLD}${traceur.name}${ChatColor.RESET}님이 ${ChatColor.GOLD}${level.name} ${ChatColor.RESET}레벨을 클리어!",
                    5,
                    90,
                    5
                )
            }

            val loc = block.location.add(0.5, 1.0, 0.5)
            loc.world.spawn(loc, Firework::class.java).apply {
                fireworkMeta = fireworkMeta.apply {
                    addEffect(FireworkEffect.builder().apply {
                        flicker(true)
                        trail(true)
                        with(FireworkEffect.Type.STAR)
                        withColor(Color.AQUA)
                        withColor(Color.RED)
                        withColor(Color.GREEN)
                        withColor(Color.YELLOW)
                        withFade(Color.WHITE)
                    }.build())
                    power = 1
                }
            }
        }
    }
}