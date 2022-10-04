package org.github.lithium.nirvana

import java.security.MessageDigest
import java.util.Base64

abstract class Password:
  def hashed: String

object Password:

  def plain(value: String): Password = Plain(value)

  def hashed(hashed: String): Password = Hashed(hashed)

  private def md5HashPassword(usPassword: String): String =
    def prependWithZeros(pwd: String): String = "%1$32s".format(pwd).replace(' ', '0')

    import java.math.BigInteger
    import java.security.MessageDigest
    val md = MessageDigest.getInstance("MD5")
    val digest: Array[Byte] = md.digest(usPassword.getBytes)
    val bigInt = new BigInteger(1, digest)
    val hashedPassword = bigInt.toString(16).trim
    prependWithZeros(hashedPassword)
  

  private case class Plain(value: String) extends Password:
    override def hashed: String = md5HashPassword(value)

  private case class Hashed(override val hashed: String) extends Password