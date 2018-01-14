package com.eitc.onlinecrm.crawler.master.actor.crawling

import java.sql.Timestamp
import java.time.LocalDateTime

import akka.actor.{ Actor, ActorLogging }
import akka.pattern._
import akka.util.Timeout
import com.eitc.onlinecrm.crawler.master.db.Rows.InstanceStateRow
import com.google.cloud.compute.AttachedDisk.CreateDiskConfiguration
import com.google.cloud.compute.Compute.{ AddressAggregatedListOption, AddressField, AddressFilter }
import com.google.cloud.compute.InstanceInfo.Status
import com.google.cloud.compute.NetworkInterface.AccessConfig
import com.google.cloud.compute._
import org.graysurf.util.logging.ProcessSerialConverter

import scala.collection.JavaConverters._
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Try

class GcpOperationActor() extends Actor with ActorLogging {

  import context.dispatcher

  implicit val timeout: Timeout = 60 minutes

  private val gcpExecutor = context.system.dispatchers.lookup("dispatcher.gcp-compute")

  val compute: Compute = ComputeOptions.getDefaultInstance.getService

  override def receive: Receive = {

    case ListInstanceStatusMessage(taskId: Int, region: String, zone: String) ⇒
      Future(listInstance(taskId, region, zone))(gcpExecutor).pipeTo(sender())
    case CreateInstanceMessage(taskId: Int, name: String, region: String, zone: String, image: String, cpu: Int, memory: Int) ⇒
      Future(createInstance(taskId, name, region, zone, image, cpu, memory))(gcpExecutor).pipeTo(sender())
    case DeleteInstanceMessage(taskId: Int, name: String, region: String, zone: String) ⇒
      Future(deleteInstance(taskId: Int, name, region, zone))(gcpExecutor).pipeTo(sender())
    case ChangeInstanceAddressMessage(instanceName: String, region: String, zone: String) ⇒
      Future(changeInstanceAddress(instanceName, region, zone))(gcpExecutor).pipeTo(sender())
    case msg ⇒
      log.warning(s"${this.self.path.name} received unknown message $msg")
  }

  def listInstance(taskId: Int, region: String, zone: String): Seq[InstanceStatus] = {
    val instances = compute.listInstances(s"$region-$zone").iterateAll.asScala.toSeq.filter(_.getInstanceId.getInstance().startsWith(s"crawler-task-${taskId.formatted("%04d")}-${ProcessSerialConverter.serial}"))
    val instancesStatus = instances.map(v ⇒ InstanceStatus(v.getInstanceId.getInstance(), v.getInstanceId.getZone, Try(v.getNetworkInterfaces.asScala.head.getAccessConfigurations.asScala.head.getNatIp).getOrElse(""), v.getStatus, new Timestamp(v.getCreationTimestamp).toLocalDateTime))
    instancesStatus
  }

  def createInstance(taskId: Int, name: String, region: String, zone: String, image: String, cpu: Int, memory: Int): InstanceStateRow = {
    val regionZone = s"$region-$zone"
    val imageId = ImageId.of(image)
    val attachedDisk = AttachedDisk.of(CreateDiskConfiguration.newBuilder(imageId).setAutoDelete(true).build())
    val networkInterface = NetworkInterface.newBuilder("default").setAccessConfigurations(AccessConfig.of()).build()
    val instanceId = InstanceId.of(regionZone, name)
    val machineTypeId = MachineTypeId.of(regionZone, s"custom-$cpu-$memory")
    val serviceAccount = ServiceAccount.of("crawler@crawler-1383.iam.gserviceaccount.com", List("https://www.googleapis.com/auth/cloud-platform").asJava)
    val instanceInfo = InstanceInfo.newBuilder(instanceId, machineTypeId)
      .setAttachedDisks(attachedDisk)
      .setNetworkInterfaces(networkInterface)
      .setSchedulingOptions(SchedulingOptions.preemptible())
      .setServiceAccounts(List(serviceAccount).asJava)
      .build()
    val operation: Operation = compute.create(instanceInfo).waitFor()
    if (operation.getErrors == null) {
      val instance = compute.getInstance(instanceId)
      val address = instance.getNetworkInterfaces.asScala.head.getAccessConfigurations.asScala.head.getNatIp
      log.info(s"created instance: $name, $address, $regionZone")
      log.debug(instance.toString)
      InstanceStateRow(name, taskId, Status.STAGING.toString, address, LocalDateTime.now(), None, 0, List.empty)
    } else {
      throw new Exception(operation.getErrors.asScala.map(_.toString).mkString("\n"))
    }
  }

