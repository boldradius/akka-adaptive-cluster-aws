package com.boldradius.util


import akka.http.marshalling._
import akka.http.model._
import akka.http.unmarshalling._
import akka.stream.FlowMaterializer
import org.json4s._
import org.json4s.native.Serialization
import org.json4s.native.Serialization.{read, write}
import scala.language.implicitConversions

import scala.concurrent.{Future, ExecutionContext}
import scala.concurrent.duration._

trait Json4sSupport {

  implicit val formats = DefaultFormats
  implicit val formatsNative = Serialization.formats(NoTypeHints)

  implicit def json4sFromRequestUnmarshaller[T:Manifest](implicit fm:FlowMaterializer, ex:ExecutionContext):FromRequestUnmarshaller[T] =
    new Unmarshaller[HttpRequest,T] {
      def apply(req: HttpRequest): Future[T] = {
        req.entity.withContentType(ContentTypes.`application/json`).toStrict(5.second)
          .map(_.data.toArray).map(x => read[T](new String(x)))
      }
    }

  implicit def json4sFromResponseUnmarshaller[T:Manifest](implicit fm:FlowMaterializer, ex:ExecutionContext):FromResponseUnmarshaller[T] =
    new Unmarshaller[HttpResponse,T] {
      def apply(res: HttpResponse): Future[T] = {
        res.entity.withContentType(ContentTypes.`application/json`)
          .toStrict(5.second).map(_.data.toArray)
          .map(x => read[T](new String(x)))
      }
    }

  implicit def json4sToEntityMarshaller[T <: AnyRef]:ToEntityMarshaller[T] =
    Marshaller.withFixedCharset[T, MessageEntity](MediaTypes.`application/json`,HttpCharset.custom("*"))( tp =>
      HttpEntity(write[T](tp)))


  implicit def json4sToHttpEntityMarshaller[T <: AnyRef](t:T):ResponseEntity = HttpEntity(MediaTypes.`application/json`, write[T](t))




}
