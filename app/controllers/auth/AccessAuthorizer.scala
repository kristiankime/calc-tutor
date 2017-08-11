package controllers.auth

import org.apache.commons.lang3.StringUtils
import org.pac4j.core.authorization.authorizer.ProfileAuthorizer
import org.pac4j.core.context.WebContext
import org.pac4j.core.profile.CommonProfile
import play.api.{Configuration, Environment, Logger}

class AccessAuthorizer extends ProfileAuthorizer[CommonProfile] {

  def isAuthorized(context: WebContext, profiles: java.util.List[CommonProfile]): Boolean = {
    return isAnyAuthorized(context, profiles)
  }

  def isProfileAuthorized(context: WebContext, profile: CommonProfile): Boolean = {
    Logger.error("context.getRequestHeader    "    + context.getRequestHeader("ITEM"))
    Logger.error("context.getRequestHeader    "    + context.getRequestHeader("LEVEL"))
    true
  }
}