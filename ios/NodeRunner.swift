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
    var gethNode = [String: Any]()
    var node: GethNode?
    var ethClient: GethEthereumClient!
    var blockNumber: Int64!
    let datadir = NSSearchPathForDirectoriesInDomains(.documentDirectory, .userDomainMask, true)[0]
    let ctx = GethNewContext()
    private var nodeconf: GethNodeConfig?
    
    init() {
        self.nodeconf = GethNewNodeConfig()
    }
    
    func getNodeConfig() -> GethNodeConfig {
        return self.nodeconf!
    }
    
    func getNode() -> GethNode? {
        if(self.node != nil) {
            return self.node
        }
        return nil
    }
}
