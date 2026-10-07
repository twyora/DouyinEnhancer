package io.github.twyora.douyinenhancer.hook.ui

internal class CleanModePlaybackState {
    enum class Event { PLAY, PAUSE, LIVE, VIDEO, SCROLL, SCROLL_IDLE, LEAVE }

    private var playing = false
    private var scrolling = false
    private var pendingPause = false
    private var live = false
    val clean get() = playing

    fun accept(event: Event) {
        when (event) {
            Event.PLAY -> {
                live = false
                playing = true
                pendingPause = false
            }

            Event.LIVE -> {
                live = true
                playing = true
                pendingPause = false
            }

            Event.VIDEO -> live = false

            // The outgoing player pauses during a swipe; the next item inherits the command.
            Event.PAUSE -> if (!live) {
                if (scrolling) pendingPause = true else playing = false
            }

            Event.SCROLL -> scrolling = true

            Event.SCROLL_IDLE -> {
                scrolling = false
                if (pendingPause) playing = false
                pendingPause = false
            }

            Event.LEAVE -> {
                playing = false
                scrolling = false
                pendingPause = false
                live = false
            }
        }
    }
}
