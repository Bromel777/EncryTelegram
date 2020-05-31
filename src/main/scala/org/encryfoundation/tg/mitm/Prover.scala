package org.encryfoundation.tg.mitm

import java.math.{BigInteger, RoundingMode}
import java.util.Random

import com.google.common.math.BigIntegerMath
import it.unisa.dia.gas.jpbc.{Element, Pairing, Point}
import org.encryfoundation.mitmImun.Utils.hashFirst
import scorex.crypto.encode.Base64

case class Prover(generator1: Element, // generator of G1
                  generator2: Element, // generator of G2
                  ksi: Element, // prover secret #1
                  ti: Element, // prover secret #2
                  publicKey1: Element, // public key #1 of prover
                  publicKey2: Element,
                  zRGenerator: Element,
                  pairing: Pairing) {

  val generator3 = pairing.pairing(generator1.getImmutable, generator2.getImmutable).getImmutable

  var ny: Element = zRGenerator.getField.newElement().setToRandom()

  val rand: Random = new Random()

  //Produce S1
  def firstStep(): Element = {
    println("======First step======")
    val xHjhash = hashFirst(publicKey1.toBytes, 256)
    val xHj =
      zRGenerator.getField.newElement(generator1.getField.newElementFromHash(xHjhash, 0, xHjhash.length).getImmutable.asInstanceOf[Point[Element]].getX.toBigInteger)
    println(s"xHj: ${Base64.encode(xHj.toBytes)}")
    val mult = generator1.duplicate().mulZn(xHj.duplicate().mulZn(ny))
    println("======First step End======")
    mult.getImmutable
  }

  def genElementG1(s2: Element): Element = {
    val xHjhash = hashFirst(publicKey1.toBytes, 256)
    val xHj =
      zRGenerator.getField.newElement(generator1.getField.newElementFromHash(xHjhash, 0, xHjhash.length).getImmutable.asInstanceOf[Point[Element]].getX.toBigInteger)
    println(s"xHj: ${Base64.encode(xHj.toBytes)}")
    val firstPairingElem =
      ti.getImmutable.add(
        generator1.duplicate().mulZn(xHj.duplicate().mul(
          ny.duplicate().add(
            zRGenerator.getField.newElement(BigInteger.ONE)
          )
        ))
      )
    val secondPairingElem = s2.getImmutable
    println(s"S2: ${Base64.encode(secondPairingElem.toBytes)}")
    pairing.pairing(
      firstPairingElem,
      secondPairingElem).getImmutable
  }

  //Produce c
  def thirdStep(s2: Element): Array[Byte] = {
    println("=======THIRD STEP========")
    val g1 = genElementG1(s2)
    println(s"g1: ${Base64.encode(g1.toBytes)}")
    val res = hashFirst(g1.toBytes, BigIntegerMath.log2(generator3.getField.getOrder, RoundingMode.UP))
    println("=======THIRD STEP END======")
    res
  }

  def produceCommonKey(verifierPublicKey1: Element, s1: Element, s2: Element): Array[Byte] = {
    val xHjhash = hashFirst(publicKey1.toBytes, 256)
    val xHj =
      zRGenerator.getField.newElement(generator1.getField.newElementFromHash(xHjhash, 0, xHjhash.length).getImmutable.asInstanceOf[Point[Element]].getX.toBigInteger)
    val hash = zRGenerator.getField.newElementFromBytes(hashFirst(
      verifierPublicKey1.toBytes ++ s1.asInstanceOf[Point[Element]].getX.toBytes,
      BigIntegerMath.log2(generator3.getField.getOrder, RoundingMode.UP)
    ))
    val secondAddElemInGamma = hash.getImmutable.mulZn(ksi.getImmutable).getImmutable
    val dummyPubKey = pairing.pairing(generator1.getImmutable.mulZn(ksi.getImmutable), generator1.getImmutable).getImmutable
    val gamma = secondAddElemInGamma.getImmutable.add(xHj.getImmutable.mul(ny)).getImmutable
    val pairingRes = pairing.pairing(s2.getImmutable, generator1.getImmutable).getImmutable
    val verPubKeydegree = zRGenerator.getField.newElementFromBytes(hashFirst(
      publicKey1.toBytes ++
        publicKey2.toBytes ++
        xHj.toBytes ++
        s2.asInstanceOf[Point[Element]].getX.toBytes,
      BigIntegerMath.log2(generator3.getField.getOrder, RoundingMode.UP)
    )).getImmutable
    val verPubKeyInDeg = verifierPublicKey1.duplicate().powZn(verPubKeydegree).getImmutable
    val g4 = pairingRes.duplicate().mul(verPubKeyInDeg).getImmutable
    g4.powZn(gamma).toBytes
  }
}
