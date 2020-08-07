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
     * Creates and configures a new Geth node.
     *
     * @param config  Json object configuration node
     * @param promise Promise
     * @return Return true if created and configured node
     */
    @objc(nodeConfig:resolver:rejecter:)
    func nodeConfig(config: NSDictionary?, resolver resolve: RCTPromiseResolveBlock, rejecter reject: RCTPromiseRejectBlock) -> Void {
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
            if let httpHost = config?["httpHost"] as? String {
                nodeconfig.httpHost = httpHost
            }
            if let httpPort = config?["httpPort"] as? Int {
                nodeconfig.httpPort = httpPort
            }
            if let httpVirtualHosts = config?["httpVirtualHosts"] as? String {
                nodeconfig.httpVirtualHosts = httpVirtualHosts
            }
            if let httpModules = config?["httpModules"] as? String {
                nodeconfig.httpModules = httpModules
            }

            if let ipcPath = config?["ipcPath"] as? String {
                // Workaround gomobile objc binding bug for properties starting with a capital letter in the go source
                // See https://github.com/golang/go/issues/32008
                // Once that bug is fixed the assertion will fail and we can switch back to:
                // nodeconfig.ipcPath = ipcPath
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
    func stopNode(resolver resolve: RCTPromiseResolveBlock, rejecter reject: RCTPromiseRejectBlock) -> Void {
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
