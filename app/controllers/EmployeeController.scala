package controllers

import models.entity.Employee
import models.request.AllocationApprovalRequest
import models.response.ApiResponse
import play.api.mvc._
import play.api.libs.json._
import services.EmployeeService
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EmployeeController @Inject()(
    val cc: ControllerComponents,
    employeeService: EmployeeService
)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  // Create an employee
  def createEmployee(): Action[JsValue] = Action.async(parse.json) { request =>
    request.body.validate[Employee] match {
      case JsSuccess(employee, _) =>
        employeeService.create(employee).map { created =>
          ApiResponse.successResult(201, Json.obj("message"->"Employee created", "id"-> created))
        }.recover {
          case ex: Exception =>
            ApiResponse.errorResult(s"Error creating employee: ${ex.getMessage}", 500)
            InternalServerError(Json.obj("message" -> "Error creating employee"))
        }
      case JsError(errors) =>
        Future.successful(
          ApiResponse.errorResult(
            "Invalid employee data",
            400
          )
        )
    }
  }

  // list employee allocation requests
  def getAllocationRequests(employeeId: Long): Action[AnyContent] = Action.async {
    employeeService.getAllocationsByEmpId(employeeId).map(data =>
      ApiResponse.successResult(200, Json.toJson(data)))
  }

  // Get all equipment approval requests raised by employees under a specific manager
  def getApprovalRequests(employeeId: Long): Action[AnyContent] = Action.async {
    employeeService.getApprovalRequests(employeeId).map(data =>
      ApiResponse.successResult(200, Json.toJson(data)))
  }

  // Approve/ Reject the allocation request
  def allocationRequestApproval(): Action[JsValue] = Action.async(parse.json) { request =>
    request.body.validate[AllocationApprovalRequest] match {
      case JsSuccess(request, _) =>
        employeeService.updateAllocationApproval(request).map(data => {
          val message = if(request.isApproved) "Allocation Request is Approved" else "Allocation Request is Rejected"
          ApiResponse.successResult(201,
            Json.obj(
              "message"-> s"Updated: $message"
            ))}
        ).recover {
          case ex: Exception =>
            ApiResponse.errorResult(s"Error approving the allocationRequest: ${ex.getMessage}", 500)
        }
      case JsError(errors) =>
        Future.successful(
          ApiResponse.errorResult(
            "Invalid AllocationApprovalRequest data",
            400
          )
        )
    }
  }
}
