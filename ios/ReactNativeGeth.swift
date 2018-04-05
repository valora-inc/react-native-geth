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
    private var ETH_DIR: String = ".ethereum";
    private var KEY_STORE_DIR: String = "keystore";
    let ctx = GethNewContext()
    
    @objc(addEvent:location:date:)
    func addEvent(name: String, location: String, date: NSNumber) -> Void {
        NSLog("Adding new event...")
    }
}
