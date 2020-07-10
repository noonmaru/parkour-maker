package com.github.noonmaru.parkourmaker.command

import com.github.noonmaru.parkourmaker.Challenge
import com.github.noonmaru.parkourmaker.ParkourMaker
import com.github.noonmaru.parkourmaker.ParkourMaker.traceur
import com.github.noonmaru.tap.command.ArgumentList
import com.github.noonmaru.tap.command.CommandComponent
import com.github.noonmaru.tap.command.tabComplete
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class CommandStart : CommandComponent {
    override fun onCommand(sender: CommandSender, label: String, componentLabel: String, args: ArgumentList): Boolean {
        // style: better code style (from CommandQuit.kt)
        val action = fun(player: Player, challenge: Challenge) {
            challenge.addTraceur(player.traceur)
            player.gameMode = GameMode.ADVENTURE
            challenge.respawns[player.traceur]?.let { player.teleport(it.respawn) }
        }

        // fix: exception thrown when ArgumentList is empty
        if (!args.hasNext()) { // bug: ArgumentList::isEmpty does not return true
            return false
        } else {
            val name = args.next()
            val level = ParkourMaker.levels[name]?: run {
                sender.sendMessage("$name 레벨을 찾지 못했습니다.")
                return true
            }

            val challenge = level.challenge ?: level.startChallenge()

            if (!args.hasNext()) {
                if (sender !is Player)
                    return false

                action(sender, challenge)
            } else {
                do {
                    val playerName = args.next()
                    Bukkit.getPlayerExact(playerName)?.let {
                        action(it, challenge)
                    } ?: sender.sendMessage("$playerName 플레이어를 찾지 못했습니다.")
                } while (args.hasNext())
            }
        }

        return true
    }

    override fun onTabComplete(sender: CommandSender, label: String, componentLabel: String, args: ArgumentList): List<String> {
        return when (args.remain()) {
            1 -> ParkourMaker.levels.keys.tabComplete(args.next())
            else -> Bukkit.getOnlinePlayers().tabComplete(args.last()) { it.name }
        }
    }
}