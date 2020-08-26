package com.github.noonmaru.parkourmaker.task

import com.github.noonmaru.parkourmaker.util.Tick
import java.util.*
import kotlin.math.max

class ParkourTask internal constructor(
    private val scheduler: ParkourScheduler, val runnable: Runnable, delay: Long
) : Comparable<ParkourTask> {

    companion object {
        internal const val ERROR = 0L
        internal const val NO_REPEATING = -1L
        internal const val CANCEL = -2L
        internal const val DONE = -3L
    }

    internal var nextRun: Long = Tick.currentTicks + max(0L, delay)

    internal var period: Long = 0L

    val isScheduled: Boolean
        get() = period.let { it != ERROR && it > CANCEL }

    val isCancelled
        get() = period == CANCEL

    val isDone
        get() = period == DONE

    internal fun execute() {
        runnable.runCatching { run() }
    }

    fun cancel() {
        if (!isScheduled) return

        period = CANCEL

        //256 tick 이상이면 큐에서 즉시 제거, 아닐경우 자연스럽게 제거
        val remainTicks = nextRun - Tick.currentTicks

        if (remainTicks > 0xFF)
            scheduler.remove(this)
    }

    override fun compareTo(other: ParkourTask): Int {
        return nextRun.compareTo(other.nextRun)
    }
}

class ParkourScheduler : Runnable {
    private val queue = PriorityQueue<ParkourTask>()

    fun runTask(runnable: Runnable, delay: Long): ParkourTask {
        ParkourTask(this, runnable, delay).apply {
            this.period = ParkourTask.NO_REPEATING
            queue.offer(this)
            return this
        }
    }

    fun runTaskTimer(runnable: Runnable, delay: Long, period: Long): ParkourTask {
        ParkourTask(this, runnable, delay).apply {
            this.period = max(1L, period)
            queue.offer(this)
            return this
        }
    }

    override fun run() {
        val current = Tick.currentTicks

        while (queue.isNotEmpty()) {
            val task = queue.peek()

            if (task.nextRun > current)
                break

            queue.remove()

            if (task.isScheduled) {

                task.run {
                    execute()
                    if (period > 0) {
                        nextRun = current + period
                        queue.offer(task)
                    } else {
                        period == ParkourTask.DONE
                    }
                }
            }
        }
    }

    internal fun cancelAll() {
        val queue = this.queue
        queue.forEach { it.period = ParkourTask.CANCEL }
        queue.clear()
    }

    fun remove(task: ParkourTask) {
        queue.remove(task)
    }
}