package controllers

import models.entity.Equipment
import models.response.ApiResponse
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import play.api.mvc.{AbstractController, Action, ControllerComponents}
import services.EquipmentService

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import play.api.mvc._

class EquipmentController @Inject()(
   val cc: ControllerComponents,
   equipmentService: EquipmentService
)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  // create an equipment
  def createEquipment() : Action[JsValue] = Action.async(parse.json) { request =>
    request.body.validate[Equipment] match {
      case JsSuccess(equipment, _) =>
        equipmentService.create(equipment).map(created =>
          ApiResponse.successResult(201, Json.obj("message"->"Equipment created", "id"-> created))
        ).recover{
          case ex: Exception =>
            ApiResponse.errorResult(s"Error creating the equipment: ${ex.getMessage}", 400)
        }
      case JsError(errors) =>
        Future.successful(ApiResponse.errorResult(
          "Invalid equipment data",
          400
        ))
    }
  }

  // update an equipment
  def updateEquipment(id: Long): Action[JsValue] = Action.async(parse.json) { request =>
    request.body.validate[Equipment] match {
      case JsSuccess(equipment, _) =>
        equipmentService.update(id, equipment).map {updated =>
          ApiResponse.successResult(200, Json.toJson(updated))
        }
      case JsError(errors) =>
        Future.successful(ApiResponse.errorResult(
          "Invalid equipment data",
          400
        ))
    }
  }

  // get details of an equipment by Id
  def getEquipmentDetailsById(id: Long): Action[AnyContent] = Action.async {
    equipmentService.getEquipmentById(id).map {equipment =>
      ApiResponse.successResult(200, Json.toJson(equipment))
    }
  }
}
