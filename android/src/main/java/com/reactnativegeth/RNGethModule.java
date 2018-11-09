package com.reactnativegeth;

/**
 * Created by yaska on 17-09-29.
 */

import android.app.Activity;
import android.content.Intent;
import android.os.ConditionVariable;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.security.keystore.UserNotAuthenticatedException;
import android.support.annotation.Nullable;
import android.util.Log;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
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
import org.ethereum.geth.Geth;
import org.ethereum.geth.Header;
import org.ethereum.geth.KeyStore;
import org.ethereum.geth.NewHeadHandler;
import org.ethereum.geth.Node;
import org.ethereum.geth.NodeConfig;
import org.ethereum.geth.SyncProgress;
import org.ethereum.geth.Transaction;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.util.concurrent.locks.Condition;

import javax.crypto.KeyGenerator;

public class RNGethModule extends ReactContextBaseJavaModule {

    private static final String TAG = "Geth";
    private static final String CONFIG_NODE_ERROR = "CONFIG_NODE_ERROR";
    private static final String START_NODE_ERROR = "START_NODE_ERROR";
    private static final String STOP_NODE_ERROR = "STOP_NODE_ERROR";
    private static final String NEW_ACCOUNT_ERROR = "NEW_ACCOUNT_ERROR";
    private static final String SET_ACCOUNT_ERROR = "SET_ACCOUNT_ERROR";
    private static final String GET_ACCOUNT_ERROR = "GET_ACCOUNT_ERROR";
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
    private static final String NEW_TRANSACTION_ERROR = "NEW_TRANSACTION_ERROR";
    private static final String SUGGEST_GAS_PRICE_ERROR = "SUGGEST_GAS_PRICE_ERROR";
    private static final String DEVICE_SECURE_ERROR = "DEVICE_SECURE_ERROR";
    private static final String MAKE_DEVICE_SECURE_ERROR = "MAKE_DEVICE_SECURE_ERROR";
    private static final String KEYSTORE_INIT_ERROR = "KEYSTORE_INIT_ERROR";
    private static final String USER_NOT_AUTHENTICATED_ERROR = "USER_NOT_AUTHENTICATED_ERROR";
    private static final String STORE_PIN_ERROR = "STORE_PIN_ERROR";
    private static final String RETRIEVE_PIN_ERROR = "RTETRIEVE_PIN_ERROR";
    private static final String ETH_DIR = ".ethereum";
    private static final String KEY_STORE_DIR = "keystore";
    private GethHolder GethHolder;

