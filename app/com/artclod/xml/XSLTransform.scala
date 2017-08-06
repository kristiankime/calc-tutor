package com.artclod.xml

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import javax.xml.transform.TransformerFactory
import javax.xml.transform.stream.{StreamResult, StreamSource}

import scala.util._
import scala.xml._

object XSLTransform {
	val factory = TransformerFactory.newInstance()

	def apply(dataXML: Elem, inputXSL: Elem): Try[Elem] = apply(dataXML.toString, inputXSL.toString)

	def apply(dataXML: String, inputXSL: Elem): Try[Elem] = apply(dataXML, inputXSL.toString)

	def apply(dataXML: Elem, inputXSL: String): Try[Elem] = apply(dataXML.toString, inputXSL)

	def apply(dataXML: String, inputXSL: String): Try[Elem] = {
		Try({
		val xslStream = new StreamSource(new ByteArrayInputStream(inputXSL.getBytes()))
		val transformer = factory.newTransformer(xslStream)
		val in = new StreamSource(new ByteArrayInputStream(dataXML.getBytes()))
		val out = new ByteArrayOutputStream()
		transformer.transform(in, new StreamResult(out))
		out.toString()})
		.flatMap(s => Try(XML.loadString(s)))
	}

}