import { NativeModules } from 'react-native'
import { NodeConfig, Account } from './types'
import { GethNativeModule } from "./GethNativeModule";

export default class RNGeth {
  geth: GethNativeModule = NativeModules.RNGeth

  constructor(protected config: NodeConfig) {
    this.geth.nodeConfig(this.config)
  }

  /**
   * Start creates a live P2P node and starts running it.
   * @returns success status of operation
   */
  async start(): Promise<boolean> {
    return await this.geth.startNode()
  }

  /**
   * Terminates a running node along with all it's services.
   * @returns success status of operation
   */
  async stop(): Promise<boolean> {
    return await this.geth.stopNode()
  }

  /**
   * Subscribes to notifications about the current blockchain head
   * @return true if subscribed
   */
  async subscribeNewHead(): Promise<boolean> {
    return await this.geth.subscribeNewHead()
  }

  /**
   * Add a new account
   * @param privateKey - the hex-encoded private key
   * @param passphrase - the passphrase used for the account
   * @returns the new account
   */
  async addAccount(privateKey:string , passphrase: string): Promise<string> {
    return await this.geth.addAccount(privateKey, passphrase)
  }

  /**
   * Returns all key files present in the directory.
   * @returns all accounts
   */
  async listAccounts(): Promise<Account[]> {
    return await this.geth.listAccounts()
  }

  /**
   * Unlock an account
   * @param address - the address to unlock
   * @param passphrase - the passphrase of the account
   * @param timeout - unlock duration in seconds
   * @returns the unlocked status of the account
   */
  async unlockAccount(account: string, passphrase: string, timeout: number): Promise<boolean> {
    // In Go: time.Second = 1000000000
    return await this.geth.unlockAccount(account, passphrase, timeout * 1000000000)
  }

  /**
   * Sign a RLP-encoded transaction with the passphrase
   * @param txRLP - The RLP encoded transaction
   * @param signer - Address of the signer
   * @param  passphrase - The passphrase for the signer's account
   * @returns the signed transaction in RLP as a hex string
   */
  async signTransactionPassphrase(txRLP: string, signer: string, passphrase: string): Promise<string> {
    const signedTxRLP = await this.geth.signTransactionPassphrase(txRLP, signer, passphrase)
    return signedTxRLP.toLowerCase()
  }

  /**
   * Sign a RLP-encoded transaction with an unlocked account
   * @param txRLP - The RLP encoded transaction
   * @param signer - Address of the signer
   * @returns the signed transaction in RLP as a hex string
   */
  async signTransaction(txRLP: string, signer: string): Promise<string> {
    const signedTxRLP = await this.geth.signTransaction(txRLP, signer)
    return signedTxRLP.toLowerCase()
  }
}
