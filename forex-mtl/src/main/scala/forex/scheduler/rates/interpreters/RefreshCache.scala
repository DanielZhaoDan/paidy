package forex.scheduler.rates.interpreters

import akka.actor.ActorSystem
import scala.concurrent.duration._
import scala.concurrent.Future
import scala.util.{ Failure, Success }
import java.util.concurrent.TimeUnit
import forex.cache.rates.LocalCache.cacheServer
import forex.config.ApplicationConfig
import forex.domain._
import forex.domain.external._
import forex.scheduler.rates.Algebra
import scalacache.modes.sync._
import net.liftweb.json._

import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import HttpMethods._


class RefreshCache(
    config: ApplicationConfig
) extends Algebra {

  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global
  val params = assembleRateFetchParam()
  implicit val actorSystem = ActorSystem()
  implicit val scheduler = actorSystem.scheduler
  implicit val executor = actorSystem.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  execute()

  override def execute(): Unit = {
    val task = new Runnable {
      def run(): Unit = {
        fetchLatestRatePair(config, 0)
      }
    }
    scheduler.schedule(
      initialDelay = Duration(config.scheduler.initialDelay.toLong, TimeUnit.SECONDS),
      interval = Duration(config.scheduler.interval.toLong, TimeUnit.SECONDS),
      runnable = task
    )
    ()
  }

  def fetchLatestRatePair(config: ApplicationConfig, retryTime: Int): Unit = {
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
        case Failure(_) => fetchLatestRatePair(config, retryTime+1)
      }
    ()
  }

  def assembleRateFetchParam(): String = {
    var returnVal : String = ""

    var I : Int = 0
    var J : Int = 0
    val len : Int = Currency.supportedCurrencies.size

    while (I < len) {
      J = 0
      while (J < len) {
        if (I != J) {
          returnVal = returnVal.concat(s"pair=${Currency.supportedCurrencies.apply(I)}${Currency.supportedCurrencies.apply(J)}&")
        }
        J = J + 1
      }
      I = I + 1
    }
    print(returnVal)
    returnVal
  }

  def doRefreshCache(resp: HttpResponse, cacheTTL: FiniteDuration) : Unit = {
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

