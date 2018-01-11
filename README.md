# React Native Geth

![Ethereum](https://geth.ethereum.org/static/images/ethereum.png)

## Description

RNGeth makes using [Go-Ethereum](https://github.com/ethereum/go-ethereum) ( Official Go implementation of the Ethereum protocol ) with React Native simple.

## Supported platforms

-   Android
-   iOS (Need PR)

## Initial Setup

```shell
$ npm i react-native-geth --save

$ react-native link react-native-geth
```

## JavaScript Usage

```js
import Geth from 'react-native-geth';

// Ethereum Network Frontier
const Eth = async () => {
  const geth = new Geth()
  // start node
  const start = await geth.start()

  if (start) {
    console.log('Start :', start)
    // stop node
    const stop = await geth.stop()
    console.log('Stop :', stop)
  }
}

// Custom Ethereum Network
const PrivateEth = async () => {
  // Network ID
  const chainID = 17
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
  }`

  const config = {
    "chainID": chainID, // --networkid / Network identifier (integer, 0=Olympic (disused), 1=Frontier, 2=Morden (disused), 3=Ropsten) (default: 1)
    "maxPeers": 0, // --maxpeers / Maximum number of network peers (network disabled if set to 0) (default: 25)
    "genesis": genesis, // genesis.json file
    "nodeDir": ".private-ethereum", // --datadir / Data directory for the databases and keystore
    "keyStoreDir": "keystore", // --keystore / Directory for the keystore (default = inside the datadir)
    "enodes": "enode://XXXX@X[::]:XXXX" // --bootnodes / Comma separated enode URLs for P2P discovery bootstrap
  }

  const geth = new Geth(config)
  // start node
  const start = await geth.start()

  if (start) {
    console.log('Start :', start)
    const stop = await geth.stop()
    console.log('Stop :', stop)
  }
}

```

## Documentation :
### Table of Contents

-   [Geth](#geth)
    -   [start](#start)
    -   [stop](#stop)
    -   [newAccount](#newaccount)
    -   [setAccount](#setaccount)
    -   [getAddress](#getaddress)
    -   [balanceAccount](#balanceaccount)
    -   [balanceAt](#balanceat)
    -   [syncProgress](#syncprogress)
    -   [subscribeNewHead](#subscribenewhead)
    -   [updateAccount](#updateaccount)
    -   [deleteAccount](#deleteaccount)
    -   [exportKey](#exportkey)
    -   [importKey](#importkey)
    -   [listAccounts](#listaccounts)
    -   [createAndSendTransaction](#createandsendtransaction)
    -   [suggestGasPrice](#suggestgasprice)
    -   [getPendingNonce](#getpendingnonce)

## Geth

Geth object

**Parameters**

-   `config` **[Object](https://developer.mozilla.org/docs/Web/JavaScript/Reference/Global_Objects/Object)**
    -   `config.chainID` **[number](https://developer.mozilla.org/docs/Web/JavaScript/Reference/Global_Objects/Number)** Network identifier (integer, 0=Olympic (disused), 1=Frontier, 2=Morden (disused), 3=Ropsten) (default: 1)
    -   `config.maxPeers` **[number](https://developer.mozilla.org/docs/Web/JavaScript/Reference/Global_Objects/Number)** Maximum number of network peers (network disabled if set to 0) (default: 25)
    -   `config.genesis` **[string](https://developer.mozilla.org/docs/Web/JavaScript/Reference/Global_Objects/String)** genesis.json file
    -   `config.nodeDir` **[string](https://developer.mozilla.org/docs/Web/JavaScript/Reference/Global_Objects/String)** Data directory for the databases and keystore
    -   `config.keyStoreDir` **[string](https://developer.mozilla.org/docs/Web/JavaScript/Reference/Global_Objects/String)** Directory for the keystore (default = inside the datadir)
    -   `config.enodes` **[string](https://developer.mozilla.org/docs/Web/JavaScript/Reference/Global_Objects/String)** Comma separated enode URLs for P2P discovery bootstrap

### start

Start creates a live P2P node and starts running it.

Returns **[Boolean](https://developer.mozilla.org/docs/Web/JavaScript/Reference/Global_Objects/Boolean)** return true if started.

### stop

Terminates a running node along with all it's services.

Returns **[Boolean](https://developer.mozilla.org/docs/Web/JavaScript/Reference/Global_Objects/Boolean)** return true if stopped.

### newAccount

Create a new account with the specified encryption passphrase.

**Parameters**

-   `passphrase` **[String](https://developer.mozilla.org/docs/Web/JavaScript/Reference/Global_Objects/String)** Passphrase

Returns **[Object](https://developer.mozilla.org/docs/Web/JavaScript/Reference/Global_Objects/Object)** return new account object

### setAccount

Sets the default account at the given index in the listAccounts.

**Parameters**

-   `accID` **[Number](https://developer.mozilla.org/docs/Web/JavaScript/Reference/Global_Objects/Number)** index in the listAccounts

Returns **[Boolean](https://developer.mozilla.org/docs/Web/JavaScript/Reference/Global_Objects/Boolean)** return true if sets.

### getAddress

Retrieves the address associated with the current account.

Returns **[String](https://developer.mozilla.org/docs/Web/JavaScript/Reference/Global_Objects/String)** return address..

### balanceAccount

Returns the wei balance of the current account.

Returns **[String](https://developer.mozilla.org/docs/Web/JavaScript/Reference/Global_Objects/String)** return balance.

### balanceAt

Returns the wei balance of the specified account.

**Parameters**

-   `address` **[String](https://developer.mozilla.org/docs/Web/JavaScript/Reference/Global_Objects/String)** Address of account being looked up.

Returns **[String](https://developer.mozilla.org/docs/Web/JavaScript/Reference/Global_Objects/String)** Return balance.

### syncProgress

Retrieves the current progress of the sync algorithm.

Returns **[Object](https://developer.mozilla.org/docs/Web/JavaScript/Reference/Global_Objects/Object)** Return object sync progress or null

### subscribeNewHead

Subscribes to notifications about the current blockchain head

Returns **[Boolean](https://developer.mozilla.org/docs/Web/JavaScript/Reference/Global_Objects/Boolean)** Return true if subscribed

### updateAccount

Changes the passphrase of current account.

**Parameters**

-   `oldPassphrase` **[String](https://developer.mozilla.org/docs/Web/JavaScript/Reference/Global_Objects/String)** Passphrase
-   `newPassphrase` **[String](https://developer.mozilla.org/docs/Web/JavaScript/Reference/Global_Objects/String)** New passphrase

Returns **[Boolean](https://developer.mozilla.org/docs/Web/JavaScript/Reference/Global_Objects/Boolean)** Return true if passphrase changed

### deleteAccount

Deletes the key matched by current account if the passphrase is correct.

**Parameters**

-   `passphrase` **[string](https://developer.mozilla.org/docs/Web/JavaScript/Reference/Global_Objects/String)**

Returns **[Boolean](https://developer.mozilla.org/docs/Web/JavaScript/Reference/Global_Objects/Boolean)** Return true if account deleted

### exportKey

Exports as a JSON key of current account, encrypted with new passphrase.

**Parameters**

-   `creationPassphrase` **[String](https://developer.mozilla.org/docs/Web/JavaScript/Reference/Global_Objects/String)** Old Passphrase
-   `exportPassphrase` **[String](https://developer.mozilla.org/docs/Web/JavaScript/Reference/Global_Objects/String)** New passphrase

Returns **[String](https://developer.mozilla.org/docs/Web/JavaScript/Reference/Global_Objects/String)** Return key

### importKey

Stores the given encrypted JSON key into the key directory.

**Parameters**

-   `key` **[String](https://developer.mozilla.org/docs/Web/JavaScript/Reference/Global_Objects/String)** Passphrase
-   `oldPassphrase` **[String](https://developer.mozilla.org/docs/Web/JavaScript/Reference/Global_Objects/String)** Old passphrase
-   `newPassphrase` **[String](https://developer.mozilla.org/docs/Web/JavaScript/Reference/Global_Objects/String)** New passphrase

Returns **[Object](https://developer.mozilla.org/docs/Web/JavaScript/Reference/Global_Objects/Object)** Return account object

### listAccounts

Returns all key files present in the directory.

Returns **[Array](https://developer.mozilla.org/docs/Web/JavaScript/Reference/Global_Objects/Array)** Return array of accounts objects

### createAndSendTransaction

Create and send transaction.

**Parameters**

-   `passphrase` **[String](https://developer.mozilla.org/docs/Web/JavaScript/Reference/Global_Objects/String)** Passphrase
-   `nonce` **[Number](https://developer.mozilla.org/docs/Web/JavaScript/Reference/Global_Objects/Number)** Account nonce (use -1 to use last known nonce)
-   `toAddress` **[String](https://developer.mozilla.org/docs/Web/JavaScript/Reference/Global_Objects/String)** Address destination
-   `amount` **[Number](https://developer.mozilla.org/docs/Web/JavaScript/Reference/Global_Objects/Number)** Amount
-   `gasLimit` **[Number](https://developer.mozilla.org/docs/Web/JavaScript/Reference/Global_Objects/Number)** Gas limit
-   `gasPrice` **[Number](https://developer.mozilla.org/docs/Web/JavaScript/Reference/Global_Objects/Number)** Gas price
-   `data` **[Number](https://developer.mozilla.org/docs/Web/JavaScript/Reference/Global_Objects/Number)**

Returns **[String](https://developer.mozilla.org/docs/Web/JavaScript/Reference/Global_Objects/String)** Return transaction

### suggestGasPrice

Retrieves the currently suggested gas price to allow a timely execution of a transaction.

Returns **Double** Return suggested gas price

### getPendingNonce

Retrieves this account's pending nonce. This is the nonce you should use when creating a transaction.

Returns **Double** Return nonce

---
React Native Geth is released under the [MIT license](https://raw.githubusercontent.com/YsnKsy/react-native-geth/master/LICENSE.md)
