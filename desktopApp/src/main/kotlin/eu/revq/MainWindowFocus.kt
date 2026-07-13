package eu.revq

import java.awt.Window
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.Timer

sealed interface DesktopFocusEvent {
    data object ContentReady : DesktopFocusEvent
    data object WindowActivated : DesktopFocusEvent
    data class VisibilityChanged(val visible: Boolean) : DesktopFocusEvent
    data object HotReload : DesktopFocusEvent
}

enum class DesktopFocusOperation {
    BringToFront,
    RequestNativeFocus,
    RequestComposeRootFocus,
}

data class DesktopFocusSnapshot(
    val visible: Boolean = true,
    val contentReady: Boolean = false,
    val listenerAttached: Boolean = false,
    val composeFocusRequests: Int = 0,
)

interface DesktopFocusAdapter {
    fun installActivationListener(onActivated: () -> Unit): AutoCloseable
    fun bringToFront()
    fun requestNativeFocus()
    fun requestComposeRootFocus()
    fun schedule(delayMillis: Long, action: () -> Unit): AutoCloseable
}

class DesktopFocusLifecycle(
    private val adapter: DesktopFocusAdapter,
) : AutoCloseable {
    private var listener: AutoCloseable? = null
    private val scheduledFocusWork = mutableListOf<AutoCloseable>()

    var snapshot: DesktopFocusSnapshot = DesktopFocusSnapshot()
        private set

    fun attach() {
        if (listener != null) return
        listener = adapter.installActivationListener {
            apply(DesktopFocusEvent.WindowActivated)
        }
        snapshot = snapshot.copy(listenerAttached = true)
    }

    fun apply(event: DesktopFocusEvent) {
        when (event) {
            DesktopFocusEvent.ContentReady -> {
                val becameReady = !snapshot.contentReady
                snapshot = snapshot.copy(contentReady = true)
                if (snapshot.visible && becameReady) activateWindowAndCompose()
            }

            DesktopFocusEvent.WindowActivated -> {
                if (snapshot.visible) requestComposeRootFocus()
            }

            is DesktopFocusEvent.VisibilityChanged -> {
                val becameVisible = event.visible && !snapshot.visible
                snapshot = snapshot.copy(visible = event.visible)
                if (!event.visible) cancelScheduledFocusWork()
                if (becameVisible && snapshot.contentReady) activateWindowAndCompose()
            }

            DesktopFocusEvent.HotReload -> {
                if (snapshot.visible && snapshot.contentReady) activateWindowAndCompose()
            }
        }
    }

    private fun activateWindowAndCompose() {
        scheduleFocusWork(NativeActivationDelayMillis) {
            adapter.bringToFront()
            adapter.requestNativeFocus()
            requestComposeRootFocus()
        }
    }

    private fun requestComposeRootFocus() {
        listOf(ComposeFocusDelayMillis, ComposeFocusRetryDelayMillis).forEach { delayMillis ->
            scheduleFocusWork(delayMillis) {
                adapter.requestComposeRootFocus()
                snapshot = snapshot.copy(composeFocusRequests = snapshot.composeFocusRequests + 1)
            }
        }
    }

    private fun scheduleFocusWork(delayMillis: Long, action: () -> Unit) {
        val scheduled = adapter.schedule(delayMillis) {
            if (snapshot.listenerAttached && snapshot.visible) action()
        }
        scheduledFocusWork += scheduled
    }

    private fun cancelScheduledFocusWork() {
        scheduledFocusWork.forEach(AutoCloseable::close)
        scheduledFocusWork.clear()
    }

    override fun close() {
        cancelScheduledFocusWork()
        listener?.close()
        listener = null
        snapshot = snapshot.copy(listenerAttached = false)
    }

    private companion object {
        const val NativeActivationDelayMillis = 75L
        const val ComposeFocusDelayMillis = 50L
        const val ComposeFocusRetryDelayMillis = 175L
    }
}

class AwtDesktopFocusAdapter(
    private val window: Window,
    private val composeRootFocus: () -> Unit,
) : DesktopFocusAdapter {
    override fun installActivationListener(onActivated: () -> Unit): AutoCloseable {
        val listener = object : WindowAdapter() {
            override fun windowActivated(event: WindowEvent?) {
                onActivated()
            }
        }
        window.addWindowListener(listener)
        return AutoCloseable { window.removeWindowListener(listener) }
    }

    override fun bringToFront() = window.toFront()

    override fun requestNativeFocus() {
        window.requestFocus()
    }

    override fun requestComposeRootFocus() = composeRootFocus()

    override fun schedule(delayMillis: Long, action: () -> Unit): AutoCloseable {
        val timer = Timer(delayMillis.toInt()) { action() }.apply {
            isRepeats = false
            start()
        }
        return AutoCloseable(timer::stop)
    }
}

class DeterministicDesktopFocusAdapter(
    private val executeScheduledImmediately: Boolean = true,
) : DesktopFocusAdapter {
    val operations = mutableListOf<DesktopFocusOperation>()
    val scheduledDelays = mutableListOf<Long>()
    var listenerInstalled: Boolean = false
        private set
    private var activationListener: (() -> Unit)? = null
    private val scheduledWork = mutableListOf<ScheduledFocusWork>()

    override fun installActivationListener(onActivated: () -> Unit): AutoCloseable {
        listenerInstalled = true
        activationListener = onActivated
        return AutoCloseable {
            listenerInstalled = false
            activationListener = null
        }
    }

    override fun bringToFront() {
        operations += DesktopFocusOperation.BringToFront
    }

    override fun requestNativeFocus() {
        operations += DesktopFocusOperation.RequestNativeFocus
    }

    override fun requestComposeRootFocus() {
        operations += DesktopFocusOperation.RequestComposeRootFocus
    }

    override fun schedule(delayMillis: Long, action: () -> Unit): AutoCloseable {
        scheduledDelays += delayMillis
        if (executeScheduledImmediately) {
            action()
            return AutoCloseable {}
        }
        val scheduled = ScheduledFocusWork(action)
        scheduledWork += scheduled
        return AutoCloseable { scheduled.cancelled = true }
    }

    fun activateWindow() {
        activationListener?.invoke()
    }

    fun runScheduledWork() {
        val pending = scheduledWork.toList()
        scheduledWork.clear()
        pending.filterNot(ScheduledFocusWork::cancelled).forEach { it.action() }
    }

    private data class ScheduledFocusWork(
        val action: () -> Unit,
        var cancelled: Boolean = false,
    )
}
