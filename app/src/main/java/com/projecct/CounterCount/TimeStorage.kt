package com.projecct.CounterCount

//Used to store the data about the time selected and operate on it
data class TimeStorage(var min:Int,var sec:Int) {

        override fun toString(): String {
            return  "$min m $sec s"
        }
    fun isTimeEmpty():Boolean{
        if(min == 0 && sec == 0){
            return true
        }
        return false
    }
    fun reset(){
        min=0
        sec=0
    }


}