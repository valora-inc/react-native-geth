package com.reactnativegeth;

/**
 * Created by yaska on 17-09-29.
 */

import android.util.Base64;
import android.util.Log;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
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
import org.ethereum.geth.Enode;
import org.ethereum.geth.Enodes;
import org.ethereum.geth.EthereumClient;
import org.ethereum.geth.Geth;
import org.ethereum.geth.Header;
import org.ethereum.geth.KeyStore;
import org.ethereum.geth.NewHeadHandler;
import org.ethereum.geth.Node;
import org.ethereum.geth.NodeConfig;
import org.ethereum.geth.SyncProgress;
import org.ethereum.geth.Transaction;
import org.ethereum.geth.NodeInfo;
import org.ethereum.geth.Strings;

import java.nio.Buffer;
import java.nio.ByteBuffer;

public class RNGethModule extends ReactContextBaseJavaModule {

    private static final String TAG = "RNGeth";
    private static final String CONFIG_NODE_ERROR = "CONFIG_NODE_ERROR";
    private static final String START_NODE_ERROR = "START_NODE_ERROR";
    private static final String STOP_NODE_ERROR = "STOP_NODE_ERROR";
    private static final String NEW_ACCOUNT_ERROR = "NEW_ACCOUNT_ERROR";
    private static final String UNLOCK_ACCOUNT_ERROR = "UNLOCK_ACCOUNT_ERROR";
    private static final String GET_ACCOUNT_ERROR = "GET_ACCOUNT_ERROR";
    private static final String ADD_ACCOUNT_ERROR = "ADD_ACCOUNT_ERROR";
    private static final String BALANCE_ACCOUNT_ERROR = "BALANCE_ACCOUNT_ERROR";
    private static final String BALANCE_AT_ERROR = "BALANCE_AT_ERROR";
    private static final String SYNC_PROGRESS_ERROR = "SYNC_PROGRESS_ERROR";
    private static final String SUBSCRIBE_NEW_HEAD_ERROR = "SUBSCRIBE_NEW_HEAD_ERROR";
    private static final String UPDATE_ACCOUNT_ERROR = "UPDATE_ACCOUNT_ERROR";
    private static final String DELETE_ACCOUNT_ERROR = "DELETE_ACCOUNT_ERROR";
    private static final String EXPORT_KEY_ERROR = "EXPORT_ACCOUNT_KEY_ERROR";
    private static final String IMPORT_KEY_ERROR = "IMPORT_ACCOUNT_KEY_ERROR";
    private static final String GET_ACCOUNTS_ERROR = "GET_ACCOUNTS_ERROR";
    private static final String GET_NONCE_ERROR = "GET_NONCE_ERROR";
    private static final String SUGGEST_GAS_PRICE_ERROR = "SUGGEST_GAS_PRICE_ERROR";
    private static final String SIGN_TRANSACTION_ERROR = "SIGN_TRANSACTION_ERROR";
    private static final String SIGN_TRANSACTION_PASSPHRASE_ERROR = "SIGN_TRANSACTION_PASSPHRASE_ERROR";
    private static final String SIGN_HASH_ERROR = "SIGN_HASH_ERROR";
    private static final String SIGN_HASH_PASSPHRASE_ERROR = "SIGN_HASH_PASSPHRASE_ERROR";
    private static final String ETH_DIR = ".ethereum";
    private static final String KEY_STORE_DIR = "keystore";
    private static final String COMPUTE_SHARED_SECRET_ERROR = "COMPUTE_SHARED_SECRET_ERROR";
    private static final String DECRYPT_ERROR = "DECRYPT_ERROR";

    private GethHolder gethHolder;

    public RNGethModule(ReactApplicationContext reactContext, GethHolder gethHolder) {
        super(reactContext);
        this.gethHolder = gethHolder;
    }

    @Override
    public String getName() {
        return TAG;
    }

