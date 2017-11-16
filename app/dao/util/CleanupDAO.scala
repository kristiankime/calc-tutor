package dao.util

import javax.inject.{Inject, Singleton}

import dao.auth.table.NamePassTable
import dao.organization.table.{CourseTables, OrganizationTables}
import dao.quiz.table.{AnswerTables, QuestionTables, QuizTables, SkillTables}
import dao.user.table.UserTables
import models.auth.NamePassLogin
import models.organization.{Course, Organization}
import models.quiz._
import models.user.User
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver

import scala.concurrent.{ExecutionContext, Future}

// ====
import slick.driver.JdbcProfile
//import slick.jdbc.JdbcProfile // Use this after upgrading slick
// ====

@Singleton
class CleanupDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider,
                           protected val namePassTable: NamePassTable,
                           protected val userTables: UserTables,
                           protected val organizationTables: OrganizationTables,
                           protected val courseTables: CourseTables,
                           protected val quizTables: QuizTables,
                           protected val questionTables: QuestionTables,
                           protected val answerTables: AnswerTables,
                           protected val skillTables: SkillTables
                          )
                          (implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {
  // ====
  //  import profile.api._ // Use this after upgrading slick
  import dbConfig.driver.api._
  // ====

  val Logins = namePassTable.Logins

  val tables: Vector[TableQuery[_]] = Vector(namePassTable.Logins, userTables.Users, organizationTables.Organizations, courseTables.Courses,
    quizTables.Quizzes, questionTables.Questions, answerTables.Answers, skillTables.Skills, skillTables.UserAnswerCounts).reverse

  def clear(): Future[Int] = db.run({
//https://stackoverflow.com/questions/39287829/close-or-shutdown-of-h2-database-after-tests-is-not-working
//    skillTables.UserAnswerCounts.delete.flatMap( uac =>
//      skillTables.Skills.delete.flatMap( sk =>
//        answerTables.Answers.delete.flatMap( an =>
//          questionTables.Questions.delete.flatMap( qn =>
//            quizTables.Quizzes.delete.flatMap( qz =>
//              courseTables.Courses.delete.flatMap( cu =>
//                organizationTables.Organizations.delete.flatMap( or =>
//                  userTables.Users.delete.flatMap( u =>
//                    namePassTable.Logins.delete
//                  )
//                )
//              )
//            )
//          )
//        )
//      )
//    )

    skillTables.UserAnswerCounts.delete andThen
      skillTables.Skills.delete andThen
        answerTables.Answers.delete andThen
          questionTables.Questions.delete andThen
            quizTables.Quizzes.delete andThen
              courseTables.Courses.delete andThen
                organizationTables.Organizations.delete andThen
                  userTables.Users.delete



    //    (for(l <- skillTables.UserAnswerCounts) yield l).delete
//    val foo: driver.JdbcProfile.this.DriverAction[Int, NoStream, Effect.Write] = namePassTable.Logins.delete
//    val foo: this.DriverAction[Int, NoStream, Effect.Write] =

//    val deletes = tables.map(t => (for(r <- t) yield r).delete)
//    deletes.reduce( (a,b) => a.flatMap(x => b))
  })

  def insert(cat: NamePassLogin): Future[Unit] = db.run(Logins += cat).map { _ => () }

  def byId(id : String): Future[Option[NamePassLogin]] = db.run(Logins.filter(_.id === id).result.headOption)
  
}

