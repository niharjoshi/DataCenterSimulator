package HelperUtils

import com.typesafe.config.Config
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.datacenters.{Datacenter, DatacenterSimple}
import org.cloudbus.cloudsim.brokers.{DatacenterBroker, DatacenterBrokerSimple}
import org.cloudbus.cloudsim.cloudlets.CloudletSimple
import org.cloudbus.cloudsim.hosts.{Host, HostSimple}
import org.cloudbus.cloudsim.vms.{Vm, VmSimple}
import org.cloudbus.cloudsim.resources.{Pe, PeSimple}
import org.cloudbus.cloudsim.network.topologies.BriteNetworkTopology
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicyAbstract
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerAbstract
import org.cloudbus.cloudsim.utilizationmodels.{UtilizationModelDynamic, UtilizationModelFull}

import scala.collection.mutable.ListBuffer
import scala.collection.JavaConverters.*

class CloudsimUtils

object CloudsimUtils {

  def initializeProcessingElement(mipsCapacity: Double): PeSimple = {
    return new PeSimple(mipsCapacity)
  }

  def initializeVirtualMachine(
                                name: String,
                                config: Config,
                                id: String = ""
                              ): Vm = {

    val mipsCapacity: Double = config.getDouble(name + ".vm" + id + ".mipsCapacity")
    val nCPUs: Long = config.getLong(name + ".vm" + id + ".nPEs")
    val ram: Long = config.getLong(name + ".vm" + id + ".RAMInMBs")
    val storage: Long = config.getLong(name + ".vm" + id + ".storageInMBs")
    val bandwidth: Long = config.getLong(name + ".vm" + id + ".bandwidth")

    return new VmSimple(mipsCapacity, nCPUs).setRam(ram).setBw(bandwidth).setSize(storage)
  }

  def initializeHost(
                      name: String,
                      config: Config,
                      policy: VmSchedulerAbstract,
                      id: String = ""
                    ): Host = {

    val hostPes = ListBuffer.empty[PeSimple]
    val nPEs: Int = config.getInt(name + ".host" + id + ".nPEs")
    val mipsCapacity: Double = config.getDouble(name + ".host" + id + ".mipsCapacity")
    1 to nPEs foreach {_ => hostPes.addOne(initializeProcessingElement(mipsCapacity))}

    val ram: Long = config.getLong(name + ".host" + id + ".RAMInMBs")
    val storage: Long = config.getLong(name + ".host" + id + ".storageInMBs")
    val bandwidth: Long = config.getLong(name + ".host" + id + ".bandwidth")

    return new HostSimple(ram, bandwidth, storage, hostPes.toList.asJava).setVmScheduler(policy)
  }

  def initializeBroker(cloudsim: CloudSim): DatacenterBrokerSimple = {
    return new DatacenterBrokerSimple(cloudsim)
  }

  def initializeDatacenter(
                            name: String,
                            config: Config,
                            cloudsim: CloudSim,
                            hosts: ListBuffer[Host],
                            policy: VmAllocationPolicyAbstract,
                            id: String = ""
                          ): Datacenter = {

    val cost = config.getDouble(name + ".datacenter" + id + ".cost")
    val costPerMemory = config.getDouble(name + ".datacenter" + id + ".costPerMemory")
    val costPerStorage = config.getDouble(name + ".datacenter" + id + ".costPerStorage")
    val costPerBandwidth = config.getDouble(name + ".datacenter" + id + ".costPerBandwidth")
    val os = config.getString(name + ".datacenter" + id + ".OS")

    val dc = DatacenterSimple(cloudsim, hosts.toList.asJava, policy)
    dc.getCharacteristics
      .setCostPerSecond(cost)
      .setCostPerMem(costPerMemory)
      .setCostPerStorage(costPerStorage)
      .setCostPerBw(costPerBandwidth)
      .setOs(os)

    return dc
  }

  def initializeNetworkTopology(
                                 cloudsim: CloudSim,
                                 datacenter: Datacenter,
                                 broker: DatacenterBrokerSimple,
                                 topology_file: String = "network_topology.brite"
                               ): Unit = {

    val topology = BriteNetworkTopology.getInstance(topology_file)
    cloudsim.setNetworkTopology(topology)

    topology.mapNode(datacenter, 0)
    topology.mapNode(broker, 4)
  }

  def initializeCloudlets(
                           name: String,
                           config: Config,
                           id: String = ""
                         ): CloudletSimple = {

    val length = config.getLong(name + ".cloudlet" + id + ".length")
    val nPEs = config.getInt(name + ".cloudlet" + id + ".nPEs")
    val utilizationModel = config.getString(name + ".cloudlet" + id + ".utilizationModel")

    if (utilizationModel == "dynamic") {
      val utilizationRatio = config.getDouble(name + ".cloudlet" + id + ".utilizationRatio")
      return new CloudletSimple(length, nPEs, new UtilizationModelDynamic(utilizationRatio))
    }
    else {
      return new CloudletSimple(length, nPEs, new UtilizationModelFull())
    }
  }

  def calculateCost(cloudlets: ListBuffer[CloudletSimple]): Double = {

    val finishedCloudlets = ListBuffer.empty[CloudletSimple]
    cloudlets.foreach(
      cloudlet => {
        if (cloudlet.isFinished()) {
          finishedCloudlets.addOne(cloudlet)
        }
      }
    )

    return finishedCloudlets.foldLeft(0.0)(
      (i, j) => (i + (j.getCostPerSec() * j.getActualCpuTime() * j.getCostPerBw()))
    )
  }
}
