# Name: Nihar Shailesh Joshi

# Homework 1

## Introduction
In this assignment, we will explore the CloudSim Plus cloud simulation framework and analyze the simulation results of
various scenarios that could realistically occur in a real-world cloud service provider & cloud service customer
situation.

As a Scala newbie, I felt that emulating certain scenarios which I have previously encountered in my software
development career (from the perspective of a cloud service provider) would be the most ideal way to approach the
problem statement.

We will explore these (three) scenarios as we move ahead in this documentation.

## Prerequisites & Installation
In order to run the simulations implemented in this assignment, I recommend cloning this repository onto your local
machine and running it from the command-line using the interactive build tool **sbt**.

*Note: In order to install sbt, please follow the OS-specific instructions at
https://www.scala-sbt.org/1.x/docs/Setup.html.*

To clone the repo use:
```console
git clone https://github.com/niharjoshi/CloudOrgSimulator.git
```

Next, navigate to the repo and use the following command to run the simulations:
```console
sbt clean compile run
```

To run the unit tests (which might be ideally done before running the simulations themselves), use the following
command:
```console
sbt clean compile test
```

## Configurations
As per the requirements specified in this assignment, I have not made use of any hardcoded configuration parameters.
All the configurations are stored in the **application.conf** file located in the *src/main/resources* folder.

Each simulation-specific configuration is stored under a new {} block with one or more nested {} blocks inside it.
The scope of each nested block {} is limited only within its own context, under its parent {} block.

A sample application.conf example should look as follows:
```console
ExampleSimulationConfig1 {
  DatacenterConfig {
    param1 = value1
  }
  HostConfig {
    param2 = value2
    param3 = value3
  }
}
.
.
.
ExampleSimulationConfigN {
  ...
}
```

Simulation configurations contain the following nested configurations:
- Datacenters
- Hosts
- Virtual Machines
- Cloudlets
- Simulations

## Simulations
As mentioned in the introduction, we will simulate and analyze three scenarios and then find the most optimal
architecture that can support these scenarios through fine-tuning.

### Simulation Scenario 1 - Virtual Machine Allocation Algorithms
In this scenario, we will compare three different algorithms for allocating VMs and see how the architecture performs 
when we submit cloudlets to it.

First, let us take a look at the structure of our datacenter.

![Alt text](simulation1_arch.jpg?raw=true "Simulation 1 - Datacenter Architecture")

In this datacenter, we have two hosts, each containing 4 virtual machines each. They are topologically connected by a 
VPC. A broker submits cloudlets to the datacenter after which the VM allocation algorithms come into play.

#### Our first algorithm is the **Round Robin VM Allocation algorithm**. This is a pre-emptive allocation algorithm in which VMs are assigned hosts in a circular order, without priority and in equal proportions.

![Alt text](round_robin_allocation.jpg?raw=true "Round Robin Allocation Algorithm")

The configuration for this simulation is as follows:
```console
RoundRobinVmAllocationSimulation {

    host {
        nHosts = 2
        RAMInMBs = 8000
        storageInMBs = 10000
        bandwidth = 2000
        mipsCapacity = 4000
        nPEs = 6
    }

    datacenter {
        cost = 5.0
        costPerMemory = 0.5
        costPerStorage = 0.005
        costPerBandwidth = 0.05
        OS = "CentOS"
    }

    vm {
        nVMs = 4
        mipsCapacity = 2000
        storageInMBs = 5000
        RAMInMBs = 4000
        bandwidth = 1000
        nPEs = 3
    }

    cloudlet {
        nCloudlets = 10
        length = 100000
        nPEs = 3
        utilizationModel = dynamic
        utilizationRatio = 0.3
    }
}
```
After submitting 10 cloudlets to the broker and performing the simulation, we get the following simulation results:

![Alt text](round_robin_simulation_results.png?raw=true "Round Robin Simulation Results")

#### Our second algorithm is the **Best Fit VM Allocation algorithm**. This algorithm involves allocating each VM into the host with the least available PEs that are enough for the VM.

![Alt text](best_fit_allocation.png?raw=true "Round Robin Allocation Algorithm")

The configuration for this simulation is as follows:
```console
BestFitVmAllocationSimulation {

    host {
        nHosts = 2
        RAMInMBs = 8000
        storageInMBs = 10000
        bandwidth = 2000
        mipsCapacity = 4000
        nPEs = 6
    }

    datacenter {
        cost = 5.0
        costPerMemory = 0.5
        costPerStorage = 0.005
        costPerBandwidth = 0.05
        OS = "CentOS"
    }

    vm {
        nVMs = 4
        mipsCapacity = 2000
        storageInMBs = 5000
        RAMInMBs = 4000
        bandwidth = 1000
        nPEs = 3
    }

    cloudlet {
        nCloudlets = 1
        length = 100000
        nPEs = 3
        utilizationModel = dynamic
        utilizationRatio = 0.1
    }
}
```
After submitting 10 cloudlets to the broker and performing the simulation, we get the following simulation results:

![Alt text](best_fit_simulation_results.png?raw=true "Best Fit Simulation Results")

#### Our third algorithm is the **First Fit VM Allocation algorithm**. This algorithm finds the first host having suitable resources to place a given VM.

![Alt text](first_fit_allocation.png?raw=true "First Fit Allocation Algorithm")

