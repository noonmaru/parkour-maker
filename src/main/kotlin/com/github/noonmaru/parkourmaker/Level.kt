package com.github.noonmaru.parkourmaker

import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard
import com.sk89q.worldedit.extent.clipboard.Clipboard
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats
import com.sk89q.worldedit.function.operation.ForwardExtentCopy
import com.sk89q.worldedit.function.operation.Operation
import com.sk89q.worldedit.function.operation.Operations
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.regions.CuboidRegion
import com.sk89q.worldedit.session.ClipboardHolder
import org.bukkit.Bukkit
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.util.BoundingBox
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.security.DigestInputStream
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*

class Level {
    val name: String

    private val file: File

    val region: CuboidRegion

    private var clipboard: Clipboard? = null

    var challenge: Challenge? = null
        private set

    private val courseFile: File
        get() = File(ParkourMaker.coursesFolder, "$name.schem")

    private var valid = true

    constructor(name: String, region: CuboidRegion) {
        checkNotNull(region.world) { "Region must have region!" }

        this.name = name
        this.region = region.clone()
        file = File(ParkourMaker.levelFolder, "$name.yml")
    }

    constructor(file: File) {
        name = file.name.removeSuffix(".yml")
        this.file = file

        YamlConfiguration.loadConfiguration(file).run {
            region = CuboidRegion(
                BukkitAdapter.adapt(Bukkit.getWorlds()[0]),
                getBlockVector3("min"),
                getBlockVector3("max")
            )
        }
    }

    init {
        courseFile.run {
            if (exists()) {
                val format = ClipboardFormats.findByFile(this)!!
                clipboard = format.getReader(FileInputStream(this)).use { it.read() }
            }
        }
    }

    fun startChallenge(): Challenge {
        checkState()
        check(challenge == null) { "Challenge is already in progress." }
        copyCourse()

        val challenge = Challenge(this).apply {
            parseBlocks()
        }
        this.challenge = challenge
        return challenge
    }

    internal fun copyCourse() {
        this.clipboard = BlockArrayClipboard(region).apply {
            Operations.complete(
                ForwardExtentCopy(
                    WorldEdit.getInstance().editSessionFactory.getEditSession(
                        region.world,
                        -1
                    ), region, this, region.minimumPoint
                ).apply {
                    isCopyingEntities = true
                })
        }.apply {
            save()
        }
    }

    fun stopChallenge() {
        checkState()

        challenge.let { challenge ->
            checkNotNull(challenge) { "Challenge is not in progress." }
            this.challenge = null
            challenge.destroy()

            val world = BukkitAdapter.asBukkitWorld(region.world).world
            val min = region.minimumPoint.run { world.getBlockAt(x, y, z) }
            val max = region.maximumPoint.run { world.getBlockAt(x, y, z) }
            val box = BoundingBox.of(min, max)
            world.getNearbyEntities(box) { it !is Player }.forEach { it.remove() }
            clipboard?.paste()
        }
    }

    private fun Clipboard.save() {
        val file = courseFile
        val temp = File(file.parentFile.apply { mkdirs() }, "${file.name}.tmp")

        BuiltInClipboardFormat.SPONGE_SCHEMATIC.getWriter(FileOutputStream(temp)).use { writer -> writer.write(this) }

        if (file.exists()) {
            if (file.md5Digest.contentEquals(temp.md5Digest)) {
                Files.copy(
                    file.toPath(),
                    File(
                        ParkourMaker.historyFolder.apply { mkdirs() },
                        "$name${SimpleDateFormat("yyyyMMddHHmmss").format(Date())}.schem"
                    ).toPath(),
                    StandardCopyOption.REPLACE_EXISTING
                )
            }

            file.delete()
        }

        temp.renameTo(file)
        temp.delete()
    }

    private fun Clipboard.paste() {
        val mp = region.minimumPoint

        WorldEdit.getInstance().editSessionFactory.getEditSession(region.world, -1).use { editSession ->
            val operation: Operation = ClipboardHolder(this)
                .createPaste(editSession)
                .to(BlockVector3.at(mp.x, mp.y, mp.z))
                .ignoreAirBlocks(false)
                .build()
            Operations.complete(operation)
        }
    }

    fun save() {
        checkState()

        val config = YamlConfiguration()
        region.let { region ->
            config.set("world", region.world!!.name)
            config.setPoint("min", region.minimumPoint)
            config.setPoint("max", region.maximumPoint)
        }

        file.parentFile.mkdirs()
        config.save(file)
    }

    fun remove() {
        challenge?.run { stopChallenge() }
        valid = false
        file.delete()
        ParkourMaker.removeLevel(this)
    }

    private fun checkState() {
        require(valid) { "Invalid $this" }
    }

    private val File.md5Digest: ByteArray
        get() {
            val md = MessageDigest.getInstance("MD5")
            DigestInputStream(FileInputStream(this).buffered(), md).use {
                while (true)
                    if (it.read() == -1)
                        break
            }
            return md.digest()
        }

    private fun ConfigurationSection.getBlockVector3(path: String): BlockVector3 {
        return getConfigurationSection(path)!!.run {
            BlockVector3.at(
                getInt("x"),
                getInt("y"),
                getInt("z")
            )
        }
    }

    private fun ConfigurationSection.setPoint(path: String, point: BlockVector3) {
        createSection(path).apply {
            this["x"] = point.blockX
            this["y"] = point.blockY
            this["z"] = point.blockZ
        }
    }
}