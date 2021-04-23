import com.binance.api.client.domain.event.CandlestickEvent
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.TemporalAccessor

data class Candle (
  val symbol: String,
  val openTime: Long,
  val closeTime: Long,
  val open: BigDecimal,
  val close: BigDecimal,
  val high: BigDecimal,
  val low: BigDecimal,
  val volume: BigDecimal,
  val nTrades: Long,
) {
  constructor(candle: CandlestickEvent) : this(
    candle.symbol,
    candle.openTime.toLong(),
    candle.closeTime.toLong(),
    candle.open.toBigDecimal(),
    candle.close.toBigDecimal(),
    candle.high.toBigDecimal(),
    candle.low.toBigDecimal(),
    candle.volume.toBigDecimal(),
    candle.numberOfTrades,
  )

  fun openTimeAsDateTime(): LocalDateTime = LocalDateTime.ofEpochSecond(this.openTime / 1000, 0, ZoneOffset.UTC)

  // Merges two candles (asserts strict left-right order).
  fun foldCandle(candle: Candle) : Candle {
    assert(this.close < candle.close)
    assert(this.symbol == candle.symbol)

    return Candle(
      this.symbol,
      this.openTime,
      candle.closeTime,
      this.open,
      candle.close,
      this.high.max(candle.high),
      this.low.min(candle.low),
      this.volume + candle.volume,
      this.nTrades + candle.nTrades,
    )
  }
}