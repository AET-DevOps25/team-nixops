package com.nixops.scraper.model

import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable

object Modules : IdTable<Int>("modules") {
  override val id = integer("module_id").entityId() // manually assigned primary key
  val moduleCode = varchar("module_code", 255)
  val moduleTitle = varchar("module_title", 255)
  val moduleTitleEn = varchar("module_title_en", 255).nullable()
  val moduleContents = text("module_content").nullable()
  val moduleContentsEn = text("module_content_en").nullable()
  val moduleOutcome = text("module_outcome").nullable()
  val moduleOutcomeEn = text("module_outcome_en").nullable()
  val moduleMethods = text("module_methods").nullable()
  val moduleMethodsEn = text("module_methods_en").nullable()
  val moduleExam = text("module_exam").nullable()
  val moduleExamEn = text("module_exam_en").nullable()
  val moduleCredits = float("module_credits")
}

class Module(id: EntityID<Int>) : Entity<Int>(id) {
  companion object : EntityClass<Int, Module>(Modules)

  var moduleCode by Modules.moduleCode
  var moduleTitle by Modules.moduleTitle
  var moduleTitleEn by Modules.moduleTitleEn
  var moduleContent by Modules.moduleContents
  var moduleContentEn by Modules.moduleContentsEn
  var moduleOutcome by Modules.moduleOutcome
  var moduleOutcomeEn by Modules.moduleOutcomeEn
  var moduleMethods by Modules.moduleMethods
  var moduleMethodsEn by Modules.moduleMethodsEn
  var moduleExam by Modules.moduleExam
  var moduleExamEn by Modules.moduleExamEn
  var moduleCredits by Modules.moduleCredits
}
