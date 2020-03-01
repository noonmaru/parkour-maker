package com.github.noonmaru.parkourmaker.command

import com.github.noonmaru.tap.command.ArgumentList
import com.github.noonmaru.tap.command.CommandComponent
import com.sk89q.worldedit.IncompleteRegionException
import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitAdapter
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class CommandLevel : CommandComponent {
    override fun test(sender: CommandSender): (() -> String)? {
        if (sender is Player) return null

        return { "콘솔에서 사용 할 수 없는 명령입니다." }
    }

    override fun onCommand(sender: CommandSender, label: String, componentLabel: String, args: ArgumentList): Boolean {
        sender as Player
        val player = BukkitAdapter.adapt(sender)
        val session = WorldEdit.getInstance().sessionManager[player]
        try {
            val region = session.getSelection(session.selectionWorld)
            sender.sendMessage("$region")
        } catch (e: IncompleteRegionException) {
            sender.sendMessage(e.localizedMessage)
        }

        return true
    }
}