The configuration for this simulation is as follows:
```console
FirstFitVmAllocationSimulation {

    host {
        nHosts = 2
        RAMInMBs = 8000
        storageInMBs = 10000
        bandwidth = 2000
        mipsCapacity = 4000
        nPEs = 6
    }

    datacenter {
        cost = 5.0
        costPerMemory = 0.5
        costPerStorage = 0.005
        costPerBandwidth = 0.05
        OS = "CentOS"
    }

    vm {
        nVMs = 4
        mipsCapacity = 2000
        storageInMBs = 5000
        RAMInMBs = 4000
        bandwidth = 1000
        nPEs = 3
    }

    cloudlet {
        nCloudlets = 10
        length = 100000
        nPEs = 3
        utilizationModel = dynamic
        utilizationRatio = 0.1
    }
}
```
After submitting 10 cloudlets to the broker and performing the simulation, we get the following simulation results:

![Alt text](first_fit_simulation_results.png?raw=true "First Fit Simulation Results")

After running the three allocation algorithms, we can take a look at the following table and see the VM 
allocation details and the cost incurred for running 10 cloudlets:

| Algorithm | Cost | Vm Assignments |
| ------------- |:-------------:| -----:|
| Round Robin | 1083.50 | (v0, h1), (v1, h1), (v2, h0), (v3, h1) |
| Best Fit | 3250.17 | (v0, h0), (v1, h0), (v2, h1), (v3, h1) |
| First Fit | 3250.17 | (v0, h0), (v1, h0), (v2, h1), (v3, h1) |

We can see that Best Fit and First Fit performed similarly, while Round Robin showed more cost - efficiency.

### Simulation Scenario 2 - Horizontal Autoscaling for Virtual Machines
In this scenario, we will take a look at how horizontal virtual machine scaling takes place. In this simulation, we 
have a datacenter with 50 hosts with 4 VMs on each host. We flood the datacenter with cloudlets of variable lengths 
and keep producing cloudlets until the CloudSim clock hits 50 cycles. At each cycle, we spawn 10 cloudlets and observe 
the behaviour of the VMs in the datacenter.

![Alt text](autoscaling_logic.png?raw=true "Autoscaling Logic")

The config file for this simulation is as follows:
```console
HorizontalVMAutoscalingUsingLoadBalancer {

    vm {
        nVMs = 4
        mipsCapacity = 1000
        storageInMBs = 10000
        RAMInMBs = 500
        bandwidth = 1000
        nPEs = 2
    }

    host {
        nHosts = 50
        RAMInMBs = 2000
        storageInMBs = 1000000
        bandwidth = 10000
        mipsCapacity = 1000
        nPEs = 32
    }

    datacenter {
        cost = 5.0
        costPerMemory = 0.5
        costPerStorage = 0.005
        costPerBandwidth = 0.05
        OS = "Linux"
    }

    cloudlet {
        length = 2000
        lengths = [2000, 4000, 10000, 16000, 2000, 30000, 20000]
        nPEs = 2
        utilizationModel = full
    }

    scheduling {
        cloudletsAtStart = 6
        cloudletsAddedPerCycle = 10
        VMDestructionDelay = 10
        schedulingInterval = 5
        maxSimulationCycles = 50
    }

}
```

At the beginning, we have 6 cloudlets being submitted to the broker. We add 10 cloudlets per cycle and our scheduling 
interval is 5. Unused VMs are destroyed after a 10 second wait time.

Once we perform the simulation, we get the following results:

![Alt text](horizontal_autoscaling_simulation_results.png?raw=true "Horizontal Autoscaling Simulation Results")

The results keep going until 50 cycles. You can find the output in doc/horizontal_autoscaling_simulation_results.txt.

As we can see, VMs are being dynamically created and destroyed, as if evident by the VM IDs. The total cost of the 
simulation comes out to be different in each run due to the random lengths of the cloudelts. We can also see the 
execution time of the cloudlets vary due to the same reason. In the run shown above, the final cost of the simulation 
was 5222.71.

### Simulation Scenario 2 - Map Reduce Simulation

MapReduce is a processing technique and a program model for distributed computing. The MapReduce algorithm contains 
two important tasks - Map and Reduce. Map takes a set of data and converts it into another set of data, where 
individual elements are broken down into tuples (key/value pairs). Reduce takes the output from a map as an input and 
combines those data tuples into a smaller set of tuples.

![Alt text](map_reduce.png?raw=true "Map Reduce")

In this scenario, we will simulate a datacenter where we first submit mapping cloudlets to the broker followed by a set 
of reducing cloudlets.

The configuration for this simulation is:
```console
MapReduceSimulation {

    host {
        nHosts = 2
        RAMInMBs = 8000
        storageInMBs = 10000
        bandwidth = 2000
        mipsCapacity = 4000
        nPEs = 6
    }

    datacenter {
        cost = 5.0
        costPerMemory = 0.5
        costPerStorage = 0.005
        costPerBandwidth = 0.05
        OS = "Alpine"
    }

    vm {
        nVMs = 4
        mipsCapacity = 2000
        storageInMBs = 5000
        RAMInMBs = 4000
        bandwidth = 1000
        nPEs = 3
    }

    cloudlet {
        nCloudlets = 10
        length = 100000
        nPEs = 3
        utilizationModel = dynamic
        utilizationRatio = 0.1
    }
}
```
After running MapReduce, we get the following results:

![Alt text](map_reduce_results.png?raw=true "Map Reduce Results")

### Checklist:
- [x] 3 scenarios
- [x] More than 5 unit tests
- [x] Comments and explanations
- [x] Logging statements
- [x] No hardcoded values
- [x] No var or heap-based variables used
- [x] No for, while or do-while loops used
- [x] Installation instructions in README









