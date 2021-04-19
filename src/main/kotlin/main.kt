import com.binance.api.client.BinanceApiClientFactory
import com.binance.api.client.domain.market.CandlestickInterval

fun main(args: Array<String>) {
  val clientFactory = BinanceApiClientFactory.newInstance("", "")
  val restClient = clientFactory.newRestClient()
  val wsClient = clientFactory.newWebSocketClient()

  print("Trying to contact Binance. . .")
  restClient.ping()
  println("success.")

  val pair = "ethbtc"

  val serverTime = restClient.exchangeInfo.serverTime
  println("Binance server time: $serverTime")

  println("Fetching candles . . .")
  val manager = CandleManager()

  val closeHandle = wsClient.onCandlestickEvent(pair, CandlestickInterval.ONE_MINUTE, manager)

  // TODO: determine multi-threadedness / race conditions

  Thread.sleep(1000 * 120)

  closeHandle.close()
  manager.finalize()
  println("All done.")
}