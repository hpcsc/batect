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

package batect.config

import batect.config.io.ConfigurationException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Configuration(
    @SerialName("project_name") val projectName: String,
    val tasks: TaskMap = TaskMap(),
    val containers: ContainerMap = ContainerMap(),
    @SerialName("config_variables") val configVariables: ConfigVariableMap = ConfigVariableMap()
) {
    fun applyImageOverrides(overrides: Map<String, ImageSource>): Configuration {
        val updatedContainers = overrides.entries.fold(containers.values) { updatedContainers, override ->
            val containerName = override.key
            val oldContainer = containers[containerName]

            if (oldContainer == null) {
                throw ConfigurationException("Cannot override image for container '${override.key}' because there is no container named '${override.key}' defined.")
            }

            updatedContainers - oldContainer + oldContainer.copy(imageSource = override.value)
        }

        return this.copy(containers = ContainerMap(updatedContainers))
    }
}
