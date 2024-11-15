package repositories

import models.entity.Equipment
import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EquipmentRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._


  private class EquipmentTable(tag: Tag) extends Table[Equipment](tag, "equipments")  {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def model = column[String]("model")
    def serialNumber = column[String]("serial_number")
    def equipmentType = column[String]("equipment_type")
    def equipmentCondition = column[String]("equipment_condition")
    def isAvailable = column[Boolean]("is_available", O.Default(true))

    def * = (id.?, name, model, serialNumber, equipmentType, equipmentCondition, isAvailable) <> ((Equipment.apply _).tupled, Equipment.unapply)
  }

  private val equipments = TableQuery[EquipmentTable]

  // Create an Equipment
  def createEquipmentRecord(equipment: Equipment): Future[Long] = {
    val insertQueryThenReturnId = equipments returning equipments.map(_.id)

    db.run(insertQueryThenReturnId += equipment)
  }

  // Update an Equipment
  def update(id: Long, equipment: Equipment): Future[Equipment] = {
    val updateQuery = equipments.filter(equipment => equipment.id === id)
      .map(ele => (ele.name, ele.model, ele.serialNumber, ele.equipmentType, ele.equipmentCondition, ele.isAvailable))
      .update((equipment.name, equipment.model, equipment.serialNumber, equipment.equipmentType, equipment.equipmentCondition, equipment.isAvailable))

    // flatMap removes Some
    db.run(updateQuery).flatMap {_ =>
      getEquipmentById(id) // Updated, hence get the `Person` details using `get(id)`
    }
  }

  // Update status
  def updateStatus(id: Long, status: String): Future[Equipment] = {
    val equipment = equipments.filter(_.id === id)
      .map(ele => ele.equipmentCondition)
      .update(status)

    db.run(equipment).flatMap{_ =>
      getEquipmentById(id)
    }
  }

  // Get Equipment details by id
  def getEquipmentById(id: Long): Future[Equipment] =
    db.run(equipments.filter(equipment => equipment.id === id).result.head)

  // Get Available Equipments by type
  def getEquipmentsByType(equipmentType: String): Future[Seq[Equipment]] =
    db.run(equipments.filter(eq => eq.equipmentType === equipmentType && eq.isAvailable).result)
}