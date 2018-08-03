package com.artclod.play

import play.api.libs.json.{JsPath, JsonValidationError}


case class JsResultsError( errors: Seq[(JsPath, Seq[JsonValidationError])] ) extends RuntimeException(errors.mkString("<",">,<",">"))

