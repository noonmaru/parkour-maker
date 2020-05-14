package com.github.noonmaru.parkourmaker.block

import com.github.noonmaru.parkourmaker.Challenge
import com.github.noonmaru.parkourmaker.Traceur
import org.bukkit.block.Block

abstract class ParkourBlockData {
    lateinit var block: Block

    lateinit var parkourBlock: ParkourBlock
        internal set

    open fun onInitialize(challenge: Challenge) {}

    open fun onPass(traceur: Traceur) {}

    open fun onStep(traceur: Traceur) {}

    open fun changeState(challenge: Challenge) {}

    open fun destroy() {}
}