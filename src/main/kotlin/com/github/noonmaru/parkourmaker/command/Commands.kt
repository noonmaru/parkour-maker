package com.github.noonmaru.parkourmaker.command

import com.github.noonmaru.parkourmaker.ParkourMaker
import com.github.noonmaru.parkourmaker.traceur
import com.github.noonmaru.parkourmaker.util.selection
import com.github.noonmaru.tap.command.ArgumentList
import com.github.noonmaru.tap.command.CommandComponent
import com.github.noonmaru.tap.command.tabComplete
import com.sk89q.worldedit.regions.CuboidRegion
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.GameMode
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

class CommandRemove : CommandComponent {
    override val argsCount: Int = 1

    override fun onCommand(sender: CommandSender, label: String, componentLabel: String, args: ArgumentList): Boolean {
        do {
            val name = args.next()
            ParkourMaker.levels[name]?.let { level ->
                level.remove()
                sender.sendMessage("${ChatColor.BOLD}$name ${ChatColor.RESET} 레벨을 제거했습니다.")
            } ?: sender.sendMessage("$name 생성되지 않은 레벨입니다.")
        } while (args.hasNext())

        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        label: String,
        componentLabel: String,
        args: ArgumentList
    ): List<String> {
        return ParkourMaker.levels.keys.tabComplete(args.last())
    }
}

class CommandList : CommandComponent {
    override fun onCommand(sender: CommandSender, label: String, componentLabel: String, args: ArgumentList): Boolean {
        sender.sendMessage(ParkourMaker.levels.values.joinToString(transform = { it.name }))
        return true
    }
}

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
                }
                    ?: sender.sendMessage("$playerName 플레이어를 찾지 못했습니다.")
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

    override fun onTabComplete(
        sender: CommandSender,
        label: String,
        componentLabel: String,
        args: ArgumentList
    ): List<String> {
        return when (args.remain()) {
            1 -> ParkourMaker.levels.keys.tabComplete(args.next())
            else -> Bukkit.getOnlinePlayers().tabComplete(args.last()) { it.name }
        }
    }
}

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

        if (args.isEmpty()) { // self
            if (sender !is Player) {
                return false
            }

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

    override fun onTabComplete(
        sender: CommandSender,
        label: String,
        componentLabel: String,
        args: ArgumentList
    ): List<String> {
        return Bukkit.getOnlinePlayers().tabComplete(args.last()) { it.name }
    }
}

class CommandStop : CommandComponent {
    override val argsCount: Int = 1

    override fun onCommand(sender: CommandSender, label: String, componentLabel: String, args: ArgumentList): Boolean {
        do {
            val name = args.next()
            val level = ParkourMaker.levels[name]

            if (level == null) {
                sender.sendMessage("$name 레벨을 찾지 못했습니다.")
                return true
            }

            if (level.challenge == null) {
                sender.sendMessage("도전 진행중이 아닙니다.")
                return true
            }

            level.stopChallenge()
            sender.sendMessage("$name 도전을 종료했습니다.")
        } while (args.hasNext())
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        label: String,
        componentLabel: String,
        args: ArgumentList
    ): List<String> {
        return ParkourMaker.levels.keys.tabComplete(args.last())
    }
}