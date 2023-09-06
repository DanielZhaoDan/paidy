package forex.cache.rates

import cats.Applicative
import cats.syntax.applicative._
import cats.syntax.either._
import errors._
import forex.config.ApplicationConfig
import forex.cache.rates.Algebra
import forex.domain._

class Cache[F[_]: Applicative] (
    config: ApplicationConfig
) extends Algebra[F] {

  override def get(pair: Rate.Pair): F[Error Either Rate] = {
    print(s"port: $config.memcached.port, host:$config.memcached.host \n")
    Rate(pair, Price(BigDecimal(123)), Timestamp.now).asRight[Error].pure[F]
  }

  override def set(key: String, rate: Rate, ttl:Long): Unit =
    print(s"set $key: $Rate")

}

object Cache {

  def apply[F[_]: Applicative](
      config: ApplicationConfig
  ): Algebra[F] = {
    print("start cache\n")
    new Cache[F](config)
  }

}
