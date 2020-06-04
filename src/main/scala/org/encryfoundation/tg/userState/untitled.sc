import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory
import org.encryfoundation.tg.pipelines.groupVerification.messages.StepMsg.GroupVerificationStepMsg.VerifierSecondStepMsg
import org.encryfoundation.tg.pipelines.groupVerification.messages.serializer.groupVerification.VerifierSecondMsgSerializer._
import org.encryfoundation.tg.pipelines.groupVerification.messages.serializer.StepMsgSerializer

val pairing = PairingFactory.getPairing("/Users/aleksandr/IdeaProjects/telegramDragon/src/main/resources/properties/a.properties")

val verSec = VerifierSecondStepMsg(
  pairing.getGT.newRandomElement(),
  pairing.getG1.newRandomElement()
)

val bytes = StepMsgSerializer.toBytes(verSec)

val parsed = StepMsgSerializer.parseBytes(bytes)

println(parsed)