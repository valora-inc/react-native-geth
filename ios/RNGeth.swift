//
//  RNGeth.swift
//  RNGeth
//
//  Created by 0mkar on 04/04/18.
//

import Foundation
import Geth

@objc(RNGeth)
class RNGeth: RCTEventEmitter, GethNewHeadHandlerProtocol {
    func onError(_ failure: String!) {
        NSLog("@", failure)
    }
    
    private var ETH_DIR: String = ".ethereum"
    private var KEY_STORE_DIR: String = "keystore"
    private let ctx: GethContext
    private var geth_node: NodeRunner
    private var datadir = NSHomeDirectory() + "/Documents"

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
    
    func onNewHead(_ header: GethHeader) {
        guard bridge != nil else {
            // Don't call sendEvent when the bridge is not set
            // this happens when RN is reloaded and this module is unregistered
            return
        }

        do {
            let json = try header.encodeJSON()
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
        do {
            let nodeconfig: GethNodeConfig = geth_node.getNodeConfig()!
            let nodeDir: String = (config?["nodeDir"] as? String) ?? ETH_DIR
            let keyStoreDir: String = (config?["keyStoreDir"] as? String) ?? KEY_STORE_DIR
            var error: NSError?
            
            if let enodes = config?["enodes"] as? String {
                geth_node.writeStaticNodesFile(enodes: enodes)
            }
            if let networkID = config?["networkID"] as? Int64 {
                nodeconfig.setEthereumNetworkID(networkID)
            }
            if let maxPeers = config?["maxPeers"] as? Int {
                nodeconfig.setMaxPeers(maxPeers)
            }
            if let genesis = config?["genesis"] as? String {
                nodeconfig.setEthereumGenesis(genesis)
            }
            if let syncMode = config?["syncMode"] as? Int {
                nodeconfig.setSyncMode(syncMode)
            }
            if let useLightweightKDF = config?["useLightweightKDF"] as? Bool {
                nodeconfig.setUseLightweightKDF(useLightweightKDF)
            }

            let node: GethNode = GethNewNode(datadir + "/" + nodeDir, nodeconfig, &error)
            let keyStore: GethKeyStore = GethNewKeyStore(keyStoreDir, GethLightScryptN, GethLightScryptP)
            if error != nil {
                reject(nil, nil, error)
                return
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
                try geth_node.getNode()?.stop()
                result = true
            }
            resolve([result] as NSObject)
        } catch let NSErr as NSError {
            NSLog("@", NSErr)
            reject(nil, nil, NSErr)
        }
    }
}
