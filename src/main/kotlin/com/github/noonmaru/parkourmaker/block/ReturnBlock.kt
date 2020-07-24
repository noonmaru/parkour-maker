package com.github.noonmaru.parkourmaker.block

import com.github.noonmaru.parkourmaker.Traceur
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Color
import org.bukkit.FireworkEffect
import org.bukkit.block.Block
import org.bukkit.entity.Firework

class ReturnBlock : ParkourBlock() {
    override fun newBlockData(block: Block): ParkourBlockData {
        return ReturnData()
    }

    class ReturnData : ParkourBlockData() {
        override fun onStep(traceur: Traceur) {
            traceur.player?.teleport(traceur.challenge!!.respawns[traceur]?.respawn?: SpawnBlock.SpawnData().respawn)
        }
    }
}