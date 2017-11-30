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

package io.igx.iot

import io.igx.iot.model.Column
import io.igx.iot.model.Report
import io.igx.iot.model.SensorData
import io.igx.iot.model.safeDouble
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*

/**
 * @author Vinicius Carvalho
 */
class EcoBeeParser(input: InputStream, basicHeadersLength: Int = 19) {
    var headers = mutableMapOf<String, List<Column>>()

    var memory: MutableMap<String,SensorData> = mutableMapOf()

    private val reader = BufferedReader(InputStreamReader(input))

    private val parser: CSVParser

    private lateinit var report: Report

    private var iterator: Iterator<CSVRecord>

    private val df = SimpleDateFormat("yyyy-MM-dd HH:mm:SS")

    init {
        var basicColumns = listOf(
                Column("desired_cool", 6),
                Column("desired_heat", 7),
                Column("humidity", 9),
                Column("cool", 12),
                Column("heat", 13),
                Column("temperature", 16),
                Column("motion", 18)
        )

        with(reader){
            var line = readLine()
            var id = line.split(",")[3]
            line = readLine()
            var name = line.split(",")[3]
            line = readLine()
            var startDate = LocalDate.parse(line.split(",")[3])
            line = readLine()
            var endDate = LocalDate.parse(line.split(",")[3])
            report = Report(name, id, startDate, endDate)
            readLine()
        }

        headers.put(report.device, basicColumns)
        headers.put("outdoor", listOf(Column("temperature", 10),
                Column("wind_speed", 11)
        ))

        parser = CSVParser(reader, CSVFormat.EXCEL.withFirstRecordAsHeader())

        val headerList = parser.headerMap.keys.toList()

        for(i in basicHeadersLength until headerList.size step 2){
            val name = headerList.get(i)
            headers.put(name.substring(0, name.lastIndexOf("(")).trim(), listOf(
                    Column("temperature", i),
                    Column("motion", i+1)
            ))
        }
        iterator = parser.iterator()

        headers.forEach{ (name, value) -> value.forEach{ column ->
            memory["${name}.${column.type}"] = SensorData(column.type, name, Date(), 0.0, report.id)
        }
        }

    }

    fun readLine() : List<SensorData> {

        var sensors = mutableListOf<SensorData>()
        if(iterator.hasNext()){
            val record = iterator.next()
            val date = df.parse(record.get(0) + " " + record.get(1))
            headers.entries.forEach{ entry ->
                entry.value.forEach { column ->
                    var read = if(record.get(column.position).safeDouble() == 0.0 && (column.type in "temperature | humidity") ) {
                        memory["${entry.key}.${column.type}"]!!.value
                    }else{
                        record.get(column.position).safeDouble()
                    }
                    sensors.add(SensorData(column.type, entry.key, date, read, report.id))
                }
            }
        }
        sensors.forEach{
            memory.put(it.name+"."+it.type, it)
        }
        return sensors
    }


    fun iterator() : SensorIterator {
        return SensorIterator()
    }

    inner class SensorIterator : Iterator<SensorData> {

        var cursor = 0

        var list = readLine()

        override fun hasNext(): Boolean {
            if(cursor >= list.size){
                cursor = 0
                list = readLine()
            }
            return list.isNotEmpty()
        }

        override fun next(): SensorData {
            if(!hasNext()){
                throw NoSuchElementException()
            }
            return list[cursor++]
        }

    }
}
