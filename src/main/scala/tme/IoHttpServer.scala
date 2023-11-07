package tme

import cats.MonadThrow
import cats.effect.std.Random
import cats.effect.{ExitCode, IO}
import com.comcast.ip4s._
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.ember.server._
import org.http4s.implicits._
import tme.api.{Deleter, TenMinuteEmailService}
import tme.data.DataStoreMap
import tme.data.Model._
import tme.generator.WordListIdGenerator

import scala.concurrent.duration.Duration

// DISCLAIMER: The HTTP server is a bit of an after thought to allow us to interact with the code.
// I haven't spent much time/effort on it.
object IoHttpServer {

  def run: IO[ExitCode] = for {
    rng <- Random.scalaUtilRandom[IO]
    idGenerator = new WordListIdGenerator[IO]()(rng, implicitly[MonadThrow[IO]])
    dataStore <- DataStoreMap.build[IO]()
    service = new TenMinuteEmailService[IO](idGenerator, dataStore)
    deleter = new Deleter[IO](dataStore, Duration("10 minutes"))
    // Run the deleter process in parallel with the server
    server <- deleter.start() &> EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(httpRoutes(service).orNotFound)
      .build
      .use(_ => IO.never)
      .as(ExitCode.Success)
  } yield server

  private def addEmail(
      service: TenMinuteEmailService[IO],
      req: Request[IO],
      temporaryEmail: TemporaryEmail
  ): IO[Response[IO]] = for {
    body <- req.as[String]
    _ <- service.addEmail(temporaryEmail, Email(body))
    response <- Ok("Added email")
  } yield response

  private def httpRoutes(service: TenMinuteEmailService[IO]) = HttpRoutes.of[IO] {
    // POST /email
    case POST -> Root / "email" => service.createTemporaryEmail().map(_.email).flatMap(Ok(_))
    // POST /email/<temporary email prefix>
    case req @ POST -> Root / "email" / temporaryEmailPrefix =>
      addEmail(service, req, TemporaryEmail(temporaryEmailPrefix))
    // GET /email/<temporary email prefix>
    case GET -> Root / "email" / temporaryEmailPrefix =>
      service
        .getEmails(TemporaryEmail(temporaryEmailPrefix))
        .map(_.map(_.data).mkString("/n"))
        .flatMap(Ok(_))
  }

}
