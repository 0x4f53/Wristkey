package com.owais.wristkey

data class Token(val tokenNumber: Int, val accountName: String, val code: String, val counter: String)