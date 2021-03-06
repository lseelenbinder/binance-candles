import com.binance.api.client.BinanceApiCallback
import com.binance.api.client.BinanceApiClientFactory
import com.binance.api.client.BinanceApiWebSocketClient
import com.binance.api.client.domain.event.CandlestickEvent
import com.binance.api.client.domain.market.CandlestickInterval
import java.io.Closeable

fun fileNameForPair(pair: String) = "candles/$pair"

class CandleFetcher(private val pairs: Array<String>): BinanceApiCallback<CandlestickEvent> {
  init {
    println("Building candle fetcher with ${pairs.joinToString()}. . .")
  }
  private var client: BinanceApiWebSocketClient
  init {
    val clientFactory = BinanceApiClientFactory.newInstance("", "")
    client = clientFactory.newWebSocketClient()
    val restClient = clientFactory.newRestClient()

    print("Trying to contact Binance. . .")
    restClient.ping()
    println("success.")

  }

  private val writers: MutableMap<String, CandleWriter> = mutableMapOf();
  init {
    pairs.forEach {
      writers[it] = CandleWriter(fileNameForPair(it))
    }
  }

  private var closeHandle: Closeable? = null
  fun runFetch() {
    println("Fetching candles for ${pairs.joinToString()}. . .")
    closeHandle = client.onCandlestickEvent(pairs.joinToString(), CandlestickInterval.ONE_MINUTE, this)
  }

  fun close() {
    closeHandle?.close()
    writers.values.forEach {
      it.finish()
    }
  }

  override fun onResponse(event: CandlestickEvent?) {
    if (event != null) {
      if (event.barFinal) {
        // candle is closed, write it out
        val candle = Candle(event)
        this.writers[event.symbol.toLowerCase()]?.writeCandle(candle)
        println("Candle ${candle.symbol} close: ${candle.closeTime} @ ${candle.close}")
      } else {
        println("Open candle: ${event.close}")
      }
    }
  }

  override fun onFailure(cause: Throwable?) {
    println("Error encountered: $cause")
  }
}