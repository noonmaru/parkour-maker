package com.github.noonmaru.parkourmaker

class ParkourPluginScheduler : Runnable {
    override fun run() {
        ParkourMaker.fakeEntityServer.update()

        for (level in ParkourMaker.levels.values) {
            level.challenge?.update()
        }
    }
}