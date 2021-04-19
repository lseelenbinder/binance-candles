import com.binance.api.client.BinanceApiCallback
import com.binance.api.client.domain.event.CandlestickEvent

class CandleManager(
    private val candles: MutableList<Candle>,
    private val writer: CandleWriter,
): BinanceApiCallback<CandlestickEvent> {
  constructor() : this(
      mutableListOf(),
      CandleWriter("test.log"),
  ) {}

  override fun onResponse(event: CandlestickEvent?) {
    if (event != null) {
      val candle = Candle(event)

      if (event.barFinal) {
        // candle is closed, add to list
        this.candles.add(candle)
        this.writer.writeCandle(candle)
        println("Candle ${this.candles.size} close: ${candle.closeTime} @ ${candle.close}")
      } else {
        println("Intermediate candle: ${event.close}")
      }
    }
  }

  override fun onFailure(cause: Throwable?) {
    println("Error encountered: $cause")
  }

  fun finalize() {
    writer.finish()
  }
}