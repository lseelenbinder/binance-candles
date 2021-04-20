import com.binance.api.client.domain.market.CandlestickInterval

class CandleResizer(
  private val pair: String,
  private val reader: CandleReader
) {
  constructor(pair: String): this(
    pair,
    CandleReader(pair, "$pair.candles")
  )

  fun close() {
    reader.close()
  }

  fun resizeForInterval(interval: CandlestickInterval): Iterator<Candle> {
    var candles = reader.readCandles()

    val chunkSize = when (interval) {
      // This is the same size as the underlying candles.
      CandlestickInterval.ONE_MINUTE -> return candles.iterator()
      CandlestickInterval.FIVE_MINUTES -> 5
      else -> return emptyList<Candle>().iterator()
    }

    val alignment = when (interval) {
      CandlestickInterval.ONE_MINUTE -> MS_IN_MINUTE
      CandlestickInterval.FIVE_MINUTES -> 5 * MS_IN_MINUTE
      CandlestickInterval.FIFTEEN_MINUTES -> 15 * MS_IN_MINUTE
      // TODO: all intervals
      else -> return emptyList<Candle>().iterator()
    }

    return candles
      // Find the first candle that aligns with the start of the intended interval
      .dropWhile {
        it.openTime % alignment != 0L
      }
      // FIXME: missing data will break alignment and result in invalid candles.
      // Use windowed chunks to combine the remaining candles, discarding incomplete candles
      .windowed(chunkSize, chunkSize, partialWindows = false).map {
        val firstCandle = it.first()
        it.fold(firstCandle) { l: Candle, r: Candle ->
          l.foldCandle(r)
        }
      }
      .iterator()
  }
}