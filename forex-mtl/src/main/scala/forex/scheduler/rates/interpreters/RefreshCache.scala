package forex.scheduler.rates.interpreters

import akka.actor.ActorSystem
import scala.concurrent.duration._
import java.util.concurrent.TimeUnit
import forex.scheduler.rates.Algebra
import forex.cache.rates.LocalCache
import forex.config.ApplicationConfig


class RefreshCache(
    config: ApplicationConfig
) extends Algebra {

  implicit val actorSystem = ActorSystem()
  implicit val scheduler = actorSystem.scheduler
  implicit val executor = actorSystem.dispatcher

  execute()

  override def execute(): Unit = {
    val task = new Runnable {
      def run(): Unit = {
        LocalCache.fetchLatestRatePair(config, 0)
      }
    }
    scheduler.schedule(
      initialDelay = Duration(config.scheduler.initialDelay.toLong, TimeUnit.SECONDS),
      interval = Duration(config.scheduler.interval.toLong, TimeUnit.SECONDS),
      runnable = task
    )
    ()
  }

}

