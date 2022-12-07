package forex.cache.rates

object errors {

  sealed trait Error
  object Error {
    final case class CacheLookupFailed(msg: String) extends Error
  }

}

