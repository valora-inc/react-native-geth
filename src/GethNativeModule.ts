/**
 * The configuration for a node
 */
export declare type NodeConfig = {
  bootnodeEnodes?: string[];
  enodes?: string;
  genesis?: string;
  httpHost?: string;
  httpModules?: string;
  httpPort?: number;
  httpVirtualHosts?: string;
  ipcPath?: string;
  keyStoreDir?: string;
  logFile?: string;
  logFileLogLevel?: number;
  maxPeers?: number;
  networkID?: number;
  noDiscovery?: boolean;
  nodeDir?: string;
  syncMode?: number;
  useLightweightKDF?: boolean;
};
/**
* GethNativeModule defines the interface for the native modules:
* iOS:     RNGeth.m (exposed from RNGeth.swift)
* Android: RNGethModule.java (marked with @ReactMethod)
*
* We currently have more methods implemented in Android then in iOS,
* but this interface should hold the lowest common denominator.
* We should extend this list as we implement/start using more.
*/
export interface GethNativeModule {
  /**
   * Configure and prepare the node
   * @returns success status of operation
   */
  setConfig: (config: NodeConfig) => Promise<boolean>;
  /**
   * Start creates a live P2P node and starts running it.
   * @returns success status of operation
   */
  startNode: () => Promise<boolean>;
  /**
   * Terminates a running node along with all it's services.
   * @returns success status of operation
   */
  stopNode: () => Promise<boolean>;
  /**
   * Subscribes to notifications about the current blockchain head
   * @return true if subscribed
   */
  subscribeNewHead: () => Promise<boolean>;
  /**
   * Sign a RLP-encoded transaction with the passphrase
   * @param txRLPBase64 - The RLP encoded transaction
   *                      (base64 is easier to decode in native)
   * @param signer - Address of the signer (can be locked)
   * @param  passphrase - The passphrase for the signer's account
   * @returns the signed transaction in RLP as a base64 string
   */
  signTransactionPassphrase: (txRLPBase64: string, signer: string, passphrase: string) => Promise<string>;
  /**
   * Sign a RLP-encoded transaction with an unlocked account
   * @param txRLPBase64 - The RLP encoded transaction in base64
   *                      (base64 is easier to decode in native)
   * @param signer - Address of the signer (must be unlocked)
   * @returns the signed transaction in RLP as a base64 string
   */
  signTransaction: (txRLPBase64: string, signer: string) => Promise<string>;
  /**
   * Sign arbitrary hash with passphrase
   * @param hashHex - input to sign encoded as a hex string
   * @param signer - Address of the signer (can be locked)
   * @param passphrase - The passphrase for the signer's account
   */
  signHashPassphrase: (hashBase64: string, signer: string, passphrase: string) => Promise<string>;
  /**
   * Sign arbitrary hash
   * @param hashHex - input to sign encoded as a hex string
   * @param signer - Address of the signer (must be unlocked)
   */
  signHash: (hashBase64: string, signer: string) => Promise<string>;
  /**
   * Add a new account
   * @param privateKey - the private key in base64
   * @param passphrase - the passphrase used for the account
   * @returns the new account
   */
  addAccount: (privateKeyBase64: string, passphrase: string) => Promise<string>;
  /**
   * Updates the passphrase of an account
   * @param address - the account to update
   * @param oldPassphrase - the passphrase currently associated with the account
   * @param newPassphrase - the new passphrase to use with the account
   * @returns whether the update was successful
   */
  updateAccount: (address: string, oldPassphrase: string, newPassphrase: string) => Promise<boolean>;
  /**
   * Unlock an account
   * @param address - the address to unlock
   * @param passphrase - the passphrase of the account
   * @param timeout - unlock duration in nanoseconds
   * @returns the unlocked status of the account
   */
  unlockAccount: (account: string, passphrase: string, timeout: number) => Promise<boolean>;
  /**
   * Returns all key files present in the directory.
   * @returns all accounts
   */
  listAccounts: () => Promise<string[]>;
  /**
   * Computes an ECDH shared secret between the user's private key and another user's public key
   * @param account - the address of the account
   * @param publicKey - another user's public key in base64
   * @returns the shared secret
   */
  computeSharedSecret: (account: string, publicKey: string) => Promise<string>;
  /**
   * Decrypts an ECIES ciphertext
   * @param account - the address of the account
   * @param ciphertext - the cipher to be decrypted
   * @returns the decrypted text
   */
  decrypt: (account: string, ciphertext: string) => Promise<string>;    

  /**
   * Returns all stats collected by the geth mobile module
   * @returns Map of <string, string> with all the metrics
   */
  getGethStats: () => Promise<Map<string, string>>,

  /**
   * Returns the node info
   * @returns Map of <string, string> with node info
   */
  getNodeInfo: () => Promise<Map<string, string>>,
  
  /**
   * Returns the connected peer's info 
   * @returns Array of Maps that represent each peer info
   */
  getPeerInfos: () => Promise<Array<Map<string, string>>>,
}
