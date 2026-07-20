package eu.revq

import java.awt.EventQueue
import java.awt.Font
import java.awt.Graphics2D
import java.awt.Image
import java.awt.RenderingHints
import java.awt.TrayIcon
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import org.freedesktop.dbus.DBusPath
import org.freedesktop.dbus.Struct
import org.freedesktop.dbus.TypeRef
import org.freedesktop.dbus.annotations.DBusBoundProperty
import org.freedesktop.dbus.annotations.DBusInterfaceName
import org.freedesktop.dbus.annotations.DBusMemberName
import org.freedesktop.dbus.annotations.Position
import org.freedesktop.dbus.connections.impl.DBusConnection
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder
import org.freedesktop.dbus.interfaces.DBusInterface
import org.freedesktop.dbus.messages.DBusSignal
import org.freedesktop.dbus.types.UInt32

private const val STATUS_NOTIFIER_ITEM_PATH = "/StatusNotifierItem"
private val TrayBadgeColor = java.awt.Color(167, 196, 106)
private val TrayBadgeTextColor = java.awt.Color(18, 20, 15)

@DBusInterfaceName("org.kde.StatusNotifierWatcher")
interface StatusNotifierWatcher : DBusInterface {
    fun RegisterStatusNotifierItem(service: String)
}

@DBusInterfaceName("org.kde.StatusNotifierItem")
interface StatusNotifierItem : DBusInterface {
    @DBusMemberName("NewIcon")
    class NewIcon(path: String) : DBusSignal(path)

    @DBusMemberName("NewToolTip")
    class NewToolTip(path: String) : DBusSignal(path)

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

    @DBusBoundProperty(name = "IconPixmap", type = StatusNotifierPixmapList::class)
    fun getIconPixmap(): List<StatusNotifierPixmap>

    @DBusBoundProperty(name = "OverlayIconName")
    fun getOverlayIconName(): String

    @DBusBoundProperty(name = "OverlayIconPixmap", type = StatusNotifierPixmapList::class)
    fun getOverlayIconPixmap(): List<StatusNotifierPixmap>

    @DBusBoundProperty(name = "AttentionIconName")
    fun getAttentionIconName(): String

    @DBusBoundProperty(name = "AttentionIconPixmap", type = StatusNotifierPixmapList::class)
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

internal interface StatusNotifierPixmapList : TypeRef<List<StatusNotifierPixmap>>

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

fun trayBadgeLabel(reviewCount: Int): String = when {
    reviewCount <= 0 -> ""
    reviewCount > 9 -> "9+"
    else -> reviewCount.toString()
}

fun trayTooltip(
    reviewCount: Int,
    enabled: Boolean,
): String = when {
    !enabled -> "RevQ · Pull request review queue"
    reviewCount == 0 -> "RevQ · Review queue clear"
    reviewCount == 1 -> "RevQ · 1 review waiting"
    else -> "RevQ · $reviewCount reviews waiting"
}

fun loadTrayBufferedImage(
    size: Int,
    reviewCount: Int = 0,
): BufferedImage = loadTrayBufferedImage(
    size = size,
    darkAppearance = desktopUsesDarkAppearance(),
    reviewCount = reviewCount,
)

fun loadTrayBufferedImage(
    size: Int,
    darkAppearance: Boolean,
    reviewCount: Int = 0,
): BufferedImage {
    val resourceName = trayIconResourceName(darkAppearance)
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
        graphics.color = TrayBadgeColor
        graphics.fillOval(size / 8, size / 8, size * 3 / 4, size * 3 / 4)
    } else {
        configureHighQualityScaling(graphics)
        graphics.drawImage(source, 0, 0, size, size, null)
    }
    drawReviewCountBadge(graphics, size, reviewCount)
    graphics.dispose()
    return target
}

