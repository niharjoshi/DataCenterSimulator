package Simulations

// Importing cloudsim modules
import HelperUtils.{CloudsimUtils, CreateLogger, ObtainConfigReference}
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicyBestFit
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerSpaceShared
import org.cloudbus.cloudsim.cloudlets.CloudletSimple
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.distributions.UniformDistr
import org.cloudbus.cloudsim.hosts.{Host, HostSimple}
import org.cloudbus.cloudsim.vms.Vm
import org.cloudbus.cloudsim.resources.PeSimple
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelFull
import org.cloudsimplus.autoscaling.HorizontalVmScalingSimple
import org.cloudsimplus.builders.tables.CloudletsTableBuilder
import org.cloudsimplus.listeners.EventInfo

import scala.collection.mutable.ListBuffer
import scala.util.Random
import scala.collection.JavaConverters.*

class HorizontalVMAutoscalingUsingLoadBalancer

object HorizontalVMAutoscalingUsingLoadBalancer {

  // Setting simulation name for reference
  val simulation_name = "HorizontalVMAutoscalingUsingLoadBalancer"

  // Initializing logger
  val logger = CreateLogger(classOf[HorizontalVMAutoscalingUsingLoadBalancer])

  // Initializing configuration for this simulation
  val config = ObtainConfigReference(simulation_name) match {
    case Some(value) => value
    case None => throw new RuntimeException("Could not find the config data!")
  }

  // Setting time-related parameters for the autoscaling process
  val cloudletLengths = config.getIntList(simulation_name + ".cloudlet.lengths")

  // Initializing cloudsim
  val cloudsim = new CloudSim()
  cloudsim.addOnClockTickListener(dynamicallyInitializeCloudlets)
  logger.debug("Initialized cloudsim module")

  // Creating a datacenter broker
  val broker = CloudsimUtils.initializeBroker(cloudsim)
  val VMDestructionDelay = config.getDouble(simulation_name + ".scheduling.VMDestructionDelay")
  broker.setVmDestructionDelay(VMDestructionDelay)
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

  // Creating datacenter and assigning scheduling interval to it
  val schedulingInterval = config.getInt(simulation_name + ".scheduling.schedulingInterval")
  datacenter.setSchedulingInterval(schedulingInterval)
  logger.info("Created datacenter")

  // Creating virtual machines and adding horizontal scaling rules
  val vms = ListBuffer.empty[Vm]
  val nVMs = config.getInt(simulation_name + ".vm.nVMs")
  1 to nVMs foreach {
    _ => {
      val vm = createVms()
      setHorizontalScalingRules(vm)
      vms.addOne(vm)
    }
  }
  logger.info("Created " + nVMs + " virtual machines")

  // Creating cloudlets
  val cloudlets = ListBuffer.empty[CloudletSimple]
  val cloudletsAtStart = config.getInt(simulation_name + ".scheduling.cloudletsAtStart")
  1 to cloudletsAtStart foreach {
    _ =>
      cloudlets.addOne(
        CloudsimUtils.initializeCloudlets(
          simulation_name,
          config
        )
      )
  }
  logger.info("Created initial " + cloudletsAtStart + " cloudlets")

  def Start(): Unit = {

    // Submitting virtual machines to broker
    broker.submitVmList(vms.asJava)

    // Submitting cloudlets to broker
    broker.submitCloudletList(cloudlets.asJava)

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
    // Building cloudlets table
    new CloudletsTableBuilder(broker.getCloudletFinishedList()).build()
    // Calculating total cost for simulation
    logger.info("----> Total cost for the simulation: " + CloudsimUtils.calculateCost(cloudlets))
  }

  // Function to dynamicaly create new cloudlets every cycle
  def dynamicallyInitializeCloudlets(eventInfo: EventInfo): Unit = {
    // Getting current cycle
    val time = eventInfo.getTime().toLong
    // Getting creation interval, spawn count and max cycles for cloudlets
    val cloudletCreationInterval = schedulingInterval * 2
    val cloudletsAddedPerCycle = config.getInt(simulation_name + ".scheduling.cloudletsAddedPerCycle")
    val maxSimulationCycles = config.getInt(simulation_name + ".scheduling.maxSimulationCycles")
    // Getting required PEs
    val nPEs = config.getInt(simulation_name + ".cloudlet" + ".nPEs")
    // Checking whether cycle falls between 0 and max cycles
    if (time % cloudletCreationInterval == 0 && time < maxSimulationCycles) {
      // Creating cloudlets
      val cloudlets_subset = ListBuffer.empty[CloudletSimple]
      1 to cloudletsAddedPerCycle foreach {
        _ => {
          val randomIndex = Random.nextInt(cloudletLengths.size)
          val cloudlet = new CloudletSimple(cloudletLengths.get(randomIndex).toLong, nPEs)
          cloudlets_subset.addOne(
            cloudlet
          )
          cloudlets.addOne(
            cloudlet
          )
        }
      }
      // Submitting cloudlets to broker
      broker.submitCloudletList(cloudlets_subset.asJava)
      logger.info("Created and submitted " + cloudletsAddedPerCycle + " cloudlets at cycle " + time)
    }
  }

  // Utility function to create a simple VM
  def createVms(): Vm = {
    // Creating VM
    val vm = CloudsimUtils.initializeVirtualMachine(
      simulation_name,
      config
    )
    return vm
  }

  // Utility function to check if VM is overloaded based in CPU utilization
  def overloadedVmStatusChecker(vm: Vm): Boolean = {
    return vm.getCpuPercentUtilization() > 0.8
  }

  // Function to set to horizontal scaling rules for VM
  def setHorizontalScalingRules(vm: Vm): Unit = {
    // Initialzing horizontal scaling class
    val horizontalScaling = new HorizontalVmScalingSimple()
    // Setting predicate function to check overload status
    horizontalScaling.setVmSupplier(() => createVms()).setOverloadPredicate(overloadedVmStatusChecker)
    // Attaching scaling rules to VM
    vm.setHorizontalScaling(horizontalScaling)
  }
}