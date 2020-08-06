import { NodeConfig, Account } from './types';
import { GethNativeModule } from "./GethNativeModule";
export default class RNGeth {
    protected config: NodeConfig;
    geth: GethNativeModule;
    constructor(config: NodeConfig);
    /**
     * Start creates a live P2P node and starts running it.
     * @returns success status of operation
     */
    start(): Promise<boolean>;
    /**
     * Terminates a running node along with all it's services.
     * @returns success status of operation
     */
    stop(): Promise<boolean>;
    /**
     * Subscribes to notifications about the current blockchain head
     * @return true if subscribed
     */
    subscribeNewHead(): Promise<boolean>;
    /**
     * Add a new account
     * @param privateKey - the hex-encoded private key
     * @param passphrase - the passphrase used for the account
     * @returns the new account
     */
    addAccount(privateKey: string, passphrase: string): Promise<string>;
    /**
     * Returns all key files present in the directory.
     * @returns all accounts
     */
    listAccounts(): Promise<Account[]>;
    /**
     * Unlock an account
     * @param address - the address to unlock
     * @param passphrase - the passphrase of the account
     * @param timeout - unlock duration in seconds
     * @returns the unlocked status of the account
     */
    unlockAccount(account: string, passphrase: string, timeout: number): Promise<boolean>;
    /**
     * Sign a RLP-encoded transaction with the passphrase
     * @param txRLP - The RLP encoded transaction
     * @param signer - Address of the signer
     * @param  passphrase - The passphrase for the signer's account
     * @returns the signed transaction in RLP as a hex string
     */
    signTransactionPassphrase(txRLP: string, signer: string, passphrase: string): Promise<string>;
    /**
     * Sign a RLP-encoded transaction with an unlocked account
     * @param txRLP - The RLP encoded transaction
     * @param signer - Address of the signer
     * @returns the signed transaction in RLP as a hex string
     */
    signTransaction(txRLP: string, signer: string): Promise<string>;
}
