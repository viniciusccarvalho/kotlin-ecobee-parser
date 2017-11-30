/*
 *  Copyright 2017 original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package io.igx.iot.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate
import java.util.*


/**
 * @author Vinicius Carvalho
 */

data class SensorData(val type: String, val name: String, @JsonProperty("@timestamp") val timestamp: Date, val value: Double, val thermostatId: String)
data class Column(val type: String, val position: Int)
data class Report(val device: String, val id: String, val start: LocalDate, val end: LocalDate)


inline fun String.safeFloat() : Float {
    return try {
        this.toFloat()
    } catch (e: NumberFormatException){
        0.0f
    }
}

inline fun String.safeDouble() : Double {
    return try {
        this.toDouble()
    } catch (e: NumberFormatException){
        0.0
    }
}

inline fun String.safeInt() : Int {
    return try {
        this.toInt()
    } catch (e: NumberFormatException){
        0
    }
}

inline fun String.safeLong() : Long{
    return try {
        this.toLong()
    } catch (e: NumberFormatException){
        0L
    }
}
