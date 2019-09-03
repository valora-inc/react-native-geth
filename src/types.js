// @flow

type NodeConfig = ? {
  bootstrapEnodeUrls?: string[],
  networkID?: number,
  maxPeers?: number,
  genesis?: string,
  nodeDir?: string,
  keyStoreDir?: string,
  enodes?: string,
  peerDiscovery?: boolean,
  syncMode?: number
}

type Account = {
  address: string,
  account: number
}

type ListAccounts = Array<Account>

type SyncProgress = {
  startingBlock: number,
  currentBlock: number,
  highestBlock: number
}

type OnNewHead = {
  parentHash: string,
  uncleHash: string,
  coinbase: string,
  root: string,
  TxHash: string,
  receiptHash: string,
  bloom: string,
  difficulty: number,
  number: number,
  gasLimit: number,
  gasUsed: number,
  time: number,
  mixDigest: string,
  nounce: string,
  hash: string,
  extra: Array<number>
}

type GethNativeModule = {
  nodeConfig: (config: ?NodeConfig) => Promise<boolean>,
  startNode: () => Promise<boolean>,
  stopNode: () => Promise<boolean>,
  newAccount: (passphrase: string) => Promise<Account>,
  setAccount: (accID: number) => Promise<boolean>,
  getAddress: () => Promise<string>,
  balanceAccount: () => Promise<string>,
  balanceAt: (address: string) => Promise<string>,
  syncProgress: () => Promise<SyncProgress>,
  subscribeNewHead: () => Promise<boolean>,
  updateAccount: (oldPassphrase: string,
    newPassphrase: string) => Promise<boolean>,
  deleteAccount: (passphrase: string) => Promise<boolean>,
  exportKey: (creationPassphrase: string,
    exportPassphrase: string) => Promise<string>,
  importKey: (key: string, oldPassphrase: string,
    newPassphrase: string) => Promise<Account>,
  listAccounts: () => Promise<ListAccounts>,
  createAndSendTransaction: (passphrase: string, nonce: number,
    toAddress: string, amount: number, gasLimit: number, gasPrice: number,
    data: string) => Promise<string>,
  suggestGasPrice: () => Promise<number>,
  getPendingNonce: () => Promise<number>
}

export type {
  NodeConfig,
  Account,
  ListAccounts,
  SyncProgress,
  OnNewHead,
  GethNativeModule
}
