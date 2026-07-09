package eu.revq

import java.awt.EventQueue
import java.awt.Graphics2D
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import org.freedesktop.dbus.DBusPath
import org.freedesktop.dbus.Struct
import org.freedesktop.dbus.annotations.DBusBoundProperty
import org.freedesktop.dbus.annotations.DBusInterfaceName
import org.freedesktop.dbus.annotations.Position
import org.freedesktop.dbus.connections.impl.DBusConnection
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder
import org.freedesktop.dbus.interfaces.DBusInterface
import org.freedesktop.dbus.types.UInt32

private const val STATUS_NOTIFIER_ITEM_PATH = "/StatusNotifierItem"

@DBusInterfaceName("org.kde.StatusNotifierWatcher")
interface StatusNotifierWatcher : DBusInterface {
    fun RegisterStatusNotifierItem(service: String)
}

@DBusInterfaceName("org.kde.StatusNotifierItem")
interface StatusNotifierItem : DBusInterface {
    @DBusBoundProperty(name = "Category")
    fun getCategory(): String

    @DBusBoundProperty(name = "Id")
    fun getId(): String

    @DBusBoundProperty(name = "Title")
    fun getTitle(): String

    @DBusBoundProperty(name = "Status")
    fun getStatus(): String

    @DBusBoundProperty(name = "WindowId")
    fun getWindowId(): UInt32

    @DBusBoundProperty(name = "IconName")
    fun getIconName(): String

    @DBusBoundProperty(name = "IconPixmap")
    fun getIconPixmap(): List<StatusNotifierPixmap>

    @DBusBoundProperty(name = "OverlayIconName")
    fun getOverlayIconName(): String

    @DBusBoundProperty(name = "OverlayIconPixmap")
    fun getOverlayIconPixmap(): List<StatusNotifierPixmap>

    @DBusBoundProperty(name = "AttentionIconName")
    fun getAttentionIconName(): String

    @DBusBoundProperty(name = "AttentionIconPixmap")
    fun getAttentionIconPixmap(): List<StatusNotifierPixmap>

    @DBusBoundProperty(name = "AttentionMovieName")
    fun getAttentionMovieName(): String

    @DBusBoundProperty(name = "ToolTip")
    fun getToolTip(): StatusNotifierToolTip

    @DBusBoundProperty(name = "ItemIsMenu")
    fun getItemIsMenu(): Boolean

    @DBusBoundProperty(name = "Menu")
    fun getMenu(): DBusPath

    fun ContextMenu(x: Int, y: Int)
    fun Activate(x: Int, y: Int)
    fun SecondaryActivate(x: Int, y: Int)
    fun Scroll(delta: Int, orientation: String)
}

class StatusNotifierPixmap(
    @field:Position(0) @JvmField val width: Int,
    @field:Position(1) @JvmField val height: Int,
    @field:Position(2) @JvmField val pixels: ByteArray,
) : Struct()

class StatusNotifierToolTip(
    @field:Position(0) @JvmField val iconName: String,
    @field:Position(1) @JvmField val iconPixmap: List<StatusNotifierPixmap>,
    @field:Position(2) @JvmField val title: String,
    @field:Position(3) @JvmField val description: String,
) : Struct()

fun toStatusNotifierArgb(image: BufferedImage): ByteArray {
    val pixels = ByteArray(image.width * image.height * 4)
    var offset = 0
    for (y in 0 until image.height) {
        for (x in 0 until image.width) {
            val argb = image.getRGB(x, y)
            pixels[offset++] = (argb ushr 24).toByte()
            pixels[offset++] = (argb ushr 16).toByte()
            pixels[offset++] = (argb ushr 8).toByte()
            pixels[offset++] = argb.toByte()
        }
    }
    return pixels
}

