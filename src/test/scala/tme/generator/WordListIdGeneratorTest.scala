package tme.generator

import cats.MonadThrow
import cats.effect.IO
import cats.effect.std.Random
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec

class WordListIdGeneratorTest extends AsyncWordSpec with AsyncIOSpec with Matchers {

  "generateId" should {

    // 1/100,000,000 chance to fail
    "Generate two different IDs" in {
      val result = for {
        generator <- getGenerator
        id1 <- generator.generate()
        id2 <- generator.generate()
      } yield (id1, id2)

      result.asserting { case (id1, id2) => id1 should not equal id2 }
    }

    // 1/100,000,000 chance to fail
    "Use a different seed for each instance of generator" in {
      val result = for {
        generator1 <- getGenerator
        generator2 <- getGenerator
        id1 <- generator1.generate()
        id2 <- generator2.generate()
      } yield (id1, id2)

      result.asserting { case (id1, id2) => id1 should not equal id2 }
    }

  }

  // There is no default instance of Random for IO, so creating the generator is a bit awkward
  private def getGenerator: IO[WordListIdGenerator[IO]] = for {
    rng <- Random.scalaUtilRandom[IO]
  } yield new WordListIdGenerator[IO]()(rng, implicitly[MonadThrow[IO]])

}
