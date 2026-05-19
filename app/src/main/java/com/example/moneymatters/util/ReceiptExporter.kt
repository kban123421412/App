package com.example.moneymatters.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import androidx.core.content.FileProvider
import com.example.moneymatters.data.model.ExpenseModel
import java.io.File
import java.io.FileOutputStream

object ReceiptExporter {

    fun shareReceipt(context: Context, filterName: String, expenses: List<ExpenseModel>, currencySymbol: String) {
        if (expenses.isEmpty()) return

        //receipt layout
        val width = 650
        val itemHeight = 65
        val headerHeight = 200
        val footerHeight = 160
        val height = headerHeight + (expenses.size * itemHeight) + footerHeight

        //bitmap for receipt
        val bitmap = Bitmap.createBitmap(
            width,
            height,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)

        //white background
        canvas.drawRect(
            0f,
            0f,
            width.toFloat(),
            height.toFloat(),
            Paint().apply { color = Color.WHITE }
        )

        val textPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 24f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            isAntiAlias = true
        }

        val headerPaint = Paint().apply {
            color = Color.BLACK
            textSize = 38f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        val borderPaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 3f
            style = Paint.Style.STROKE
        }

        //receipt header
        canvas.drawText("====== MONEY MATTERS ======", (width / 2).toFloat(), 65f, headerPaint)
        canvas.drawText("SPENDING RECEIPT", (width / 2).toFloat(), 115f, Paint(textPaint).apply {
            textAlign = Paint.Align.CENTER;
            textSize = 26f; typeface = Typeface.create(
            Typeface.MONOSPACE,
            Typeface.BOLD
        ) })

        //changes the header depending on the filter
        if (filterName != "All") {
            canvas.drawText("Last: $filterName", 45f, 160f, textPaint)
        } else {
            canvas.drawText("All Time", 45f, 160f, textPaint)
        }

        canvas.drawLine(35f, 185f, (width - 35).toFloat(), 185f, borderPaint)

        var currentY = 230f
        var grandTotal = 0.0

        //loops through expenses and adds to image
        for (expense in expenses) {
            grandTotal += expense.amount

            //format length (this was a fix as longer titles would overlap with price)
            val polishedTitle = if (expense.title.length > 18) expense.title.substring(0, 15) + "..." else expense.title
            val formattedAmount = String.format("£%.2f", expense.amount)

            canvas.drawText(polishedTitle, 45f, currentY, textPaint)
            canvas.drawText(formattedAmount, (width - 45).toFloat(), currentY, Paint(textPaint).apply { textAlign = Paint.Align.RIGHT; color = Color.BLACK })
            canvas.drawText("${expense.category} | ${expense.date}", 55f, currentY + 24f, Paint(textPaint).apply { textSize = 17f; color = Color.GRAY })

            currentY += itemHeight
        }

        //draw the line abpive total
        canvas.drawLine(35f, currentY, (width - 35).toFloat(), currentY, borderPaint)
        currentY += 55f

        canvas.drawText("GRAND TOTAL:", 45f, currentY, Paint(textPaint).apply { typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD); color = Color.BLACK })
        canvas.drawText(String.format("£%.2f", grandTotal), (width - 45).toFloat(), currentY, Paint(textPaint).apply { textAlign = Paint.Align.RIGHT; typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD); color = Color.BLACK })

        currentY += 60f

        //footer
        canvas.drawText("== End of RECEIPT ==", (width / 2).toFloat(), currentY, Paint(textPaint).apply { textAlign = Paint.Align.CENTER; textSize = 20f; color = Color.LTGRAY })

        try {
            val sharedFolder = File(context.cacheDir, "shared_images") //stores image in temp cache
            sharedFolder.mkdirs() //safety check

            val imageFile = File(sharedFolder, "expense_receipt.png")
            val outputStream = FileOutputStream(imageFile)

            //canvas --> PNG
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()

            //generates URI
            val contentUri = FileProvider.getUriForFile(
                context,
                "com.example.moneymatters.fileprovider",
                imageFile
            )

            if (contentUri != null) {
                val intentPayload = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_STREAM, contentUri)
                    type = "image/png"
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                //launches actual sharesheet
                val platformChooser = Intent.createChooser(intentPayload, "Export Expense Receipt:")
                platformChooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(platformChooser)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}