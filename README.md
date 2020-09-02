# React Native Geth

## Description

RNGeth implements a bridge between React Native and `celo-blockchain` (geth) in order to use the light client as part of React Native mobile applications.

## Supported platforms

-   Android
-   iOS

## Initial Setup

```shell
$ npm i react-native-geth --save

$ react-native link react-native-geth
```

## Usage

```typescript
import RNGeth, { NodeConfig } from 'react-native-geth';

// Network ID
const networkID = 1
// Chain ID
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

const config: NodeConfig = {
  "bootnodeEnodes": [ // --bootnodesv5 / Enodes of v5 bootnodes for p2p discovery
    "enode://XXXX@X[::]:XXXX",
    "enode://YYYY@Y[::]:YYYY"
  ],
  "networkID": networkID, // --networkid / Network identifier (integer, 0=Olympic (disused), 1=Frontier, 2=Morden (disused), 3=Ropsten) (default: 1)
  "maxPeers": 0, // --maxpeers / Maximum number of network peers (network disabled if set to 0) (default: 25)
  "genesis": genesis, // genesis.json file
  "nodeDir": ".private-ethereum", // --datadir / Data directory for the databases and keystore
  "keyStoreDir": "keystore", // --keystore / Directory for the keystore (default = inside the datadir)
  "enodes": "enode://XXXX@X[::]:XXXX" // static_nodes.json file. Comma separated enode URLs
  "noDiscovery": false, // --nodiscover / determines if the node will not participate in p2p discovery (v5)
  "syncMode": 5 // the number associated with a sync mode in `celo-blockchain/mobile/geth.go`
}

<<<<<<< HEAD
// Custom Ethereum Network
const PrivateEth = async () => {
  // Network ID
  const networkID = 1
  // Chain ID
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
    bootnodeEnodes: [ // --bootnodesv5 / Enodes of v5 bootnodes for p2p discovery
      'enode://XXXX@X[::]:XXXX',
      'enode://YYYY@Y[::]:YYYY'
    ],
    networkID: networkID, // --networkid / Network identifier (integer, 42220=mainnet, 62320=baklava (testnet), 44787=alfajores (testnet)) (default: 1)
    maxPeers: 0, // --maxpeers / Maximum number of network peers (network disabled if set to 0) (default: 25)
    genesis: genesis, // genesis.json file
    nodeDir: '.celo', // --datadir / Data directory for the databases and keystore
    keyStoreDir: 'keystore', // --keystore / Directory for the keystore (default = inside the datadir)
    enodes: 'enode://XXXX@X[::]:XXXX', // static_nodes.json file. Comma separated enode URLs
    noDiscovery: false, // --nodiscover / determines if the node will not participate in p2p discovery (v5)
    syncMode: 5, // the number associated with a sync mode in `celo-blockchain/mobile/geth.go`
    // HTTP RPC server - only intended for development & debugging
    httpHost: '127.0.0.1', // host of the server
    httpPort: 8545, // port the server will be created for
    httpVirtualHosts: '*', // comma separated string of allowed virtual hostnames for requests
    httpModules: ['rpc,txpool,admin,istanbul,les,net,web3,debug,eth'], // comma separated string of RPC API modules to expose
  }

  const geth = new Geth(config)
  // start node
=======
async function main() {
  const geth = new RNGeth()
  await geth.setConfig(config)
>>>>>>> 6beb2b396c9217813290957915db430bb5187e2d
  const start = await geth.start()

  if (start) {
    console.log('Start :', start)
    const stop = await geth.stop()
    console.log('Stop :', stop)
  }
}

main())
```

## Documentation :
### Table of Contents

