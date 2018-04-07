//
//  NodeRunner.swift
//  ReactNativeGeth
//
//  Created by 0mkar on 06/04/18.
//  Copyright © 2018 Facebook. All rights reserved.
//

import Foundation
import Geth

class NodeRunner {
    private let ctx = GethNewContext()
    private var node: GethNode?
    private var nodeconf: GethNodeConfig?
    private var keyStore: GethKeyStore?
    
    init() {
        self.nodeconf = GethNewNodeConfig()
    }
    
    func getNodeConfig() -> GethNodeConfig? {
        return self.nodeconf
    }
    
    func getNode() -> GethNode? {
        return self.node
    }
    
    func setNode(node: GethNode) -> Void {
        self.node = node
    }
    
    func setNodeConfig(nc: GethNodeConfig) -> Void {
        self.nodeconf = nc
    }
    
    func setKeyStore(ks: GethKeyStore) -> Void {
        self.keyStore = ks
    }
}
