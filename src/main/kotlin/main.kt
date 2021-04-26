import com.binance.api.client.domain.market.CandlestickInterval
import java.lang.Runtime
import kotlin.system.exitProcess

enum class RunMode {
  FETCH, RESIZE
}

fun printUsage() {
  val intervals = CandlestickInterval.values().asSequence().joinToString()
  println("""
    Supports two modes:
      fetch <pair> [<pair>...]
      resize <pair> <interval>
    Possible intervals: $intervals
    """)
}


fun main(args: Array<String>) {
  var mode = RunMode.FETCH
  if (args.isNotEmpty()) {
    mode = RunMode.valueOf(args[0].toUpperCase())
  }

  println("Running in $mode mode. . .")

  when (mode) {
    RunMode.FETCH -> {
      val pairs = args.drop(1);
      if (pairs.isEmpty()) {
        println("Not enough arguments provided.")
        printUsage()
        exitProcess(1)
      }
      val fetcher = CandleFetcher(pairs.toTypedArray())
      // Setup shutdown signal
      Runtime.getRuntime().addShutdownHook(Thread {
        println("Caught signal, shutting down. . .")
        fetcher.close()
      })

      fetcher.runFetch()
    }
    RunMode.RESIZE -> {
      val pair = args.getOrNull(1)
      val interval = args.getOrNull(2)?.runCatching {
        CandlestickInterval.valueOf(this)
      }?.getOrNull()

      if (pair == null || interval == null) {
        printUsage()
        exitProcess(1)
      }

      println("Resizing all available data for $pair in $interval candles. . .")

      val resizer = CandleResizer(pair)
      for (c in resizer.resizeForInterval(interval)) {
        println("${c.openTimeAsDateTime()} to ${c.closeTimeAsDateTime()}: $c")
      }
      resizer.close()
    }
  }
}