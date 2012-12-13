Articles
1. Upgrading plugins.




################################# 1. Upgrading plugins ###############################
A. Use the following syntax for upgrading plugins from UC plugins. Similarly for other batch ( upgradetobetaplugins.bat  ) file except it will upgrade from UC betaplugins

1. upgradeplugins.bat               -- to view all the available customers folders
2. upgradeplugins.bat ksk           -- to view all the available plugins for the customer called ksk
3. upgradeplugins.bat ksk kskCorres -- to upgrade the kskCorres plugins for customer ksk
4. upgradeplugins.bat ksk *         -- to upgrade all the plugins on UC plugins for the customer ksk WITHOUT DELETING the extra plugins in your installation's plugin folder
5. upgradeplugins.bat ksk /         -- to upgrade all the plugins on UC plugins for the customer ksk AND DELETING the extra plugins in your installation's plugin folder            

B. The messages that will appear while you upgarde
Usage: upgradeplugins.bat <server-plugin-folder> <your-plugin-folder>"
NOTE: if your-plugin-folder is *, it will upgrade all your plugins without deleting other plugins.
If it is "/" (without quotes), it would upgrade all your plugins and also delete the other plugins which the server do not have.
Example upgradeplugins.bat ksk kskCorres

C. IF the upgarde process fails it will print the message
	Upgrade FAILED !!! Reason Given is %errorlevel%

D. And if succeeds .. then	
	The Upgrade process completed SUCCESSFULLY.

