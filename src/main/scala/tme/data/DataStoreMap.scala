package tme.data

import cats.Applicative
import cats.effect.kernel.{Ref, Sync}
import cats.syntax.all._
import tme.data.DataStoreMap.EmailMap
import tme.data.Model._

import java.time.LocalDateTime

object DataStoreMap {

  type EmailMap = Map[TemporaryEmail, Inbox]

  def build[F[_]: Sync](): F[DataStoreMap[F]] = for {
    ref <- Ref[F].of[EmailMap](Map())
  } yield new DataStoreMap[F](ref)

}
class DataStoreMap[F[_]: Applicative](private[data] val store: Ref[F, EmailMap]) extends DataStore[F] {

  override def createTemporaryEmail(
      temporaryEmail: TemporaryEmail,
      createdDateTime: LocalDateTime
  ): F[Unit] =
    store.update(_.updated(temporaryEmail, Inbox.empty(createdDateTime)))

  override def addEmail(key: TemporaryEmail, email: Email): F[Unit] = store.update(_.updatedWith(key) {
    case None        => None
    case Some(inbox) => Some(inbox + email)
  })

  override def getEmails(key: TemporaryEmail): F[Vector[Email]] = for {
    emailMap <- store.get
  } yield emailMap
    .get(key)
    .map(_.emails)
    .getOrElse(Vector.empty)

  override def deleteOlderThan(dateTime: LocalDateTime): F[Int] = store.modify(deleteOlderThanSync(dateTime))

  private def deleteOlderThanSync(dateTime: LocalDateTime)(emailMap: EmailMap): (EmailMap, Int) = {
    val toRemove = emailMap.filter { case (_, inbox) => inbox.createdDateTime.isBefore(dateTime) }.keys.toVector
    (emailMap.removedAll(toRemove), toRemove.length)
  }
}
