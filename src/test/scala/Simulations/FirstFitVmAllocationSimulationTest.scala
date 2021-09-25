package Simulations

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.*

import scala.collection.mutable.ListBuffer
import HelperUtils.{CloudsimUtils, CreateLogger, ObtainConfigReference}
import Simulations.BestFitVmAllocationSimulation
import org.cloudsimplus.builders.tables.CloudletsTableBuilder

class FirstFitVmAllocationSimulationTest extends AnyFunSuite {

  // Setting simulation name for reference
  val simulation_name = "testing"

  // Initializing logger
  val logger = CreateLogger(classOf[FirstFitVmAllocationSimulationTest])

  // Initializing configuration for this simulation
  val config = ObtainConfigReference(simulation_name) match {
    case Some(value) => value
    case None => throw new RuntimeException("Could not find the config data!")
  }

  test("BestFitVmAllocationSimulation test") {

    val expected_simulation_status = config.getString(simulation_name + ".simulation.expected")
    val simulation_status = BestFitVmAllocationSimulation.Start()
    assert(simulation_status == expected_simulation_status)
  }
}
