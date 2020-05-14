package com.github.noonmaru.parkourmaker.command

import com.github.noonmaru.parkourmaker.ParkourMaker
import com.github.noonmaru.parkourmaker.util.WorldEditSupport.selection
import com.github.noonmaru.tap.command.ArgumentList
import com.github.noonmaru.tap.command.CommandComponent
import com.sk89q.worldedit.regions.CuboidRegion
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class CommandCreate : CommandComponent {
    override val argsCount: Int = 1

    override fun test(sender: CommandSender): (() -> String)? {
        if (sender is Player) return null
        return { "콘솔에서 사용 할 수 없는 명령입니다." }
    }

    override fun onCommand(sender: CommandSender, label: String, componentLabel: String, args: ArgumentList): Boolean {
        sender as Player

        sender.selection?.let { region ->
            if (region !is CuboidRegion) {
                sender.sendMessage("파쿠르 코스로 지원하지 않는 구역입니다. $region")
                return true
            }

            val name = args.next()

            try {
                ParkourMaker.createLevel(name, region)
                sender.sendMessage("${ChatColor.BOLD}$name ${ChatColor.RESET} 레벨을 생성했습니다.")
            } catch (e: Exception) {
                sender.sendMessage("${ChatColor.BOLD}$name ${ChatColor.RESET} 레벨을 생성하지 못했습니다. ${ChatColor.GRAY}${e.message}")
            }
        } ?: sender.sendMessage("${ChatColor.RED}먼저 WorldEdit의 Wand로 구역을 지정해주세요.")

        return true
    }
}