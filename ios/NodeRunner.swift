//
//  NodeRunner.swift
//  ReactNativeGeth
//
//  Created by 0mkar on 06/04/18.
//

import Foundation
import CeloBlockchain

class NodeRunner {
    enum FileError: Error {
        case RuntimeError(String)
    }
    
    private let DATA_DIR = NSHomeDirectory() + "/Documents"
    private let ETH_DIR = ".ethereum"
    private var STATIC_NODES_FILES_PATH: String
    private let STATIC_NODES_FILES_NAME = "static-nodes.json"
    
    var node: GethNode?
    var nodeConfig: GethNodeConfig?
    var keyStore: GethKeyStore?
    var nodeStarted = false
    
    init() {
        self.STATIC_NODES_FILES_PATH = self.DATA_DIR + "/" + self.ETH_DIR + "/iOSGeth"
    }
    
    func getNode() throws -> GethNode {
        guard let node = node else {
            throw RuntimeError("Node not ready")
        }
        
        return node
    }
    
    func getKeyStore() throws -> GethKeyStore {
        guard let keyStore = self.keyStore else {
            throw RuntimeError("KeyStore not ready")
        }
        
        return keyStore
    }
    
    func getAccounts() throws -> GethAccounts {
        guard let accounts = try getKeyStore().getAccounts() else {
            throw RuntimeError("Accounts not ready")
        }
        
        return accounts
    }
    
    func findAccount(rawAddress: String) throws -> GethAccount {
        var error: NSError?
        guard let address = GethNewAddressFromHex(rawAddress, &error) else {
            throw error ?? RuntimeError("Invalid address")
        }
        
        let accounts = try getAccounts()
        
        for i in 0..<accounts.size()  {
            let account = try accounts.get(i)
            if address.getHex() == account.getAddress()?.getHex() {
                return account
            }
        }
        
        throw RuntimeError("Unable to find account \(rawAddress)")
    }
    
    func writeStaticNodesFile(enodes: String) -> Void {
        do {
            var isDirectory = ObjCBool(true)
            let exists = FileManager.default.fileExists(atPath: STATIC_NODES_FILES_PATH, isDirectory: &isDirectory)
            if(exists == false) {
                try FileManager.default.createDirectory(atPath: STATIC_NODES_FILES_PATH, withIntermediateDirectories: false, attributes: nil)
            }
            
            let url = NSURL(fileURLWithPath: STATIC_NODES_FILES_PATH)
            if let pathComponent = url.appendingPathComponent(STATIC_NODES_FILES_NAME) {
                let filePath = pathComponent.path
                let fileManager = FileManager.default
                if fileManager.fileExists(atPath: filePath) {
                    try enodes.write(to: pathComponent, atomically: false, encoding: .utf8)
                } else {
                    let fileContent: Data = enodes.data(using: .utf8, allowLossyConversion: true)!
                    fileManager.createFile(atPath: filePath, contents: fileContent, attributes: nil)
                }
            } else {
                throw FileError.RuntimeError("File path not found")
            }
        } catch let writeErr as NSError {
            NSLog("@s", writeErr)
        }
    }
}
