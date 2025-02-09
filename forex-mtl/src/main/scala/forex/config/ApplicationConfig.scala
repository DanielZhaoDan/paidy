package forex.config

import scala.concurrent.duration.FiniteDuration

case class ApplicationConfig(
    http: HttpConfig,
    external: ExternalConfig,
    memcached: CacheConfig,
    scheduler: SchedulerConfig,
)

case class HttpConfig(
    host: String,
    port: Int,
    timeout: FiniteDuration,
    allowedToken: String,
)

case class ExternalConfig(
    host: String,
    port: Int,
    timeout: Int,
    token: String,
)

case class CacheConfig(
    host: String,
    port: Int,
    ttl: FiniteDuration,
)

case class SchedulerConfig(
    interval: Int,
    initialDelay: Int,
)
