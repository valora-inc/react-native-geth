// @flow

import { NativeModules } from 'react-native'
import type {
  NodeConfig,
  Account,
  ListAccounts,
  SyncProgress,
  GethNativeModule
} from './types'

class Geth {
  config: ?NodeConfig
  geth: GethNativeModule = NativeModules.Geth

  constructor(config: NodeConfig): void {
    this.config = (config) ? config : {}
    this.geth.nodeConfig(this.config)
  }

  async start(): Promise<boolean> {
    return await this.geth.startNode()
  }

  async stop(): Promise<boolean> {
    return await this.geth.stopNode()
  }

  async newAccount(passphrase: string): Promise<Account> {
    return await this.geth.newAccount(passphrase)
  }

  async setAccount(
    accID: number): Promise<boolean> {
    return await this.geth.setAccount(accID)
  }

  async getAddress(): Promise<string> {
    return await this.geth.getAddress()
  }

  async balanceAccount(): Promise<string> {
    return await this.geth.balanceAccount()
  }

  async balanceAt(address: string): Promise<string> {
    return await this.geth.balanceAt(address)
  }

  async syncProgress(): Promise<SyncProgress> {
    return await this.geth.syncProgress()
  }

  async subscribeNewHead(): Promise<boolean> {
    return await this.geth.subscribeNewHead()
  }

  async updateAccount(oldPassphrase: string,
    newPassphrase: string): Promise<boolean> {
    return await this.geth.updateAccount(oldPassphrase, newPassphrase)
  }

  async deleteAccount(
    passphrase: string): Promise<boolean> {
    return await this.geth.deleteAccount(passphrase)
  }

  async exportKey(creationPassphrase: string,
    exportPassphrase: string): Promise<string> {
    return await this.geth.exportKey(creationPassphrase, exportPassphrase)
  }

  async importKey(key: string, oldPassphrase: string,
    newPassphrase: string): Promise<Account> {
    return await this.geth.importKey(key, oldPassphrase, newPassphrase)
  }

  async listAccounts(): Promise<ListAccounts> {
    return await this.geth.listAccounts()
  }

  async createAndSendTransaction(passphrase: string, nonce: number,
    toAddress: string, amount: number, gasLimit: number, gasPrice: number,
    data: string): Promise<string> {
    return await this.geth.createAndSendTransaction(passphrase, nonce,
      toAddress, amount, gasLimit, gasPrice, data)
  }

  async suggestGasPrice(): Promise<number> {
    return await this.geth.suggestGasPrice()
  }

  async getPendingNonce(): Promise<number> {
    return await this.geth.getPendingNonce()
  }
}

export default Geth
