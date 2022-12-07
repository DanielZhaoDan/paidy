package forex

package object cache {
  type RatesCache[F[_]] = rates.Algebra[F]
  final val RatesCaches = rates.Cache
}
