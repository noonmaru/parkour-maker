package com.github.noonmaru.parkourmaker.command

import com.github.noonmaru.parkourmaker.ParkourMaker
import com.github.noonmaru.tap.command.ArgumentList
import com.github.noonmaru.tap.command.CommandComponent
import com.github.noonmaru.tap.command.tabComplete
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender

class CommandRemove : CommandComponent {
    override fun onCommand(sender: CommandSender, label: String, componentLabel: String, args: ArgumentList): Boolean {
        // fix: exception thrown when ArgumentList is empty
        if (!args.hasNext()) { // bug: ArgumentList::isEmpty does not return true
            return false
        } else {
            do {
                val name = args.next()
                ParkourMaker.levels[name]?.let { level ->
                    level.remove()
                    sender.sendMessage("${ChatColor.BOLD}$name ${ChatColor.RESET} 레벨을 제거했습니다.")
                } ?: sender.sendMessage("$name 레벨을 찾을 수 없습니다.")
            } while (args.hasNext())
        }
        return true
    }

    override fun onTabComplete(sender: CommandSender, label: String, componentLabel: String, args: ArgumentList): List<String> {
        return ParkourMaker.levels.keys.tabComplete(args.last())
    }
}