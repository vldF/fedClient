package fed

import java.io.File

val newLine: String = System.lineSeparator()
val slash: String = File.separator

const val withParameterDescriptor = "Open chat with user"

const val userParameterDescriptor = "Set the username. If server hasn't this username, new account will be created. " +
        "If server has this username, but you haven't config of that user, you can't use this account"

const val serverParameterDescription = "Set server IP or domain. If port doesn't set, default will be used (35309)"

const val connectionTrouble = "Connection error. Please, check your internet connection"