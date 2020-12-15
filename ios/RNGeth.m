#import <React/RCTBridgeModule.h>
#import <Foundation/Foundation.h>
#import <React/RCTEventEmitter.h>

@interface RCT_EXTERN_MODULE(RNGeth, RCTEventEmitter)
RCT_EXTERN_METHOD(setConfig:(NSDictionary*)config resolver: (RCTResponseSenderBlock)resolve rejecter:(RCTPromiseRejectBlock) reject)
RCT_EXTERN_METHOD(startNode:(RCTResponseSenderBlock)resolve rejecter:(RCTPromiseRejectBlock) reject)
RCT_EXTERN_METHOD(stopNode:(RCTResponseSenderBlock)resolve rejecter:(RCTPromiseRejectBlock) reject)
RCT_EXTERN_METHOD(subscribeNewHead:(RCTResponseSenderBlock)resolve rejecter:(RCTPromiseRejectBlock) reject)
RCT_EXTERN_METHOD(addAccount:(NSString*)privateKeyBase64 passphrase: (NSString*)passphrase resolver: (RCTResponseSenderBlock)resolve rejecter:(RCTPromiseRejectBlock) reject)
RCT_EXTERN_METHOD(unlockAccount:(NSString*)account passphrase: (NSString*)passphrase timeout: (nonnull NSNumber*)timeout resolver: (RCTResponseSenderBlock)resolve rejecter:(RCTPromiseRejectBlock) reject)
RCT_EXTERN_METHOD(listAccounts:(RCTResponseSenderBlock)resolve rejecter:(RCTPromiseRejectBlock) reject)
RCT_EXTERN_METHOD(signTransactionPassphrase:(NSString*)txRLPBase64 signer: (NSString*)signer passphrase: (NSString*)passphrase resolver: (RCTResponseSenderBlock)resolve rejecter:(RCTPromiseRejectBlock) reject)
RCT_EXTERN_METHOD(signTransaction:(NSString*)txRLPBase64 signer: (NSString*)signer resolver: (RCTResponseSenderBlock)resolve rejecter:(RCTPromiseRejectBlock) reject)
RCT_EXTERN_METHOD(signHashPassphrase:(NSString*)hashBase64 signer: (NSString*)signer passphrase: (NSString*)passphrase resolver: (RCTResponseSenderBlock)resolve rejecter:(RCTPromiseRejectBlock) reject)
RCT_EXTERN_METHOD(signHash:(NSString*)hashBase64 signer: (NSString*)signer resolver: (RCTResponseSenderBlock)resolve rejecter:(RCTPromiseRejectBlock) reject)
RCT_EXTERN_METHOD(computeSharedSecret:(NSString*)account publicKey: (NSString*)publicKey resolver: (RCTResponseSenderBlock)resolve rejecter:(RCTPromiseRejectBlock) reject)
RCT_EXTERN_METHOD(decrypt:(NSString*)account ciphertext: (NSString*)ciphertext resolver: (RCTResponseSenderBlock)resolve rejecter:(RCTPromiseRejectBlock) reject)
@end
