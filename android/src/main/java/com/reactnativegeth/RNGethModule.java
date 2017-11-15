package com.reactnativegeth;

/**
 * Created by yaska on 17-09-29.
 */

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import org.ethereum.geth.Account;
import org.ethereum.geth.Accounts;
import org.ethereum.geth.Address;
import org.ethereum.geth.BigInt;
import org.ethereum.geth.Context;
import org.ethereum.geth.SyncProgress;
import org.ethereum.geth.Geth;
import org.ethereum.geth.KeyStore;
import org.ethereum.geth.Node;
import org.ethereum.geth.NodeConfig;
import org.ethereum.geth.NewHeadHandler;
import org.ethereum.geth.Header;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;

import android.util.Log;


public class RNGethModule extends ReactContextBaseJavaModule {
    private static final String TAG = "Geth";

    private static final String CONFIG_NODE_ERROR = "CONFIG_NODE_ERROR";
    private static final String START_NODE_ERROR = "START_NODE_ERROR";
    private static final String STOP_NODE_ERROR = "STOP_NODE_ERROR";
    private static final String NEW_ACCOUNT_ERROR = "NEW_ACCOUNT_ERROR";
    private static final String SET_ACCOUNT_ERROR = "SET_ACCOUNT_ERROR";
    private static final String GET_ACCOUNT_ERROR = "GET_ACCOUNT_ERROR";
    private static final String SYNC_PROGRESS_ERROR = "SYNC_PROGRESS_ERROR";
    private static final String SUBSCRIBE_NEW_HEAD_ERROR = "SUBSCRIBE_NEW_HEAD_ERROR";
    private static final String UPDATE_ACCOUNT_ERROR = "UPDATE_ACCOUNT_ERROR";
    private static final String DELETE_ACCOUNT_ERROR = "DELETE_ACCOUNT_ERROR";
    private static final String EXPORT_KEY_ERROR = "EXPORT_ACCOUNT_KEY_ERROR";
    private static final String IMPORT_KEY_ERROR = "IMPORT_ACCOUNT_KEY_ERROR";
    private static final String GET_ACCOUNTS_ERROR = "GET_ACCOUNTS_ERROR";
    private static final String ETH_DIR = ".ethereum";
    private static final String KEY_STORE_DIR = "keystore";
    private static final String STATIC_NODES_FILES_PATH = "/" + ETH_DIR + "/GethDroid/";
    private static final String STATIC_NODES_FILES_NAME = "static-nodes.json";

    private Node node;
    private static NodeConfig ndConfig;
    private Account account;
    private KeyStore keyStore;
    private ReactApplicationContext reactContext;

    public RNGethModule(ReactApplicationContext reactContext) {
        super(reactContext);

        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return TAG;
    }

