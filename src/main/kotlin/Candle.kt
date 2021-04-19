import com.binance.api.client.domain.event.CandlestickEvent
import java.math.BigDecimal

fun candleMoneyToBigDecimal(value: String) : BigDecimal {
  val value = value.trimEnd('0')
  return value.toBigDecimal()
}

data class Candle (
  val openTime: Long,
  val closeTime: Long,
  val open: BigDecimal,
  val close: BigDecimal,
  val high: BigDecimal,
  val low: BigDecimal,
  val volume: Double,
) {
  constructor(candle: CandlestickEvent) : this(
    candle.openTime,
    candle.closeTime,
    candle.open.toBigDecimal(),
    candle.close.toBigDecimal(),
    candle.high.toBigDecimal(),
    candle.low.toBigDecimal(),
    candle.volume.toDouble(),
  ) {

  }
}