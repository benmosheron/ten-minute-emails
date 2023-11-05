package tme.data

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import tme.data.Model._
import cats.syntax.all._

import java.time.{LocalDateTime, ZoneId}

class DataStoreMapTest extends AsyncWordSpec with AsyncIOSpec with Matchers {
  private val time = LocalDateTime.now(ZoneId.of("Z"))

  "createTemporaryEmail" should {

    "Add a TemporaryEmail to the store with an empty inbox" in {
      val result = for {
        dataStoreMap <- DataStoreMap.build[IO]()
        _ <- dataStoreMap.createTemporaryEmail(TemporaryEmail("a"), time)
        store <- dataStoreMap.store.get
      } yield store

      val expected = Map(TemporaryEmail("a") -> Inbox.empty(time))

      result.asserting(_ shouldBe expected)
    }

    "Add many TemporaryEmails to the store concurrently, all with empty inboxes" in {
      val temporaryEmails = (0 to 10000).map(_.toString).map(TemporaryEmail).toList

      val result = for {
        dataStoreMap <- DataStoreMap.build[IO]()
        _ <- temporaryEmails.parTraverse(te => dataStoreMap.createTemporaryEmail(te, time))
        store <- dataStoreMap.store.get
      } yield store

      val expected = temporaryEmails.map(te => (te, Inbox.empty(time))).toMap

      result.asserting(_ shouldBe expected)
    }

  }

  "addEmail" should {

    "add an email to the inbox of a temporary email" in {
      val key = TemporaryEmail("a")
      val email = Email("test email data")
      val result = for {
        dataStoreMap <- DataStoreMap.build[IO]()
        _ <- dataStoreMap.createTemporaryEmail(key, time)
        _ <- dataStoreMap.addEmail(key, email)
        store <- dataStoreMap.store.get
      } yield store

      val expected = Map(key -> Inbox(time, Vector(email)))

      result.asserting(_ shouldBe expected)
    }

    "add three emails to an inbox" in {
      val key = TemporaryEmail("a")
      val firstEmail = Email("test email data 1")
      val secondEmail = Email("test email data 2")
      val thirdEmail = Email("test email data 3")
      val result = for {
        dataStoreMap <- DataStoreMap.build[IO]()
        _ <- dataStoreMap.createTemporaryEmail(key, time)
        _ <- dataStoreMap.addEmail(key, firstEmail)
        _ <- dataStoreMap.addEmail(key, secondEmail)
        _ <- dataStoreMap.addEmail(key, thirdEmail)
        store <- dataStoreMap.store.get
      } yield store

      val expected = Map(key -> Inbox(time, Vector(firstEmail, secondEmail, thirdEmail)))

      result.asserting(_ shouldBe expected)
    }

    "do nothing if an email is added to a non-existent temporary email" in {
      val key = TemporaryEmail("a")
      val email = Email("test email data")
      val result = for {
        dataStoreMap <- DataStoreMap.build[IO]()
        _ <- dataStoreMap.addEmail(key, email)
        store <- dataStoreMap.store.get
      } yield store

      val expected = Map[TemporaryEmail, Inbox]()

      result.asserting(_ shouldBe expected)
    }

  }

  "getEmails" should {

    "return an empty vector if the provided key doesn't exist" in {
      val key = TemporaryEmail("a")

      val result = for {
        dataStoreMap <- DataStoreMap.build[IO]()
        emails <- dataStoreMap.getEmails(key)
      } yield emails

      val expected = Vector.empty[Email]

      result.asserting(_ shouldBe expected)
    }

    "return the vector of emails for a key with three emails, in storage order" in {
      val key = TemporaryEmail("a")
      val firstEmail = Email("test email data 1")
      val secondEmail = Email("test email data 2")
      val thirdEmail = Email("test email data 3")
      val result = for {
        dataStoreMap <- DataStoreMap.build[IO]()
        _ <- dataStoreMap.createTemporaryEmail(key, time)
        _ <- dataStoreMap.addEmail(key, firstEmail)
        _ <- dataStoreMap.addEmail(key, secondEmail)
        _ <- dataStoreMap.addEmail(key, thirdEmail)
        emails <- dataStoreMap.getEmails(key)
      } yield emails

      val expected = Vector(firstEmail, secondEmail, thirdEmail)

      result.asserting(_ shouldBe expected)
    }

  }

  "deleteOlderThan" should {

    "delete all temporary email addresses older than a given time" in {
      val cutoffTime = time

      val oldKeyA = TemporaryEmail("a")
      val oldKeyB = TemporaryEmail("b")
      val oldKeyC = TemporaryEmail("c")
      val newKeyD = TemporaryEmail("d")

      val result = for {
        dataStoreMap <- DataStoreMap.build[IO]()
        _ <- dataStoreMap.createTemporaryEmail(oldKeyA, cutoffTime.minusSeconds(3))
        _ <- dataStoreMap.createTemporaryEmail(oldKeyB, cutoffTime.minusSeconds(2))
        _ <- dataStoreMap.createTemporaryEmail(oldKeyC, cutoffTime.minusSeconds(1))
        _ <- dataStoreMap.createTemporaryEmail(newKeyD, cutoffTime.plusSeconds(1))
        deleted <- dataStoreMap.deleteOlderThan(cutoffTime)
        store <- dataStoreMap.store.get
      } yield (deleted, store)

      val expected = (3, Map(newKeyD -> Inbox.empty(cutoffTime.plusSeconds(1))))

      result.asserting(_ shouldBe expected)

    }

  }

}
