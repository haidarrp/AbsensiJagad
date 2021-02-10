package org.d3ifcool.absensijagad


class Karyawan(val imageUrl:String, val desc:String, val user:String?, val date:String, val lat: Double?, val lng: Double?) {
    constructor() : this("","","","",null,null)
}