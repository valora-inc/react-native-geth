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

extension Data {
    var bytes:[UInt8] { // fancy pretty call: myData.bytes -> [UInt8]
        return [UInt8](self)
    }

    init?(hexString: String) {
        let hexString = hexString.dropFirst(hexString.hasPrefix("0x") ? 2 : 0)
        let len = hexString.count / 2
        var data = Data(capacity: len)
        for i in 0..<len {
            let j = hexString.index(hexString.startIndex, offsetBy: i*2)
            let k = hexString.index(j, offsetBy: 2)
            let bytes = hexString[j..<k]
            if var num = UInt8(bytes, radix: 16) {
                data.append(&num, count: 1)
            } else {
                return nil
            }
        }
        self = data
    }

    func hex(prefixed isPrefixed:Bool = true) -> String {
        return self.bytes.reduce(isPrefixed ? "0x" : "") { $0 + String(format: "%02X", $1) }
    }
}

@objc(RNGeth)
class RNGeth: RCTEventEmitter, GethNewHeadHandlerProtocol {
    func onError(_ failure: String?) {
        NSLog("@", failure!)
    }

    private let ETH_DIR: String = ".ethereum"
    private let KEY_STORE_DIR: String = "keystore"
    private let DATA_DIR_PREFIX = NSHomeDirectory() + "/Documents"
    private let ctx: GethContext!
    private var geth_node: NodeRunner

    override init() {
        self.ctx = GethNewContext()
        self.geth_node = NodeRunner()
        super.init()
    }

    // Not yet sure we actually need main queue setup,
    // but do it for now to be on the safe side :D
    override static func requiresMainQueueSetup() -> Bool {
        return true
    }

    // Called when React Native is reloaded
    @objc func invalidate() {
        do {
            try geth_node.getNode()?.stop()
        } catch {
            NSLog("Failed stopping geth node: \(error)")
        }
    }

    @objc(supportedEvents)
    override func supportedEvents() -> [String]! {
        return ["GethNewHead"]
    }

