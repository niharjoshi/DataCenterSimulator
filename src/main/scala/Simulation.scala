import HelperUtils.{CreateLogger, ObtainConfigReference}
import Simulations.*
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

object Simulation:

  val logger = CreateLogger(classOf[Simulation])

  @main def runSimulation =

//    logger.info("Running iteration ")
//    RoundRobinVmAllocationSimulation.Start()
//    logger.info("Finished cloud simulation")
//
//    logger.info("Running iteration ")
//    BestFitVmAllocationSimulation.Start()
//    logger.info("Finished cloud simulation")
//
//    logger.info("Running iteration ")
//    FirstFitVmAllocationSimulation.Start()
//    logger.info("Finished cloud simulation")
    MapReduceSimulation.Start()

class Simulation