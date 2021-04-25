import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import kotlin.test.assertFails

internal class CandleTest {

  @Test
  fun foldCandleSanityCheck() {
    val candle1 = Candle(
      "ethbtc",
      0,
      499,
      0.5.toBigDecimal(),
      1.0.toBigDecimal(),
      1.2.toBigDecimal(),
      0.1.toBigDecimal(),
      20.0.toBigDecimal(),
      1,
    )
    val candle2 = Candle(
      "ethbtc",
      500,
      999,
      1.0.toBigDecimal(),
      0.5.toBigDecimal(),
      2.0.toBigDecimal(),
      0.0.toBigDecimal(),
      10.0.toBigDecimal(),
      1,
    )

    val folded = candle1.foldCandle(candle2)

    assertEquals(folded.close, candle2.close)
    assertEquals(folded.open, candle1.open)
    assertEquals(folded.openTime, candle1.openTime)
    assertEquals(folded.closeTime, candle2.closeTime)
    assertEquals(folded.low, 0.0.toBigDecimal())
    assertEquals(folded.high, 2.0.toBigDecimal())
    assertEquals(folded.volume, 30.0.toBigDecimal())
    assertEquals(folded.nTrades, 2)
  }

  @Test
  fun foldCandleAssertions() {
    val usdtCandle = Candle(
      "ethusdt",
      0,
      499,
      0.5.toBigDecimal(),
      1.0.toBigDecimal(),
      1.2.toBigDecimal(),
      0.1.toBigDecimal(),
      20.0.toBigDecimal(),
      1,
    )
    val btcCandle = Candle(
      "ethbtc",
      500,
      999,
      1.0.toBigDecimal(),
      0.5.toBigDecimal(),
      2.0.toBigDecimal(),
      0.0.toBigDecimal(),
      10.0.toBigDecimal(),
      1,
    )

    assertFails {
      usdtCandle.foldCandle(btcCandle)
    }

    var btcCandle2 = btcCandle.copy(closeTime = 488)
    assertFails {
      btcCandle.foldCandle(btcCandle2)
    }
  }
}