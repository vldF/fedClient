package fed.exceptions

open class ChatBaseException : Exception()

class WrongArgumentException : ChatBaseException()

class AccountErrorException : ChatBaseException()

class InternetConnectionException : ChatBaseException()