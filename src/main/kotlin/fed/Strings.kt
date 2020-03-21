package fed

const val withParameterDescriptor = "Open chat with user. \n--with xXx_name_xXx"

const val userParameterDescriptor = "Set the username. If server hasn't this username, new account will be created. " +
        "If server has this username, but you haven't config of that user, you can't use this account \n--user xXx_name_xXx"

const val serverParameterDescription = "Set server IP or domain. If port doesn't set, default will be used (35309)\n" +
        "--server example.com:12345"