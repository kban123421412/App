package com.example.moneymatters.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import com.example.moneymatters.data.database.ExpenseDataBase

class ExpenseContentProvider : ContentProvider() {

    companion object {
        const val AUTHORITY = "com.example.moneymatters.provider"
        val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/expenses")
        const val EXPENSES = 1
        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(AUTHORITY, "expenses", EXPENSES)
        }
    }

    override fun onCreate(): Boolean {
        return true
    }

    override fun query(
        uri: Uri, projection: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, sortOrder: String?
    ): Cursor? {
        val context = context ?: return null

        return when (uriMatcher.match(uri)) {
            EXPENSES -> {

                //gets db and calls cursor function
                val db = ExpenseDataBase.getDatabase(context)
                db.expenseDao().getAllExpensesCursor()
            }
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
    }

    override fun getType(uri: Uri): String? = "vnd.android.cursor.dir/vnd.$AUTHORITY.expenses"
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int = 0
}