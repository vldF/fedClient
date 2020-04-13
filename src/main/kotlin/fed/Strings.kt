package fed

import java.io.File

val newLine: String = System.lineSeparator()

const val WITH_DESCRIPTION = "Open chat with user"

const val USER_DESCRIPTION = "Set the username. If server hasn't this username, new account will be created. " +
        "If server has this username, but you haven't config of that user, you can't use this account"

const val SERVER_DESCRIPTION = "Set server IP or domain. If port doesn't set, default will be used (35309)"

const val CONNECTION_TROUBLE_MESSAGE = "Connection error. Please, check your internet connection"