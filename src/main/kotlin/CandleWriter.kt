import java.io.*

class CandleWriter(
  private val fileName: String,
  private var fileHandle: BufferedWriter,
) {
  constructor(fileName: String) : this(
    fileName,
    BufferedWriter(FileWriter(fileName, true))
  )

  fun writeCandle(candle: Candle) {
    val chars = candleAsRecord(candle)
    fileHandle.write(chars)
    fileHandle.newLine()
    fileHandle.flush()
  }

  fun finish() {
    // Close also flushes buffer.
    fileHandle.close()
  }

  private fun candleAsRecord(candle: Candle) : CharArray {
    return listOf(
      candle.openTime.toString(),
      candle.closeTime.toString(),
      candle.open.toString(),
      candle.close.toString(),
      candle.high.toString(),
      candle.low.toString(),
      candle.volume.toString(),
      candle.nTrades.toString(),
    ).joinToString("|").toCharArray()
  }
}