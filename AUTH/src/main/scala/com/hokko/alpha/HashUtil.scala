package com.hokko.alpha

import java.io.{File, FileInputStream}
import java.nio.charset.Charset
import java.security.{KeyStore, MessageDigest, SecureRandom}
import java.util.concurrent.TimeUnit

object HashUtil {
    val config = SecurityConfiguration.getConfiguration.getOrElse(throw new NullPointerException)
    private val _charset = Charset.forName("UTF-8")

    def createHashForUserWhenRegistering(username: String, password: String) : (Array[Byte], Array[Byte], String) = {
        val ran = SecureRandom.getInstance("SHA1PRNG")
        val salt = s"${Math.abs(ran.nextLong())}${Math.abs(ran.nextLong())}"
        val md = MessageDigest.getInstance(config.hashAlgo)
        val digest = md.digest(s"$username:$password:$salt".getBytes(_charset))
        (digest,salt.getBytes(_charset), config.hashAlgo)
    }

    def createHashForUserWhenLogginAttempt(username: String, salt: Array[Byte], algo: String, pw: String) : Array[Byte] = {
        val saltAsString = new String(salt, _charset)
        val md = MessageDigest.getInstance(algo)
        val digest = md.digest(s"$username:$pw:$saltAsString".getBytes(_charset))
        (digest)
    }

    def createJwtToken(username: String): String = {
        import com.hokko.alpha.models.dto.JWTContent._
        import pdi.jwt.{JwtAlgorithm, JwtClaim, JwtSprayJson}

        val algorithm = JwtAlgorithm.HS256
        val keystore = KeyStore.getInstance(config.keystoreAlgo);
        val path = "AUTH\\src\\main\\resources\\"
        keystore.load(new FileInputStream(new File(s"$path${config.keystoreFile}")), config.keystorePw.toCharArray)
        val key = keystore.getKey("jwt_key", config.keystorePw.toCharArray)

        def createToken(username: String, expirationPeriodInDays: Int): String = {
            val claims = JwtClaim(
                expiration = Some(System.currentTimeMillis() / 1000 + TimeUnit.DAYS.toSeconds(expirationPeriodInDays)),
                issuedAt = Some(System.currentTimeMillis() / 1000),
                issuer = Some("com.hokko.alpha"),
                content = JwtContent(s"Hi $username").toJson.prettyPrint
            )

            JwtSprayJson.encode(claims, new String(key.getEncoded, _charset), algorithm) // JWT string
        }

        createToken(username, 1)

    }
}