    func convertToDictionary(from text: String) throws -> [String: String] {
        guard let data = text.data(using: .utf8) else { return [:] }
        let anyResult: Any = try JSONSerialization.jsonObject(with: data, options: [])
        return anyResult as? [String: String] ?? [:]
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
            let dict = try self.convertToDictionary(from: json)
            self.sendEvent(withName: "GethNewHead", body:dict)
        } catch let NSErr as NSError {
            NSLog("@", NSErr)
        }
    }

    /**
     * List accounts
     * @return Return All accounts from keystore
     */
    @objc(listAccounts:rejecter:)
    func listAccounts(resolve: RCTPromiseResolveBlock, rejecter reject: RCTPromiseRejectBlock) -> Void {
        do {
            guard let keyStore = geth_node.getKeyStore() else {
                throw RuntimeError("KeyStore not ready")
            }
            let accounts = keyStore.getAccounts()
            var addresses: [String] = []

            for i in 0...((accounts?.size() ?? 1)-1) {
                let address = try accounts?.get(i).getAddress()?.getHex()
                addresses.append(address!)
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
    func unlockAccount(account: NSString, passphrase: NSString, timeout: NSInteger, resolver resolve: RCTPromiseResolveBlock, rejecter reject: RCTPromiseRejectBlock) -> Void {
        do {
            guard let keyStore = geth_node.getKeyStore() else {
                throw RuntimeError("KeyStore not ready")
            }
            let account = try geth_node.findAccount(rawAddress: account as String)!
            let _ = try keyStore.timedUnlock(account, passphrase: passphrase as String, timeout: Int64(timeout))
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
    func addAccount(privateKeyHex: NSString, passphrase: NSString, resolver resolve: RCTPromiseResolveBlock, rejecter reject: RCTPromiseRejectBlock) -> Void {
        do {
            guard let keyStore = geth_node.getKeyStore() else {
                throw RuntimeError("KeyStore not ready")
            }
            let privateKey = Data(hexString: privateKeyHex as String)
            let account = try keyStore.importECDSAKey(privateKey, passphrase: passphrase as String)
            resolve([account.getAddress()?.getHex()] as NSObject)
        } catch let NSErr as NSError {
            NSLog("@", NSErr)
            reject(nil, nil, NSErr)
        }
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
    func signHash(data: NSString, signer: NSString, resolver resolve: RCTPromiseResolveBlock, rejecter reject: RCTPromiseRejectBlock) -> Void {
        do {
            guard let keyStore = geth_node.getKeyStore() else {
                throw RuntimeError("KeyStore not ready")
            }
            let data = Data(hexString: data as String)
            let signerAccount = try geth_node.findAccount(rawAddress: signer as String)!
            let signerAddress = signerAccount.getAddress()!
            let signature = try keyStore.signHash(signerAddress, hash: data)
            resolve([signature.hex(prefixed:true)] as NSObject)
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
    func signHashPassphrase(data: NSString, signer: NSString, passphrase: NSString, resolver resolve: RCTPromiseResolveBlock, rejecter reject: RCTPromiseRejectBlock) -> Void {
        do {
            guard let keyStore = geth_node.getKeyStore() else {
                throw RuntimeError("KeyStore not ready")
            }
            let data = Data(hexString: data as String)
            let signer = try geth_node.findAccount(rawAddress: signer as String)!
            let signature = try keyStore.signHashPassphrase(signer, passphrase: passphrase as String, hash: data)
            resolve([signature.hex(prefixed:true)] as NSObject)
        } catch let NSErr as NSError {
            NSLog("@", NSErr)
            reject(nil, nil, NSErr)
        }
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
    func signTransaction(txRLPHex: NSString, signer: NSString, resolver resolve: RCTPromiseResolveBlock, rejecter reject: RCTPromiseRejectBlock) -> Void {
        var error: NSError?
        do {
            guard let keyStore = geth_node.getKeyStore() else {
                throw RuntimeError("KeyStore not ready")
            }
            let txRLP = Data(hexString: txRLPHex as String)
            guard let tx = GethNewTransactionFromRLP(txRLP, &error) else {
                throw error ?? RuntimeError("Unable to create tx from RLP")
            }
            let signer = try geth_node.findAccount(rawAddress: signer as String)!
            let chainID = GethNewBigInt(geth_node.getNodeConfig()?.ethereumNetworkID ?? 0)
            let signedTx = try keyStore.signTx(signer, tx: tx, chainID: chainID)
            let encodedTx = try signedTx.encodeRLP()
            resolve([encodedTx.hex(prefixed:true)] as NSObject)
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
    func signTransactionPassphrase(txRLPHex: NSString, signer: NSString, passphrase: NSString, resolver resolve: RCTPromiseResolveBlock, rejecter reject: RCTPromiseRejectBlock) -> Void {
        var error: NSError?
        do {
            guard let keyStore = geth_node.getKeyStore() else {
                throw RuntimeError("KeyStore not ready")
            }
            let txRLP = Data(hexString: txRLPHex as String)
            guard let tx = GethNewTransactionFromRLP(txRLP, &error) else {
                throw error ?? RuntimeError("Unable to create tx from RLP")
            }
            let signer = try geth_node.findAccount(rawAddress: signer as String)!
            let chainID = GethNewBigInt(geth_node.getNodeConfig()?.ethereumNetworkID ?? 0)
            let signedTx = try keyStore.signTxPassphrase(signer, passphrase: passphrase as String, tx: tx, chainID: chainID)
            let encodedTx = try signedTx.encodeRLP()
            resolve([encodedTx.hex(prefixed:true)] as NSObject)
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
        if (geth_node.getNodeStarted()){
            NSLog("Node is already started, skipping creation");
            resolve([true] as NSObject)
            return;
        }
        
        do {
            let nodeconfig: GethNodeConfig = geth_node.getNodeConfig()!
            let nodeDir: String = (config?["nodeDir"] as? String) ?? ETH_DIR
            let keyStoreDir: String = (config?["keyStoreDir"] as? String) ?? KEY_STORE_DIR
            var error: NSError?
            
            if let enodes = config?["enodes"] as? String {
                geth_node.writeStaticNodesFile(enodes: enodes)
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
            if let ipcPath = config?["ipcPath"] as? String {
                // Workaround gomobile objc binding bug for properties starting with a capital letter in the go source
                // See https://github.com/golang/go/issues/32008
                // Once that bug is fixed the assertion will fail and we can switch back to:
                // nodeconfig.ipcPath = ipcPath
                nodeconfig.setValue(ipcPath, forKey: "IPCPath")
                assert(nodeconfig.ipcPath == ipcPath)
            }

            let dataDir = DATA_DIR_PREFIX + "/" + nodeDir

            // Switch to dataDir if we're using a relative ipc path
            // This is to workaround the 104 chars path limit for unix domain socket
            if nodeconfig.ipcPath.first == "." {
                FileManager.default.changeCurrentDirectoryPath(dataDir)
            }

            guard let node = GethNewNode(dataDir, nodeconfig, &error) else {
                throw error ?? RuntimeError("Unable to create geth node")
            }
            guard let keyStore = GethNewKeyStore(keyStoreDir, GethLightScryptN, GethLightScryptP) else {
                throw RuntimeError("Unable to create geth key store")
            }

            geth_node.setNodeConfig(nc: nodeconfig)
            geth_node.setKeyStore(ks: keyStore)
            geth_node.setNode(node: node)
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
            var result: Bool = false
            if(geth_node.getNode() != nil) {
                try geth_node.getNode()?.start()
                result = true
                geth_node.setNodeStarted(started: true)
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
            if(geth_node.getNode() != nil) {
                try geth_node.getNode()?.getEthereumClient().subscribeNewHead(self.ctx, handler: self, buffer: 16)
            }
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
            var result: Bool = false
            if(geth_node.getNode() != nil) {
                try geth_node.getNode()?.close()
                result = true
                geth_node.setNodeStarted(started: false)
            }
            resolve([result] as NSObject)
        } catch let NSErr as NSError {
            NSLog("@", NSErr)
            reject(nil, nil, NSErr)
        }
    }
}
