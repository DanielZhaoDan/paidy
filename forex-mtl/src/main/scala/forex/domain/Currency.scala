package forex.domain

import cats.Show

sealed trait Currency

object Currency {
  case object AUD extends Currency
  case object CAD extends Currency
  case object CHF extends Currency
  case object EUR extends Currency
  case object GBP extends Currency
  case object NZD extends Currency
  case object JPY extends Currency
  case object SGD extends Currency
  case object USD extends Currency
  case object ERR extends Currency

  implicit val show: Show[Currency] = Show.show {
    case AUD => "AUD"
    case CAD => "CAD"
    case CHF => "CHF"
    case EUR => "EUR"
    case GBP => "GBP"
    case NZD => "NZD"
    case JPY => "JPY"
    case SGD => "SGD"
    case USD => "USD"
    case ERR => "ERR"
  }

  implicit val supportedCurrencies: List[String] = List("AUD", "CAD", "CHF", "EUR", "GBP", "NZD", "JPY", "SGD", "USD" )

  def fromString(s: String): Currency = s.toUpperCase match {
    case "AUD" => AUD
    case "CAD" => CAD
    case "CHF" => CHF
    case "EUR" => EUR
    case "GBP" => GBP
    case "NZD" => NZD
    case "JPY" => JPY
    case "SGD" => SGD
    case "USD" => USD
    case _ => ERR
  }

  def toString(c: Currency): String = c match {
    case AUD => "AUD"
    case CAD => "CAD"
    case CHF => "CHF"
    case EUR => "EUR"
    case GBP => "GBP"
    case NZD => "NZD"
    case JPY => "JPY"
    case SGD => "SGD"
    case USD => "USD"
    case ERR => "ERR"
  }

  implicit def assembleRateFetchParam(): String = {
    var returnVal: String = ""

    var I: Int = 0
    var J: Int = 0
    val len: Int = supportedCurrencies.size

    while (I < len) {
      J = 0
      while (J < len) {
        if (I != J) {
          returnVal = returnVal.concat(s"pair=${Currency.supportedCurrencies.apply(I)}${Currency.supportedCurrencies.apply(J)}&")
        }
        J = J + 1
      }
      I = I + 1
    }
    returnVal
  }

}
