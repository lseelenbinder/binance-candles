import java.lang.Runtime
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock

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
      TODO()
    }
  }
}