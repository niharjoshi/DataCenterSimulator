package Simulations

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.*
import HelperUtils.{CreateLogger, ObtainConfigReference}
import Simulations.HorizontalVMAutoscalingUsingLoadBalancer
import org.cloudbus.cloudsim.vms.Vm
import org.cloudsimplus.autoscaling.HorizontalVmScalingSimple


class HorizontalVMAutoscalingUsingLoadBalancerTest extends AnyFunSuite {

  // Setting simulation name for reference
  val simulation_name = "testing"

  // Initializing logger
  val logger = CreateLogger(classOf[BestFitVmAllocationSimulationTest])

  // Initializing configuration for this simulation
  val config = ObtainConfigReference(simulation_name) match {
    case Some(value) => value
    case None => throw new RuntimeException("Could not find the config data!")
  }

  test("Creating virtual machines test") {
    val vm = HorizontalVMAutoscalingUsingLoadBalancer.createVms()
    assert(vm.isInstanceOf[Vm])
  }

  test("Virtual machine overloaded status checker test") {
    val vm = HorizontalVMAutoscalingUsingLoadBalancer.createVms()
    val overloaded = HorizontalVMAutoscalingUsingLoadBalancer.overloadedVmStatusChecker(vm)
    assert(overloaded == false)
  }

  test("Horizontal scaling rules test") {
    val vm = HorizontalVMAutoscalingUsingLoadBalancer.createVms()
    HorizontalVMAutoscalingUsingLoadBalancer.setHorizontalScalingRules(vm)
    assert(vm.getHorizontalScaling().isInstanceOf[HorizontalVmScalingSimple])
  }

}
