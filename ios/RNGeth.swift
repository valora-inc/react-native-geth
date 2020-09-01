//
//  RNGeth.swift
//  RNGeth
//
//  Created by 0mkar on 04/04/18.
//

import Foundation
import CeloBlockchain

struct RuntimeError: LocalizedError {
    let message: String

    init(_ message: String) {
        self.message = message
    }

    public var errorDescription: String {
        return message
    }
}

@objc(RNGeth)
class RNGeth: RCTEventEmitter, GethNewHeadHandlerProtocol {
    private let ETH_DIR = ".ethereum"
    private let KEY_STORE_DIR = "keystore"
    private let DATA_DIR_PREFIX = NSHomeDirectory() + "/Documents"
    private let ctx: GethContext!
    private var runner: NodeRunner

    override init() {
        ctx = GethNewContext()
        runner = NodeRunner()
        super.init()
    }

    // MARK: Utils

    func convertToDictionary(from text: String) throws -> [String: String] {
        guard let data = text.data(using: .utf8) else { return [:] }
        let anyResult: Any = try JSONSerialization.jsonObject(with: data, options: [])
        return anyResult as? [String: String] ?? [:]
    }

    // MARK: RCTEventEmitter

    // Not yet sure we actually need main queue setup,
    // but do it for now to be on the safe side :D
    override static func requiresMainQueueSetup() -> Bool {
        return true
    }

    // Called when React Native is reloaded
    @objc func invalidate() {
        do {
            try runner.node?.stop()
        } catch {
            NSLog("Failed stopping geth node: \(error)")
        }
    }

    @objc(supportedEvents)
    override func supportedEvents() -> [String]! {
        return ["GethNewHead"]
    }

    // MARK: GethNewHeadHandlerProtocol

    func onError(_ failure: String?) {
        NSLog("@", failure!)
    }

    func onNewHead(_ header: GethHeader?) {
        guard bridge != nil else {
            // Don't call sendEvent when the bridge is not set
            // this happens when RN is reloaded and this module is unregistered
            return
        }

        do {
            var error: NSError?
            guard let json = header?.encodeJSON(&error) else {
                throw error ?? RuntimeError("Unable to encode geth header")
            }
            let dict = try convertToDictionary(from: json)
            sendEvent(withName: "GethNewHead", body:dict)
        } catch let NSErr as NSError {
            NSLog("@", NSErr)
        }
    }

    // MARK: Bridge methods

    /**
     * List accounts
     * @return Return All accounts from keystore
     */
    @objc(listAccounts:rejecter:)
    func listAccounts(resolve: RCTPromiseResolveBlock, rejecter reject: RCTPromiseRejectBlock) -> Void {
        do {
            var addresses: [String] = []
            let accounts = try runner.getAccounts()

            for i in 0..<accounts.size()  {
                if let address = try accounts.get(i).getAddress()?.getHex() {
                    addresses.append(address)
                }
            }

            resolve([addresses] as NSObject)
        } catch let NSErr as NSError {
            NSLog("@", NSErr)
            reject(nil, nil, NSErr)
        }
    }

    /**
     * Unlock an account with a passphrase
     *
     * @param account String account to unlock
     * @param passphrase String passphrase for the account
     * @param timeout Int64 duration of unlock period
     * @param promise Promise
     * @return Return Boolean the unlock status
     */
    @objc(unlockAccount:passphrase:timeout:resolver:rejecter:)
    func unlockAccount(accountAddress: String, passphrase: String, timeout: NSNumber, resolver resolve: RCTPromiseResolveBlock, rejecter reject: RCTPromiseRejectBlock) -> Void {
        do {
            let keyStore = try runner.getKeyStore()
            let account = try runner.findAccount(rawAddress: accountAddress)
            let _ = try keyStore.timedUnlock(account, passphrase: passphrase, timeout: timeout.int64Value)
            resolve([true] as NSObject)
        } catch let NSErr as NSError {
            NSLog("@", NSErr)
            reject(nil, nil, NSErr)
        }
    }

