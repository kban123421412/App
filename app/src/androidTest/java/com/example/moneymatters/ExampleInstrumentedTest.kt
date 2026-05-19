package com.example.moneymatters

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    @Test
    fun testContentProviderQueriesData() {

        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        val contentUri = Uri.parse("content://com.example.moneymatters.provider/expenses")

        val cursor = appContext.contentResolver.query(contentUri, null, null, null, null)

        assertNotNull("Cursor should not be null", cursor)

        cursor?.close()
    }
}