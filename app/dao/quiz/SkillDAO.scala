package dao.quiz

import java.util.Objects
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}

import com.artclod.mathml.scalar.MathMLElem
import dao.ColumnTypeMappings
import dao.quiz.table.SkillTables
import dao.user.UserDAO
import dao.user.table.UserTables
import models._
import models.organization.{Course, Course2Quiz}
import models.quiz.{AnswerPart, _}
import org.joda.time.DateTime
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.mvc.Result
import play.api.mvc.Results._
import slick.lifted

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

// ====
import slick.driver.JdbcProfile
//import slick.jdbc.JdbcProfile // Use this after upgrading slick
// ====

@Singleton
class SkillDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider, protected val skillTables: SkillTables)(implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] with ColumnTypeMappings {

  // ====
  //  import profile.api._ // Use this after upgrading slick
  import dbConfig.driver.api._

  // * ====== TABLE INSTANCES ====== *
  import skillTables.{Skills, Skills2Questions, UserAnswerCounts }

  // * ====== QUERIES ====== *

  // ====== FIND ======
  def allSkills: Future[Seq[Skill]] = db.run(Skills.result)

  def skillIdsFor(questionId: QuestionId) = db.run(Skills2Questions.filter(_.questionId === questionId).map(_.skillId).result)
  def skillsFor(questionId: QuestionId): Future[Seq[Skill]] = db.run {
    (for (s2q <- Skills2Questions; s <- Skills if s2q.questionId === questionId && s2q.skillId === s.id) yield s).result
  }

  def byId(id : SkillId): Future[Option[Skill]] = db.run(Skills.filter(_.id === id).result.headOption)

  def byName(name : String): Future[Option[Skill]] = db.run(Skills.filter(_.name === name).result.headOption)

  def skillsMap: Future[Map[String, Skill]] = db.run(Skills.result).map(_.groupBy(_.name).mapValues(_.head) )

  def skillIdsMap: Future[Map[SkillId, Skill]] = db.run(Skills.result).map(_.groupBy(_.id).mapValues(_.head) )

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

  def addSkills(question: Question, skills: Vector[Skill]) = db.run({
    val s2Qs = skills.map(s => Skill2Question(s.id, question.id))
    Skills2Questions ++= s2Qs
  })