private fun drawReviewCountBadge(
    graphics: Graphics2D,
    size: Int,
    reviewCount: Int,
) {
    val label = trayBadgeLabel(reviewCount)
    if (label.isEmpty()) return

    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)

    val badgeHeight = (size * 0.50f).toInt().coerceAtLeast(9)
    val badgeWidth = if (label.length == 1) {
        badgeHeight
    } else {
        (size * 0.68f).toInt().coerceAtLeast(badgeHeight + 2)
    }
    val x = size - badgeWidth
    val y = size - badgeHeight
    val arc = badgeHeight

    graphics.color = TrayBadgeColor
    graphics.fillRoundRect(x, y, badgeWidth, badgeHeight, arc, arc)
    graphics.color = TrayBadgeTextColor
    graphics.drawRoundRect(x, y, badgeWidth - 1, badgeHeight - 1, arc, arc)

    val fontSize = (badgeHeight * if (label.length == 1) 0.72f else 0.60f).toInt().coerceAtLeast(7)
    graphics.font = Font(Font.SANS_SERIF, Font.BOLD, fontSize)
    val metrics = graphics.fontMetrics
    val textX = x + (badgeWidth - metrics.stringWidth(label)) / 2
    val textY = y + (badgeHeight - metrics.height) / 2 + metrics.ascent
    graphics.drawString(label, textX, textY)
}

private fun configureHighQualityScaling(graphics: Graphics2D) {
    graphics.setRenderingHint(
        RenderingHints.KEY_INTERPOLATION,
        RenderingHints.VALUE_INTERPOLATION_BICUBIC,
    )
    graphics.setRenderingHint(
        RenderingHints.KEY_RENDERING,
        RenderingHints.VALUE_RENDER_QUALITY,
    )
}

private fun statusNotifierPixmaps(reviewCount: Int): List<StatusNotifierPixmap> =
    listOf(22, 32, 44).map { size ->
        val image = loadTrayBufferedImage(size, reviewCount)
        StatusNotifierPixmap(size, size, toStatusNotifierArgb(image))
    }

object StatusNotifierTray {
    private var connection: DBusConnection? = null
    private var item: RevqStatusNotifierItem? = null

    fun install(state: AppState): Boolean = runCatching {
        if (connection != null) {
            update(state.trayReviewCount, state.showReviewCountInTray)
            return true
        }

        val newConnection = DBusConnectionBuilder.forSessionBus()
            .withShared(false)
            .build()
        val serviceName = "org.freedesktop.StatusNotifierItem-${ProcessHandle.current().pid()}-1"
        val newItem = createStatusNotifierItem(
            reviewCount = state.trayReviewCount,
            badgeEnabled = state.showReviewCountInTray,
        ) {
            EventQueue.invokeLater {
                state.mainWindowVisible = true
                state.selectView(View.NeedsReview)
                state.mainWindowFocusRequest += 1
            }
        }

        newConnection.requestBusName(serviceName)
        newConnection.exportObject(STATUS_NOTIFIER_ITEM_PATH, newItem)
        val watcher = newConnection.getRemoteObject(
            "org.kde.StatusNotifierWatcher",
            "/StatusNotifierWatcher",
            StatusNotifierWatcher::class.java,
        )
        watcher.RegisterStatusNotifierItem(serviceName)
        connection = newConnection
        item = newItem
        true
    }.onFailure {
        System.err.println("RevQ tray: StatusNotifierItem initialization failed: ${it.message}")
    }.getOrDefault(false)

    @Synchronized
    fun update(
        reviewCount: Int,
        enabled: Boolean,
    ) {
        val currentItem = item ?: return
        val currentConnection = connection ?: return
        currentItem.update(
            pixmaps = statusNotifierPixmaps(if (enabled) reviewCount else 0),
            reviewCount = reviewCount,
            badgeEnabled = enabled,
        )
        runCatching {
            currentConnection.sendMessage(StatusNotifierItem.NewIcon(STATUS_NOTIFIER_ITEM_PATH))
            currentConnection.sendMessage(StatusNotifierItem.NewToolTip(STATUS_NOTIFIER_ITEM_PATH))
        }.onFailure {
            System.err.println("RevQ tray: StatusNotifierItem update failed: ${it.message}")
        }
    }
}