    /**
     * Add a new account to the keystore
     *
     * @param privateKey Data the private key to add
     * @param passphrase String the passphrase to lock it with
     * @param promise Promise
     * @return Account the new account
     */
    @objc(addAccount:passphrase:resolver:rejecter:)
    func addAccount(privateKeyBase64: String, passphrase: String, resolver resolve: RCTPromiseResolveBlock, rejecter reject: RCTPromiseRejectBlock) -> Void {
        do {
            guard let privateKey = Data(base64Encoded: privateKeyBase64) else {
                throw RuntimeError("Invalid base64 encoded private key")
            }
            let keyStore = try runner.getKeyStore()
            let account = try keyStore.importECDSAKey(privateKey, passphrase: passphrase)
            resolve([account.getAddress()?.getHex()] as NSObject)
        } catch let NSErr as NSError {
            NSLog("@", NSErr)
            reject(nil, nil, NSErr)
        }
    }

    func getHashSignature(hashBase64: String, signer: String, passphrase: String?) throws -> String {
        guard let hash = Data(base64Encoded: hashBase64) else {
            throw RuntimeError("Invalid base64 encoded hash")
        }
        let keyStore = try runner.getKeyStore()
        let signerAccount = try runner.findAccount(rawAddress: signer)
        let signature: Data
        if let passphrase = passphrase {
            signature = try keyStore.signHashPassphrase(signerAccount, passphrase: passphrase, hash: hash)
        } else {
            guard let signerAddress = signerAccount.getAddress() else {
                throw RuntimeError("Unable to get signer address")
            }
            signature = try keyStore.signHash(signerAddress, hash: hash)
        }
        return signature.base64EncodedString()
    }

    /**
     * Signs data using an unlocked account
     *
     * @param data String hex encoded input
     * @param signer String signer address
     * @param promise Promise
     * @return Return signed transaction
     */
    @objc(signHash:signer:resolver:rejecter:)
    func signHash(hashBase64: String, signer: String, resolver resolve: RCTPromiseResolveBlock, rejecter reject: RCTPromiseRejectBlock) -> Void {
        do {
            let signatureBase64 = try getHashSignature(hashBase64: hashBase64, signer: signer, passphrase: nil)
            resolve([signatureBase64] as NSObject)
        } catch let NSErr as NSError {
            NSLog("@", NSErr)
            reject(nil, nil, NSErr)
        }
    }


    /**
     * Signs data using with passphrase
     *
     * @param data String hex encoded input
     * @param signer String signer address
     * @param passphrase String the account passphrase
     * @param promise Promise
     * @return Return signed transaction
     */
    @objc(signHashPassphrase:signer:passphrase:resolver:rejecter:)
    func signHashPassphrase(hashBase64: String, signer: String, passphrase: String, resolver resolve: RCTPromiseResolveBlock, rejecter reject: RCTPromiseRejectBlock) -> Void {
        do {
            let signatureBase64 = try getHashSignature(hashBase64: hashBase64, signer: signer, passphrase: passphrase)
            resolve([signatureBase64] as NSObject)
        } catch let NSErr as NSError {
            NSLog("@", NSErr)
            reject(nil, nil, NSErr)
        }
    }

    func getSignedTx(txRLPBase64: String, signer: String, passphrase: String?) throws -> String {
        let keyStore = try runner.getKeyStore()
        guard let data = Data(base64Encoded: txRLPBase64) else {
            throw RuntimeError("Invalid base64 encoded transaction")
        }
        var error: NSError?
        guard let tx = GethNewTransactionFromRLP(data, &error) else {
            throw error ?? RuntimeError("Unable to create tx from RLP")
        }
        let signer = try runner.findAccount(rawAddress: signer)
        let chainID = GethNewBigInt(runner.nodeConfig?.ethereumNetworkID ?? 0)
        let signedTx: GethTransaction
        if let passphrase = passphrase {
            signedTx = try keyStore.signTxPassphrase(signer, passphrase: passphrase, tx: tx, chainID: chainID)
        } else {
            signedTx = try keyStore.signTx(signer, tx: tx, chainID: chainID)
        }
        let encodedTx = try signedTx.encodeRLP()
        return encodedTx.base64EncodedString()
    }

