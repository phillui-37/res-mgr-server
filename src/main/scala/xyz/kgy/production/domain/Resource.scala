package xyz.kgy.production.domain

import zio.json._

case class Resource(
  id: Option[Long],
  name: String,
  description: Option[String],
  createdAt: java.time.Instant,
  updatedAt: java.time.Instant
)

object Resource {
  implicit val encoder: JsonEncoder[Resource] = DeriveJsonEncoder.gen[Resource]
  implicit val decoder: JsonDecoder[Resource] = DeriveJsonDecoder.gen[Resource]
}

case class CreateResourceRequest(
  name: String,
  description: Option[String]
)

object CreateResourceRequest {
  implicit val encoder: JsonEncoder[CreateResourceRequest] = DeriveJsonEncoder.gen[CreateResourceRequest]
  implicit val decoder: JsonDecoder[CreateResourceRequest] = DeriveJsonDecoder.gen[CreateResourceRequest]
}

case class UpdateResourceRequest(
  name: Option[String],
  description: Option[String]
)

object UpdateResourceRequest {
  implicit val encoder: JsonEncoder[UpdateResourceRequest] = DeriveJsonEncoder.gen[UpdateResourceRequest]
  implicit val decoder: JsonDecoder[UpdateResourceRequest] = DeriveJsonDecoder.gen[UpdateResourceRequest]
} 