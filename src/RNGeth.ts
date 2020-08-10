import { NativeModules } from 'react-native'
import { NodeConfig, Account } from './types'
import { GethNativeModule } from "./GethNativeModule";

export class RNGeth {
  geth: GethNativeModule = NativeModules.RNGeth

  /**
   * Configure and prepare the node
   * @returns success status of operation
   */
  async setConfig(config: NodeConfig) {
    return this.geth.setConfig(config)
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
    const oneSecond = 1000000000 // in Go, time.Second
    return await this.geth.unlockAccount(account, passphrase, timeout * oneSecond)
  }

  /**
   * Sign a RLP-encoded transaction with the passphrase
   * @param txRLP - The RLP encoded transaction
   * @param signer - Address of the signer (can be locked)
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
   * @param signer - Address of the signer (must be unlocked)
   * @returns the signed transaction in RLP as a hex string
   */
  async signTransaction(txRLP: string, signer: string): Promise<string> {
    const signedTxRLP = await this.geth.signTransaction(txRLP, signer)
    return signedTxRLP.toLowerCase()
  }

  /**
   * Sign arbitrary data
   * @param hashHex - input to sign encoded as a hex string
   * @param signer - Address of the signer (must be unlocked)
   */
  async signHash(hashHex: string, signer: string): Promise<string> {
    return await this.geth.signHash(hashHex, signer)
  }

  /**
   * Sign arbitrary data with passphrase
   * @param hashHex - input to sign encoded as a hex string
   * @param signer - Address of the signer (can be locked)
   * @param passphrase - The passphrase for the signer's account
   */
  async signHashPassphrase(hashHex: string, signer: string, passphrase: string): Promise<string> {
    return this.geth.signHashPassphrase(hashHex, signer, passphrase)
  }
}

