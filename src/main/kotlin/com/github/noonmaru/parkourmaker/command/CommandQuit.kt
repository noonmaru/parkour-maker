package com.github.noonmaru.parkourmaker.command

import com.github.noonmaru.parkourmaker.ParkourMaker.traceur
import com.github.noonmaru.tap.command.ArgumentList
import com.github.noonmaru.tap.command.CommandComponent
import com.github.noonmaru.tap.command.tabComplete
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class CommandQuit : CommandComponent {
    override fun onCommand(sender: CommandSender, label: String, componentLabel: String, args: ArgumentList): Boolean {
        val action = fun(player: Player) {
            player.traceur.apply {
                challenge?.let {
                    it.removeTraceur(this)
                    sender.sendMessage("${player.name}(은)는 ${it.level.name} 레벨 도전을 포기했습니다.")
                } ?: sender.sendMessage("${player.name}(은)는 도전중인 레벨이 없습니다.")
            }
        }

        if (!args.hasNext()) { // bug: ArgumentList::isEmpty does not return true
            if (sender !is Player)
                return false

            action(sender)
        } else {
            do {
                val playerName = args.next()
                Bukkit.getPlayerExact(playerName)?.let {
                    action(it)
                } ?: sender.sendMessage("$playerName 플레이어를 찾지 못했습니다.")
            } while (args.hasNext())
        }

        return true
    }

    override fun onTabComplete(sender: CommandSender, label: String, componentLabel: String, args: ArgumentList): List<String> {
        return Bukkit.getOnlinePlayers().tabComplete(args.last()) { it.name }
    }
}