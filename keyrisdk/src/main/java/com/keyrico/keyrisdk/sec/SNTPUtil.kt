package com.keyrico.keyrisdk.sec

import android.os.SystemClock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

internal object SNTPUtil {
    suspend fun requestTimeMilliseconds(
        timeZone: TimeZone = TimeZone.getDefault(),
        host: String? = "time.google.com",
        timeout: Int = 3_000,
    ): Result<Date?> {
        return withContext(Dispatchers.IO) {
            try {
                DatagramSocket().use { socket ->
                    socket.soTimeout = timeout

                    val address = InetAddress.getByName(host)
                    val buffer = ByteArray(NTP_PACKET_SIZE)
                    val request = DatagramPacket(buffer, buffer.size, address, NTP_PORT)

                    buffer[0] = (NTP_MODE_CLIENT or (NTP_VERSION shl 3)).toByte()

                    val requestTime = System.currentTimeMillis()
                    val requestTicks = SystemClock.elapsedRealtime()

                    writeTimeStamp(buffer, requestTime)

                    socket.send(request)

                    val response = DatagramPacket(buffer, buffer.size)

                    socket.receive(response)

                    val responseTicks = SystemClock.elapsedRealtime()
                    val responseTime = requestTime + (responseTicks - requestTicks)

                    val originateTime = readTimeStamp(buffer, ORIGINATE_TIME_OFFSET)
                    val receiveTime = readTimeStamp(buffer, RECEIVE_TIME_OFFSET)
                    val transmitTime = readTimeStamp(buffer, TRANSMIT_TIME_OFFSET)
                    val clockOffset =
                        ((receiveTime - originateTime) + (transmitTime - responseTime)) / 2

                    val nowAsPerDeviceTimeZone = responseTime + clockOffset

                    SIMPLE_DATE_FORMAT.timeZone = timeZone

                    val rawDate = SIMPLE_DATE_FORMAT.format(nowAsPerDeviceTimeZone)
                    val date = SIMPLE_DATE_FORMAT.parse(rawDate)

                    withContext(Dispatchers.Main) {
                        Result.success(date)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Result.failure(e)
                }
            }
        }
    }

    private fun read32(
        buffer: ByteArray,
        offset: Int,
    ): Long {
        val b0 = buffer[offset]
        val b1 = buffer[offset + 1]
        val b2 = buffer[offset + 2]
        val b3 = buffer[offset + 3]

        val i0 = (if ((b0.toInt() and 0x80) == 0x80) (b0.toInt() and 0x7F) + 0x80 else b0.toInt())
        val i1 = (if ((b1.toInt() and 0x80) == 0x80) (b1.toInt() and 0x7F) + 0x80 else b1.toInt())
        val i2 = (if ((b2.toInt() and 0x80) == 0x80) (b2.toInt() and 0x7F) + 0x80 else b2.toInt())
        val i3 = (if ((b3.toInt() and 0x80) == 0x80) (b3.toInt() and 0x7F) + 0x80 else b3.toInt())

        return (i0.toLong() shl 24) + (i1.toLong() shl 16) + (i2.toLong() shl 8) + (i3.toLong())
    }

    private fun readTimeStamp(
        buffer: ByteArray,
        offset: Int,
    ): Long {
        val seconds = read32(buffer, offset)
        val fraction = read32(buffer, offset + 4)

        return ((seconds - OFFSET_1900_TO_1970) * 1000) + ((fraction * 1000L) / 0x100000000L)
    }

    private fun writeTimeStamp(
        buffer: ByteArray,
        time: Long,
    ) {
        var offset = TRANSMIT_TIME_OFFSET
        var seconds = time / 1000L
        val milliseconds = time - seconds * 1000L

        seconds += OFFSET_1900_TO_1970

        buffer[offset++] = (seconds shr 24).toByte()
        buffer[offset++] = (seconds shr 16).toByte()
        buffer[offset++] = (seconds shr 8).toByte()
        buffer[offset++] = (seconds shr 0).toByte()

        val fraction = milliseconds * 0x100000000L / 1000L

        buffer[offset++] = (fraction shr 24).toByte()
        buffer[offset++] = (fraction shr 16).toByte()
        buffer[offset++] = (fraction shr 8).toByte()

        buffer[offset] = (Math.random() * 255.0).toInt().toByte()
    }

    private const val ORIGINATE_TIME_OFFSET = 24
    private const val RECEIVE_TIME_OFFSET = 32
    private const val TRANSMIT_TIME_OFFSET = 40
    private const val NTP_PACKET_SIZE = 48

    private const val NTP_PORT = 123
    private const val NTP_MODE_CLIENT = 3
    private const val NTP_VERSION = 4

    private const val OFFSET_1900_TO_1970 = ((365L * 70L) + 17L) * 24L * 60L * 60L
    private const val DATE_FORMAT: String = "yyyy-MM-dd'T'HH:mm:ssZ"

    private val SIMPLE_DATE_FORMAT: SimpleDateFormat = SimpleDateFormat(DATE_FORMAT, Locale.US)
}