    public static void init() {
        try {
            NodeConfig nc = new NodeConfig();
            setNodeConfig(nc);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private NodeConfig getNodeConfig() {
        return ndConfig;
    }

    private static void setNodeConfig(NodeConfig nc) {
        ndConfig = nc;
    }

    private Node getNode() {
        return node;
    }

    private void setNode(Node node) {
        this.node = node;
    }

    private Account getAccount() {
        return account;
    }

    private void setAccount(Account account) {
        this.account = account;
    }

    private KeyStore getKeyStore() {
        return keyStore;
    }

    private void setKeyStore(KeyStore keyStore) {
        this.keyStore = keyStore;
    }

    private void writeStaticNodesFile(String enodes) {
        try {
            File dir = new File(getReactApplicationContext().getFilesDir() + STATIC_NODES_FILES_PATH);
            if (dir.exists() == false) dir.mkdirs();
            File f = new File(dir, STATIC_NODES_FILES_NAME);
            if (f.exists() == false) {
                if(f.createNewFile() == true) {
                    WritableArray staticNodes = new WritableNativeArray();
                    staticNodes.pushString(enodes);
                    Writer output = new BufferedWriter(new FileWriter(f));
                    output.write(staticNodes.toString());
                    output.close();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @ReactMethod
    public void nodeConfig(ReadableMap config, Promise promise) {
        try {
            NodeConfig nc = this.getNodeConfig();
            String nodeDir = ETH_DIR;
            String keyStoreDir = KEY_STORE_DIR;

            if (config.hasKey("enodes")) this.writeStaticNodesFile(config.getString("enodes"));
            if (config.hasKey("chainID")) nc.setEthereumNetworkID(config.getInt("chainID"));
            if (config.hasKey("maxPeers")) nc.setMaxPeers(config.getInt("maxPeers"));
            if (config.hasKey("genesis")) nc.setEthereumGenesis(config.getString("genesis"));
            if (config.hasKey("nodeDir")) nodeDir = config.getString("nodeDir");
            if (config.hasKey("keyStoreDir")) keyStoreDir = config.getString("keyStoreDir");

            Node nd = Geth.newNode(getReactApplicationContext().getFilesDir() + "/" + nodeDir, nc);
            KeyStore ks = new KeyStore(getReactApplicationContext().getFilesDir() + "/" + keyStoreDir, Geth.LightScryptN, Geth.LightScryptP);

            setNodeConfig(nc);
            this.setKeyStore(ks);
            this.setNode(nd);

            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(CONFIG_NODE_ERROR, e);
        }
    }

    @ReactMethod
    public void startNode(Promise promise) {
        Boolean result = false;
        try {
            if(node != null) {
                this.getNode().start();
                result = true;
            }
            promise.resolve(result);
        } catch (Exception e) {
            promise.reject(START_NODE_ERROR, e);
        }
    }

    @ReactMethod
    public void stopNode(Promise promise) {
        Boolean result = false;
        try {
            if(this.node != null) {
                this.getNode().stop();
                result = true;
            }
            promise.resolve(result);
        } catch (Exception e) {
            promise.reject(STOP_NODE_ERROR, e);
        }
    }

    // Create a new account with the specified encryption passphrase.
    @ReactMethod
    public void newAccount(String pwd, Promise promise) {
        try {
            Account acc = this.getKeyStore().newAccount(pwd);
            WritableMap newAccount = new WritableNativeMap();
            newAccount.putString("address", acc.getAddress().getHex());
            newAccount.putDouble("account", this.getKeyStore().getAccounts().size() - 1);
            promise.resolve(newAccount);
        } catch (Exception e) {
            promise.reject(NEW_ACCOUNT_ERROR, e);
        }
    }

    @ReactMethod
    public void setAccount(Integer accId, Promise promise) {
        try {
            Accounts accounts = this.getKeyStore().getAccounts();
            Account acc = accounts.get(accId);
            this.setAccount(acc);
            //accounts.set(0, acc);
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(SET_ACCOUNT_ERROR, e);
        }
    }

    // return current account address
    @ReactMethod
    public void getAddress(Promise promise) {
        try {
            Account acc = this.getAccount();
            if( acc != null ) {
                Address address = acc.getAddress();
                promise.resolve(address.getHex());
            } else {
                promise.reject(GET_ACCOUNT_ERROR, "call method setAccount() before");
            }
        } catch (Exception e) {
            promise.reject(GET_ACCOUNT_ERROR, e);
        }
    }

    // return current account balance
    @ReactMethod
    public void balanceAccount(Promise promise) {
        try {
            Account acc = this.getAccount();
            if( acc != null ) {
                Context ctx = new Context();
                BigInt balanceAt = this.getNode().getEthereumClient().getBalanceAt(ctx, acc.getAddress(), -1);
                promise.resolve(balanceAt.toString());
            } else {
                promise.reject(GET_ACCOUNT_ERROR, "call method setAccount() before");
            }
        } catch (Exception e) {
            promise.reject(GET_ACCOUNT_ERROR, e);
        }
    }

    // Return sync progress
    @ReactMethod
    public void getSyncProgress(Promise promise) {
        try {
            Account acc = this.getAccount();
            if (acc != null) {
                Context ctx = new Context();
                SyncProgress sp = this.getNode().getEthereumClient().syncProgress(ctx);

                if (sp != null) {
                    WritableMap syncProgress = new WritableNativeMap();
                    syncProgress.putDouble("startingBlock", sp.getStartingBlock());
                    syncProgress.putDouble("currentBlock", sp.getCurrentBlock());
                    syncProgress.putDouble("highestBlock", sp.getHighestBlock());

                    promise.resolve(syncProgress);
                    return;
                }

                // Syncing has either not starter, or has already stopped.
                promise.resolve(null);
            } else {
                promise.reject(SYNC_PROGRESS_ERROR, "call method setAccount() before");
            }
        } catch (Exception e) {
            promise.reject(SYNC_PROGRESS_ERROR, e);
        }
    }

    // Start emitting GethNewHead events.
    @ReactMethod
    public void subscribeNewHead(Promise promise) {
        try {
            Account acc = this.getAccount();
            if (acc != null) {
                NewHeadHandler handler = new NewHeadHandler() {
                    @Override public void onError(String error) {
                        Log.e("GETH", "Error emitting new head event: " + error);
                    }
                    @Override public void onNewHead(final Header header) {
                        WritableMap headerMap = new WritableNativeMap();
                        WritableArray extraArray = new WritableNativeArray();

                        for (byte extraByte: header.getExtra()) {
                          extraArray.pushInt(extraByte);
                        }

                        headerMap.putString("parentHash", header.getParentHash().getHex());
                        headerMap.putString("uncleHash", header.getUncleHash().getHex());
                        headerMap.putString("coinbase", header.getCoinbase().getHex());
                        headerMap.putString("root", header.getRoot().getHex());
                        headerMap.putString("TxHash", header.getTxHash().getHex());
                        headerMap.putString("receiptHash", header.getReceiptHash().getHex());
                        headerMap.putString("bloom", header.getBloom().getHex());
                        headerMap.putDouble("difficulty", (double) header.getDifficulty().getInt64());
                        headerMap.putDouble("number", (double) header.getNumber());
                        headerMap.putDouble("gasLimit", (double) header.getGasLimit());
                        headerMap.putDouble("gasUsed", (double) header.getGasUsed());
                        headerMap.putDouble("time", (double) header.getTime());
                        headerMap.putString("mixDigest", header.getMixDigest().getHex());
                        headerMap.putString("nounce", header.getNonce().getHex());
                        headerMap.putString("hash", header.getHash().getHex());
                        headerMap.putArray("extra", extraArray);

                        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                            .emit("GethNewHead", headerMap);
                    }
                };

                Context ctx = new Context();
                this.getNode().getEthereumClient().subscribeNewHead(ctx, handler, 16);
                promise.resolve(true);
            } else {
                promise.reject(SUBSCRIBE_NEW_HEAD_ERROR, "call method setAccount() before");
            }
        } catch (Exception e) {
            promise.reject(SUBSCRIBE_NEW_HEAD_ERROR, e);
        }
    }
    
    // Update the passphrase of current account
    @ReactMethod
    public void updateAccount(String oldPassword, String newPassword, Promise promise) {
        try {
            Account acc = this.getAccount();
            if( acc != null ) {
                this.getKeyStore().updateAccount(acc, oldPassword, newPassword);
                promise.resolve(true);
            } else {
                promise.reject(UPDATE_ACCOUNT_ERROR, "call method setAccount() before");
            }
        } catch (Exception e) {
            promise.reject(UPDATE_ACCOUNT_ERROR, e);
        }
    }

    // Delete current account from the local keystore.
    @ReactMethod
    public void deleteAccount(String password, Promise promise) {
        try {
            Account acc = this.getAccount();
            if( acc != null ) {
                KeyStore ks = this.getKeyStore();
                ks.deleteAccount(acc, password);
                promise.resolve(true);
            } else {
                promise.reject(DELETE_ACCOUNT_ERROR, "call method setAccount('accountId') before");
            }
        } catch (Exception e) {
            promise.reject(DELETE_ACCOUNT_ERROR, e);
        }
    }

    // return byte
    @ReactMethod
    public void exportKey(String creationPassword, String exportPassword, Promise promise) {
        try {
            Account acc = this.getAccount();
            if( acc != null ) {
                KeyStore ks = this.getKeyStore();
                byte[] key = ks.exportKey(acc, creationPassword, exportPassword);
                promise.resolve(key);
            } else {
                promise.reject(EXPORT_KEY_ERROR, "call method setAccount('accountId') before");
            }
        } catch (Exception e) {
            promise.reject(EXPORT_KEY_ERROR, e);
        }
    }

    // return Account
    @ReactMethod
    public void importKey(byte[] key, String oldPassword, String newPassword, Promise promise) {
        try {
            KeyStore ks = this.getKeyStore();
            Account acc = ks.importKey(key, oldPassword, newPassword);
            Accounts accounts = ks.getAccounts();
            promise.resolve(acc);
        } catch (Exception e) {
            promise.reject(IMPORT_KEY_ERROR, e);
        }
    }

    @ReactMethod
    public void getAccounts(Promise promise) {
        try {
            Accounts accounts = this.getKeyStore().getAccounts();
            Long nb = accounts.size();
            WritableArray listAccounts = new WritableNativeArray();
            if( nb > 0 ) {
                for(long i=0; i<nb; i++){
                    Account acc = accounts.get(i);
                    Address address = acc.getAddress();
                    WritableMap resultAcc = new WritableNativeMap();
                    resultAcc.putString("address", address.getHex());
                    resultAcc.putDouble("account", i);
                    listAccounts.pushMap(resultAcc);
                }
            }
            promise.resolve(listAccounts);
        } catch (Exception e) {
            promise.reject(GET_ACCOUNTS_ERROR, e);
        }
    }

}

    /*
    // return Account
    @ReactMethod
    public void importECDSAKey(Byte account, String password, Promise promise) {
    }

    // return Account
    @ReactMethod
    public void importPreSaleKey(Byte account, String password, Promise promise) {
    }

    // return void
    @ReactMethod
    public void lock(String account, Promise promise) {
    }

    // return void
    @ReactMethod
    public void unlock(String account, String password, Promise promise) {
    }

    // return void
    @ReactMethod
    public void timedUnlock(String account, String password, String time, Promise promise) {
    }

    // return boolean
    @ReactMethod
    public void hasAddress(String account, Promise promise) {
    }
    */
