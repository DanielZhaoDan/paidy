package forex.domain

case class Rate(
    pair: Rate.Pair,
    ask: Price,
    bid: Price,
    price: Price,
    timestamp: String
)



object Rate {
  final case class Pair(
      from: Currency,
      to: Currency
  )
}