    public RNGethModule(ReactApplicationContext reactContext) {
        super(reactContext);
        GethHolder = new GethHolder(reactContext);
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
    public void nodeConfig(ReadableMap config, Promise promise) {
        try {
            NodeConfig nc = GethHolder.getNodeConfig();
            String nodeDir = ETH_DIR;
            String keyStoreDir = KEY_STORE_DIR;
            if (config.hasKey("enodes"))
                GethHolder.writeStaticNodesFile(config.getString("enodes"));
            if (config.hasKey("networkID")) nc.setEthereumNetworkID(config.getInt("networkID"));
            if (config.hasKey("maxPeers")) nc.setMaxPeers(config.getInt("maxPeers"));
            if (config.hasKey("genesis")) nc.setEthereumGenesis(config.getString("genesis"));
            if (config.hasKey("nodeDir")) nodeDir = config.getString("nodeDir");
            if (config.hasKey("keyStoreDir")) keyStoreDir = config.getString("keyStoreDir");
            if (config.hasKey("syncMode")) nc.setSyncMode(config.getInt("syncMode"));
            Node nd = Geth.newNode(getReactApplicationContext()
                    .getFilesDir() + "/" + nodeDir, nc);
            KeyStore ks = new KeyStore(getReactApplicationContext()
                    .getFilesDir() + "/" + keyStoreDir, Geth.LightScryptN, Geth.LightScryptP);
            GethHolder.setNodeConfig(nc);
            GethHolder.setKeyStore(ks);
            GethHolder.setNode(nd);
            promise.resolve(true);
        } catch (Exception e) {
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
            if (GethHolder.getNode() != null) {
                GethHolder.getNode().start();
                result = true;
            }
            promise.resolve(result);
        } catch (Exception e) {
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
            if (GethHolder.getNode() != null) {
                GethHolder.getNode().stop();
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
            Account acc = GethHolder.getKeyStore().newAccount(passphrase);
            WritableMap newAccount = new WritableNativeMap();
            newAccount.putString("address", acc.getAddress().getHex());
            newAccount.putDouble("account", GethHolder.getKeyStore().getAccounts().size() - 1);
            promise.resolve(newAccount);
        } catch (Exception e) {
            promise.reject(NEW_ACCOUNT_ERROR, e);
        }
    }

    /**
     * Sets the default account at the given index in the listAccounts.
     *
     * @param accID   index in the listAccounts
     * @param promise Promise
     * @return Return true if sets.
     */
    @ReactMethod
    public void setAccount(Integer accID, Promise promise) {
        try {
            Account acc = GethHolder.getKeyStore().getAccounts().get(accID);
            GethHolder.setAccount(acc);
            //accounts.set(0, acc);
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(SET_ACCOUNT_ERROR, e);
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
            Account acc = GethHolder.getAccount();
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
            Account acc = GethHolder.getAccount();
            if (acc != null) {
                Context ctx = new Context();
                BigInt balance = GethHolder.getNode().getEthereumClient()
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
            BigInt balance = GethHolder.getNode().getEthereumClient()
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
            SyncProgress sp = GethHolder.getNode().getEthereumClient().syncProgress(ctx);
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
                    Log.e("GETH", "Error emitting new head event: " + error);
                }

                @Override
                public void onNewHead(final Header header) {
                    WritableMap headerMap = new WritableNativeMap();
                    WritableArray extraArray = new WritableNativeArray();
                    for (byte extraByte : header.getExtra()) {
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
                    getReactApplicationContext()
                        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                            .emit("GethNewHead", headerMap);
                }
            };

            Context ctx = new Context();
            GethHolder.getNode().getEthereumClient().subscribeNewHead(ctx, handler, 16);
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
            Account acc = GethHolder.getAccount();
            if (acc != null) {
                GethHolder.getKeyStore().updateAccount(acc, oldPassphrase, newPassphrase);
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
            Account acc = GethHolder.getAccount();
            if (acc != null) {
                GethHolder.getKeyStore().deleteAccount(acc, passphrase);
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
            Account acc = GethHolder.getAccount();
            if (acc != null) {
                byte[] key = GethHolder.getKeyStore()
                    .exportKey(acc, creationPassphrase, exportPassphrase);
                promise.resolve(key);
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
            Account acc = GethHolder.getKeyStore().importKey(key, oldPassphrase, newPassphrase);
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
            Accounts accounts = GethHolder.getKeyStore().getAccounts();
            Long nb = accounts.size();
            WritableArray listAccounts = new WritableNativeArray();
            if (nb > 0) {
                for (long i = 0; i < nb; i++) {
                    WritableMap resultAcc = new WritableNativeMap();
                    resultAcc.putString("address", accounts.get(i).getAddress().getHex());
                    resultAcc.putDouble("account", i);
                    listAccounts.pushMap(resultAcc);
                }
            }
            promise.resolve(listAccounts);
        } catch (Exception e) {
            promise.reject(GET_ACCOUNTS_ERROR, e);
        }
    }

    /**
     * Create and send transaction.
     *
     * @param passphrase Passphrase
     * @param nonce      Account nonce (use -1 to use last known nonce)
     * @param toAddress  Address destination
     * @param amount     Amount
     * @param gasLimit   Gas limit
     * @param gasPrice   Gas price
     * @param data
     * @param promise    Promise
     * @return Return String transaction
     */
    @ReactMethod
    public void createAndSendTransaction(String passphrase, double nonce, String toAddress,
                                         String amount, double gasLimit, double gasPrice,
                                         String data, Promise promise) {
        try {
            Account acc = GethHolder.getAccount();
            Address fromAddress = acc.getAddress();
            BigInt chain = new BigInt(GethHolder.getNodeConfig().getEthereumNetworkID());
            Context ctx = new Context();
            BigInt bigIntAmount = new BigInt(0);
            bigIntAmount.setString(amount, 10);
            if (nonce == -1) nonce = GethHolder.getNode().getEthereumClient()
                .getPendingNonceAt(ctx, fromAddress);
            Transaction tx = new Transaction(
                    (long) nonce,
                    new Address(toAddress),
                    bigIntAmount,
                    (long) gasLimit,
                    new BigInt((long) gasPrice),
                    data.getBytes("UTF8"));
            // Sign a transaction with a single authorization
            Transaction signed = GethHolder.getKeyStore()
                .signTxPassphrase(acc, passphrase, tx, chain);
            // Send it out to the network.
            GethHolder.getNode().getEthereumClient().sendTransaction(ctx, signed);
            promise.resolve(tx.toString());
        } catch (Exception e) {
            promise.reject(NEW_TRANSACTION_ERROR, e);
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
            long gasPrice = GethHolder.getNode().getEthereumClient().suggestGasPrice(ctx).getInt64();
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
            Account acc = GethHolder.getAccount();
            Context ctx = new Context();
            Address address = acc.getAddress();
            long nonce = GethHolder.getNode().getEthereumClient().getPendingNonceAt(ctx, address);
            promise.resolve((double) nonce);
        } catch (Exception e) {
            promise.reject(GET_NONCE_ERROR, e);
        }
    }

    @ReactMethod
    public void isDeviceSecure(Promise promise) {
        try {
            promise.resolve(AndroidKeyStoreHelper.isDeviceSecure(getReactApplicationContext()));
        } catch (Exception e) {
            promise.reject(DEVICE_SECURE_ERROR, e);
        }
    }

    @ReactMethod
    public void makeDeviceSecure(String message, String actionButtonLabel, final Promise promise) {
        final int requestCodeForSetPasswordAction = 3;

        AndroidKeyStoreHelper.MakeDeviceSecureCallback makeDeviceSecureCallback =
                new AndroidKeyStoreHelper.MakeDeviceSecureCallback() {
                    @Override
                    public void onUserCancelled() {
                        Log.d(TAG, "makeDeviceSecure/onUserCancelled");
                        promise.reject(MAKE_DEVICE_SECURE_ERROR, "User dismissed dialog");
                    }

                    @Override
                    public void onUserTransitionToSetupDeviceLock() {
                        ActivityEventListener activityEventListener = new ActivityEventListener() {
                            @Override
                            public void onActivityResult(Activity activity,
                                                         int requestcode,
                                                         int resultCode,
                                                         Intent intent) {
                                if (requestcode == requestCodeForSetPasswordAction) {
                                    if (AndroidKeyStoreHelper.isDeviceSecure(getReactApplicationContext())) {
                                        promise.resolve(true);
                                    } else if (resultCode == Activity.RESULT_OK) {
                                        // Retry since now the user is authenticated.
                                        Log.d(TAG, "makeDeviceSecure/onActivityResult/ok");
                                        promise.resolve(AndroidKeyStoreHelper.isDeviceSecure(
                                                getReactApplicationContext()));
                                    } else {
                                        // User decided to reject authentication.
                                        Log.d(TAG, "makeDeviceSecure/onActivityResult/user-canceled-setup/" + resultCode);
                                        promise.reject(MAKE_DEVICE_SECURE_ERROR, "User canceled setup");
                                    }
                                    getReactApplicationContext().removeActivityEventListener(this);
                                }
                                getReactApplicationContext().removeActivityEventListener(this);
                            }

                            @Override
                            public void onNewIntent(Intent intent) {
                                // Do nothing
                            }
                        };
                        getReactApplicationContext().addActivityEventListener(activityEventListener);
                    }
                };
        try {
            AndroidKeyStoreHelper.makeDeviceSecure(
                    getCurrentActivity(),
                    message,
                    actionButtonLabel,
                    requestCodeForSetPasswordAction,
                    makeDeviceSecureCallback);
        } catch (Exception e) {
            Log.d(TAG, "makeDeviceSecure/error", e);
            promise.reject(MAKE_DEVICE_SECURE_ERROR, e);
        }
    }

    /**
     * This can be called multiple times, it won't recreate the key if the key already exists.
     */
    @ReactMethod
    public void keyStoreInit(String keyName,
            int reauthenticationTimeoutInSecs,
            boolean invalidateKeyByNewBiometricEnrollment,
            Promise promise) {
        try {
            if (!AndroidKeyStoreHelper.isDeviceSecure(getReactApplicationContext())) {
                promise.reject(DEVICE_SECURE_ERROR, new Exception("Device is not secure"));
                return;
            }
            if (!AndroidKeyStoreHelper.keyExists(keyName)) {
                Log.i(TAG, "keyStoreInit/key does not exist, creating it");
                createKey(keyName, reauthenticationTimeoutInSecs, invalidateKeyByNewBiometricEnrollment);
                promise.resolve(true);
            } else {
                Log.i(TAG, "keyStoreInit/key exists");
                promise.resolve(true);
            }
            Log.d("GethModule", "key created");
        } catch (Exception e) {
            promise.reject(KEYSTORE_INIT_ERROR, e);
        }
    }

    /**
     * Creates a symmetric key in the Android Key Store which can only be used after
     * the user has authenticated with device credentials within the last X seconds.
     */
    private void createKey(String keyName, int reauthenticationTimeoutInSecs,
            boolean invalidateKeyByNewBiometricEnrollment) {
        try {
            boolean result = AndroidKeyStoreHelper.createKey(keyName,
                    reauthenticationTimeoutInSecs, invalidateKeyByNewBiometricEnrollment);
            Log.i(TAG, "createKey/key creation result: " + result);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create a symmetric key", e);
        }
    }

    @ReactMethod
    public void storePin(final String keyName, final String pinValue, final Promise promise) {
        try {
            boolean result = AndroidKeyStoreHelper.storePin(
                    getReactApplicationContext(),
                    keyName,
                    pinValue);
            promise.resolve(result);
        } catch (final UserNotAuthenticatedException e) {
            final int authenticateForEncryptionRequestCode = 1;
            final Runnable retryRunnable = new Runnable() {
                @Override
                public void run() {
                    storePin(keyName, pinValue, promise);
                }
            };
            performAuthentication(promise, e, authenticateForEncryptionRequestCode, retryRunnable);
        } catch (Exception e) {
            promise.reject(STORE_PIN_ERROR, e);
        }
    }

    private void performAuthentication(final Promise promise,
                                       final UserNotAuthenticatedException e,
                                       final int requestCode,
                                       final Runnable retryRunnable) {
        ActivityEventListener activityEventListener = new ActivityEventListener() {
            @Override
            public void onActivityResult(Activity activity,
                                         int requestcode,
                                         int resultCode,
                                         Intent intent) {
                if (requestcode == requestCode) {
                    if (resultCode == Activity.RESULT_OK) {
                        // Retry since now the user is authenticated.
                        retryRunnable.run();
                    } else {
                        // User decided to reject authentication.
                        promise.reject(USER_NOT_AUTHENTICATED_ERROR, e);
                    }
                    getReactApplicationContext().removeActivityEventListener(this);
                }
            }

            @Override
            public void onNewIntent(Intent intent) {
                // Do nothing
            }
        };
        getReactApplicationContext().addActivityEventListener(activityEventListener);
        Activity currentActivity = getCurrentActivity();
        if (currentActivity == null) {
            // Current activity is null, cannot authenticate.
            promise.reject(USER_NOT_AUTHENTICATED_ERROR, e);
        } else {
            AndroidKeyStoreHelper.authenticateUser(
                    currentActivity,
                    requestCode);
        }
    }

    @ReactMethod
    public void retrievePin(String keyName, Promise promise) {
        try {
            String result = AndroidKeyStoreHelper.retrievePin(getReactApplicationContext(),
                    keyName);
            promise.resolve(result);
        } catch (UserNotAuthenticatedException e) {
            promise.reject(USER_NOT_AUTHENTICATED_ERROR, e);
        } catch (Exception e) {
            promise.reject(RETRIEVE_PIN_ERROR, e);
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
