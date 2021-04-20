import com.binance.api.client.BinanceApiCallback
import com.binance.api.client.BinanceApiClientFactory
import com.binance.api.client.BinanceApiWebSocketClient
import com.binance.api.client.domain.event.CandlestickEvent
import com.binance.api.client.domain.market.CandlestickInterval
import java.io.Closeable

const val MS_IN_MINUTE = 60 * 1000
const val MS_IN_HOUR = MS_IN_MINUTE * 60
const val MS_IN_DAY = MS_IN_HOUR * 24
const val MS_IN_WEEK = MS_IN_DAY * 7
// TODO: how are months actually defined?
const val MS_IN_MONTH = MS_IN_DAY * 30

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
      writers[it] = CandleWriter("$it.candles")
    }
  }

  private var closeHandle: Closeable? = null
  fun runFetch() {
    println("Fetching candles for ${pairs.joinToString()}. . .")
    closeHandle = client.onCandlestickEvent(pairs.joinToString(), CandlestickInterval.ONE_MINUTE, this)
  }

  fun close() {
    closeHandle?.close()
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

  /*
  fun resizeCandles(interval: CandlestickInterval): Iterator<Candle> {
    val chunkSize = when (interval) {
      // This is the same size as the underlying candles.
      CandlestickInterval.ONE_MINUTE -> return this.candles.iterator()
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

    if (this.candles.size < chunkSize) {
      return emptyList<Candle>().iterator()
    }

    // Find the first candle that aligns with the start of the intended interval
    val candles = Iterable { candles.iterator() }.dropWhile {
      it.openTime % alignment != 0L
    }

    return candles.windowed(chunkSize, chunkSize, partialWindows = false).map {
      val firstCandle = it.first()
      it.fold(firstCandle) { l: Candle, r: Candle ->
        l.foldCandle(r)
      }
    }.iterator()
  }
   */

  fun finalize() {
    this.writers.values.forEach() {
      it.finish()
    }
  }
}