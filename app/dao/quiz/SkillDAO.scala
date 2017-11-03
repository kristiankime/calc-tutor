package dao.quiz

import javax.inject.{Inject, Singleton}

import com.artclod.mathml.scalar.MathMLElem
import dao.ColumnTypeMappings
import dao.user.UserDAO
import models._
import models.organization.{Course, Course2Quiz}
import models.quiz.{AnswerPart, _}
import org.joda.time.DateTime
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.mvc.Result
import play.api.mvc.Results._
import slick.lifted

import scala.concurrent.{ExecutionContext, Future}

// ====
import slick.driver.JdbcProfile
//import slick.jdbc.JdbcProfile // Use this after upgrading slick
// ====

@Singleton
class SkillDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider, protected val userDAO: UserDAO, protected val questionDAO: QuestionDAO, protected val answerDAO: AnswerDAO)(implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] with ColumnTypeMappings {
  // ====
  //  import profile.api._ // Use this after upgrading slick
  import dbConfig.driver.api._

  // * ====== TABLE INSTANCES ====== *
  val Skills = lifted.TableQuery[SkillTable]
  val Skills2Questions = lifted.TableQuery[Skill2QuestionTable]
  val UserAnswerCounts = lifted.TableQuery[UserAnswerCountTable]

  // * ====== QUERIES ====== *

  // ====== FIND ======
  def allSkills: Future[Seq[Skill]] = db.run(Skills.result)

  def skillsFor(questionId: QuestionId) = db.run(Skills2Questions.filter(_.questionId === questionId).result)

  def byId(id : SkillId): Future[Option[Skill]] = db.run(Skills.filter(_.id === id).result.headOption)

  def byName(name : String): Future[Option[Skill]] = db.run(Skills.filter(_.name === name).result.headOption)


  // ====== Create ======
  def insert(skill: Skill): Future[Skill] = db.run(
    (Skills returning Skills.map(_.id) into ((needsId, id) => needsId.copy(id = id))) += skill
  )

  def insertAll(skills: Skill*) : Future[Seq[Skill]] = db.run(
    (Skills returning Skills.map(_.id) into ((needsId, id) => needsId.copy(id = id))) ++= skills
  )

//  def insertOrUpdateAll(skills: Skill*) : Future[Seq[Skill]] = db.run(
//    (Skills returning Skills.map(_.id) into ((needsId, id) => needsId.copy(id = id))) insertOrUpdate skills
//  )

  def insertOrUpdate(skill: Skill) : Future[Option[Skill]]  = db.run(
    (Skills returning Skills.map(_.id) into ((needsId, id) => needsId.copy(id = id))) insertOrUpdate skill
  )

  def defaultSkills = {
    allSkills.map(skills =>
      if(skills.isEmpty) { insertAll(
          //                                            intercept correct incorrect
          //                                                 β      γ       ρ
          Skill(null, "Numerical",                "Num",      -0.297, 0.025, -0.021),
          Skill(null, "Verbal",                   "Ver",      -0.177, 0.000, -0.147),
          Skill(null, "Algebraic",                "Alg",      -0.115, 0.093,  0.013),
          Skill(null, "Precalc",                  "PrC",       0.487, 0.013, -0.047),
          Skill(null, "Trig",                     "Trg",      -0.148, 0.009, -0.007),
          Skill(null, "Logs",                     "Log",       0.763, 0.000, -0.104),
          Skill(null, "Exponents",                "Exp",      -0.693, 0.028,  0.001),
          Skill(null, "Alt.Var.Names",            "VaN",       0.196, 0.000, -0.023),
          Skill(null, "Abstract.Constants",       "AbC",       0.137, 0.000, -0.042),
          Skill(null, "Limits...Continuity",      "Lim",      -0.021, 0.016, -0.012),
          Skill(null, "Continuity..Definition",   "CnD",       0.544, 0.000, -0.183),
          Skill(null, "Derivative..Definition",   "DeD",       0.548, 0.000, -0.045),
          Skill(null, "Derivative..Shortcuts",    "DeS",       0.866, 0.003, -0.025),
          Skill(null, "Product.Rule",             "PrR",      -0.295, 0.011,  0.014),
          Skill(null, "Quotient.Rule",            "QuR",      -0.308, 0.002, -0.032),
          Skill(null, "Chain.Rule",               "ChR",      -0.092, 0.000, -0.006),
          Skill(null, "Implicit.Differentiation", "IpD",       0.112, 0.000, -0.200),
          Skill(null, "Function.Analysis",        "FAn",      -0.138, 0.012, -0.025),
          Skill(null, "Applications",             "App",      -0.430, 0.014,  0.001),
          Skill(null, "Antiderivatives",          "Ant",       0.177, 0.043, -0.023)
      )}
    )
  }

  // === Support ===

  // * ====== TABLE CLASSES ====== *
  class SkillTable(tag: Tag) extends Table[Skill](tag, "skill") {
    def id = column[SkillId]("id", O.AutoInc)
    def name = column[String]("name", O.PrimaryKey)
    def shortName = column[String]("short_name")
    def intercept = column[Double]("intercept")
    def correct = column[Double]("correct")
    def incorrect = column[Double]("incorrect")

    def idIdx = index("skill_idx__id", id, unique = true)

    def * = (id, name, shortName, intercept, correct, incorrect) <> (Skill.tupled, Skill.unapply)
  }

  class Skill2QuestionTable(tag: Tag) extends Table[Skill2Question](tag, "skill_2_question") {
    def skillId = column[SkillId]("skill_id")
    def questionId = column[QuestionId]("question_id")

    def pk = primaryKey("skill_2_question_pk", (skillId, questionId))

    def skillIdFK = foreignKey("skill_2_question_fk__skill_id", skillId, Skills)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    def questionIdFK = foreignKey("skill_2_question_fk__question_id", questionId, questionDAO.Questions)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)

    def * = (skillId, questionId) <> (Skill2Question.tupled, Skill2Question.unapply)
  }

  class UserAnswerCountTable(tag: Tag) extends Table[UserAnswerCount](tag, "user_answer_count") {
    def userId = column[UserId]("user_id")
    def skillId = column[SkillId]("skill_id")
    def correct = column[Int]("correct")
    def incorrect = column[Int]("incorrect")

    def pk = primaryKey("skill_2_question_pk", (userId, skillId))

    def userIdFK = foreignKey("skill_2_question_fk__user_id", userId, userDAO.Users)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    def skillIdFK = foreignKey("skill_2_question_fk__skill_id", skillId, Skills)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)

    def * = (userId, skillId, correct, incorrect) <> (UserAnswerCount.tupled, UserAnswerCount.unapply)
  }

}

