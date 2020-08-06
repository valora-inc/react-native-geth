import { Account, NodeConfig } from "./types";
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
    nodeConfig: (config: NodeConfig) => Promise<boolean>;
    startNode: () => Promise<boolean>;
    stopNode: () => Promise<boolean>;
    subscribeNewHead: () => Promise<boolean>;
    signTransactionPassphrase: (txRLPHex: string, signer: string, passphrase: string) => Promise<string>;
    signTransaction: (txRLPHex: string, signer: string) => Promise<string>;
    addAccount: (privateKeyHex: string, passphrase: string) => Promise<string>;
    unlockAccount: (account: string, passphrase: string, timeout: number) => Promise<boolean>;
    listAccounts: () => Promise<Account[]>;
}
