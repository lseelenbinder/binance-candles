import com.binance.api.client.domain.market.CandlestickInterval
import java.lang.Runtime

enum class RunMode {
  FETCH, RESIZE
}


fun main(args: Array<String>) {
  var mode = RunMode.FETCH
  if (args.isNotEmpty()) {
    mode = RunMode.valueOf(args[0].toUpperCase())
  }

  println("Running in $mode mode. . .")

  when (mode) {
    RunMode.FETCH -> {
      val pairs = arrayOf("ethbtc", "ethusdt")
      val fetcher = CandleFetcher(pairs)
      // Setup shutdown signal
      Runtime.getRuntime().addShutdownHook(Thread {
        println("Caught signal, shutting down. . .")
        fetcher.close()
      })

      fetcher.runFetch()
    }
    RunMode.RESIZE -> {
      val resizer = CandleResizer("ethbtc")
      for (c in resizer.resizeForInterval(CandlestickInterval.FIVE_MINUTES)) {
        println("${c.openTimeAsDateTime()} to ${c.closeTimeAsDateTime()}: $c")
      }
      resizer.close()
    }
  }
}