package com.pbaltazar.blindo.components.filters.entities.orderby

interface OrderByEnum {
    fun getName(): String
    fun associatedIds(): List<Int>
    fun getDirection(): OrderByDirection
}
