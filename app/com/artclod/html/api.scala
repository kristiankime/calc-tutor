package com.artclod.html

import controllers.quiz.QuestionCreate.{I_, D_, S_}
import models.quiz.QuestionUserConstantsFrame
import models.user.User
import play.twirl.api.Html

package object html {

  implicit class EnhancedHtml(html: Html) {
    def fixConstants(user: User, userConstants: QuestionUserConstantsFrame) = {
      val htmlStr = html.toString()
      var htmlRep = htmlStr
      for( uc <- userConstants.all) {
        val mat = uc.matchStr
//        val rep = "\\$\\$" + uc.replaceStr(user) + "\\$\\$"
        val rep = uc.replaceStr(user)
        htmlRep = htmlRep.replaceAll(mat, rep)
      }
      Html(htmlRep)
    }
  }

}
