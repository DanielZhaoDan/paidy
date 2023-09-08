package forex.cache.rates

import cats.Applicative
import cats.syntax.applicative._
import cats.syntax.either._
import errors._

import forex.config.ApplicationConfig
import forex.cache.rates.Algebra
import forex.domain._
import scala.concurrent.duration._
import scalacache._
import scalacache.memcached._
import scalacache.modes.sync._
import scalacache.serialization.binary._

class LocalCache[F[_]: Applicative] (
    cacheServer: Cache[Rate]
) extends Algebra[F] {

  override def get(pair: Rate.Pair): F[Error Either Rate] = {
    print("cache get")
    Rate(pair, Price(BigDecimal(123)), Timestamp.now).asRight[Error].pure[F]
  }

  override def set(key: String, rate: Rate, ttl:Long): Unit = {
    print(s"cache set: ${key}")
    cacheServer.put(key)(rate, ttl=Some(1.seconds))
    ()
  }
}

object LocalCache {

  implicit var cacheServer: Cache[Rate] = _

  def apply[F[_]: Applicative](
      config: ApplicationConfig
  ): Algebra[F] = {
    print("start cache\n")
    cacheServer = MemcachedCache(s"${config.memcached.host}:${config.memcached.port}")
    new LocalCache[F](cacheServer)
  }

}
