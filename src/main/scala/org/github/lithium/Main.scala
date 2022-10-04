import scala.util.chaining._
import zio.ZIOAppDefault
import zio.Scope
import zio.ZIO
import zio.ZIOAppArgs
import zio.Console
import zio.stream.ZStream
import zio.durationInt
import zio.Clock
import zio.Schedule
import zio.Ref
import zio.Cause
import java.util.concurrent.TimeUnit
import org.github.lithium.domain.Timestamp
import org.github.lithium.nirvana.Password
import zhttp.service.ChannelFactory
import zhttp.service.EventLoopGroup
import org.github.lithium.Lithium
import org.github.lithium.LithiumLive
import org.github.lithium.nirvana.Error
import java.io.IOException

object Main extends ZIOAppDefault:

  def streamProgram(stop: Ref[Boolean]) = for 
    _    <- ZStream.fromZIO(Console.printLine("Hello, from stream program"))
    schedule = Schedule.spaced(10.seconds) && Schedule.recurUntilZIO(_ => stop.get)
    now  <- ZStream.fromZIO(Clock.currentTime(TimeUnit.SECONDS))
    task <- Lithium.getUpdatedTaskStream(schedule, starting = now)
    _    <- ZStream.debug(task.name, task.tags)
  yield ()

  lazy val program = (
    for
      stop  <- Ref.make(false)
      fiber <- streamProgram(stop).runDrain.fork
      _     <- fiber.join
                .ensuring(stop.set(true))
    yield ()
  )

  override def run: ZIO[Any & (ZIOAppArgs & Scope), Any, Any] = 
    program
      .catchAll(Console.printError(_))
      .provide(
        Lithium.live,
        EventLoopGroup.auto(),
        ChannelFactory.auto
      )
      .exitCode
