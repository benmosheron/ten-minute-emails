package tme

import cats.Monad
import tme.data.DataStore
import tme.data.Model._
import cats.syntax.all._

import java.time.{LocalDateTime, ZoneId}

class TenMinuteEmailService[F[_]: Monad](idGenerator: IdGenerator[F], dataStore: DataStore[F])
    extends TenMinuteEmailApi[F] {

  override def createTemporaryEmail(): F[TemporaryEmail] = for {
    temporaryEmail <- idGenerator.generate()
    _ <- dataStore.createTemporaryEmail(
      temporaryEmail,
      LocalDateTime.now(ZoneId.of("Z"))
    )
  } yield temporaryEmail

  override def addEmail(key: TemporaryEmail, email: Email): F[Unit] = dataStore.addEmail(key, email)

  override def getEmails(key: TemporaryEmail): F[Vector[Email]] = dataStore.getEmails(key)

}
