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
        // 1. Get the context of the app running on the emulator
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        // 2. Define the URI that points to our new ContentProvider
        val contentUri = Uri.parse("content://com.example.moneymatters.provider/expenses")

        // 3. Ask Android's ContentResolver to query that URI (Mimicking a 3rd party app)
        val cursor = appContext.contentResolver.query(contentUri, null, null, null, null)

        // 4. Assert that the cursor is not null (meaning the provider successfully connected to the database)
        assertNotNull("Cursor should not be null", cursor)

        // 5. Close the cursor to prevent memory leaks
        cursor?.close()
    }
}