package com.github.noonmaru.parkourmaker

import org.bukkit.entity.Player

class Traceur(_player: Player) {
    val name = _player.name
    var player: Player? = _player
    var challenge: Challenge? = null
        internal set
}