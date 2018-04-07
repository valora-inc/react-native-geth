//
//  ReactNativeGeth.swift
//  ReactNativeGeth
//
//  Created by 0mkar on 04/04/18.
//  Copyright © 2018 Facebook. All rights reserved.
//

import Foundation
import Geth

@objc(ReactNativeGeth)
class ReactNativeGeth: NSObject {
    private var TAG: String = "Geth"
    private var ETH_DIR: String = ".ethereum"
    private var KEY_STORE_DIR: String = "keystore"
    private let ctx: GethContext
    private var geth_node: NodeRunner
    private var datadir = NSSearchPathForDirectoriesInDomains(.documentDirectory, .userDomainMask, true)[0]

    override init() {
        self.ctx = GethNewContext()
        self.geth_node = NodeRunner()
    }
    @objc(getName)
    func getName() -> String {
        return TAG
    }
    
    /**
     * Creates and configures a new Geth node.
     *
     * @param config  Json object configuration node
     * @param promise Promise
     * @return Return true if created and configured node
     */
    @objc(nodeConfig:resolver:rejecter:)
    func nodeConfig(config: NSObject, resolver resolve: RCTPromiseResolveBlock, rejecter reject: RCTPromiseRejectBlock) -> Void {
        do {
            let nodeconfig: GethNodeConfig = geth_node.getNodeConfig()!
            var nodeDir: String = ETH_DIR
            var keyStoreDir: String = KEY_STORE_DIR
            var error: NSError?
            
            if(config.value(forKey: "enodes") != nil) {
                // TODO: use static nodes from config
                //geth_node.writeStaticNodesFile(config.valueForKey("enodes"))
            }
            if((config.value(forKey: "chainID")) != nil) {
                nodeconfig.setEthereumNetworkID(config.value(forKey: "chainID") as! Int64)
            }
            if(config.value(forKey: "maxPeers") != nil) {
                nodeconfig.setMaxPeers(config.value(forKey: "maxPeers") as! Int)
            }
            if(config.value(forKey: "genesis") != nil) {
                nodeconfig.setEthereumGenesis(config.value(forKey: "genesis") as! String)
            }
            if(config.value(forKey: "nodeDir") != nil) {
                nodeDir = config.value(forKey: "nodeDir") as! String
            }
            if(config.value(forKey: "keyStoreDir") != nil) {
                keyStoreDir = config.value(forKey: "keyStoreDir") as! String
            }
            
            let node: GethNode = GethNewNode(datadir + nodeDir, nodeconfig, &error)
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
            reject(nil, nil, NSErr)
        }
    }
}
