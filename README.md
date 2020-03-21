# fedClient
Simple terminal client for [this server](https://github.com/vldF/fedServer).
# Screenshots
![image](https://i.ibb.co/sFtrkvn/image.png)
![image](https://i.ibb.co/DM03R99/image.png)
# Features
1. Sending messages using server
2. Creating new user
3. UI based on [Lanterna](https://github.com/mabe02/lanterna)
4. Password-less authentication (server-side generates password, not user-side)

# Command line parameters
```
--server (-s) server : Set server IP or domain. If port doesn't set, default
                        will be used (35309)
                        --server example.com:12345
 --user (-u) userName : Set the username. If server hasn't this username, new
                        account will be created. If server has this username,
                        but you haven't config of that user, you can't use this
                        account
                        --user xXx_name_xXx (default: )
 --with (-w) userName : Open chat with user. 
                        --with xXx_name_xXx
 --help (-h)          : Show this help
```

Task #2 for Peter the Great Polytechnic University, St. Petersburg
