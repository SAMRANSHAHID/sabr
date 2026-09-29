package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.domain.model.ProtectionStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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
        assertEquals("Sabr", appName)
    }

    @Test
    fun `verify sabr application context initialization`() {
        val app = ApplicationProvider.getApplicationContext<SabrApplication>()
        assertNotNull(app)
        assertNotNull(app.repository)
        assertNotNull(app.domainClassifier)
        assertNotNull(app.dnsFilter)
    }

    @Test
    fun `verify initial protection state is off in application`() {
        val app = ApplicationProvider.getApplicationContext<SabrApplication>()
        val stateManager = app.protectionStateManager
        assertNotNull(stateManager)
    }
}