    /**
     * Creates and configures a new Geth node.
     *
     * @param config  Json object configuration node
     * @param promise Promise
     * @return Return true if created and configured node
     */
    @ReactMethod
    public void setConfig(ReadableMap config, Promise promise) {
        if (gethHolder.getNodeStarted() == true) {
            Log.w(TAG, "RNGeth already has a node *started*, skipping creation of a new one");
            promise.resolve(true);
            return;
        }

        try {
            Log.i(TAG, "Configuring node config");
            NodeConfig nc = new NodeConfig();
            String nodeDir = ETH_DIR;
            String keyStoreDir = KEY_STORE_DIR;
            if (config.hasKey("enodes"))
                gethHolder.writeStaticNodesFile(config.getString("enodes"));
            if (config.hasKey("networkID")) nc.setEthereumNetworkID(config.getInt("networkID"));
            if (config.hasKey("maxPeers")) nc.setMaxPeers(config.getInt("maxPeers"));
            if (config.hasKey("genesis")) nc.setEthereumGenesis(config.getString("genesis"));
            if (config.hasKey("nodeDir")) nodeDir = config.getString("nodeDir");
            if (config.hasKey("keyStoreDir")) keyStoreDir = config.getString("keyStoreDir");
            if (config.hasKey("syncMode")) nc.setSyncMode(config.getInt("syncMode"));
            if (config.hasKey("useLightweightKDF")) nc.setUseLightweightKDF(config.getBoolean("useLightweightKDF"));
            if (config.hasKey("noDiscovery")) nc.setNoDiscovery(config.getBoolean("noDiscovery"));
            if (config.hasKey("bootnodeEnodes")) {
                ReadableArray bootnodeEnodes = config.getArray("bootnodeEnodes");
                int enodesSize = bootnodeEnodes.size();
                Enodes enodes = new Enodes(enodesSize);
                for (int i = 0; i < enodesSize; i++) {
                    Enode enode = new Enode(bootnodeEnodes.getString(i));
                    enodes.set(i, enode);
                }
                nc.setBootstrapNodes(enodes);
            }
            // HTTP RPC configurations, which should only be used for development & debugging
            if (config.hasKey("httpHost")) nc.setHTTPHost(config.getString("httpHost"));
            if (config.hasKey("httpPort")) nc.setHTTPPort(config.getInt("httpPort"));
            if (config.hasKey("httpVirtualHosts")) nc.setHTTPVirtualHosts(config.getString("httpVirtualHosts"));
            if (config.hasKey("httpModules")) nc.setHTTPModules(config.getString("httpModules"));

            if (config.hasKey("ipcPath")) nc.setIPCPath(config.getString("ipcPath"));
            if (config.hasKey("logFile")) {
                String logFileName = config.getString("logFile");
                int logLevel = 3;  // Info
                if (config.hasKey("logFileLogLevel")) {
                    logLevel = config.getInt("logFileLogLevel");
                }
                Geth.sendLogsToFile(logFileName, logLevel, "term");
            }

            Log.i(TAG, "Making a new Geth Node");
            String dataDir = getReactApplicationContext().getFilesDir() + "/" + nodeDir;
            Node nd = Geth.newNode(dataDir, nc);
            KeyStore ks = new KeyStore(dataDir + "/" + keyStoreDir, Geth.LightScryptN, Geth.LightScryptP);
            gethHolder.setNodeConfig(nc);
            gethHolder.setKeyStore(ks);
            gethHolder.setNode(nd);
            Log.i(TAG, "Done creating and configuring node");
            promise.resolve(true);
        } catch (Exception e) {
            e.printStackTrace();
            promise.reject(CONFIG_NODE_ERROR, e);
        }
    }

    /**
     * Start creates a live P2P node and starts running it.
     *
     * @param promise Promise
     * @return Return true if started.
     */
    @ReactMethod
    public void startNode(Promise promise) {
        Boolean result = false;
        try {
            if (gethHolder.getNodeStarted() == false) {
                Log.i(TAG, "Starting node");
                gethHolder.getNode().start();
                gethHolder.setNodeStarted(true);
                result = true;
            }
            promise.resolve(result);
        } catch (Exception e) {
            e.printStackTrace();
            promise.reject(START_NODE_ERROR, e);
        }
    }

    /**
     * Terminates a running node along with all it's services.
     *
     * @param promise Promise
     * @return return true if stopped.
     */
    @ReactMethod
    public void stopNode(Promise promise) {
        Boolean result = false;
        try {
            if (gethHolder.getNodeStarted() == true) {
                Log.i(TAG, "Stopping node");
                gethHolder.getNode().close();
                gethHolder.setNodeStarted(false);
                result = true;
            }
            promise.resolve(result);
        } catch (Exception e) {
            promise.reject(STOP_NODE_ERROR, e);
        }
    }

