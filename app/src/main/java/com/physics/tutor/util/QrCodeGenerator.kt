package com.physics.tutor.util

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter

/**
 * Генератор QR-кодов на основе библиотеки ZXing Core.
 *
 * Работает полностью офлайн — никаких сетевых запросов.
 * Результат — объект Bitmap, который можно отобразить в ImageView.
 */
object QrCodeGenerator {

    /**
     * Создаёт Bitmap с QR-кодом для переданного текста.
     *
     * @param content  Строка, кодируемая в QR (например, 6-значный код результата)
     * @param sizePx   Размер QR-кода в пикселях (ширина = высота)
     * @return         Bitmap или null при ошибке
     */
    fun generate(content: String, sizePx: Int = 512): Bitmap? {
        return try {
            val hints = mapOf(
                EncodeHintType.CHARACTER_SET to "UTF-8",
                EncodeHintType.MARGIN to 2
            )
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx, hints)

            val bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.RGB_565)
            for (x in 0 until sizePx) {
                for (y in 0 until sizePx) {
                    bmp.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            bmp
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
