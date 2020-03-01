package com.github.noonmaru.parkourmaker.command

import com.github.noonmaru.parkourmaker.Level
import com.github.noonmaru.parkourmaker.ParkourMaker
import com.github.noonmaru.parkourmaker.traceur
import com.github.noonmaru.tap.command.ArgumentList
import com.github.noonmaru.tap.command.CommandComponent
import com.sk89q.worldedit.IncompleteRegionException
import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.regions.CuboidRegion
import com.sk89q.worldedit.regions.Region
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

            if (name in ParkourMaker.levels) {
                sender.sendMessage("$name(은)는 이미 등록된 레벨입니다.")
                return true
            }

            ParkourMaker._levels[name] = Level(name, region).apply {
                copyCourse()
                save()
            }
            sender.sendMessage("파쿠르 레벨을 생성했습니다. $name $region")
        } ?: sender.sendMessage("${ChatColor.RED}먼저 WorldEdit의 Wand로 구역을 지정해주세요.")

        return true
    }
}

val Player.selection: Region?
    get() {
        return WorldEdit.getInstance().sessionManager[BukkitAdapter.adapt(this)]?.run {
            try {
                getSelection(selectionWorld)
            } catch (e: IncompleteRegionException) {
                null
            }
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

        if (level.challenge != null) {
            sender.sendMessage("이미 도전이 진행중입니다.")
            return true
        }

        val challenge = level.startChallenge()

        if (args.hasNext()) {
            do {
                val playerName = args.next()
                Bukkit.getPlayerExact(playerName)?.run {
                    challenge.addTraceur(traceur!!)
                    gameMode = GameMode.ADVENTURE
                    challenge.spawns[traceur!!]?.let { teleport(it) }
                }
                    ?: sender.sendMessage("$playerName 플레이어를 찾지 못했습니다.")
            } while (args.hasNext())
        } else {
            if (sender is Player) {
                challenge.addTraceur(sender.traceur!!)
                sender.gameMode = GameMode.ADVENTURE
                challenge.spawns[sender.traceur!!]?.let { sender.teleport(it) }
            }
        }

        return true
    }
}

class CommandStop : CommandComponent {
    override val argsCount: Int = 1

    override fun onCommand(sender: CommandSender, label: String, componentLabel: String, args: ArgumentList): Boolean {
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
        return true
    }
}