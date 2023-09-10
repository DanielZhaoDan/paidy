package forex.http
package rates

import cats.effect.Sync
import cats.syntax.flatMap._
import forex.config.ApplicationConfig
import forex.domain._
import forex.programs.RatesProgram
import forex.programs.rates.{ Protocol => RatesProgramProtocol }
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

class RatesHttpRoutes[F[_]: Sync](rates: RatesProgram[F], config: ApplicationConfig) extends Http4sDsl[F] {

  import Converters._, QueryParams._, Protocol._

  private[http] val prefixPath = "/rates"

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root :? FromQueryParam(from) +& ToQueryParam(to) +& TokenQueryParam(token) =>
      // TODO: use AuthMiddleware for authentication
      if (token != config.http.allowedToken) {
        Forbidden("Your request is forbidden because of Missing or wrong token used")
      } else {
        if (from == Currency.ERR || to == Currency.ERR) {
          BadRequest("invalid currency argument")
        } else {
          rates.get(RatesProgramProtocol.GetRatesRequest(from, to)).flatMap(Sync[F].fromEither).flatMap { rate =>
            val resp = rate.asGetApiResponse
            if (resp.bid == Price(BigDecimal(-1))) {
              // if no rate pair get, return service is temporarily unavailable
              ServiceUnavailable("Service unavailable, please try again later")
            } else {
              Ok(resp)
            }
          }
        }
      }
  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )

}
