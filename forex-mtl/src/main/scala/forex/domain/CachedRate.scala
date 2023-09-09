package forex.domain

case class CachedRate(
    price: Double,
    bid: Double,
    ask: Double,
    timestamp: String
)