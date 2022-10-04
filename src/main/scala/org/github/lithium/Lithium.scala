package org.github.lithium

import zio.ZIO
import zio.Console
import java.io.IOException
import zio.stream.ZStream
import zio.ZLayer.apply
import zio.ZLayer
import zhttp.service.Client
import zio.Clock
import zio.durationInt
import zio.Scope
import zio.json._
import zhttp.service.ChannelFactory
import zhttp.service.EventLoopGroup
import org.github.lithium.domain.State
import org.github.lithium.domain.Task
import org.github.lithium.domain.Timestamp
import org.github.lithium.domain.Type
import org.github.lithium.domain.UUID
import org.github.lithium.nirvana.Nirvana
import zio.Cause
import zhttp.http.Method
import zhttp.http.Headers
import zhttp.http.HttpData
import org.github.lithium.nirvana.Password
import org.github.lithium.http.encodeURI
import org.github.lithium.http.encodeURIParams
import org.github.lithium.http.longEncoder
import zio.Random
import java.util.concurrent.TimeUnit
import org.github.lithium.nirvana.Nirvana
import org.github.lithium.nirvana.Nirvana.UserDataResponse
import io.netty.channel.Channel
import org.github.lithium.nirvana.Nirvana.UserDataResponse.Result
import scala.util.chaining._
import org.github.lithium.nirvana.Error
import org.github.lithium.nirvana.Error.ParseError
import org.github.lithium.nirvana.Error.InvalidAuthtoken
import org.github.lithium.nirvana.Error.FetchError
import zio.Ref
import zio.Schedule


trait Lithium:

  def getUpdatedTaskStream(schedule: Schedule[Any, Any, Any], starting: Option[Long] = None): ZStream[Any, Error, Task]

class LithiumLive(
  val channelFactory: ChannelFactory,
  val eventLoopGroup: EventLoopGroup,
) extends Lithium:

  def authenticate(email: String, password: Password) = for
    _     <- ZIO.log("Attempting authentication...")
    res   <- Client.request(
      Nirvana.LOGIN_URL,
      method = Method.POST,
      headers = Headers(
        "Content-Type" -> "application/x-www-form-urlencoded"
      ),
      content = HttpData.fromString(encodeURI(
        "method" -> "auth.new",
        "u" -> email,
        "p" -> password.hashed,
      ))).mapError(_ => FetchError)
    data  <- res.bodyAsString.orDie
    d     <- ZIO.fromEither(data.fromJson[Nirvana.LoginResponse])
      .mapError(ParseError(_))
    token <- ZIO.fromEither(
      d.results.headOption.toRight(InvalidAuthtoken)
        .map(_.auth.token)
    )
    _     <- ZIO.log("Authenticated successfully.")
  yield token


  def fetchUserData(since: Long, tokenStorage: Ref[String]): ZIO[EventLoopGroup & ChannelFactory, Error, List[UserDataResponse.Result]] = 
    def fetch(token: String) = for
      uuid    <- Random.nextUUID
      time    <- Clock.currentTime(TimeUnit.SECONDS)
      res     <- Client.request(encodeURIParams(Nirvana.TASKS_URL, 
        "api" -> "rest",
        "method" -> "everything",
        "since" -> since,
        "requestId" -> uuid.toString(),
        "clienttime" -> time,
        "authtoken" -> token,
        "appid" -> Nirvana.APP_ID,
        "appversion" -> Nirvana.APP_VERSION,
      )).mapError(_ => FetchError)
      data    <- res.bodyAsString.orDie
      results <- ZIO.fromEither(data.fromJson[Nirvana.UserDataResponse])
        .map(_.results)
        .mapError(ParseError(_))
      _       <- results.collectFirst { case t: Result.Error => t }
        .fold(ZIO.unit)(_ => ZIO.fail(InvalidAuthtoken))
    yield results

    for 
      token <- tokenStorage.get
      results <- fetch(token).catchSome {
        case InvalidAuthtoken => authenticate("laselek500@orlydns.com", Password.plain("laselek500"))
          .flatMap(token => tokenStorage.set(token) *> fetch(token))
      }
    yield results

  override def getUpdatedTaskStream(schedule: Schedule[Any, Any, Any], starting: Option[Long] = None): ZStream[Any, Error, Task] = 
    def getAndSetTime(lastUpdated: Ref[Long]) = for
      time     <- Clock.currentTime(TimeUnit.SECONDS)
      lastTime <- lastUpdated.getAndSet(time)
    yield lastTime

    def updateTimeAndFetch(lastUpdated: Ref[Long], tokenStorage: Ref[String]) = for
      time <- getAndSetTime(lastUpdated)
      data <- fetchUserData(time, tokenStorage)
    yield data

    def go(lastUpdated: Ref[Long], tokenStorage: Ref[String]) =
      ZStream
        .repeatZIOWithSchedule(
          updateTimeAndFetch(lastUpdated, tokenStorage)
            .provide(ZLayer.succeed(channelFactory), ZLayer.succeed(eventLoopGroup)),
          schedule
        )
        .flatMap(ZStream.fromIterable(_))
        .collect {
          case t: Nirvana.UserDataResponse.Result.Task => Task.fromResultTask(t)
        }
      
    for
      lastUpdated  <- ZStream.fromZIO(Ref.make[Long](starting.getOrElse(0)))
      tokenStorage <- ZStream.fromZIO(Ref.make(""))
      task         <- go(lastUpdated, tokenStorage)
    yield task

object Lithium:

  def live: ZLayer[EventLoopGroup & ChannelFactory, Nothing, Lithium] = 
    ZLayer.fromFunction(LithiumLive.apply(_, _))

  def getUpdatedTaskStream(schedule: Schedule[Any, Any, Any], starting: Long): ZStream[Lithium, Error, Task] =
    getUpdatedTaskStream(schedule, Some(starting))

  def getUpdatedTaskStream(schedule: Schedule[Any, Any, Any], starting: Option[Long] = None): ZStream[Lithium, Error, Task] =
    ZStream.environmentWithStream(_.get.getUpdatedTaskStream(schedule))
  

