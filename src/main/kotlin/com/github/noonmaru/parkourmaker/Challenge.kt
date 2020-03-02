package com.github.noonmaru.parkourmaker

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMap
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.regions.Region
import org.bukkit.block.Block


class Challenge(val level: Level) {

    lateinit var dataMap: Map<ParkourBlock, Set<ParkourBlockData>>

    lateinit var dataByBlock: Map<Block, ParkourBlockData>

    lateinit var startLocs: List<SpawnBlock.SpawnData>

    private var _traceurs = HashSet<Traceur>()

    val traceurs: Set<Traceur>
        get() = _traceurs

    private var _respawns = HashMap<Traceur, Respawnable>()

    val respawns: Map<Traceur, Respawnable>
        get() = _respawns

    var toggle = Toggle.RED
        internal set

    var valid = true
        private set

    internal fun parseBlocks() {
        checkState()

        val dataMap = HashMap<ParkourBlock, HashSet<ParkourBlockData>>()
        val dataByBlock = HashMap<Block, ParkourBlockData>()
        val startLocs = ArrayList<SpawnBlock.SpawnData>()

        level.region.forEachBlocks { block ->
            ParkourBlocks.getBlock(block)?.let { parkourBlock ->
                val data = parkourBlock.createBlockData(block).apply {
                    onInitialize(this@Challenge)
                }
                dataMap.computeIfAbsent(parkourBlock) { HashSet() } += data
                dataByBlock[block] = data

                if (data is SpawnBlock.SpawnData) {
                    startLocs.add(data)
                }
            }
        }

        this.dataMap = ImmutableMap.copyOf(dataMap)
        this.dataByBlock = ImmutableMap.copyOf(dataByBlock)
        this.startLocs = ImmutableList.copyOf(startLocs)
    }

    fun addTraceur(traceur: Traceur) {
        checkState()

        traceur.challenge?.let {
            if (this == this)
                return

            it.removeTraceur(traceur)
        }

        if (_traceurs.add(traceur)) {
            startLocs.let { locs ->
                if (locs.isNotEmpty()) {
                    _respawns[traceur] = locs.random()
                }
            }

            traceur.challenge = this
        }
    }

    fun removeTraceur(traceur: Traceur) {
        checkState()

        if (_traceurs.remove(traceur)) {
            _respawns.remove(traceur)
            traceur.challenge = null
        }
    }

    internal fun destroy() {
        checkState()

        valid = false

        _traceurs.apply {
            forEach { it.challenge = null }
            clear()
        }
        _respawns.clear()

        dataByBlock.values.forEach {
            it.destroy()
        }
    }

    fun checkState() {
        check(this.valid) { "Invalid $this" }
    }

    internal fun setSpawn(traceur: Traceur, respawnable: Respawnable): Respawnable? {
        return _respawns.put(traceur, respawnable)
    }
}

fun Region.forEachBlocks(action: (Block) -> Unit) {
    val w = BukkitAdapter.asBukkitWorld(world).world

    forEach {
        action.invoke(w.getBlockAt(it.x, it.y, it.z))
    }
}