internal fun createStatusNotifierItem(
    reviewCount: Int,
    badgeEnabled: Boolean,
    activate: () -> Unit,
): RevqStatusNotifierItem = RevqStatusNotifierItem(
    pixmaps = statusNotifierPixmaps(if (badgeEnabled) reviewCount else 0),
    reviewCount = reviewCount,
    badgeEnabled = badgeEnabled,
    activate = activate,
)

internal class RevqStatusNotifierItem(
    pixmaps: List<StatusNotifierPixmap>,
    reviewCount: Int,
    badgeEnabled: Boolean,
    private val activate: () -> Unit,
) : StatusNotifierItem {
    @Volatile
    private var currentPixmaps: List<StatusNotifierPixmap> = pixmaps

    @Volatile
    private var currentReviewCount: Int = reviewCount

    @Volatile
    private var currentBadgeEnabled: Boolean = badgeEnabled

    fun update(
        pixmaps: List<StatusNotifierPixmap>,
        reviewCount: Int,
        badgeEnabled: Boolean,
    ) {
        currentPixmaps = pixmaps
        currentReviewCount = reviewCount
        currentBadgeEnabled = badgeEnabled
    }

    override fun getObjectPath(): String = STATUS_NOTIFIER_ITEM_PATH
    override fun getCategory(): String = "ApplicationStatus"
    override fun getId(): String = "revq"
    override fun getTitle(): String = if (currentBadgeEnabled && currentReviewCount > 0) {
        "RevQ · $currentReviewCount"
    } else {
        "RevQ"
    }
    override fun getStatus(): String = "Active"
    override fun getWindowId(): UInt32 = UInt32(0)
    override fun getIconName(): String = ""
    override fun getIconPixmap(): List<StatusNotifierPixmap> = currentPixmaps
    override fun getOverlayIconName(): String = ""
    override fun getOverlayIconPixmap(): List<StatusNotifierPixmap> = emptyList()
    override fun getAttentionIconName(): String = ""
    override fun getAttentionIconPixmap(): List<StatusNotifierPixmap> = emptyList()
    override fun getAttentionMovieName(): String = ""
    override fun getToolTip(): StatusNotifierToolTip =
        StatusNotifierToolTip(
            iconName = "",
            iconPixmap = currentPixmaps,
            title = getTitle(),
            description = trayTooltip(currentReviewCount, currentBadgeEnabled),
        )
    override fun getItemIsMenu(): Boolean = false
    override fun getMenu(): DBusPath = DBusPath("/")
    override fun ContextMenu(x: Int, y: Int) = activate()
    override fun Activate(x: Int, y: Int) = activate()
    override fun SecondaryActivate(x: Int, y: Int) = activate()
    override fun Scroll(delta: Int, orientation: String) = Unit
}

object AwtTrayController {
    private var trayIcon: TrayIcon? = null
    private var iconSize: Int = 22

    fun attach(
        trayIcon: TrayIcon,
        iconSize: Int,
    ) {
        this.trayIcon = trayIcon
        this.iconSize = iconSize
    }

    fun update(
        reviewCount: Int,
        enabled: Boolean,
    ) {
        val current = trayIcon ?: return
        EventQueue.invokeLater {
            current.image = awtTrayImage(iconSize, if (enabled) reviewCount else 0)
            current.toolTip = trayTooltip(reviewCount, enabled)
        }
    }
}

fun updateTrayReviewCount(
    reviewCount: Int,
    enabled: Boolean,
) {
    StatusNotifierTray.update(reviewCount, enabled)
    AwtTrayController.update(reviewCount, enabled)
}

fun awtTrayImage(
    size: Int,
    reviewCount: Int = 0,
): Image = loadTrayBufferedImage(size, reviewCount)
