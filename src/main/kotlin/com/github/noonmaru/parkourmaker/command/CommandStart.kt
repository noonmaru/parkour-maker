package com.github.noonmaru.parkourmaker.command

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
    override val argsCount: Int = 1

    override fun onCommand(sender: CommandSender, label: String, componentLabel: String, args: ArgumentList): Boolean {
        val name = args.next()
        val level = ParkourMaker.levels[name]

        if (level == null) {
            sender.sendMessage("$name 레벨을 찾지 못했습니다.")
            return true
        }

        val challenge = level.challenge ?: level.startChallenge()

        if (args.hasNext()) {
            do {
                val playerName = args.next()
                Bukkit.getPlayerExact(playerName)?.run {
                    challenge.addTraceur(traceur)
                    gameMode = GameMode.ADVENTURE
                    challenge.respawns[traceur]?.let { teleport(it.respawn) }
                } ?: sender.sendMessage("$playerName 플레이어를 찾지 못했습니다.")
            } while (args.hasNext())
        } else {
            if (sender is Player) {
                challenge.addTraceur(sender.traceur)
                sender.gameMode = GameMode.ADVENTURE
                challenge.respawns[sender.traceur]?.let { sender.teleport(it.respawn) }
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