package forex.cache.rates

import cats.Applicative
import cats.syntax.applicative._
import cats.syntax.either._
import errors._
import forex.config.ApplicationConfig
import forex.cache.rates.Algebra
import forex.domain._

class Cache[F[_]: Applicative] extends Algebra[F] {

  override def get(pair: Rate.Pair): F[Error Either Rate] =
    Rate(pair, Price(BigDecimal(123)), Timestamp.now).asRight[Error].pure[F]
  override def set(key: String, rate: Rate, ttl:Long): F[Error Either Unit] =
    print(s"set $key: $Rate").asRight[Error].pure[F]

}

object Cache(
config: ApplicationConfig
) {

  def apply[F[_]: Applicative](config: ApplicationConfig): Algebra[F] = {
    new Cache[F]()
    print(config.memcached.port)
  }

}