    /**
     * Create a new account with the specified encryption passphrase.
     *
     * @param passphrase Passphrase
     * @param promise    Promise
     * @return return new account object.
     */
    @ReactMethod
    public void newAccount(String passphrase, Promise promise) {
        try {
            Account acc = gethHolder.getKeyStore().newAccount(passphrase);
            WritableMap newAccount = new WritableNativeMap();
            newAccount.putString("address", acc.getAddress().getHex());
            newAccount.putDouble("account", gethHolder.getKeyStore().getAccounts().size() - 1);
            promise.resolve(newAccount);
        } catch (Exception e) {
            promise.reject(NEW_ACCOUNT_ERROR, e);
        }
    }

    /**
     * Sets the default account at the given index in the listAccounts.
     *
     * @param privateKeyBase64 Base64 encoded private key
     * @param passphrase The passphrase to use with this new account
     * @param promise Promise
     * @return Return the address of the newly added account
     */
    @ReactMethod
    public void addAccount(String privateKeyBase64, String passphrase, Promise promise) {
        try {
            byte[] privateKey = Base64.decode(privateKeyBase64, Base64.DEFAULT);
            Account account = gethHolder.getKeyStore().importECDSAKey(privateKey, passphrase);
            promise.resolve(account.getAddress().getHex());
        } catch (Exception e) {
            promise.reject(ADD_ACCOUNT_ERROR, e);
        }
    }

    /**
     * Unlock an account with the passphrase provided.
     *
     * @param address The address to unlock
     * @param passphrase The passphrase used to unlock
     * @param promise Promise
     * @return Return the address of the newly added account
     */
    @ReactMethod
    public void unlockAccount(String address, String passphrase, Double timeout, Promise promise) {
        try {
            Account account = gethHolder.findAccount(address);
            this.gethHolder.getKeyStore().timedUnlock(account, passphrase, timeout.longValue());
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(UNLOCK_ACCOUNT_ERROR, e);
        }
    }

    /**
     * Retrieves the address associated with the current account.
     *
     * @param promise Promise
     * @return Return string address.
     */
    @ReactMethod
    public void getAddress(Promise promise) {
        try {
            Account acc = gethHolder.getAccount();
            if (acc != null) {
                Address address = acc.getAddress();
                promise.resolve(address.getHex());
            } else {
                promise.reject(GET_ACCOUNT_ERROR, "call method setAccount() before");
            }
        } catch (Exception e) {
            promise.reject(GET_ACCOUNT_ERROR, e);
        }
    }

    /**
     * Returns the wei balance of the current account.
     *
     * @param promise Promise
     * @return Return String balance.
     */
    @ReactMethod
    public void balanceAccount(Promise promise) {
        try {
            Account acc = gethHolder.getAccount();
            if (acc != null) {
                Context ctx = new Context();
                BigInt balance = gethHolder.getNode().getEthereumClient()
                        .getBalanceAt(ctx, acc.getAddress(), -1);
                promise.resolve(balance.toString());
            } else {
                promise.reject(BALANCE_ACCOUNT_ERROR, "call method setAccount() before");
            }
        } catch (Exception e) {
            promise.reject(BALANCE_ACCOUNT_ERROR, e);
        }
    }

    /**
     * Returns the wei balance of the specified account.
     *
     * @param address Address of account being looked up.
     * @param promise Promise
     * @return Return String balance.
     */
    @ReactMethod
    public void balanceAt(String address, Promise promise) {
        try {
            Context ctx = new Context();
            BigInt balance = gethHolder.getNode().getEthereumClient()
                    .getBalanceAt(ctx, new Address(address), -1);
            promise.resolve(balance.toString());
        } catch (Exception e) {
            promise.reject(BALANCE_AT_ERROR, e);
        }
    }

    /**
     * SyncProgress retrieves the current progress of the sync algorithm.
     * - Break change : getSyncProgress ==> syncProgress -
     *
     * @param promise Promise
     * @return Return object sync progress or null
     */
    @ReactMethod
    public void syncProgress(Promise promise) {
        try {
            Context ctx = new Context();
            SyncProgress sp = gethHolder.getNode().getEthereumClient().syncProgress(ctx);
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
        } catch (Exception e) {
            promise.reject(SYNC_PROGRESS_ERROR, e);
        }
    }

