# React Native Geth

React Native Geth is released under the [MIT license](https://raw.githubusercontent.com/YsnKsy/react-native-geth/master/LICENSE.md)

## Description

## Initial Setup

```shell
$ npm i react-native-geth --save

$ react-native link react-native-geth
```
### Android Installation

The Geth module need to be initialized. In the MainApplication.java file, add the following:

```java
...
  @Override
  public void onCreate() {
    super.onCreate();

    // Initialise Geth module
    RNGethModule.init(); //  <= add this line

    SoLoader.init(this, /* native exopackage */ false);
  }
...
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
  "nodeDir": ".private-ethereum-", // --datadir / Data directory for the databases and keystore
  "keyStoreDir": "keystore", // --keystore / Directory for the keystore (default = inside the datadir)
  "enodes": "enode://8c544b4a07da02a9ee024def6f3ba24b2747272b64e16ec5dd6b17b55992f8980b77938155169d9d33807e501729ecb42f5c0a61018898c32799ced152e9f0d7@9[::]:30301" // --bootnodes / Comma separated enode URLs for P2P discovery bootstrap
};

async function Ethereum() {

  // Configure ethereum private light client node
  const init = await Geth.nodeConfig(config);

  if (init) {
    // Start node
    const start = await Geth.startNode();
  }
}

Ethereum();
```
