package io.github.twyora.douyinenhancer.hook.ui

import io.github.twyora.douyinenhancer.hook.ui.CleanModePlaybackState.Event
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CleanModePlaybackStateTest {
    @Test
    fun pauseAndResumeRestoreControls() {
        val state = CleanModePlaybackState()
        state.accept(Event.PLAY)
        assertTrue(state.clean)
        state.accept(Event.PAUSE)
        assertFalse(state.clean)
        state.accept(Event.PLAY)
        assertTrue(state.clean)
    }

    @Test
    fun outgoingPlayerPauseDoesNotFlashControlsDuringSwipe() {
        val state = CleanModePlaybackState()
        state.accept(Event.PLAY)
        state.accept(Event.SCROLL)
        state.accept(Event.PAUSE)
        assertTrue(state.clean)
        state.accept(Event.PLAY)
        state.accept(Event.SCROLL_IDLE)
        assertTrue(state.clean)
        state.accept(Event.PAUSE)
        assertFalse(state.clean)
    }

    @Test
    fun scrollingPausedVideoDoesNotAssumePlayback() {
        val state = CleanModePlaybackState()
        state.accept(Event.SCROLL)
        state.accept(Event.SCROLL_IDLE)
        assertFalse(state.clean)
    }

    @Test
    fun leavingDuringSwipeResetsSession() {
        val state = CleanModePlaybackState()
        state.accept(Event.PLAY)
        state.accept(Event.SCROLL)
        state.accept(Event.LEAVE)
        assertFalse(state.clean)
        state.accept(Event.PLAY)
        state.accept(Event.PAUSE)
        assertFalse(state.clean)
    }

    @Test
    fun cancelledSwipeAppliesDeferredPause() {
        val state = CleanModePlaybackState()
        state.accept(Event.PLAY)
        state.accept(Event.SCROLL)
        state.accept(Event.PAUSE)
        assertTrue(state.clean)
        state.accept(Event.SCROLL_IDLE)
        assertFalse(state.clean)
    }

    @Test
    fun liveCardKeepsCleanModeAfterOutgoingVideoPause() {
        val state = CleanModePlaybackState()
        state.accept(Event.PLAY)
        state.accept(Event.SCROLL)
        state.accept(Event.PAUSE)
        state.accept(Event.LIVE)
        state.accept(Event.PAUSE)
        state.accept(Event.SCROLL_IDLE)
        state.accept(Event.PAUSE)
        assertTrue(state.clean)
    }

    @Test
    fun videoPauseWorksAfterLeavingLiveCard() {
        val state = CleanModePlaybackState()
        state.accept(Event.LIVE)
        state.accept(Event.SCROLL)
        state.accept(Event.VIDEO)
        state.accept(Event.PLAY)
        state.accept(Event.SCROLL_IDLE)
        state.accept(Event.PAUSE)
        assertFalse(state.clean)
    }

    @Test
    fun leavingLiveCardResetsPausePolicy() {
        val state = CleanModePlaybackState()
        state.accept(Event.LIVE)
        state.accept(Event.LEAVE)
        state.accept(Event.PLAY)
        state.accept(Event.PAUSE)
        assertFalse(state.clean)
    }
}
