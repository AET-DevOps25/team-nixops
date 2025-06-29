import com.nixops.scraper.model.Group
import com.nixops.scraper.model.Groups
import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable

object Courses : IdTable<Int>("courses") {
  override val id = integer("course_id").entityId()

  val courseName = text("course_name")
  val courseNameEn = text("course_name_en").nullable()
  val courseNameList = text("course_name_list").nullable()
  val courseNameListEn = text("course_name_list_en").nullable()
  val description = text("description").nullable()
  val descriptionEn = text("description_en").nullable()
  val teachingMethod = text("teaching_method").nullable()
  val teachingMethodEn = text("teaching_method_en").nullable()
  val activityId = text("activity_id").nullable()
  val activityName = text("activity_name").nullable()
  val activityNameEn = text("activity_name_en").nullable()
  val note = text("note").nullable()
  val noteEn = text("note_en").nullable()

  override val primaryKey = PrimaryKey(Courses.id, name = "PK_COURSE_ID")
}

/* object CourseInstructionLanguages : IntIdTable("course_instruction_languages") {
  val course = reference("course_id", Courses, onDelete = ReferenceOption.CASCADE)
  val instructionLanguage = varchar("instruction_language", 255)
}*/

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
  var activityId by Courses.activityId
  var activityName by Courses.activityName
  var activityNameEn by Courses.activityNameEn
  var note by Courses.note
  var noteEn by Courses.noteEn

  val groups by Group referrersOn Groups.courseId

  // val instructionLanguages by
  //   CourseInstructionLanguage referrersOn CourseInstructionLanguages.course
}

/* class CourseInstructionLanguage(id: EntityID<Int>) : IntEntity(id) {
  companion object : IntEntityClass<CourseInstructionLanguage>(CourseInstructionLanguages)

  var course by Course referencedOn CourseInstructionLanguages.course
  var instructionLanguage by CourseInstructionLanguages.instructionLanguage
}*/
