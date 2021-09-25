package Simulations

// Importing cloudsim modules
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicyBestFit
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerSpaceShared
import org.cloudbus.cloudsim.cloudlets.CloudletSimple
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.hosts.{Host, HostSimple}
import org.cloudbus.cloudsim.vms.Vm
import org.cloudbus.cloudsim.resources.PeSimple
import org.cloudsimplus.builders.tables.{CloudletsTableBuilder}

import HelperUtils.{CloudsimUtils, CreateLogger, ObtainConfigReference}
import scala.collection.mutable.ListBuffer
import scala.collection.JavaConverters.*

class BestFitVmAllocationSimulation

object BestFitVmAllocationSimulation {

  def Start(): String = {
    // Setting simulation name for reference
    val simulation_name = "BestFitVmAllocationSimulation"

    // Initializing logger
    val logger = CreateLogger(classOf[BestFitVmAllocationSimulation])

    // Initializing configuration for this simulation
    val config = ObtainConfigReference(simulation_name) match {
      case Some(value) => value
      case None => throw new RuntimeException("Could not find the config data!")
    }

    // Initializing cloudsim
    val cloudsim = new CloudSim()
    logger.debug("Initialized cloudsim module")

    // Creating a datacenter broker
    val broker = CloudsimUtils.initializeBroker(cloudsim)
    logger.info("Created datacenter broker")

    // Creating hosts
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
    logger.info("Created " + nHosts + " hosts")

    // Creating datacenter
    val datacenter = CloudsimUtils.initializeDatacenter(
      simulation_name,
      config,
      cloudsim,
      hosts,
      new VmAllocationPolicyBestFit()
    )
    logger.info("Created datacenter")

    // Configuring network topology
    // Please note: This .brite file is taken from cloudslab cloudsim example resources
    // You can find it at https://github.com/Cloudslab/cloudsim/blob/master/modules/cloudsim-examples/topology.brite
    CloudsimUtils.initializeNetworkTopology(cloudsim, datacenter, broker)
    logger.info("Configured network topology from default BRITE file")

    // Creating virtual machines
    val vms = ListBuffer.empty[Vm]
    val nVMs = config.getInt(simulation_name + ".vm.nVMs")
    1 to nVMs foreach {
      _ =>
        vms.addOne(
          CloudsimUtils.initializeVirtualMachine(
            simulation_name,
            config
          )
        )
    }
    logger.info("Created " + nVMs + " virtual machines")

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
    logger.info("Created " + nCloudlets + " cloudlets")

    // Submitting virtual machines to broker
    broker.submitVmList(vms.toList.asJava)

    // Submitting cloudlets to broker
    broker.submitCloudletList(cloudlets.toList.asJava)

    logger.info("----> Started " + simulation_name)
    // Getting simulation start time
    val simulationStartTime = System.currentTimeMillis()
    // Starting simualtion
    cloudsim.start()
    // Getting simulation stop time
    val simulationStopTime = System.currentTimeMillis()
    logger.info("----> Finished " + simulation_name)
    // Calculating total time for simulation
    val totalTimeTaken = (simulationStopTime - simulationStartTime) / 1000.0
    logger.info("----> Total time for simulation: " + totalTimeTaken + " seconds")
    // Calculating total cost for simulation
    logger.info("----> Total cost for the simulation: " + CloudsimUtils.calculateCost(cloudlets))
    // Building cloudlets table
    new CloudletsTableBuilder(broker.getCloudletFinishedList()).build()
    return "Simulation successful!"
  }
}
