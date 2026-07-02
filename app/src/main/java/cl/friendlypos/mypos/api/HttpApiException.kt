package cl.friendlypos.mypos.api

class HttpApiException(message: String, val code: Int) : Exception(message)
