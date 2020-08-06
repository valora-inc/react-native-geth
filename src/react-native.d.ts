/**
 * Because react-native is a peer dependency we don't get or need any types for it.
 * The only thing we want is to expose our interface for the Geth native module
 * which we have to manually keep in sync with what's implemented in the
 * platform sources.
 */
declare module "react-native" {
    import {GethNativeModule} from "./GethNativeModule";
    export const NativeModules: {
        RNGeth: GethNativeModule
    }
}