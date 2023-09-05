package forex.services.rates

import cats.Applicative
import interpreters._
import forex.cache.RatesCache

object Interpreters {
  def dummy[F[_]: Applicative]: Algebra[F] = new OneFrameDummy[F]()
  def liveRate[F[_]: Applicative](ratesCache: RatesCache[F]): Algebra[F] = new OneFrameLive[F](ratesCache)
}
