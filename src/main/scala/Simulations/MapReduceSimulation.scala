package Simulations

import HelperUtils.{CreateLogger, ObtainConfigReference}
import collection.JavaConverters.*
import scala.collection.mutable.ListBuffer


// Importing cloudsim modules
import HelperUtils.CloudsimUtils
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicyFirstFit
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.resources.PeSimple
import org.cloudbus.cloudsim.hosts.{Host, HostSimple}
import org.cloudbus.cloudsim.cloudlets.CloudletSimple
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerSpaceShared
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelDynamic
import org.cloudbus.cloudsim.vms.Vm
import org.cloudsimplus.builders.tables.CloudletsTableBuilder

class MapReduceSimulation

object MapReduceSimulation {

  val simulation_name: String = "MapReduceSimulation"

  // Initializing logger
  val logger = CreateLogger(classOf[MapReduceSimulation])

  // Importing configuration for this simulation
  val config = ObtainConfigReference(simulation_name) match {
    case Some(value) => value
    case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
  }

  def Start(): String = {

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
      new VmAllocationPolicyFirstFit()
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

    // Getting total number of mapping and reducing cloudlets
    val nCloudlets = config.getInt(simulation_name + ".cloudlet.nCloudlets")

    // Creating mapping cloudlets
    val map_cloudlets = ListBuffer.empty[CloudletSimple]
    1 to nCloudlets foreach {
      _ =>
        map_cloudlets.addOne(
          CloudsimUtils.initializeCloudlets(
            simulation_name,
            config
          )
        )
    }
    logger.info("Created " + nCloudlets + " cloudlets")

    // Creating reduce cloudlets
    val reduce_cloudlets = ListBuffer.empty[CloudletSimple]
    1 to nCloudlets foreach {
      _ =>
        reduce_cloudlets.addOne(
          CloudsimUtils.initializeCloudlets(
            simulation_name,
            config
          )
        )
    }
    logger.info("Created " + nCloudlets + " cloudlets")

    // Submitting VMs to broker
    broker.submitVmList(vms.asJava)

    // Submitting mappers and reducers to broker
    broker.submitCloudletList(map_cloudlets.asJava)
    broker.submitCloudletList(reduce_cloudlets.asJava)

    logger.info("----> Started MapReduceSimulation")
    // Getting simulation start time
    val simulationStartTime = System.currentTimeMillis()
    // Starting simualtion
    cloudsim.start()
    // Getting simulation stop time
    val simulationStopTime = System.currentTimeMillis()
    logger.info("----> Finished MapReduceSimulation")
    // Calculating total time for simulation
    val totalTimeTaken = (simulationStopTime - simulationStartTime) / 1000.0
    logger.info("----> Total time for simulation: " + totalTimeTaken + " seconds")
    // Calculating total cost for mappers and reducers
    logger.info("----> Total cost for mappers: " + CloudsimUtils.calculateCost(map_cloudlets))
    logger.info("----> Total cost for reducers: " + CloudsimUtils.calculateCost(reduce_cloudlets))
    // Building cloudlets table
    new CloudletsTableBuilder(broker.getCloudletFinishedList()).build()
    return "Simulation successful!"

  }
}