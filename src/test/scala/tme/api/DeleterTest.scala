package tme.api

import cats.MonadThrow
import cats.effect.IO
import cats.effect.std.Random
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.Assertion
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory
import tme.data.DataStoreMap
import tme.data.Model.Email
import tme.generator.WordListIdGenerator

import scala.concurrent.duration.Duration

class DeleterTest extends AsyncWordSpec with AsyncIOSpec with Matchers {

  private implicit val loggerFactory: LoggerFactory[IO] = Slf4jFactory[IO]
  private val logger = loggerFactory.getLogger
  private val deleteInterval = Duration("3 seconds")
  private val oneSecond = Duration("1 seconds")

  "Deleter" should {

    "Delete all temporary emails parallel with the service creating them" in {

      def runTest(service: TenMinuteEmailService[IO]): IO[Assertion] = for {
        _ <- logger.info("Warming up")
        _ <- IO.sleep(oneSecond)

        // Create some emails
        _ <- logger.info("Creating Data")
        temp1 <- service.createTemporaryEmail()
        temp2 <- service.createTemporaryEmail()
        _ <- service.addEmail(temp1, Email("1a"))
        _ <- service.addEmail(temp1, Email("1b"))
        _ <- service.addEmail(temp2, Email("2a"))

        // Confirm email creation
        startEmails1 <- service.getEmails(temp1)
        startEmails2 <- service.getEmails(temp2)

        // Deleter runs, but nothing is old enough to be deleted
        _ <- logger.info("Waiting first interval")
        _ <- IO.sleep(deleteInterval)
        midEmails1 <- service.getEmails(temp1)
        midEmails2 <- service.getEmails(temp2)
        temp3 <- service.createTemporaryEmail()
        _ <- service.addEmail(temp3, Email("3a"))

        // Deleter runs, both address are cleared
        _ <- logger.info("Waiting second interval")
        _ <- IO.sleep(deleteInterval)
        endEmails1 <- service.getEmails(temp1)
        endEmails2 <- service.getEmails(temp2)
        endEmails3 <- service.getEmails(temp3)
        _ <- logger.info("Test complete")

        assertion = (startEmails1, startEmails2, midEmails1, midEmails2, endEmails1, endEmails2, endEmails3) shouldBe (
          // Start
          Vector(Email("1a"), Email("1b")),
          Vector(Email("2a")),
          // Mid
          Vector(Email("1a"), Email("1b")),
          Vector(Email("2a")),
          // End
          Vector.empty[Email],
          Vector.empty[Email],
          Vector(Email("3a"))
        )
      } yield assertion

      for {
        rng <- Random.scalaUtilRandom[IO]
        idGenerator = new WordListIdGenerator[IO]()(rng, implicitly[MonadThrow[IO]])
        dataStore <- DataStoreMap.build[IO]()
        deleter = new Deleter[IO](dataStore, deleteInterval)
        service = new TenMinuteEmailService[IO](idGenerator, dataStore)
        // Start the deleter, racing with running the test
        assertionOpt <- IO.race(deleter.start(), runTest(service))
        result = assertionOpt match {
          case Left(())         => fail()
          case Right(assertion) => assertion
        }
      } yield result

    }

  }

}
