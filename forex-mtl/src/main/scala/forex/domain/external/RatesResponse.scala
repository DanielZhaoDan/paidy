package forex.domain.external

case class RatesResponse(
    from: String,
    to: String,
    bid: Double,
    ask: Double,
    price: Double,
    time_stamp: String,
)
