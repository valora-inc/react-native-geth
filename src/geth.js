// @flow

import { Platform, NativeModules } from 'react-native'
import type {
  NodeConfig,
  Account,
  ListAccounts,
  SyncProgress,
  GethNativeModule
} from './types'

/**
 * Geth object
 * @param {Object} config
 * @param {number} config.networkID Network identifier (integer, 0=Olympic (disused), 1=Frontier, 2=Morden (disused), 3=Ropsten) (default: 1)
 * @param {number} config.maxPeers Maximum number of network peers (network disabled if set to 0) (default: 25)
 * @param {string} config.genesis genesis.json file
 * @param {string} config.nodeDir Data directory for the databases and keystore
 * @param {string} config.keyStoreDir Directory for the keystore (default = inside the datadir)
 * @param {string} config.enodes Comma separated enode URLs for P2P discovery bootstrap
 */
class Geth {
  config: ?NodeConfig
  geth: GethNativeModule = Platform.select({
      ios: NativeModules.ReactNativeGeth,
      android: NativeModules.Geth
  });

  constructor(config: NodeConfig): void {
    this.config = (config) ? config : {}
    this.geth.nodeConfig(this.config)
  }

  /**
 * Start creates a live P2P node and starts running it.
 * @return {Boolean} return true if started.
 */
  async start(): Promise<boolean> {
    return await this.geth.startNode()
  }

  /**
  * Terminates a running node along with all it's services.
  * @return {Boolean} return true if stopped.
  */
  async stop(): Promise<boolean> {
    return await this.geth.stopNode()
  }

  /**
  * Create a new account with the specified encryption passphrase.
  * @param {String} passphrase Passphrase
  * @return {Object} return new account object
  */
  async newAccount(passphrase: string): Promise<Account> {
    return await this.geth.newAccount(passphrase)
  }

  /**
  * Sets the default account at the given index in the listAccounts.
  * @param {Number} accID   index in the listAccounts
  * @return {Boolean} return true if sets.
  */
  async setAccount(accID: number): Promise<boolean> {
    return await this.geth.setAccount(accID)
  }

  /**
  * Retrieves the address associated with the current account.
  * @return {String} return address..
  */
  async getAddress(): Promise<string> {
    return await this.geth.getAddress()
  }

  /**
  * Returns the wei balance of the current account.
  * @return {String} return balance.
  */
  async balanceAccount(): Promise<string> {
    return await this.geth.balanceAccount()
  }

  /**
  * Returns the wei balance of the specified account.
  * @param {String} address Address of account being looked up.
  * @return {String} Return balance.
  */
  async balanceAt(address: string): Promise<string> {
    return await this.geth.balanceAt(address)
  }

  /**
  * Retrieves the current progress of the sync algorithm.
  * @return {Object} Return object sync progress or null
  */
  async syncProgress(): Promise<SyncProgress> {
    return await this.geth.syncProgress()
  }

  /**
  * Subscribes to notifications about the current blockchain head
  * @return {Boolean} Return true if subscribed
  */
  async subscribeNewHead(): Promise<boolean> {
    return await this.geth.subscribeNewHead()
  }

  /**
  * Changes the passphrase of current account.
  * @param {String} oldPassphrase Passphrase
  * @param {String} newPassphrase New passphrase
  * @return {Boolean} Return true if passphrase changed
  */
  async updateAccount(oldPassphrase: string,
    newPassphrase: string): Promise<boolean> {
    return await this.geth.updateAccount(oldPassphrase, newPassphrase)
  }

  /**
  * Deletes the key matched by current account if the passphrase is correct.
  * @return {Boolean} Return true if account deleted
  */
  async deleteAccount(
    passphrase: string): Promise<boolean> {
    return await this.geth.deleteAccount(passphrase)
  }

  /**
  * Exports as a JSON key of current account, encrypted with new passphrase.
  * @param {String} creationPassphrase Old Passphrase
  * @param {String} exportPassphrase New passphrase
  * @return {String} Return key
  */
  async exportKey(creationPassphrase: string,
    exportPassphrase: string): Promise<string> {
    return await this.geth.exportKey(creationPassphrase, exportPassphrase)
  }

  /**
  * Stores the given encrypted JSON key into the key directory.
  * @param {String} key Passphrase
  * @param {String} oldPassphrase Old passphrase
  * @param {String} newPassphrase New passphrase
  * @return {Object} Return account object
  */
  async importKey(key: string, oldPassphrase: string,
    newPassphrase: string): Promise<Account> {
    return await this.geth.importKey(key, oldPassphrase, newPassphrase)
  }

  /**
  * Returns all key files present in the directory.
  * @return {Array} Return array of accounts objects
  */
  async listAccounts(): Promise<ListAccounts> {
    return await this.geth.listAccounts()
  }

  /**
  * Create and send transaction.
  * @param {String} passphrase Passphrase
  * @param {Number} nonce      Account nonce (use -1 to use last known nonce)
  * @param {String} toAddress  Address destination
  * @param {Number} amount     Amount
  * @param {Number} gasLimit   Gas limit
  * @param {Number} gasPrice   Gas price
  * @param {Number} data
  * @return {String} Return transaction
  */
  async createAndSendTransaction(passphrase: string, nonce: number,
    toAddress: string, amount: number, gasLimit: number, gasPrice: number,
    data: string): Promise<string> {
    return await this.geth.createAndSendTransaction(passphrase, nonce,
      toAddress, amount, gasLimit, gasPrice, data)
  }

  /**
  * Retrieves the currently suggested gas price to allow a timely execution of a transaction.
  * @return {Double} Return suggested gas price
  */
  async suggestGasPrice(): Promise<number> {
    return await this.geth.suggestGasPrice()
  }

  /**
  * Retrieves this account's pending nonce. This is the nonce you should use when creating a transaction.
  * @return {Double} Return nonce
  */
  async getPendingNonce(): Promise<number> {
    return await this.geth.getPendingNonce()
  }

  async isDeviceSecure(): Promise<boolean> {
    return await this.geth.isDeviceSecure()
  }

  async makeDeviceSecure(message: string, actionButtonLabel: string) : Promise<boolean> {
    return await this.geth.makeDeviceSecure(message, actionButtonLabel)
  }
  

  async keystoreInit(
    keyName: string,
    reauthenticationTimeoutInSecs: number,
     invalidateKeyByNewBiometricEnrollment: boolean): Promise<boolean> {
       return await this.geth.keyStoreInit(keyName,
          reauthenticationTimeoutInSecs,
           invalidateKeyByNewBiometricEnrollment)
  }

  async storePin(
    keyName: string,
    pinValue: string): Promise<boolean> {
      return await this.geth.storePin(keyName, pinValue)
    }

  async retrievePin(keyName: string): Promise<string> {
    return await this.geth.retrievePin(keyName)
  }
}

export default Geth
