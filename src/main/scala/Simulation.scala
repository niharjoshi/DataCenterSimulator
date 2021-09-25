import HelperUtils.{CreateLogger, ObtainConfigReference}
import Simulations.*
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

object Simulation:

  val logger = CreateLogger(classOf[Simulation])

  @main def runSimulation =

    logger.info("Running Round Robin Simulation")
    RoundRobinVmAllocationSimulation.Start()
    logger.info("Finished Round Robin Simulation")

    logger.info("Running Best Fit Simulation")
    BestFitVmAllocationSimulation.Start()
    logger.info("Finished Best Fit Simulation")

    logger.info("Running First Fit Simulation")
    FirstFitVmAllocationSimulation.Start()
    logger.info("Finished First Fit Simulation")

    logger.info("Running MapReduce Simulation")
    MapReduceSimulation.Start()
    logger.info("Finished MapReduce Simulation")

    logger.info("Running Horizontal VM Autoscaler Simulation")
    HorizontalVMAutoscalingUsingLoadBalancer.Start()
    logger.info("Finished Horizontal VM Autoscaler Simulation")

class Simulation