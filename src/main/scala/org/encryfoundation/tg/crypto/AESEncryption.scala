package org.encryfoundation.tg.crypto

import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException
import javax.crypto.spec.SecretKeySpec
import java.security.NoSuchAlgorithmException
import java.security.NoSuchProviderException

import org.bouncycastle.jce.provider.BouncyCastleProvider
import scorex.crypto.encode.{Base16, Base64}
import scorex.crypto.hash.Blake2b256

case class AESEncryption(keyBytes: Array[Byte]) {

  import java.security.Security

  val cipher = Cipher.getInstance("AES/CBC/PKCS7PADDING", "BC")
  var key: SecretKeySpec = new SecretKeySpec(Blake2b256.hash(keyBytes), "AES")

  import javax.crypto.spec.IvParameterSpec
  import java.security.spec.AlgorithmParameterSpec

  val IVspec = new IvParameterSpec("0123456789ABCDEF".getBytes)

  def encrypt(plainText: Array[Byte]): Array[Byte] = {
    cipher.init(Cipher.ENCRYPT_MODE, key, IVspec)
    val cipherText = cipher.doFinal(plainText)
    cipherText
  }

  def decrypt(cipherText: Array[Byte]): Array[Byte] = {
    cipher.init(Cipher.DECRYPT_MODE, key, IVspec)
    cipher.doFinal(cipherText)
  }
}