    /**
     * Signs a transaction using an unlocked account
     *
     * @param txRLP Data RLP encoded transaction
     * @param from Stirng signer address
     * @param promise Promise
     * @return Return signed transaction
     */
    @objc(signTransaction:signer:resolver:rejecter:)
    func signTransaction(txRLPBase64: String, signer: String, resolver resolve: RCTPromiseResolveBlock, rejecter reject: RCTPromiseRejectBlock) -> Void {
        do {
            let signedTxRLPBase64 = try getSignedTx(txRLPBase64: txRLPBase64, signer: signer, passphrase: nil)
            resolve([signedTxRLPBase64] as NSObject)
        } catch let NSErr as NSError {
            NSLog("@", NSErr)
            reject(nil, nil, NSErr)
        }
    }

    /**
     * Signs a transaction using a passphrase.
     *
     * @param txRLP Data RLP encoded transaction
     * @param from Stirng signer address
     * @param passphrase String passphrase
     * @param promise Promise
     * @return Return signed transaction
     */
    @objc(signTransactionPassphrase:signer:passphrase:resolver:rejecter:)
    func signTransactionPassphrase(txRLPBase64: String, signer: String, passphrase: String, resolver resolve: RCTPromiseResolveBlock, rejecter reject: RCTPromiseRejectBlock) -> Void {
        do {
            let signedTxRLPBase64 = try getSignedTx(txRLPBase64: txRLPBase64, signer: signer, passphrase: passphrase)
            resolve([signedTxRLPBase64] as NSObject)
        } catch let NSErr as NSError {
            NSLog("@", NSErr)
            reject(nil, nil, NSErr)
        }
    }

