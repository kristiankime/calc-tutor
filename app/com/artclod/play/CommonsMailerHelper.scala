package com.artclod.play


/**
 * See http://chocksaway.com/blog/?p=547
 */
object CommonsMailerHelper {
// TODO
//  def sendEmail(body : String, subject: String, from: String, recipients : String*) =  {
//    val mail = use[MailerPlugin].email
//    mail.setSubject(subject)
//    mail.setRecipient(recipients: _*)
//    mail.setFrom(from)
//    mail.send(body)
//  }
//
//  def emailDefault(body : String, subject: String, recipients : String*) =  {
//    sendEmail(body, subject, fromAddress, recipients :_*)
//  }
//
//  def fromAddress = Play.current.configuration.getString("smtp.from").get
//
//  def defaultMailSetup(recipients: String*) : MailerAPI = {
//    val mail = use[MailerPlugin].email
//    val from = fromAddress
//    mail.setFrom(from)
//    if(recipients.nonEmpty){ mail.setRecipient(recipients:_*) }
//    mail
//  }
//
//  def serverAddress(request: play.api.mvc.Request[_]) = {
//    val url = request.host
//    val secure = false // TODO when upgraded to play 2.3+ use request.secure
//    val protocol = if(secure) { "https://" } else { "http://" }
//    protocol + url
//  }

}
