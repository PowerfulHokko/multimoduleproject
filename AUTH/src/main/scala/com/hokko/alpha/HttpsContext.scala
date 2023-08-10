package com.hokko.alpha

import akka.http.scaladsl.{ConnectionContext, HttpsConnectionContext}

import java.io.InputStream
import java.security.{KeyStore, SecureRandom}
import javax.net.ssl.{KeyManagerFactory, SSLContext, TrustManagerFactory}

object HttpsContext{
    private val config = SecurityConfiguration.getConfiguration.getOrElse(throw new NoSuchElementException())

    //1: key store
    private val ks: KeyStore = KeyStore.getInstance(config.keystoreAlgo)
    private val keystoreFile: InputStream = getClass.getClassLoader.getResourceAsStream(config.keystoreFile)
    //new FileInputStream(new File("src/main/resources/filename"))
    private val password = config.keystorePw.toCharArray //don't store passwords in code, fetch them from a secure place
    ks.load(keystoreFile, password)

    //2: init key manager
    private val keyManagerFactory = KeyManagerFactory.getInstance("SunX509") //PKI = public key infrastructure
    keyManagerFactory.init(ks, password)

    //3: init trust manager
    private val trustManagerFactory = TrustManagerFactory.getInstance("SunX509")
    trustManagerFactory.init(ks)

    //4: init SSL context
    private val sslContext : SSLContext = SSLContext.getInstance("TLS") //Transport layer security
    sslContext.init(keyManagerFactory.getKeyManagers, trustManagerFactory.getTrustManagers, new SecureRandom())

    //5: return the https connection context
    val httpsConnectionContext : HttpsConnectionContext = ConnectionContext.https(sslContext)
}