import java.io.*

class CandleWriter(
  private val fileName: String,
  private var fileHandle: BufferedWriter,
) {
  constructor(fileName: String) : this(
    fileName,
    BufferedWriter(FileWriter(fileName))
  ) {
  }

  fun writeCandle(candle: Candle) {
    val chars = candleAsRecord(candle)
    fileHandle.write(chars)
    fileHandle.newLine()
    fileHandle.flush()
  }

  fun finish() {
    fileHandle.close()
  }

  private fun candleAsRecord(candle: Candle) : CharArray {
    return candle.toString().toCharArray()
  }
}