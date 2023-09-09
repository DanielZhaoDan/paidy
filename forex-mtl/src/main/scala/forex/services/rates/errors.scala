package forex.services.rates

import forex.cache.rates.errors.{ Error => RatesCacheError }

object errors {

  sealed trait Error
  object Error {
    final case class OneFrameLookupFailed(msg: String) extends Error
  }

  def toServicesError(error: RatesCacheError): Error = error match {
    case RatesCacheError.CacheLookupFailed(msg) => Error.OneFrameLookupFailed(msg)
  }

}