  def deleteInstance(taskId: Int, name: String, region: String, zone: String): InstanceStateRow = {
    val regionZone = s"$region-$zone"
    val instanceId = InstanceId.of(regionZone, name)
    val operation = compute.deleteInstance(instanceId)

    if (operation == null) {
      log.warning(s"$name was terminated by other")
      InstanceStateRow(name, taskId, Status.TERMINATED.toString, "", LocalDateTime.MIN, None, 0, List.empty)
    } else if (operation.waitFor().getErrors == null) {
      // delete old external address
      val addressFilter: AddressFilter = Compute.AddressFilter.equals(AddressField.NAME, name)
      val option = AddressAggregatedListOption.filter(addressFilter)
      val deleteAddresses = compute.listAddresses(option).iterateAll().asScala.toSeq.map {
        _address ⇒
          val address = _address.getAddressId.asInstanceOf[RegionAddressId].getAddress
          val addressId = RegionAddressId.of(region, address)
          compute.deleteAddress(addressId).waitFor()
          address
      }
      log.info(s"deleted instance: $name")
      // missing active_time, ip
      InstanceStateRow(name, taskId, Status.TERMINATED.toString, deleteAddresses.mkString(", "), LocalDateTime.MIN, Some(LocalDateTime.now()), 0, List.empty)

    } else {
      throw new Exception(operation.getErrors.asScala.map(_.toString).mkString("\n"))
    }

  }

  def changeInstanceAddress(instanceName: String, region: String, zone: String): Unit = {
    // delete old external address
    val addressFilter: AddressFilter = Compute.AddressFilter.equals(AddressField.NAME, instanceName)
    val option = AddressAggregatedListOption.filter(addressFilter)
    val iterator = compute.listAddresses(option).iterateAll().asScala
    while (iterator.hasNext) {
      val address = iterator.next().getAddressId.asInstanceOf[RegionAddressId].getAddress
      val addressId = RegionAddressId.of(region, address)
      compute.deleteAddress(addressId)
    }

    // create new external address
    val addressId = RegionAddressId.of(region, instanceName)
    compute.create(AddressInfo.of(addressId))

    // bind new external address
    val instance = compute.getInstance(InstanceId.of(zone, instanceName))
    val oldAddress = instance.getNetworkInterfaces.asScala.head.getAccessConfigurations.asScala.head.getNatIp
    instance.deleteAccessConfig("nic0", "external-nat")
    instance.deleteAccessConfig("nic0", "External NAT")
    val accessConfig = AccessConfig.of(compute.getAddress(addressId).getAddress)
    instance.addAccessConfig("nic0", accessConfig)
    log.info(s"change $instanceName address: $oldAddress to ${accessConfig.getNatIp}")

  }

}

case class ListInstanceStatusMessage(taskId: Int, region: String, zone: String)

case class CreateInstanceMessage(taskId: Int, name: String, region: String, zone: String, image: String, cpu: Int, memory: Int)

case class DeleteInstanceMessage(taskId: Int, name: String, region: String, zone: String)

case class ChangeInstanceAddressMessage(instanceName: String, region: String, zone: String)

case class InstanceStatus(name: String, regionZone: String, ip: String, status: Status, createTime: LocalDateTime)

