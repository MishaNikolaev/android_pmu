package com.nmichail.android_pmu.data.dto

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(name = "Metall", strict = false)
class MetallResponse {
    @field:ElementList(entry = "Record", inline = true, required = false)
    var records: List<MetallRecordDto>? = null
}

@Root(name = "Record", strict = false)
class MetallRecordDto {
    @field:Attribute(name = "Code", required = false)
    var code: String? = null

    @field:Element(name = "Buy", required = false)
    var buy: String? = null
}