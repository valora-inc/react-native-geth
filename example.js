// @flow
/* eslint-disable no-console */

import Geth from './src/geth'

// Public Ethereum Network
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

// Private Ethereum Network
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
  "networkID": networkID, // --networkid / Network identifier (integer, 0=Olympic (disused), 1=Frontier, 2=Morden (disused), 3=Ropsten) (default: 1)
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

Eth() // run public node ethereum
PrivateEth() // run private node ethereum
