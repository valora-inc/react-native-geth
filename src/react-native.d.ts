/**
 * Because react-native is a peer dependency we don't get or need any types for it.
 */
declare module "react-native" {
    export const NativeModules: any
}
