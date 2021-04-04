# LadCoin
LadCoin is a restful blockchain API using java and spring boot , inspired from bitcoin

# About LadCoin

LadCoin is a java restful blockchain api .
this repo is the core api . Below links are wallet and miner program for LadCoin

https://github.com/zmilad97/LadCoin-Wallet

https://github.com/zmilad97/LadCoin-Miner

## Running LadCoin via Docker


## Running LadCoin localy

```
git clone https://github.com/zmilad97/LadCoin.git
cd LadCoin.git
./mvnw package
java -jar target/*.jar
```
You can then access LadCoin here: http://localhost:8181/
other nodes for getting chain can be run on : http://localhost:5050/ or http://localhost:4040/ (chain is null if you run theese for first time or shut all them
and running again)

Or you can run it from Maven directly using the Spring Boot Maven plugin. If you do this it will pick up changes that you make in the project immediately (changes to Java source files require a compile as well ):

```
./mvnw spring-boot:run
```
## using LadCoin
LadCoin is currently running on below nodes you can use your wallet or miner program to use it


----------------------------------------------------------------------------------------------------------------




