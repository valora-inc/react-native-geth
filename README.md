# React Native Geth

React Native Geth is released under the [MIT license](https://raw.githubusercontent.com/YsnKsy/react-native-geth/master/LICENSE.md)

## Description

RNGeth makes using [Go-Ethereum](https://github.com/ethereum/go-ethereum) ( Official Go implementation of the Ethereum protocol ) with React Native simple. It supports Android platforms.

## Supported platforms

-   Android

## Initial Setup

```shell
$ npm i react-native-geth --save

$ react-native link react-native-geth
```

## JavaScript Usage ( private ethereum network )

```js
import Geth from 'react-native-geth';

// Network ID
const chainID = 17;

// genesis.json
const genesis = `{
  "config": {
    "chainId": ${chainID},
    "homesteadBlock": 0,
    "eip155Block": 0,
    "eip158Block": 0
  },
  "difficulty": "20",
  "gasLimit": "10000000",
  "alloc": {}
}`;

const config = {
  "chainID": chainID, // --networkid / Network identifier (integer, 0=Olympic (disused), 1=Frontier, 2=Morden (disused), 3=Ropsten) (default: 1)
  "maxPeers": 0, // --maxpeers / Maximum number of network peers (network disabled if set to 0) (default: 25)
  "genesis": genesis, // genesis.json file
  "nodeDir": ".private-ethereum", // --datadir / Data directory for the databases and keystore
  "keyStoreDir": "keystore", // --keystore / Directory for the keystore (default = inside the datadir)
  "enodes": "enode://8c544b4a07da02a9ee024def6f3ba24b2747272b64e16ec5dd6b17b55992f8980b77938155169d9d33807e501729ecb42f5c0a61018898c32799ced152e9f0d7@9[::]:30301" // --bootnodes / Comma separated enode URLs for P2P discovery bootstrap
};

async function Eth() {
  const geth = new Geth(config); // new Geth({}) for default network Ethereum
  const start = await geth.start(); // Start node

  if (start) {    
    const stop = await Geth.stopNode();
  }
}

Eth();
```
