/*
   Copyright 2017-2020 Charles Korn.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package batect.execution.model.stages

import batect.config.BuildImage
import batect.config.Container
import batect.config.PullImage
import batect.execution.ContainerDependencyGraph
import batect.execution.ContainerDependencyGraphNode
import batect.execution.model.rules.TaskStepRule
import batect.execution.model.rules.run.BuildImageStepRule
import batect.execution.model.rules.run.CreateContainerStepRule
import batect.execution.model.rules.run.CreateTaskNetworkStepRule
import batect.execution.model.rules.run.InitialiseCachesStepRule
import batect.execution.model.rules.run.PullImageStepRule
import batect.execution.model.rules.run.RunContainerSetupCommandsStepRule
import batect.execution.model.rules.run.RunContainerStepRule
import batect.execution.model.rules.run.WaitForContainerToBecomeHealthyStepRule
import batect.logging.Logger
import batect.utils.flatMapToSet
import batect.utils.mapToSet

class RunStagePlanner(
    private val graph: ContainerDependencyGraph,
    private val logger: Logger
) {
    fun createStage(): RunStage {
        val allContainersInTask = graph.allNodes.mapToSet { it.container }

        val rules = graph.allNodes.flatMapToSet { executionStepsFor(it, allContainersInTask) } +
            CreateTaskNetworkStepRule +
            InitialiseCachesStepRule(allContainersInTask)

        logger.info {
            message("Created run plan.")
            data("rules", rules.map { it.toString() })
        }

        return RunStage(rules, graph.taskContainerNode.container)
    }

    private fun executionStepsFor(node: ContainerDependencyGraphNode, allContainersInNetwork: Set<Container>) = setOf(
        imageCreationRuleFor(node.container),
        CreateContainerStepRule(node.container, node.config, allContainersInNetwork),
        RunContainerStepRule(node.container, node.dependsOnContainers),
        WaitForContainerToBecomeHealthyStepRule(node.container),
        RunContainerSetupCommandsStepRule(node.container, node.config, allContainersInNetwork)
    )

    private fun imageCreationRuleFor(container: Container): TaskStepRule = when (container.imageSource) {
        is PullImage -> PullImageStepRule(container.imageSource)
        is BuildImage -> BuildImageStepRule(container)
    }
}