    /**
     * Subscribes to notifications about the current blockchain head
     *
     * @param promise Promise
     * @return Return true if subscribed
     */
    @ReactMethod
    public void subscribeNewHead(Promise promise) {
        try {
            NewHeadHandler handler = new NewHeadHandler() {
                @Override
                public void onError(String error) {
                    Log.e(TAG, "Error emitting new head event: " + error);
                }

                @Override
                public void onNewHead(final Header header) {
                    WritableMap headerMap = new WritableNativeMap();
                    WritableArray extraArray = new WritableNativeArray();
                    for (byte extraByte : header.getExtra()) {
                        extraArray.pushInt(extraByte);
                    }
                    headerMap.putString("parentHash", header.getParentHash().getHex());
                    headerMap.putString("coinbase", header.getCoinbase().getHex());
                    headerMap.putString("root", header.getRoot().getHex());
                    headerMap.putString("TxHash", header.getTxHash().getHex());
                    headerMap.putString("receiptHash", header.getReceiptHash().getHex());
                    headerMap.putString("bloom", header.getBloom().getHex());
                    headerMap.putDouble("number", (double) header.getNumber());
                    headerMap.putDouble("gasUsed", (double) header.getGasUsed());
                    headerMap.putDouble("time", (double) header.getTime());
                    headerMap.putString("hash", header.getHash().getHex());
                    headerMap.putArray("extra", extraArray);

                    getReactApplicationContext()
                            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                            .emit("GethNewHead", headerMap);
                }
            };

            Context ctx = new Context();
            Log.i(TAG, "Subscribing to new head");
            gethHolder.getNode().getEthereumClient().subscribeNewHead(ctx, handler, 16);
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(SUBSCRIBE_NEW_HEAD_ERROR, e);
        }
    }

    /**
     * Changes the passphrase of current account.
     *
     * @param oldPassphrase Passphrase
     * @param newPassphrase New passphrase
     * @param promise       Promise
     * @return Return true if passphrase changed
     */
    @ReactMethod
    public void updateAccount(String oldPassphrase, String newPassphrase, Promise promise) {
        try {
            Account acc = gethHolder.getAccount();
            if (acc != null) {
                gethHolder.getKeyStore().updateAccount(acc, oldPassphrase, newPassphrase);
                promise.resolve(true);
            } else {
                promise.reject(UPDATE_ACCOUNT_ERROR, "call method setAccount() before");
            }
        } catch (Exception e) {
            promise.reject(UPDATE_ACCOUNT_ERROR, e);
        }
    }

    /**
     * Deletes the key matched by current account if the passphrase is correct.
     *
     * @param passphrase Passphrase
     * @param promise    Promise
     * @return Return true if account deleted
     */
    @ReactMethod
    public void deleteAccount(String passphrase, Promise promise) {
        try {
            Account acc = gethHolder.getAccount();
            if (acc != null) {
                gethHolder.getKeyStore().deleteAccount(acc, passphrase);
                promise.resolve(true);
            } else {
                promise.reject(DELETE_ACCOUNT_ERROR,
                        "call method setAccount('accountId') before");
            }
        } catch (Exception e) {
            promise.reject(DELETE_ACCOUNT_ERROR, e);
        }
    }

    /**
     * Exports as a JSON key, encrypted with new passphrase.
     *
     * @param creationPassphrase Passphrase
     * @param exportPassphrase   New passphrase
     * @param promise            Promise
     * @return Return key string
     */
    @ReactMethod
    public void exportKey(String creationPassphrase, String exportPassphrase, Promise promise) {
        try {
            Account acc = gethHolder.getAccount();
            if (acc != null) {
                byte[] key = gethHolder.getKeyStore()
                        .exportKey(acc, creationPassphrase, exportPassphrase);
                promise.resolve(new String(key, "UTF-8"));
            } else {
                promise.reject(EXPORT_KEY_ERROR, "call method setAccount('accountId') before");
            }
        } catch (Exception e) {
            promise.reject(EXPORT_KEY_ERROR, e);
        }
    }

    /**
     * Stores the given encrypted JSON key into the key directory.
     *
     * @param key           JSON key
     * @param oldPassphrase Passphrase
     * @param newPassphrase New passphrase
     * @param promise       Promise
     * @return Return account object
     */
    @ReactMethod
    public void importKey(byte[] key, String oldPassphrase, String newPassphrase, Promise promise) {
        try {
            Account acc = gethHolder.getKeyStore().importKey(key, oldPassphrase, newPassphrase);
            promise.resolve(acc);
        } catch (Exception e) {
            promise.reject(IMPORT_KEY_ERROR, e);
        }
    }

