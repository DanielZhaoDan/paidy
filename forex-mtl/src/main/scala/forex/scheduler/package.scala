package forex

package object scheduler {
  type RatesScheduler = rates.Algebra
  final val RatesSchedulers = rates.Interpreters
}
