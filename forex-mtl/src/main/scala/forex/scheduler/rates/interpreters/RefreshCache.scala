package forex.scheduler.rates.interpreters

import akka.actor.ActorSystem
import scala.concurrent.duration._
import java.util.concurrent.TimeUnit
import forex.cache.rates.LocalCache.cacheServer
import forex.config.ApplicationConfig
import forex.domain._
import forex.scheduler.rates.Algebra
import scalacache.modes.sync._


class RefreshCache(
    config: ApplicationConfig
) extends Algebra {

  override def execute(): Unit = {
    val actorSystem = ActorSystem()
    val scheduler = actorSystem.scheduler
    val task = new Runnable {
      def run(): Unit = {
        print("refreshing...")
        fetchLatestRatePair(config)
        cacheServer.put("1")(Rate(Rate.Pair(Currency.USD, Currency.JPY), Price(BigDecimal(100)), Timestamp.now), ttl=Some(1.seconds))
        ()
//        cacheServer.put("1", Rate(Rate.Pair(Currency.USD, Currency.JPY), Price(BigDecimal(100)), Timestamp.now), config.scheduler.interval.toLong)
      }
    }
    implicit val executor = actorSystem.dispatcher

    scheduler.schedule(
      initialDelay = Duration(config.scheduler.initialDelay.toLong, TimeUnit.SECONDS),
      interval = Duration(config.scheduler.interval.toLong, TimeUnit.SECONDS),
      runnable = task
    )
    print("executing...")
  }

  def fetchLatestRatePair(config: ApplicationConfig): Unit = {

    val params = collection.mutable.Map[String, String]()
    params += {"pair" -> "USDJPY"}
    val r = requests.get(
      s"http://${config.external.host}:${config.external.port}/rates",
      params = params,
      headers = Map("token" -> config.external.token),
      connectTimeout = config.external.timeout
    )
    print(s"status: ${r.statusCode}")
    print(s"data: ${r.data}")
    ()
  }

}