    /**
     * Creates and configures a new Geth node.
     *
     * @param config  Json object configuration node
     * @param promise Promise
     * @return Return true if created and configured node
     */
    @objc(setConfig:resolver:rejecter:)
    func setConfig(config: NSDictionary?, resolver resolve: RCTPromiseResolveBlock, rejecter reject: RCTPromiseRejectBlock) -> Void {
        if (runner.nodeStarted){
            NSLog("Node is already started, skipping creation")
            resolve([true] as NSObject)
            return;
        }

        do {
            guard let nodeconfig = GethNewNodeConfig() else {
                throw RuntimeError("Unable to create node config")
            }
            let nodeDir = (config?["nodeDir"] as? String) ?? ETH_DIR
            let keyStoreDir = (config?["keyStoreDir"] as? String) ?? KEY_STORE_DIR

            if let enodes = config?["enodes"] as? String {
                runner.writeStaticNodesFile(enodes: enodes)
            }
            if let networkID = config?["networkID"] as? Int64 {
                nodeconfig.ethereumNetworkID = networkID
            }
            if let maxPeers = config?["maxPeers"] as? Int {
                nodeconfig.maxPeers = maxPeers
            }
            if let genesis = config?["genesis"] as? String {
                nodeconfig.ethereumGenesis = genesis
            }
            if let syncMode = config?["syncMode"] as? Int {
                nodeconfig.syncMode = syncMode
            }
            if let useLightweightKDF = config?["useLightweightKDF"] as? Bool {
                nodeconfig.useLightweightKDF = useLightweightKDF
            }
            if let noDiscovery = config?["noDiscovery"] as? Bool {
                nodeconfig.noDiscovery = noDiscovery
            }
            if let bootnodeEnodes = config?["bootnodeEnodes"] as? [String] {
                guard let enodes = GethEnodes(bootnodeEnodes.count) else {
                    throw error ?? RuntimeError("Unable to create GethEnodes")
                }
                for i in 0..<bootnodeEnodes.count {
                    try enodes.set(i, enode: GethEnode(bootnodeEnodes[i]))
                }
                nodeconfig.bootstrapNodes = enodes
            }
            // HTTP RPC configurations, which should only be used for development & debugging
            if let httpHost = config?["httpHost"] as? String {
                // Workaround gomobile objc binding bug for properties starting with a capital letter in the go source
                // See https://github.com/golang/go/issues/32008
                // Once that bug is fixed the assertion will fail and we can switch back to:
                // nodeconfig.httpHost = httpHost
                nodeconfig.setValue(httpHost, forKey: "HTTPHost")
            }
            if let httpPort = config?["httpPort"] as? Int {
                // See comment for httpHost
                nodeconfig.setValue(httpPort, forKey: "HTTPPort")
            }
            if let httpVirtualHosts = config?["httpVirtualHosts"] as? String {
                // See comment for httpHost
                nodeconfig.setValue(httpVirtualHosts, forKey: "HTTPVirtualHosts")
            }
            if let httpModules = config?["httpModules"] as? String {
                // See comment for httpHost
                nodeconfig.setValue(httpModules, forKey: "HTTPModules")
            }
            if let ipcPath = config?["ipcPath"] as? String {
                // See comment for httpHost
                nodeconfig.setValue(ipcPath, forKey: "IPCPath")
                assert(nodeconfig.ipcPath == ipcPath)
            }
            if let logFile = config?["logFile"] as? String {
                var logLevel = 3  // Info
                if let logFileLogLevel = config?["logFileLogLevel"] as? Int {
                    logLevel = logFileLogLevel
                }
                GethSendLogsToFile(logFile, logLevel, "term")
            }

            let dataDir = DATA_DIR_PREFIX + "/" + nodeDir

            // Switch to dataDir if we're using a relative ipc path
            // This is to workaround the 104 chars path limit for unix domain socket
            if nodeconfig.ipcPath.first == "." {
                FileManager.default.changeCurrentDirectoryPath(dataDir)
            }

            var error: NSError?
            guard let node = GethNewNode(dataDir, nodeconfig, &error) else {
                throw error ?? RuntimeError("Unable to create geth node")
            }
            guard let keyStore = GethNewKeyStore(keyStoreDir, GethLightScryptN, GethLightScryptP) else {
                throw RuntimeError("Unable to create geth key store")
            }

            runner.nodeConfig = nodeconfig
            runner.keyStore = keyStore
            runner.node = node
            resolve([true] as NSObject)
        } catch let NCErr as NSError {
            NSLog("@", NCErr)
            reject(nil, nil, NCErr)
        }
    }

    /**
     * Start creates a live P2P node and starts running it.
     *
     * @param promise Promise
     * @return Return true if started.
     */
    @objc(startNode:rejecter:)
    func startNode(resolver resolve: RCTPromiseResolveBlock, rejecter reject: RCTPromiseRejectBlock) -> Void {
        do {
            var result = false
            if !runner.nodeStarted {
                try runner.getNode().start()
                runner.nodeStarted = true
                result = true
            }
            resolve([result] as NSObject)
        } catch let NSErr as NSError {
            NSLog("@", NSErr)
            reject(nil, nil, NSErr)
        }
    }

    @objc(subscribeNewHead:rejecter:)
    func subscribeNewHead(resolver resolve: RCTPromiseResolveBlock, rejecter reject: RCTPromiseRejectBlock) -> Void {
        do {
            try runner.getNode().getEthereumClient().subscribeNewHead(ctx, handler: self, buffer: 16)
            resolve([true] as NSObject)
        } catch let NSErr as NSError {
            NSLog("@", NSErr)
            reject(nil, nil, NSErr)
        }
    }

    /**
     * Terminates a running node along with all it's services.
     *
     * @param promise Promise
     * @return return true if stopped.
     */
    @objc(stopNode:rejecter:)
    func stopNode(resolve: RCTPromiseResolveBlock, rejecter reject: RCTPromiseRejectBlock) -> Void {
        do {
            var result = false
            if runner.nodeStarted {
                try runner.getNode().close()
                runner.nodeStarted = false
                result = true
            }
            resolve([result] as NSObject)
        } catch let NSErr as NSError {
            NSLog("@", NSErr)
            reject(nil, nil, NSErr)
        }
    }
}