  // ====== DEFAULT SKILLS ======
  // For now we have a default set of skills (and coef) that are hard coded and inserted into the db
  def defaultSkills = {
    allSkills.map(skills =>
      if(skills.isEmpty) { insertAll(
          //                                                intercept correct incorrect
          //                                                   β      γ       ρ
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


  // =========== Skill Computations ========

  // ----- Update Counts
  def incrementsCounts(userId: UserId, questionId: QuestionId, correct: Int, incorrect: Int): Future[Vector[Int]] =
    skillIdsFor(questionId).flatMap(skillIds => incrementCounts(userId, skillIds.map(s => (s, correct, incorrect)):_*))

  def incrementCounts(userId: UserId, skills: (SkillId, Int, Int)*): Future[Vector[Int]] = {
    val seqFutures = skills.map(s => incrementCount(userId, s._1, s._2, s._3) )
    com.artclod.concurrent.raiseFuture(seqFutures)
  }

  def incrementCount(userId: UserId, skillId: SkillId, correct: Int, incorrect: Int): Future[Int] = {
    val currentFuture = db.run(  UserAnswerCounts.filter(uac => uac.userId === userId && uac.skillId === skillId).result.headOption )

    // As of 2018-01-04 There doesn't seem to be an update "in place" ability in Slick so we get the data manually https://github.com/slick/slick/issues/497
    currentFuture.flatMap( currentOption => {
      currentOption match {
        case Some(current) => db.run(UserAnswerCounts.filter(uac => uac.userId === userId && uac.skillId === skillId).update(UserAnswerCount(userId, skillId, current.correct + correct, current.incorrect + incorrect) ))
        case None => db.run(UserAnswerCounts += UserAnswerCount(userId, skillId, correct, incorrect ))
      }
    })
  }

  // ----- Get Counts
  def getCount(userId: UserId, skillId: SkillId): Future[Option[UserAnswerCount]] =
    db.run(UserAnswerCounts.filter(uac => uac.userId === userId && uac.skillId === skillId).result.headOption)

  def getCounts(userId: UserId): Future[Seq[UserAnswerCount]] =
    db.run(UserAnswerCounts.filter(uac => uac.userId === userId).result)

  def skillCountsMaps(userId: UserId): Future[Map[SkillId, UserAnswerCount]] =
    db.run(UserAnswerCounts.filter(uac => uac.userId === userId).result.map(_.groupBy(_.skillId).mapValues(_.head)))

  // ----- PFA Computations
  def pfaProbability(userId: UserId, questionId: QuestionId): Future[Double] =
    skillIdsFor(questionId).flatMap(skills => skillIdsMap.flatMap(skillData => skillCountsMaps(userId).map(skillCounts =>
      pfaProbability(skills, skillData, skillCounts)
    )))

  def pfaProbability(skills: Seq[SkillId], skillData: Map[SkillId, Skill], skillCounts: Map[SkillId, UserAnswerCount]): Double = {
    // http://pact.cs.cmu.edu/koedinger/pubs/AIED%202009%20final%20Pavlik%20Cen%20Keodinger%20corrected.pdf
    val m = pfaM(skills, skillData, skillCounts)
    1 / (1 + math.exp(-m))
  }

  def pfaM(skills: Seq[SkillId], skillData: Map[SkillId, Skill], skillCounts: Map[SkillId, UserAnswerCount]): Double = {
    skills.map(s => {
      val skillCoef = Objects.requireNonNull(skillData(s), "Skill id " + s + " was not in skillData coding error")
      val skillCount = skillCounts.getOrElse(s, UserAnswerCount(null, s, 0 , 0))
      skillComputation(skillCoef, skillCount)
    }).sum
  }

  def skillComputationSigmod(skillCoef: Skill, skillCount: UserAnswerCount) = {
    val m = skillComputation(skillCoef, skillCount)
    1 / (1 + math.exp(-m))
  }

  def skillComputation(skillCoef: Skill, skillCount: UserAnswerCount) = // http://pact.cs.cmu.edu/koedinger/pubs/AIED%202009%20final%20Pavlik%20Cen%20Keodinger%20corrected.pdf
    skillCoef.β + (skillCount.correct * skillCoef.γ) + (skillCount.incorrect * skillCoef.ρ)

  // ----- Single User Skill Levels
  def userSkillLevels(userId: UserId) : Future[Seq[(Skill, Double)]] =
    allSkills.flatMap(as => skillCountsMaps(userId).map(skillCounts => userSkillLevels(as, skillCounts)))

  def userSkillLevels(allSkills: Seq[Skill], skillCounts: Map[SkillId, UserAnswerCount]): Seq[(Skill, Double)] = {
    allSkills.map(skillCoef => {
      val skillCount = skillCounts.getOrElse(skillCoef.id, UserAnswerCount(null, skillCoef.id, 0 , 0))
      (skillCoef, skillComputationSigmod(skillCoef, skillCount))
    })
  }

  // ----- Group User Skill Levels
  def usersSkillLevels(allSkills: Seq[Skill], userIds: Seq[UserId]): Future[Seq[(Skill, Seq[Double])]] = {
    val allUserSkillsFuture = db.run(UserAnswerCounts.filter(uac => uac.userId inSet userIds.toSet).result)

    allUserSkillsFuture.map(allUserSkills => {
      val allSkillsCountMap = allUserSkills.groupBy(_.skillId)

      val allSkillsCountMapPadded = allSkillsCountMap.map(e => (e._1, e._2.padTo(userIds.size, UserAnswerCount(null, e._1, 0 , 0))) )

      val allSkillsCounts = allSkills.map(sk =>
        (sk, allSkillsCountMapPadded.getOrElse(sk.id, Seq.fill(userIds.size)(UserAnswerCount(null, sk.id, 0 , 0)))))

      allSkillsCounts.map(sk => (sk._1, sk._2.map(skillComputation(sk._1,_)))  )
    })
  }

}

