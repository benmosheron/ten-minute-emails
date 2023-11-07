package tme.api

import cats.Monad
import cats.syntax.all._
import org.typelevel.log4cats.LoggerFactory
import tme.data.DataStore
import tme.data.Model._
import tme.generator.IdGenerator

import java.time.{LocalDateTime, ZoneId}

class TenMinuteEmailService[F[_]: Monad: LoggerFactory](idGenerator: IdGenerator[F], dataStore: DataStore[F])
    extends TenMinuteEmailApi[F] {

  private val logger = LoggerFactory[F].getLogger

  override def createTemporaryEmail(): F[TemporaryEmail] = for {
    temporaryEmail <- idGenerator.generate()
    time = LocalDateTime.now(ZoneId.of("Z"))
    _ <- dataStore.createTemporaryEmail(temporaryEmail, time)
    _ <- logger.info(s"Created temporary email [${temporaryEmail.email}] at time [${time.toString}]")
  } yield temporaryEmail

  override def addEmail(key: TemporaryEmail, email: Email): F[Unit] = for {
    _ <- logger.info(s"Attempting to add to [${key.email}]. Email: ${email.data}")
    _ <- dataStore.addEmail(key, email)
  } yield ()

  override def getEmails(key: TemporaryEmail): F[Vector[Email]] = for {
    emails <- dataStore.getEmails(key)
    _ <- logger.info(s"Found [${emails.length}] emails for [${key.email}]")
  } yield emails

}
