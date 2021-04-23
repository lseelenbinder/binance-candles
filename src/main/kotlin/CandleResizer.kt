import com.binance.api.client.domain.market.CandlestickInterval
import java.time.LocalDateTime
import java.time.ZoneOffset

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

  fun resizeForInterval(interval: CandlestickInterval): Sequence<Candle> {
    var candles = reader.readCandles().iterator()

    val chunkSize = when (interval) {
      // This is the same as the underlying candles, so short-circuit.
      CandlestickInterval.ONE_MINUTE -> return candles.asSequence()
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

    return sequence {
      var lastCandle: Candle = candles.next()
      while (candles.hasNext()) {
        var nDropped = 0
        // Drop candles until we align with the next candle window.
        val startingCandle = (sequenceOf(lastCandle) + candles.asSequence()).dropWhile {
          val aligned = it.openTime % alignment == 0L
          if (!aligned) { nDropped++ }
          !aligned
        }.firstOrNull() ?: break // If we're out of candles, we're done.

        if (nDropped > 0) {
          println("WARNING: Dropped $nDropped candles to preserve alignment.")
        }

        // Combine all candles in the window into one resized candle
        val resizedCandleEndTime = startingCandle.openTime + alignment - 1
        var candleCount = 0
        val resizedCandle = candles.asSequence().takeWhile {
          candleCount++
          // This is an non-peekable iterator under the hood, so we need to
          // capture the last candle and use it at the beginning of the next loop,
          // otherwise it is discarded.
          lastCandle = it
          it.closeTime <= resizedCandleEndTime
        }.fold(startingCandle) { l: Candle, r: Candle -> l.foldCandle(r) }

        // Print a warning if we didn't process the full candle window.
        if (candleCount < chunkSize || resizedCandle.closeTime != resizedCandleEndTime) {
          println("WARNING: Missing candles--candle or data may be bogus.")
        }

        yield(resizedCandle)
      }
    }
  }
}