fun loadTrayBufferedImage(size: Int): BufferedImage {
    val resourceName = trayIconResourceName(desktopUsesDarkAppearance())
    val resource = Thread.currentThread().contextClassLoader.getResource(resourceName)
    val source = when {
        resource != null -> ImageIO.read(resource)
        File("src/main/resources/$resourceName").exists() ->
            ImageIO.read(File("src/main/resources/$resourceName"))
        else -> null
    }
    val target = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)
    val graphics = target.createGraphics()
    if (source == null) {
        graphics.color = java.awt.Color(167, 196, 106)
        graphics.fillOval(size / 8, size / 8, size * 3 / 4, size * 3 / 4)
    } else {
        configureHighQualityScaling(graphics)
        graphics.drawImage(source, 0, 0, size, size, null)
    }
    graphics.dispose()
    return target
}

private fun configureHighQualityScaling(graphics: Graphics2D) {
    graphics.setRenderingHint(
        java.awt.RenderingHints.KEY_INTERPOLATION,
        java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC,
    )
    graphics.setRenderingHint(
        java.awt.RenderingHints.KEY_RENDERING,
        java.awt.RenderingHints.VALUE_RENDER_QUALITY,
    )
}

object StatusNotifierTray {
    private var connection: DBusConnection? = null

    fun install(state: AppState): Boolean = runCatching {
        if (connection != null) return true

        val newConnection = DBusConnectionBuilder.forSessionBus()
            .withShared(false)
            .build()
        val serviceName = "org.freedesktop.StatusNotifierItem-${ProcessHandle.current().pid()}-1"
        val pixmaps = listOf(22, 32, 44).map { size ->
            val image = loadTrayBufferedImage(size)
            StatusNotifierPixmap(size, size, toStatusNotifierArgb(image))
        }
        val item = RevqStatusNotifierItem(pixmaps) {
            EventQueue.invokeLater {
                state.mainWindowVisible = true
                state.selectView(View.NeedsReview)
            }
        }

        newConnection.requestBusName(serviceName)
        newConnection.exportObject(STATUS_NOTIFIER_ITEM_PATH, item)
        val watcher = newConnection.getRemoteObject(
            "org.kde.StatusNotifierWatcher",
            "/StatusNotifierWatcher",
            StatusNotifierWatcher::class.java,
        )
        watcher.RegisterStatusNotifierItem(serviceName)
        connection = newConnection
        true
    }.onFailure {
        System.err.println("RevQ tray: StatusNotifierItem initialization failed: ${it.message}")
    }.getOrDefault(false)
}

private class RevqStatusNotifierItem(
    private val pixmaps: List<StatusNotifierPixmap>,
    private val activate: () -> Unit,
) : StatusNotifierItem {
    override fun getObjectPath(): String = STATUS_NOTIFIER_ITEM_PATH
    override fun getCategory(): String = "ApplicationStatus"
    override fun getId(): String = "revq"
    override fun getTitle(): String = "RevQ"
    override fun getStatus(): String = "Active"
    override fun getWindowId(): UInt32 = UInt32(0)
    override fun getIconName(): String = ""
    override fun getIconPixmap(): List<StatusNotifierPixmap> = pixmaps
    override fun getOverlayIconName(): String = ""
    override fun getOverlayIconPixmap(): List<StatusNotifierPixmap> = emptyList()
    override fun getAttentionIconName(): String = ""
    override fun getAttentionIconPixmap(): List<StatusNotifierPixmap> = emptyList()
    override fun getAttentionMovieName(): String = ""
    override fun getToolTip(): StatusNotifierToolTip =
        StatusNotifierToolTip("", pixmaps, "RevQ", "Pull request review queue")
    override fun getItemIsMenu(): Boolean = false
    override fun getMenu(): DBusPath = DBusPath("/")
    override fun ContextMenu(x: Int, y: Int) = activate()
    override fun Activate(x: Int, y: Int) = activate()
    override fun SecondaryActivate(x: Int, y: Int) = activate()
    override fun Scroll(delta: Int, orientation: String) = Unit
}

fun awtTrayImage(size: Int): Image = loadTrayBufferedImage(size)
