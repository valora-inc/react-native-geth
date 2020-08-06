"use strict";
var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
var __generator = (this && this.__generator) || function (thisArg, body) {
    var _ = { label: 0, sent: function() { if (t[0] & 1) throw t[1]; return t[1]; }, trys: [], ops: [] }, f, y, t, g;
    return g = { next: verb(0), "throw": verb(1), "return": verb(2) }, typeof Symbol === "function" && (g[Symbol.iterator] = function() { return this; }), g;
    function verb(n) { return function (v) { return step([n, v]); }; }
    function step(op) {
        if (f) throw new TypeError("Generator is already executing.");
        while (_) try {
            if (f = 1, y && (t = op[0] & 2 ? y["return"] : op[0] ? y["throw"] || ((t = y["return"]) && t.call(y), 0) : y.next) && !(t = t.call(y, op[1])).done) return t;
            if (y = 0, t) op = [op[0] & 2, t.value];
            switch (op[0]) {
                case 0: case 1: t = op; break;
                case 4: _.label++; return { value: op[1], done: false };
                case 5: _.label++; y = op[1]; op = [0]; continue;
                case 7: op = _.ops.pop(); _.trys.pop(); continue;
                default:
                    if (!(t = _.trys, t = t.length > 0 && t[t.length - 1]) && (op[0] === 6 || op[0] === 2)) { _ = 0; continue; }
                    if (op[0] === 3 && (!t || (op[1] > t[0] && op[1] < t[3]))) { _.label = op[1]; break; }
                    if (op[0] === 6 && _.label < t[1]) { _.label = t[1]; t = op; break; }
                    if (t && _.label < t[2]) { _.label = t[2]; _.ops.push(op); break; }
                    if (t[2]) _.ops.pop();
                    _.trys.pop(); continue;
            }
            op = body.call(thisArg, _);
        } catch (e) { op = [6, e]; y = 0; } finally { f = t = 0; }
        if (op[0] & 5) throw op[1]; return { value: op[0] ? op[1] : void 0, done: true };
    }
};
Object.defineProperty(exports, "__esModule", { value: true });
var react_native_1 = require("react-native");
var RNGeth = /** @class */ (function () {
    function RNGeth(config) {
        this.config = config;
        this.geth = react_native_1.NativeModules.RNGeth;
        this.geth.nodeConfig(this.config);
    }
    /**
     * Start creates a live P2P node and starts running it.
     * @returns success status of operation
     */
    RNGeth.prototype.start = function () {
        return __awaiter(this, void 0, void 0, function () {
            return __generator(this, function (_a) {
                switch (_a.label) {
                    case 0: return [4 /*yield*/, this.geth.startNode()];
                    case 1: return [2 /*return*/, _a.sent()];
                }
            });
        });
    };
    /**
     * Terminates a running node along with all it's services.
     * @returns success status of operation
     */
    RNGeth.prototype.stop = function () {
        return __awaiter(this, void 0, void 0, function () {
            return __generator(this, function (_a) {
                switch (_a.label) {
                    case 0: return [4 /*yield*/, this.geth.stopNode()];
                    case 1: return [2 /*return*/, _a.sent()];
                }
            });
        });
    };
    /**
     * Subscribes to notifications about the current blockchain head
     * @return true if subscribed
     */
    RNGeth.prototype.subscribeNewHead = function () {
        return __awaiter(this, void 0, void 0, function () {
            return __generator(this, function (_a) {
                switch (_a.label) {
                    case 0: return [4 /*yield*/, this.geth.subscribeNewHead()];
                    case 1: return [2 /*return*/, _a.sent()];
                }
            });
        });
    };
    /**
     * Add a new account
     * @param privateKey - the hex-encoded private key
     * @param passphrase - the passphrase used for the account
     * @returns the new account
     */
    RNGeth.prototype.addAccount = function (privateKey, passphrase) {
        return __awaiter(this, void 0, void 0, function () {
            return __generator(this, function (_a) {
                switch (_a.label) {
                    case 0: return [4 /*yield*/, this.geth.addAccount(privateKey, passphrase)];
                    case 1: return [2 /*return*/, _a.sent()];
                }
            });
        });
    };
    /**
     * Returns all key files present in the directory.
     * @returns all accounts
     */
    RNGeth.prototype.listAccounts = function () {
        return __awaiter(this, void 0, void 0, function () {
            return __generator(this, function (_a) {
                switch (_a.label) {
                    case 0: return [4 /*yield*/, this.geth.listAccounts()];
                    case 1: return [2 /*return*/, _a.sent()];
                }
            });
        });
    };
    /**
     * Unlock an account
     * @param address - the address to unlock
     * @param passphrase - the passphrase of the account
     * @param timeout - unlock duration in seconds
     * @returns the unlocked status of the account
     */
    RNGeth.prototype.unlockAccount = function (account, passphrase, timeout) {
        return __awaiter(this, void 0, void 0, function () {
            return __generator(this, function (_a) {
                switch (_a.label) {
                    case 0: return [4 /*yield*/, this.geth.unlockAccount(account, passphrase, timeout * 1000000000)];
                    case 1: 
                    // In Go: time.Second = 1000000000
                    return [2 /*return*/, _a.sent()];
                }
            });
        });
    };
    /**
     * Sign a RLP-encoded transaction with the passphrase
     * @param txRLP - The RLP encoded transaction
     * @param signer - Address of the signer
     * @param  passphrase - The passphrase for the signer's account
     * @returns the signed transaction in RLP as a hex string
     */
    RNGeth.prototype.signTransactionPassphrase = function (txRLP, signer, passphrase) {
        return __awaiter(this, void 0, void 0, function () {
            var signedTxRLP;
            return __generator(this, function (_a) {
                switch (_a.label) {
                    case 0: return [4 /*yield*/, this.geth.signTransactionPassphrase(txRLP, signer, passphrase)];
                    case 1:
                        signedTxRLP = _a.sent();
                        return [2 /*return*/, signedTxRLP.toLowerCase()];
                }
            });
        });
    };
    /**
     * Sign a RLP-encoded transaction with an unlocked account
     * @param txRLP - The RLP encoded transaction
     * @param signer - Address of the signer
     * @returns the signed transaction in RLP as a hex string
     */
    RNGeth.prototype.signTransaction = function (txRLP, signer) {
        return __awaiter(this, void 0, void 0, function () {
            var signedTxRLP;
            return __generator(this, function (_a) {
                switch (_a.label) {
                    case 0: return [4 /*yield*/, this.geth.signTransaction(txRLP, signer)];
                    case 1:
                        signedTxRLP = _a.sent();
                        return [2 /*return*/, signedTxRLP.toLowerCase()];
                }
            });
        });
    };
    return RNGeth;
}());
exports.default = RNGeth;