    /**
     * Returns all key files present in the directory.
     * - Break change : methode getAccounts => listAccounts -
     *
     * @param promise Promise
     * @return Return array of accounts objects
     */
    @ReactMethod
    public void listAccounts(Promise promise) {
        try {
            Accounts accounts = gethHolder.getKeyStore().getAccounts();
            Long nb = accounts.size();
            WritableArray result = new WritableNativeArray();
            for (long i = 0; i < nb; i++) {
                result.pushString(accounts.get(i).getAddress().getHex());
            }
            promise.resolve(result);
        } catch (Exception e) {
            promise.reject(GET_ACCOUNTS_ERROR, e);
        }
    }

    /**
     * Retrieves the currently suggested gas price to allow a timely execution of a transaction.
     *
     * @param promise Promise
     * @return Return Double suggested gas price
     */
    @ReactMethod
    public void suggestGasPrice(Promise promise) {
        try {
            Context ctx = new Context();
            long gasPrice = gethHolder.getNode().getEthereumClient().suggestGasPrice(ctx).getInt64();
            promise.resolve((double) gasPrice);
        } catch (Exception e) {
            promise.reject(SUGGEST_GAS_PRICE_ERROR, e);
        }
    }

    /**
     * Retrieves this account's pending nonce. This is the nonce you should use when creating a
     * transaction.
     *
     * @param promise Promise
     * @return return Double nonce
     */
    @ReactMethod
    public void getPendingNonce(Promise promise) {
        try {
            Account acc = gethHolder.getAccount();
            Context ctx = new Context();
            Address address = acc.getAddress();
            long nonce = gethHolder.getNode().getEthereumClient().getPendingNonceAt(ctx, address);
            promise.resolve((double) nonce);
        } catch (Exception e) {
            promise.reject(GET_NONCE_ERROR, e);
        }
    }

    /**
     * Get the signature by either using the passphrase or if null, consider
     * the account unlocked
     * @param txRLPBase64 base64 encoded RLP transaction
     * @param signer the address signing the transaction
     * @param passphrase the passphrase which unlocks the signer
     * @throws NoSuchElementException - when no account is found for the signer
     * @throws Exception - if the passphrase is wrong / signer isn't unlocked
     * @return return base64 encoded RLP of the signed transaction
     */
    private String getTxSignature(String txRLPBase64, String signer, String passphrase) throws Exception {
        byte[] txRLP = Base64.decode(txRLPBase64, Base64.DEFAULT);
        Account account = gethHolder.findAccount(signer);
        Transaction tx = new Transaction(txRLP);
        BigInt chainID = new BigInt(gethHolder.getNodeConfig().getEthereumNetworkID());
        Transaction signedTx;
        if (passphrase == null) {
            signedTx = gethHolder.getKeyStore().signTx(account, tx, chainID);
        } else {
            signedTx = gethHolder.getKeyStore().signTxPassphrase(account, passphrase, tx, chainID);
        }
        return Base64.encodeToString(signedTx.encodeRLP(), Base64.DEFAULT);
    }

    /**
     * Signs a transaction with an unlocked account
     *
     * @param txRLPBase64 base64 encoded RLP transaction
     * @param signer the address signing the transaction
     * @param promise Promise
     * @return return base64 encoded RLP of the signed transaction
     */
    @ReactMethod
    public void signTransaction(String txRLPBase64, String signer, Promise promise) {
        try {
            promise.resolve(getTxSignature(txRLPBase64, signer, null));
        } catch (Exception e) {
            promise.reject(SIGN_TRANSACTION_ERROR, e);
        }
    }

    /**
     * Signs a transaction with a passphrase
     *
     * @param txRLPBase64 base64 encoded RLP transaction
     * @param signer the address signing the transaction
     * @param passphrase the passphrase which unlocks the signer
     * @param promise Promise
     * @return return base64 encoded RLP of the signed transaction
     */
    @ReactMethod
    public void signTransactionPassphrase(String txRLPBase64, String signer, String passphrase, Promise promise) {
        try {
            promise.resolve(getTxSignature(txRLPBase64, signer, passphrase));
        } catch (Exception e) {
            promise.reject(SIGN_TRANSACTION_PASSPHRASE_ERROR, e);
        }
    }

