import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable

object Degrees : LongIdTable("degrees") {
  val degreeTypeName = varchar("degree_type_name", 255)
}

class Degree(id: EntityID<Long>) : LongEntity(id) {
  companion object : LongEntityClass<Degree>(Degrees)

  var degreeTypeName by Degrees.degreeTypeName
}
