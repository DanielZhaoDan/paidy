package forex.cache.rates

import cats.Applicative
import cats.syntax.applicative._
import cats.syntax.either._
import errors._

import forex.config.ApplicationConfig
import forex.cache.rates.Algebra
import forex.domain._
import scalacache._
import scalacache.memcached._
import scalacache.modes.sync._
import scalacache.serialization.binary._

class LocalCache[F[_]: Applicative] (
    cacheServer: Cache[CachedRate]
) extends Algebra[F] {

  override def get(pair: Rate.Pair): F[Error Either Rate] = {
    val cacheResultOption = cacheServer.get("USDSGD")
    if (cacheResultOption.isEmpty) {
      Rate(pair, Price(BigDecimal(100)), Price(BigDecimal(100)), Price(BigDecimal(100)), "1970-01-01T00:00:00.00Z")
        .asRight[Error].pure[F]
    } else {
      val cacheResult = cacheResultOption.get
      Rate(pair,
        Price(BigDecimal(cacheResult.ask)),
        Price(BigDecimal(cacheResult.bid)),
        Price(BigDecimal(cacheResult.price)),
        cacheResult.timestamp,
      ).asRight[Error].pure[F]
    }
  }
}

object LocalCache {

  implicit var cacheServer: Cache[CachedRate] = _

  def apply[F[_]: Applicative](
      config: ApplicationConfig
  ): Algebra[F] = {
    cacheServer = MemcachedCache(s"${config.memcached.host}:${config.memcached.port}")
    new LocalCache[F](cacheServer)
  }

}