    /**
     * Get the hash signature either by using the passphrase if it's provided
     * or by relying on the account being unlocked
     * @param hashBase64 base64 encoded hash
     * @param signer the address signing the hash
     * @param passphrase (optional) the passphrase to unlock the account
     * @throws NoSuchElementException - when no account is found for the signer
     * @throws Exception - if the passphrase is wrong / signer isn't unlocked
     * @return The base64 encoded signature
     */
    private String getHashSignature(String hashBase64, String signer, String passphrase) throws Exception {
        byte[] hashBytes = Base64.decode(hashBase64, Base64.DEFAULT);
        Account account = gethHolder.findAccount(signer);
        byte[] signature;
        if (passphrase == null) {
            signature = gethHolder.getKeyStore().signHash(account.getAddress(), hashBytes);
        } else {
            signature = gethHolder.getKeyStore().signHashPassphrase(account, passphrase, hashBytes);
        }
        return Base64.encodeToString(signature, Base64.DEFAULT);
    }

    /**
     * Signs a hash with an unlocked account
     *
     * @param hashBase64 base64 encoded hash
     * @param signer the address signing the hash
     * @param promise Promise
     * @return return base64 encoded RLP of the signed transaction
     */
    @ReactMethod
    public void signHash(String hashBase64, String signer, Promise promise) {
        try {
            promise.resolve(getHashSignature(hashBase64, signer, null));
        } catch (Exception e) {
            promise.reject(SIGN_HASH_ERROR, e);
        }
    }

    /**
     * Signs a hash with a passphrase
     *
     * @param hashBase64 base64 encoded RLP transaction
     * @param signer the address signing the transaction
     * @param passphrase the passphrase to unlock the signer
     * @param promise Promise
     * @return return base64 encoded RLP of the signed transaction
     */
    @ReactMethod
    public void signHashPassphrase(String hashBase64, String signer, String passphrase, Promise promise) {
        try {
            promise.resolve(getHashSignature(hashBase64, signer, passphrase));
        } catch (Exception e) {
            promise.reject(SIGN_HASH_PASSPHRASE_ERROR, e);
        }
    }

    /**
     * Retrieves the nodeInfo
     *
     * @param promise Promise
     * @return return a map with node info
     */
    @ReactMethod
    public void getNodeInfo(Promise promise) {
        WritableMap result = new WritableNativeMap();
        NodeInfo nodeInfo = gethHolder.getNode().getNodeInfo();
        result.putString("enode", nodeInfo.getEnode());
        result.putString("id", nodeInfo.getID());
        result.putString("ip", nodeInfo.getIP());
        result.putString("listenerAddress", nodeInfo.getListenerAddress());
        result.putString("name", nodeInfo.getName());
        result.putString("protocols", nodeInfo.getProtocols().toString());
        result.putDouble("discoveryPort", nodeInfo.getDiscoveryPort());
        result.putDouble("listenerPort", nodeInfo.getListenerPort());
        promise.resolve(result);
    }

    /**
     * Computes an ECDH shared secret between the user's privayeahte key and another user's public key
     *
     * @param address the address of the user
     * @param publicKey another user's public key
     * @param promise Promise
     * @return return a shared secret
     */
    @ReactMethod
    public void computeSharedSecret(String address, String publicKeyBase64, Promise promise) {
      try{
        Account account = gethHolder.findAccount(address);
        byte[] publicKey = Base64.decode(publicKeyBase64, Base64.DEFAULT);
        byte[] secret = gethHolder.getKeyStore().computeECDHSharedSecret(account, publicKey);
        promise.resolve(Base64.encodeToString(secret, Base64.DEFAULT));
      } catch (Exception e){
        promise.reject(COMPUTE_SHARED_SECRET_ERROR, e);
      }
        
    }

    /**
     * Decrypts an ECIES ciphertext
     *
     * @param address the address of the user
     * @param cipher the cipher to be decrypted
     * @param promise Promise
     * @return return the decrypted text
     */
    @ReactMethod
    public void decrypt(String address, String cipherBase64, Promise promise) {
      try{
        Account account = gethHolder.findAccount(address);
        byte[] cipher = Base64.decode(cipherBase64, Base64.DEFAULT);
        byte[] text = gethHolder.getKeyStore().decrypt(account, cipher);
        promise.resolve(Base64.encodeToString(text, Base64.DEFAULT));
      } catch (Exception e){
        promise.reject(DECRYPT_ERROR, e);
      }
        
    }
}

