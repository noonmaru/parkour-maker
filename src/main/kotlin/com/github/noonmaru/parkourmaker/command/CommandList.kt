package com.github.noonmaru.parkourmaker.command

import com.github.noonmaru.parkourmaker.ParkourMaker
import com.github.noonmaru.tap.command.ArgumentList
import com.github.noonmaru.tap.command.CommandComponent
import org.bukkit.command.CommandSender

class CommandList : CommandComponent {
    override fun onCommand(sender: CommandSender, label: String, componentLabel: String, args: ArgumentList): Boolean {
        sender.sendMessage(ParkourMaker.levels.values.joinToString { it.name }) // style: lambda
        return true
    }
}