package tme

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import tme.data.DataStoreMap
import tme.data.Model.{Email, TemporaryEmail}

import java.util.concurrent.atomic.AtomicInteger

class TenMinuteEmailServiceTest extends AsyncWordSpec with AsyncIOSpec with Matchers {

  private def testIdGenerator: IdGenerator[IO] = new IdGenerator[IO] {
    val counter = new AtomicInteger(0)
    override def generate(): IO[TemporaryEmail] = IO.blocking(
      TemporaryEmail(counter.addAndGet(1).toString)
    )
  }

  "createTemporaryEmail" should {

    "Generate three temporary emails" in {
      val result = for {
        dataStore <- DataStoreMap.build[IO]()
        service = new TenMinuteEmailService[IO](testIdGenerator, dataStore)
        t1 <- service.createTemporaryEmail()
        t2 <- service.createTemporaryEmail()
        t3 <- service.createTemporaryEmail()
      } yield (t1, t2, t3)

      result.asserting(_ shouldBe (TemporaryEmail("1"), TemporaryEmail("2"), TemporaryEmail("3")))
    }

  }

  "addEmail and getEmails" should {

    "Add two emails each to two temporary emails and retrieve them" in {
      val result = for {
        dataStore <- DataStoreMap.build[IO]()
        service = new TenMinuteEmailService[IO](testIdGenerator, dataStore)
        t1 <- service.createTemporaryEmail()
        t2 <- service.createTemporaryEmail()
        _ <- service.addEmail(t1, Email("first"))
        _ <- service.addEmail(t1, Email("second"))
        _ <- service.addEmail(t2, Email("third"))
        _ <- service.addEmail(t2, Email("fourth"))
        inbox1 <- service.getEmails(t1)
        inbox2 <- service.getEmails(t2)
      } yield (inbox1, inbox2)

      result.asserting(
        _ shouldBe (
          Vector(Email("first"), Email("second")),
          Vector(Email("third"), Email("fourth")),
        )
      )
    }

  }

}
