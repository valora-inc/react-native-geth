// @flow

import { NativeModules } from 'react-native'

type NodeConfigType = {
  chainID: ?number,
  maxPeers: ?number,
  genesis: ?string,
  nodeDir: ?string,
  keyStoreDir: ?string,
  enodes: ?string
}

type GethType = {
  nodeConfig: (config: ?NodeConfigType) => Promise<any>,
  startNode: () => Promise<any>,
  stopNode: () => Promise<any>,
  newAccount: (passphrase: string) => Promise<any>,
  setAccount: (accID: number) => Promise<any>,
  getAddress: () => Promise<any>,
  balanceAccount: () => Promise<any>,
  balanceAt: (address: string) => Promise<any>,
  syncProgress: () => Promise<any>,
  subscribeNewHead: () => Promise<any>,
  updateAccount: (oldPassphrase: string, newPassphrase: string) => Promise<any>,
  deleteAccount: (passphrase: string) => Promise<any>,
  exportKey: (creationPassphrase: string, exportPassphrase: string) => Promise<any>,
  importKey: (key: string, oldPassphrase: string, newPassphrase: string) => Promise<any>,
  listAccounts: () => Promise<any>,
  createAndSendTransaction: (passphrase: string, nonce: number, toAddress: string,
    amount: number, gasLimit: number, gasPrice: number, data: string) => Promise<any>,
  suggestGasPrice: () => Promise<any>,
  getPendingNonce: () => Promise<any>
}

class Geth {
  config: NodeConfigType
  geth: GethType = NativeModules.Geth

  constructor(config: NodeConfigType): void {
    this.config = config
  }

  async nodeConfig(): Promise<any> {
    return await this.geth.nodeConfig(this.config)
  }

  async start(): Promise<any> {
    return await this.geth.startNode()
  }

  async stop(): Promise<any> {
    return await this.geth.stopNode()
  }

  async newAccount(passphrase: string): Promise<any> {
    return await this.geth.newAccount(passphrase)
  }

  async setAccount(accID: number): Promise<any> {
    return await this.geth.setAccount(accID)
  }

  async getAddress(): Promise<any> {
    return await this.geth.getAddress()
  }

  async balanceAccount(): Promise<any> {
    return await this.geth.balanceAccount()
  }

  async balanceAt(address: string): Promise<any> {
    return await this.geth.balanceAt(address)
  }

  async syncProgress(): Promise<any> {
    return await this.geth.syncProgress()
  }

  async subscribeNewHead(): Promise<any> {
    return await this.geth.subscribeNewHead()
  }

  async updateAccount(oldPassphrase: string, newPassphrase: string): Promise<any> {
    return await this.geth.updateAccount(oldPassphrase, newPassphrase)
  }

  async deleteAccount(passphrase: string): Promise<any> {
    return await this.geth.deleteAccount(passphrase)
  }

  async exportKey(creationPassphrase: string, exportPassphrase: string): Promise<any> {
    return await this.geth.exportKey(creationPassphrase, exportPassphrase)
  }

  async importKey(key: string, oldPassphrase: string, newPassphrase: string): Promise<any> {
    return await this.geth.importKey(key, oldPassphrase, newPassphrase)
  }

  async listAccounts(): Promise<any> {
    return await this.geth.listAccounts()
  }

  async createAndSendTransaction(passphrase: string, nonce: number, toAddress: string,
    amount: number, gasLimit: number, gasPrice: number, data: string): Promise<any> {
    return await this.geth.createAndSendTransaction(passphrase, nonce, toAddress,
      amount, gasLimit, gasPrice, data)
  }

  async suggestGasPrice(): Promise<any> {
    return await this.geth.suggestGasPrice()
  }

  async getPendingNonce(): Promise<any> {
    return await this.geth.getPendingNonce()
  }
}

export default Geth
