package forex.cache.rates

import cats.Applicative
import cats.syntax.applicative._
import cats.syntax.either._
import errors._
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{ Failure, Success }
import scalacache._
import scalacache.memcached._
import scalacache.modes.sync._
import scalacache.serialization.binary._
import net.liftweb.json._
import akka.actor.ActorSystem
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import HttpMethods._
import forex.config.ApplicationConfig
import forex.cache.rates.Algebra
import forex.domain._
import forex.domain.external._

class LocalCache[F[_]: Applicative] (
    cacheServer: Cache[CachedRate],
) extends Algebra[F] {

  override def get(pair: Rate.Pair): F[Error Either Rate] = {
    val fromCurrency : String = Currency.toString(pair.from)
    val toCurrency : String = Currency.toString(pair.to)

    val cacheResultOption = cacheServer.get(s"$fromCurrency$toCurrency")
    if (cacheResultOption.isEmpty) {
      Rate(pair, Price(BigDecimal(-1)), Price(BigDecimal(-1)), Price(BigDecimal(-1)), "").asRight[Error].pure[F]
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

  implicit val actorSystem = ActorSystem()
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  implicit var cacheServer: Cache[CachedRate] = _

  val params = Currency.assembleRateFetchParam()

  implicit def fetchLatestRatePair(config: ApplicationConfig, retryTime: Int): Unit = {
    print(s"refreshing for No. ${retryTime}...\n")

    val url = s"http://${config.external.host}:${config.external.port}/rates?${params}"

    val responseFuture: Future[HttpResponse] = Http().singleRequest(
      HttpRequest(GET, uri = url)
        .withHeaders(
          RawHeader("token", config.external.token)
        )
    )
    responseFuture
      .onComplete {
        case Success(res) => doRefreshCache(res, config.memcached.ttl)
        case Failure(_) => fetchLatestRatePair(config, retryTime + 1)
      }
    ()
  }

  def apply[F[_]: Applicative](
      config: ApplicationConfig
  ): Algebra[F] = {
    cacheServer = MemcachedCache(s"${config.memcached.host}:${config.memcached.port}")
    new LocalCache[F](cacheServer)
  }


  def doRefreshCache(resp: HttpResponse, cacheTTL: FiniteDuration): Unit = {
    implicit val formats = net.liftweb.json.DefaultFormats

    val respStrFuture = Unmarshal(resp.entity).to[String]

    respStrFuture foreach {
      respStr =>
        val ratePairList: List[RatesResponse] = parse(respStr).extract[List[RatesResponse]]
        print(s"doRefreshCache get pairs size: ${ratePairList.size}\n")

        for (ratePair <- ratePairList)
          cacheServer.put(s"${ratePair.from}${ratePair.to}")(CachedRate(ratePair.price, ratePair.bid, ratePair.ask, ratePair.time_stamp), ttl = Some(cacheTTL))
        ()
    }
  }
}
