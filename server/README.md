#Utilisation
## Rouler localement
1) ~npm install
2) ~npm start

##Rouler avec IPv4
1) modifier "this.server.listen(this.appPort);" par "this.server.listen(this.appPort as number, "0.0.0.0");"
2) ~npm install
3) ~npm start