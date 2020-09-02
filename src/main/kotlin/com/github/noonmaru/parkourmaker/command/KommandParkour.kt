package com.github.noonmaru.parkourmaker.command

import com.github.noonmaru.kommand.KommandBuilder
import com.github.noonmaru.kommand.KommandContext
import com.github.noonmaru.kommand.argument.KommandArgument
import com.github.noonmaru.kommand.argument.player
import com.github.noonmaru.kommand.argument.string
import com.github.noonmaru.kommand.sendFeedback
import com.github.noonmaru.parkourmaker.Level
import com.github.noonmaru.parkourmaker.ParkourMaker
import com.github.noonmaru.parkourmaker.traceur
import com.github.noonmaru.parkourmaker.util.selection
import com.sk89q.worldedit.regions.CuboidRegion
import org.bukkit.GameMode
import org.bukkit.attribute.Attribute
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object KommandParkour {
    fun register(builder: KommandBuilder) {
        builder.apply {
            then("create") {
                then("name" to string()) {
                    require { this is Player }
                    executes {
                        create(it.sender as Player, it.parseArgument("name"))
                    }
                }
            }
            then("remove") {
                then("level" to LevelArgument) {
                    executes {
                        remove(it.sender, it.parseArgument("level"))
                    }
                }
            }
            then("start") {
                then("level" to LevelArgument) {
                    then("player" to player()) {
                        executes {
                            start(it.sender, it.parseArgument("level"), it.parseArgument("player"))
                        }
                    }
                    require {
                        this is Player
                    }
                    executes {
                        start(it.sender, it.parseArgument("level"), it.sender as Player)
                    }
                }
            }
            then("quit") {
                require { this is Player }
                executes {
                    quit(it.sender, this as Player)
                }
                then("player" to player()) {
                    executes {
                        quit(it.sender, it.parseArgument("player"))
                    }
                }
            }
            then("stop") {
                then("level" to LevelArgument) {
                    executes {
                        stop(it.sender, it.parseArgument("level"))
                    }
                }
            }
        }
    }

    private fun create(sender: Player, name: String) {
        sender.selection?.let { region ->
            if (region !is CuboidRegion) {
                sender.sendFeedback("파쿠르 코스로 지원하지 않는 구역입니다. $region")
            } else {
                ParkourMaker.runCatching {
                    createLevel(name, region)
                }.onSuccess {
                    sender.sendFeedback("${it.name} 파쿠르 레벨을 생성했습니다.")
                }.onFailure {
                    sender.sendFeedback("$name 파쿠르 레벨 생성을 실패했습니다. ${it.message}")
                }
            }
        } ?: sender.sendFeedback("파쿠르 레벨을 생성할 구역을 WorldEdit의 Wand로 지정해주세요")
    }

    private fun remove(sender: CommandSender, level: Level) {
        level.remove()
        sender.sendFeedback("${level.name} 파쿠르 레벨을 제거했습니다.")
    }

    private fun start(sender: CommandSender, level: Level, player: Player) {
        val challenge = level.startChallenge()
        player.run {
            health = requireNotNull(getAttribute(Attribute.GENERIC_MAX_HEALTH)).value
            foodLevel = 20
            saturation = 4.0F
            challenge.addTraceur(traceur)
            gameMode = GameMode.ADVENTURE
            challenge.respawns[traceur]?.let { teleport(it.respawn) }
        }
    }

    private fun quit(sender: CommandSender, player: Player) {
        player.traceur.apply {
            challenge?.let {
                it.removeTraceur(this)
                sender.sendFeedback("${player.name}(은)는 ${it.level.name} 레벨 도전을 포기했습니다.")
            } ?: sender.sendFeedback("${player.name}(은)는 도전중인 레벨이 없습니다.")
        }
    }

    private fun stop(sender: CommandSender, level: Level) {
        if (level.challenge == null) {
            sender.sendFeedback("도전 진행중이 아닙니다.")
        } else {
            level.stopChallenge()
            sender.sendFeedback("${level.name} 도전을 종료했습니다.")
        }
    }
}

object LevelArgument : KommandArgument<Level> {
    override fun parse(context: KommandContext, param: String): Level? {
        return ParkourMaker.levels[param]
    }

    override fun listSuggestion(context: KommandContext, target: String): Collection<String> {
        return ParkourMaker.levels.keys.filter { it.startsWith(target, true) }
    }
}