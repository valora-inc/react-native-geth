# cp android/src/main/java/com/reactnativegeth/RNGethModule.java /Users/ashishb/celo/celo-monorepo/packages/mobile/node_modules/react-native-geth/android/src/main/java/com/reactnativegeth/RNGethModule.java
# cp /Users/ashishb/celo/react-native-geth/src/geth.js /Users/ashishb/celo/celo-monorepo/packages/mobile/node_modules/react-native-geth/src/geth.js
# cp /Users/ashishb/celo/react-native-geth/src/types.js /Users/ashishb/celo/celo-monorepo/packages/mobile/node_modules/react-native-geth/src/types.js

SRC=/Users/ashishb/celo/react-native-geth
DST=/Users/ashishb/celo/celo-monorepo/packages/mobile/node_modules/react-native-geth
# rm -rf $DST
cp $SRC/src/geth.js  $DST/src/geth.js
cp $SRC/src/types.js $DST/src/types.js
cp {$SRC,$DST}/android/src/main/java/com/reactnativegeth/RNGethModule.java
cp {$SRC,$DST}/android/src/main/java/com/reactnativegeth/AndroidKeyStoreHelper.java
