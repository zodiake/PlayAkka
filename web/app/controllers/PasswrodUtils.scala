package controllers

import java.math.BigInteger
import java.security.MessageDigest

/**
  * Created by zodiake on 16-10-19.
  */
object PasswordUtils {
  def getHash(s: String) = {
    val digest = MessageDigest.getInstance("MD5")
    val c = digest.digest(s.getBytes())
    val bigInt = new BigInteger(1, c)
    bigInt.toString(16)
  }
}
