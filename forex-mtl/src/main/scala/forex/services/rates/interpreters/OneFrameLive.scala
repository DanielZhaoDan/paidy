package forex.services.rates.interpreters

import forex.services.rates.Algebra
import cats.Applicative
import cats.data.EitherT
import forex.cache.RatesCache
import forex.domain.Rate
import forex.services.rates.errors._

class OneFrameLive[F[_]: Applicative](
  ratesCache: RatesCache[F]
) extends Algebra[F] {

  override def get(pair: Rate.Pair): F[Error Either Rate] =
    EitherT(ratesCache.get(pair)).leftMap(toProgramError(_)).value

}

object OneFrameLive {

  def apply[F[_]: Applicative](
      ratesCache: RatesCache[F]
  ): Algebra[F] = new OneFrameLive[F](ratesCache)

}
