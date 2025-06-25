import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object Courses : IdTable<Int>("courses") {
  override val id = integer("course_id").entityId()

  val courseName = text("course_name")
  val courseNameEn = text("course_name_en")
  val courseNameList = text("course_name_list")
  val courseNameListEn = text("course_name_list_en")
  val description = text("description").nullable()
  val descriptionEn = text("description_en").nullable()
  val teachingMethod = text("teaching_method").nullable()
  val teachingMethodEn = text("teaching_method_en").nullable()
  val note = text("note").nullable()
  val noteEn = text("note_en").nullable()
}

object CourseInstructionLanguages : IntIdTable("course_instruction_languages") {
  val course = reference("course_id", Courses, onDelete = ReferenceOption.CASCADE)
  val instructionLanguage = varchar("instruction_language", 255)
}

class Course(id: EntityID<Int>) : Entity<Int>(id) {
  companion object : EntityClass<Int, Course>(Courses)

  var courseName by Courses.courseName
  var courseNameEn by Courses.courseNameEn
  var courseNameList by Courses.courseNameList
  var courseNameListEn by Courses.courseNameListEn
  var description by Courses.description
  var descriptionEn by Courses.descriptionEn
  var teachingMethod by Courses.teachingMethod
  var teachingMethodEn by Courses.teachingMethodEn
  var note by Courses.note
  var noteEn by Courses.noteEn

  val instructionLanguages by
      CourseInstructionLanguage referrersOn CourseInstructionLanguages.course
}

class CourseInstructionLanguage(id: EntityID<Int>) : IntEntity(id) {
  companion object : IntEntityClass<CourseInstructionLanguage>(CourseInstructionLanguages)

  var course by Course referencedOn CourseInstructionLanguages.course
  var instructionLanguage by CourseInstructionLanguages.instructionLanguage
}
