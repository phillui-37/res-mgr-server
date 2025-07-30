package xyz.kgy.production.util

object SysUtil {
  def safeGetEnv(key: String): Option[String] = try {
    Option(sys.env(key))
  } catch {
    case _: NoSuchElementException => None
    case th: Throwable => throw th
  }
}