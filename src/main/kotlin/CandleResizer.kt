import com.binance.api.client.domain.market.CandlestickInterval

private const val MS_IN_MINUTE = 60 * 1000

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
      // This is the same as the underlying candles, so short-circuit.
      CandlestickInterval.ONE_MINUTE -> return candles.iterator()
      CandlestickInterval.THREE_MINUTES -> 3
      CandlestickInterval.FIVE_MINUTES -> 5
      CandlestickInterval.FIFTEEN_MINUTES -> 15
      CandlestickInterval.HALF_HOURLY -> 30
      CandlestickInterval.HOURLY -> 60
      CandlestickInterval.TWO_HOURLY -> 2 * 60
      CandlestickInterval.FOUR_HOURLY -> 4 * 60
      CandlestickInterval.SIX_HOURLY -> 6 * 60
      CandlestickInterval.EIGHT_HOURLY -> 8 * 60
      CandlestickInterval.TWELVE_HOURLY -> 12 * 60
      CandlestickInterval.DAILY -> 24 * 60
      CandlestickInterval.THREE_DAILY -> 3 * 24 * 60
      CandlestickInterval.WEEKLY -> 7 * 24 * 60
      CandlestickInterval.MONTHLY -> 30 * 24 * 60
    }

    val alignment = chunkSize * MS_IN_MINUTE

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