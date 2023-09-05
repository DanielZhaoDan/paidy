package forex.cache.rates

import forex.domain.Rate
import errors._

trait Algebra[F[_]] {
  def get(pair: Rate.Pair): F[Error Either Rate]
  def set(key: String, rate: Rate, ttl:Long): Unit
}
