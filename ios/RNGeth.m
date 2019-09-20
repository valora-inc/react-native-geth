#import <React/RCTBridgeModule.h>
#import <Foundation/Foundation.h>
#import <React/RCTEventEmitter.h>

@interface RCT_EXTERN_REMAP_MODULE(RNGeth, RCTEventEmitter)
RCT_EXTERN_METHOD(nodeConfig:(NSDictionary*)config resolver: (RCTResponseSenderBlock)resolve rejecter:(RCTPromiseRejectBlock) reject)
RCT_EXTERN_METHOD(startNode:(RCTResponseSenderBlock)resolve rejecter:(RCTPromiseRejectBlock) reject)
RCT_EXTERN_METHOD(stopNode:(RCTResponseSenderBlock)resolve rejecter:(RCTPromiseRejectBlock) reject)
RCT_EXTERN_METHOD(subscribeNewHead:(RCTResponseSenderBlock)resolve rejecter:(RCTPromiseRejectBlock) reject)
@end
