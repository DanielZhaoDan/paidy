package forex.scheduler.rates

import interpreters._
import forex.config.ApplicationConfig

object Interpreters {
  def refreshCache(config: ApplicationConfig): Algebra = new RefreshCache(config)
}
