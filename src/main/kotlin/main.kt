import com.binance.api.client.BinanceApiCallback
import com.binance.api.client.BinanceApiClientFactory
import com.binance.api.client.domain.event.CandlestickEvent
import com.binance.api.client.domain.market.CandlestickInterval
import kotlin.system.exitProcess

fun main(args: Array<String>) {
  val clientFactory = BinanceApiClientFactory.newInstance("", "")
  val restClient = clientFactory.newRestClient()
  val wsClient = clientFactory.newWebSocketClient()

  print("Trying to contact Binance. . .")
  restClient.ping()
  println("success.")

  val serverTime = restClient.exchangeInfo.serverTime
  println("Binance server time: $serverTime")

  println("Fetching candles . . .")
  val manager = CandleManager()

  val candleClosable = wsClient.onCandlestickEvent("ethbtc", CandlestickInterval.ONE_MINUTE, manager)
  Thread.sleep(1000 * 120)

  candleClosable.close()
  manager.finalize()
  println("All done.")
}

class CandleManager(
  private val candles: MutableList<Candle>,
  private val writer: CandleWriter,
): BinanceApiCallback<CandlestickEvent> {
  constructor() : this(
    mutableListOf(),
    CandleWriter("test.log"),
  ) {}

  override fun onResponse(candle: CandlestickEvent?) {
    if (candle != null) {
      if (candle.barFinal) {
        // candle is closed, add to list
        val candle = Candle(candle)
        this.candles.add(candle)
        this.writer.writeCandle(candle)
        println("Candle ${this.candles.size} close: ${candle.closeTime} @ ${candle.close}")
      } else {
        Candle(candle)
        println("Intermediate candle: ${candle.close}")
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