# Binance Candle Service

Supports fetching and resizing of candles obtained from Binance. Written as a
first foray into Kotlin and Binance's API.

NOT FOR PRODUCTION. Really. This saves candles to a hardcoded folder based on
the current working directory for use in the supported resize mode.

## Running

You can run this in two modes: `fetch` and `resize`.

### Fetch

To fetch candles (for later resizing), you can
use `gradle run --args="fetch <pairs>"`. For example:

    gradle run --args="fetch ethbtc ethusdt"

would fetch minute candles for both ETH/BTC and ETH/USDT.

To stop the fetching of candles, use `ctrl-c`.

### Resize

To resize candles to a given interval (1 minute or longer), you can
use `gradle run --args="resize <pair> <interval>"`. For example:

    gradle run --args="resize ethbtc HOURLY"

would resize all available candles (all that have been fetched by running in
fetch mode) into hourly candles. Warnings are printed if the data is incomplete
or otherwise missing.

## Build notes

- You must already have the Binance Java API installed. Follow the
  instructions [here](https://github.com/binance-exchange/binance-java-api#installation)
  to install it. This is somewhat brittle, since the versions aren't pinned with tags.

## Known issues

- Very unconfigurable
- Likely breaks multiple Kotlin idioms and best practices
- Minimally (unit/integration) tested