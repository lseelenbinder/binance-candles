import java.io.BufferedReader
import java.io.FileReader
import kotlin.streams.asSequence

class CandleReader(
  private val symbol: String,
  private val fileHandle: BufferedReader
) {
  constructor(symbol: String, fileName: String) : this(
    symbol,
    BufferedReader(FileReader(fileName))
  )

  fun readCandle() : Candle {
    return recordAsCandle(fileHandle.readLine())
  }

  fun readCandles() : Sequence<Candle> {
    return fileHandle.lines().map {
      recordAsCandle(it)
    }.asSequence()
  }

  fun close() {
    fileHandle.close()
  }

  private fun recordAsCandle(record: String) : Candle {
    val fields = record.split("|")
    return Candle(
      this.symbol,
      fields[0].toLong(),       // openTime
      fields[1].toLong(),       // closeTime
      fields[2].toBigDecimal(), // open
      fields[3].toBigDecimal(), // close
      fields[4].toBigDecimal(), // high
      fields[5].toBigDecimal(), // low
      fields[6].toBigDecimal(), // volume
      fields[7].toLong(),       // nTrades
    )
  }
}