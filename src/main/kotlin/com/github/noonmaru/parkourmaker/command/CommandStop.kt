package com.github.noonmaru.parkourmaker.command

import com.github.noonmaru.parkourmaker.ParkourMaker
import com.github.noonmaru.tap.command.ArgumentList
import com.github.noonmaru.tap.command.CommandComponent
import com.github.noonmaru.tap.command.tabComplete
import org.bukkit.command.CommandSender

class CommandStop : CommandComponent {
    override fun onCommand(sender: CommandSender, label: String, componentLabel: String, args: ArgumentList): Boolean {
        // fix: exception thrown when ArgumentList is empty
        if (!args.hasNext()) { // bug: ArgumentList::isEmpty does not return true
            return false
        } else {
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
        }

        return true
    }

    override fun onTabComplete(sender: CommandSender, label: String, componentLabel: String, args: ArgumentList): List<String> {
        return ParkourMaker.levels.keys.tabComplete(args.last()).toMutableList()
    }
}