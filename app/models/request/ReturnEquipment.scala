package models.request

import models.enums.MaintenanceStatus.MaintenanceStatus
import play.api.libs.functional.syntax._
import play.api.libs.json._

case class ReturnEquipment(
    allocationId: Long,
    maintenanceRequired: Boolean,
    maintenanceStatus: Option[MaintenanceStatus] = None
)

object ReturnEquipment {
  // Read for the Allocation fields
  private val allocationIdReads: Reads[Long] = (JsPath \ "allocationId").read[Long]
  private val maintenanceRequiredReads: Reads[Boolean] = (JsPath \ "maintenanceRequired").read[Boolean]
  private val maintenanceStatusReads: Reads[Option[MaintenanceStatus]] = (JsPath \ "maintenanceStatus").readNullable[MaintenanceStatus]

  // Combine all the reads
  implicit val returnEquipmentReads: Reads[ReturnEquipment] = (
    allocationIdReads and
      maintenanceRequiredReads and
      maintenanceStatusReads
    )(ReturnEquipment.apply _)

  // Use Json.writes to generate Writes automatically
  implicit val returnEquipmentWrites: Writes[ReturnEquipment] = Json.writes[ReturnEquipment]

  // Combine Reads and Writes into Format
  implicit val returnEquipmentFormat: Format[ReturnEquipment] = Format(returnEquipmentReads, returnEquipmentWrites)
}



