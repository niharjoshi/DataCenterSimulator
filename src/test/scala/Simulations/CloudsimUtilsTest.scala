package Simulations

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.*
import HelperUtils.{CloudsimUtils, CreateLogger, ObtainConfigReference}
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicyBestFit
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple
import org.cloudbus.cloudsim.cloudlets.{CloudletSimple, Cloudlet}
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.hosts.Host
import org.cloudbus.cloudsim.resources.PeSimple
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerSpaceShared
import org.cloudbus.cloudsim.vms.VmSimple
import org.cloudbus.cloudsim.datacenters.Datacenter
import org.cloudbus.cloudsim.network.topologies.BriteNetworkTopology

import scala.collection.mutable.ListBuffer

class CloudsimUtilsTest extends AnyFunSuite {

  // Setting simulation name for reference
  val simulation_name = "testing"

  // Initializing logger
  val logger = CreateLogger(classOf[CloudsimUtilsTest])

  // Initializing configuration for this simulation
  val config = ObtainConfigReference(simulation_name) match {
    case Some(value) => value
    case None => throw new RuntimeException("Could not find the config data!")
  }

  // Initializing clousim instance for testing
  val cloudsim = new CloudSim()

  test("Creation of processing element test") {
    val pe = CloudsimUtils.initializeProcessingElement(config.getDouble(simulation_name + ".pe.mipsCapacity"))
    assert(pe.isInstanceOf[PeSimple])
  }
  
  test("Creation of vm test") {
    val vm = CloudsimUtils.initializeVirtualMachine(simulation_name, config)
    assert(vm.isInstanceOf[VmSimple])
  }

  test("Creation of host test") {
    val host = CloudsimUtils.initializeHost(simulation_name, config, new VmSchedulerSpaceShared())
    assert(host.isInstanceOf[Host])
  }

  test("Creation of broker test") {
    val broker = CloudsimUtils.initializeBroker(cloudsim)
    assert(broker.isInstanceOf[DatacenterBrokerSimple])
  }

  test("Creation of datacenter test") {
    val nHosts = config.getInt(simulation_name + ".host.nHosts")
    val hosts = ListBuffer.empty[Host]
    1 to nHosts foreach {
      _ =>
        hosts.addOne(
          CloudsimUtils.initializeHost(
            simulation_name,
            config,
            new VmSchedulerSpaceShared()
          )
        )
    }

    val datacenter = CloudsimUtils.initializeDatacenter(
      simulation_name,
      config,
      cloudsim,
      hosts,
      new VmAllocationPolicyBestFit()
    )
    assert(datacenter.isInstanceOf[Datacenter])
  }

  test("Creation of network topology test") {
    val broker = CloudsimUtils.initializeBroker(cloudsim)
    val nHosts = config.getInt(simulation_name + ".host.nHosts")
    val hosts = ListBuffer.empty[Host]
    1 to nHosts foreach {
      _ =>
        hosts.addOne(
          CloudsimUtils.initializeHost(
            simulation_name,
            config,
            new VmSchedulerSpaceShared()
          )
        )
    }
    val datacenter = CloudsimUtils.initializeDatacenter(
      simulation_name,
      config,
      cloudsim,
      hosts,
      new VmAllocationPolicyBestFit()
    )
    CloudsimUtils.initializeNetworkTopology(cloudsim, datacenter, broker)
    assert(cloudsim.getNetworkTopology.isInstanceOf[BriteNetworkTopology])
  }

  test("Creation of cloudlets test") {
    val cloudlet = CloudsimUtils.initializeCloudlets(simulation_name, config)
    assert(cloudlet.isInstanceOf[CloudletSimple])
  }

  test("Calculation of cost test") {
    // Creating cloudlets
    val cloudlets = ListBuffer.empty[CloudletSimple]
    val nCloudlets = config.getInt(simulation_name + ".cloudlet.nCloudlets")
    1 to nCloudlets foreach {
      _ =>
        cloudlets.addOne(
          CloudsimUtils.initializeCloudlets(
            simulation_name,
            config
          )
        )
    }
    cloudlets.foreach(cloudlet => cloudlet.addFinishedLengthSoFar(config.getLong(simulation_name + ".cloudlet.length")))
    cloudlets.foreach(cloudlet => cloudlet.setStatus(Cloudlet.Status.SUCCESS))
    val cost = CloudsimUtils.calculateCost(cloudlets)
    assert(cost==config.getDouble(simulation_name + ".simulation.cost"))
  }
}
