/*
   Copyright 2017 Charles Korn.

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

package batect.logging

class Logger(private val sourceName: String, private val destination: LogSink) {
    fun debug(build: LogMessageBuilder.() -> LogMessageBuilder) = destination.write(Severity.DEBUG, additionalInfo, build)
    fun info(build: LogMessageBuilder.() -> LogMessageBuilder) = destination.write(Severity.INFO, additionalInfo, build)
    fun warn(build: LogMessageBuilder.() -> LogMessageBuilder) = destination.write(Severity.WARNING, additionalInfo, build)
    fun error(build: LogMessageBuilder.() -> LogMessageBuilder) = destination.write(Severity.ERROR, additionalInfo, build)

    private val additionalInfo = mapOf("@source" to sourceName)
}
