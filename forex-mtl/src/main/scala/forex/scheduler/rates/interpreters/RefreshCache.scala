package forex.scheduler.rates.interpreters

import akka.actor.ActorSystem
import scala.concurrent.duration._
import java.util.concurrent.TimeUnit
import forex.config.ApplicationConfig
import forex.scheduler.rates.Algebra

class RefreshCache(
    config: ApplicationConfig
) extends Algebra {

  override def execute(): Unit = {
    val actorSystem = ActorSystem()
    val scheduler = actorSystem.scheduler
    val task = new Runnable {
      def run(): Unit = {
        print("refreshing...")
      }
    }
    implicit val executor = actorSystem.dispatcher

    scheduler.schedule(
      initialDelay = Duration(config.scheduler.initialDelay.toLong, TimeUnit.SECONDS),
      interval = Duration(config.scheduler.interval.toLong, TimeUnit.SECONDS),
      runnable = task
    )
    print("executed...")
  }

}

