package forex.config

import scala.concurrent.duration.FiniteDuration

case class ApplicationConfig(
    http: HttpConfig,
    external: HttpConfig,
    memcached: CacheConfig,
    scheduler: SchedulerConfig,
)

case class HttpConfig(
    host: String,
    port: Int,
    timeout: FiniteDuration,
)

case class CacheConfig(
    host: String,
    port: Int,
)

case class SchedulerConfig(
    interval: Int,
    initialDelay: Int,
)
