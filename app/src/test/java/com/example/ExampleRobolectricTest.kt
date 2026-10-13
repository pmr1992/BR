package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.blackridge.engine.BlackridgeEngine
import com.example.blackridge.engine.WorldData
import com.example.blackridge.model.NsfwSetting
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Blackridge", appName)
    }

    @Test
    fun `test blackridge engine skeleton format`() = runBlocking {
        val engine = BlackridgeEngine()
        val initialPreset = WorldData.getPresetState(1, NsfwSetting.OFF, "SKIP")
        val turnOne = engine.generateTurnOne(initialPreset)

        val skeleton = turnOne.toSkeletonString()
        assertTrue(skeleton.contains("=== BLACKRIDGE ==="))
        assertTrue(skeleton.contains("TURN: 1"))
        assertTrue(skeleton.contains("--- SCENE ---"))
        assertTrue(skeleton.contains("--- WORLD TICK ---"))
        assertTrue(skeleton.contains("--- NPC ACTION ---"))
        assertTrue(skeleton.contains("--- STATE CHANGES ---"))
        assertTrue(skeleton.contains("--- PIC ---"))
        assertTrue(skeleton.contains("ACTIONS:"))

        // Advance turn
        val (state2, turn2) = engine.resolveTurn(initialPreset, "Step inside Murphy's Pub and look for a warm seat")
        assertEquals(2, state2.turn)
        assertTrue(turn2.turnNumber == 2)
        val skeleton2 = turn2.toSkeletonString()
        assertTrue(skeleton2.contains("TURN: 2"))
    }
}
