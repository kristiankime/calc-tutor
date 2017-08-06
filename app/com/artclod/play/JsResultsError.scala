package com.artclod.play

import play.api.data.validation.ValidationError
import play.api.libs.json.JsPath


case class JsResultsError( errors: Seq[(JsPath, Seq[ValidationError])] ) extends RuntimeException(errors.mkString("<",">,<",">"))