-   [NodeConfig](#NodeConfig)
-   [RNGeth](#RNgeth)
    -   [setConfig](#setConfig)
    -   [start](#start)
    -   [stop](#stop)
    -   [addAccount](#addAccount)
    -   [listAccounts](#listAccounts)
    -   [unlockAccount](#unlockAccount)
    -   [signTransaction](#signTransaction)
    -   [signTransactionPassphrase](#signTransactionPassphrase)
    -   [signHash](#signHash)
    -   [signHashPassphrase](#signHashPassphrase)

## NodeConfig

The object that holds the config of the node consists of these fields:
-   `bootnodeEnodes` **[]string** Enode URLs for P2P discovery bootstrap
-   `enodes` **string** Comma separated enode URLs of static nodes
-   `genesis` **string** genesis.json file
-   `keyStoreDir` **string** Directory for the keystore (default = inside the datadir)
-   `logFile` **string** Path where to write geth logfile
-   `logFileLogLevel` **number** Log level when writing to file
-   `maxPeers` **number** Maximum number of network peers (network disabled if set to 0) (default: 25)
-   `networkID` **number** Network identifier
-   `noDiscovery` **boolean** Determines if the node will not participate in p2p discovery (v5)
-   `nodeDir` **string** Data directory for the databases and keystore
-   `syncMode` **number** The number associated with a sync mode in `celo-blockchain/mobile/geth.go`
-   `useLightweightKDF` **boolean** Enable Lightweight KDF

## RNGeth

#### Notes on binary data parameters

When dealing with blockchain accounts we're usually handling binary data in the hexadecimal format as a general convention of the ecosystem.
These can be private keys, hashes, or transactions encoded in RLP format.
The `celo-blockchain` (geth) library being more low-level expects byte arrays, but getting byte arrays accross the native bridge is troublesome.
As mentioned above a hexadecimal encoded string is the common way of passing around such data but our native environments are lacking in standard library support for parsing hex strings.
We could have added additional code for this, but it would increase our attack surface, it being hard(er) to test and maintain.
Therefore, we've decided to rely on Base64 encoding which is better supported by the native platforms.

This means in several places where we're passing binary data to the bridge `base64` is te preferred encoding. This is easily achieved on the javascript side with access to the `Buffer` type.

```typescript
const base64String = Buffer.from(hexString, 'hex').toString('base64');
```

### setConfig

**setConfig(config: NodeConfig): Promise<boolean>**

Configures the node and returns true on success, may throw errors.

### start

**start(): Promise<boolean>**

Start creates a live P2P node and starts running it.
Returns true if a node was started, false if node was already running and may throw errors.

### stop

**stop(): Promise<boolean>**

Terminates a running node along with all it's services.
Returns true if a node was stopped, false if node was not running and may throw errors.

### addAccount

**addAccount(privateKeyBase64: string, passphrase: string): Promise<address: string>**

Adds an account based on the private key and a passphrase.

**Parameters**

-   `privateKeyBase64` **string** Private Key encoded in base64
-   `passphrase` **string** Passphrase

Returns **Promise<string>** return the address of the created account

### unlockAccount

**unlockAccount(address: string, passphrase: string, timeout: number): Promise<address: string>**

Unlock an account with a passphrase for a set duration.

**Parameters**

-   `address` **string** The address of the account to unlock
-   `passphrase` **string** The passphrase that unlocks the account
-   `timeout` **number** The duration (in seconds) the account should remain unlocked

### subscribeNewHead

**subscribeNewhead(): Promise<boolean>**

Subscribes to notifications about the current blockchain head, returns true if successful, may throw error.

### listAccounts

**listAccounts(): Promise<string[]>**

Returns all account addresses managed by the key store.

### signTransaction

**signTransaction(txRLPBase64: string, signer: string): Promise<signedTxRLPBase64: string>**

Sign a transaction with a previously unlocked account.

**Parameters**:
- `txRLPBase64` - base64 encoded transaction in RLP format
- `signer` - the address of the signer (must be unlocked beforehand)

Returns the signed transaction in RLP format encoded as base64.

### signTransactionPassphrase

**signTransactionPassphrase(txRLPBase64: string, signer: string, passphrase: string): Promise<signedTxRLPBase64: string>**

Sign a transaction with a passphrase for the signer account.

**Parameters**:
- `txRLPBase64` - base64 encoded transaction in RLP format
- `signer` - the address of the signer
- `passphrase` - the passphrase to unlock the signer account

Returns the signed transaction in RLP format encoded as base64.

### signHash

**signHash(hashBase64: string, signer: string): Promise<signatureBase64: string>**

Sign a hash with a previously unlocked account.

**Parameters**:
- `hashBase64` - base64 encoded hash
- `signer` - the address of the signer (must be unlocked beforehand)

Returns the signature (binary) encoded as base64.

### signHashPassphrase

**signHashPassphrase(hashBase64: string, signer: string, passphrase: string): Promise<hashBase64: string>**

Sign a hash with a passphrase for the signer account.

**Parameters**:
- `hashBase64` - base64 encoded hash
- `signer` - the address of the signer
- `passphrase` - the passphrase to unlock the signer account

Returns the signature (binary) encoded as base64.

React Native Geth is released under the [MIT license](https://raw.githubusercontent.com/YsnKsy/react-native-geth/master/LICENSE.md)
