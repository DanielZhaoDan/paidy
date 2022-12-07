package forex.services.rates.interpreters

import forex.services.rates.Algebra
import cats.Applicative
import forex.cache.RatesCache
import forex.services.rates.errors._

class OneFrameLive[F[_]: Applicative](
  ratesCache: RatesCache
) extends Algebra[F] {

  override def get(pair: Rate.Pair): F[Error Either Rate] =
    ratesCache.get(pair)

}
