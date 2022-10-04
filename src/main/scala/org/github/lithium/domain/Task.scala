package org.github.lithium.domain

opaque type Timestamp = Long

object Timestamp:
  def apply(l: Long): Timestamp = l

opaque type UUID = String

object UUID:
  def apply(s: String): UUID = s

enum Type(val value: Int):
  case Task extends Type(0)
  case Project extends Type(1)
  case Link extends Type(2)
  case Reference extends Type(3)

object Type:
  def fromValue(value: Int): Type = Type.values.find(_.value == value).head

enum State(val value: Int):
  case Inbox extends State(0)
  case Next extends State(1)
  case Waiting extends State(2)
  case Scheduled extends State(3)
  case Someday extends State(4)
  case Trash extends State(6)
  case Logbook extends State(7)
  case Project extends State(11)

object State:
  def fromValue(value: Int): State = State.values.find(_.value == value).head

opaque type Tag = String

object Tag:
  def apply(s: String): Tag = s

enum EnergyLevel(val value: Int):
  case Low extends EnergyLevel(1)
  case Medium extends EnergyLevel(2)
  case High extends EnergyLevel(3)

object EnergyLevel:
  def fromValue(value: Int): EnergyLevel = EnergyLevel.values.find(_.value == value).head

final case class Task(
  id: UUID,
  ttype: Type,
  state: State,
  parentId: Option[UUID],
  name: String,
  tags: List[Tag],
  estimatedTime: Int,
  energyLevel: Option[EnergyLevel],
  waitingFor: Option[Tag],
  startDate: Option[Timestamp],
  dueDate: Option[Timestamp],
  note: Option[String],
  completed: Option[Timestamp],
  cancelled: Option[Timestamp],
  ucreated: Option[Timestamp],
  deleted: Option[Timestamp],
  lastUpdated: Option[Timestamp],
)

object Task:
  import org.github.lithium.nirvana.Nirvana.UserDataResponse.Result
  import scala.util.chaining._

  def fromResultTask(in: Result.Task): Task = Task(
    id = UUID(in.id),
    ttype = Type.fromValue(in.ttype.toInt),
    state = State.fromValue(in.state.toInt),
    parentId = if in.parentid == "" then None else Some(UUID(in.parentid)),
    name = in.name,
    tags = in.tags.split(",").filter(!_.isEmpty).map(_.trim).map(Tag.apply).toList,
    estimatedTime = in.etime.toInt,
    energyLevel = if in.energy.toInt == 0 then None else Some(EnergyLevel.fromValue(in.energy.toInt)),
    waitingFor = if in.waitingfor == "" then None else Some(in.waitingfor.pipe(Tag.apply)),
    startDate = if in.startdate == "" then None else Some(in.startdate.toLong.pipe(Timestamp.apply)),
    dueDate = if in.duedate == "" then None else Some(in.duedate.toLong.pipe(Timestamp.apply)),
    note = if in.note == "" then None else Some(in.note),
    completed = None,
    cancelled = None,
    ucreated = None,
    deleted = None,
    lastUpdated = None
  )