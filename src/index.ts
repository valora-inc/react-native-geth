import { NativeModules } from 'react-native'
import { GethNativeModule } from './GethNativeModule';
export { NodeConfig, GethNativeModule } from "./GethNativeModule";

export default NativeModules.RNGeth as GethNativeModule
