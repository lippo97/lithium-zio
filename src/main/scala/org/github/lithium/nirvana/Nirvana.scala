package org.github.lithium.nirvana

import zio.json._
import zio.json.jsonField
import zio.json.JsonDecoder
import zio.json.DeriveJsonDecoder

object Nirvana:

  val LOGIN_URL = "https://www.nirvanahq.com/api?api=rest"

  val TASKS_URL = "https://focus.nirvanahq.com/api/"

  val APP_ID = "n2desktop"
  
  val APP_VERSION = "1659178761"

  case class LoginResponse(
    results: List[LoginResponse.Result]
  ) 

  object LoginResponse:
    case class Result(auth: Auth)
    case class Auth(token: String)

    implicit lazy val loginResponseDecoder: JsonDecoder[LoginResponse] = DeriveJsonDecoder.gen[LoginResponse]
    implicit lazy val resultDecoder: JsonDecoder[Result] = DeriveJsonDecoder.gen[Result]
    implicit lazy val authDecoder: JsonDecoder[Auth] = DeriveJsonDecoder.gen[Auth]

  case class UserDataResponse(
    results: List[UserDataResponse.Result]
  )

  object UserDataResponse:
    enum Result:
      @jsonHint("user")  case User()  extends Result
      @jsonHint("pref")  case Pref()  extends Result
      @jsonHint("tag")   case Tag()   extends Result
      @jsonHint("error") case Error(
        code: Int,
        message: String
      ) extends Result
      @jsonHint("task")  case Task(
        id: String,
        @jsonField("type") ttype: String,
        _type: String,
        ps: String,
        _ps: String,
        state: String,
        _state: String,
        parentid: String,
        _parentid: String,
        seq: String,
        _seq: String,
        seqt: String,
        _seqt: String,
        seqp: String,
        _seqp: String,
        name: String,
        _name: String,
        tags: String,
        _tags: String,
        etime: String,
        _etime: String,
        energy: String,
        _energy: String,
        waitingfor: String,
        _waitingfor: String,
        startdate: String,
        _startdate: String,
        duedate: String,
        _duedate: String,
        reminder: String,
        _reminder: String,
        recurring: String,
        _recurring: String,
        note: String,
        _note: String,
        completed: String,
        _completed: String,
        cancelled: String,
        _cancelled: String,
        ucreated: String,
        _ucreated: String,
        deleted: String,
        _deleted: String,
        updated: String
      ) extends Result

    implicit lazy val userDataResponseDecoder: JsonDecoder[UserDataResponse] = DeriveJsonDecoder.gen[UserDataResponse]
    implicit lazy val resultDecoder: JsonDecoder[Result] = DeriveJsonDecoder.gen[Result]
