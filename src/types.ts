export type NodeConfig = {
  bootnodeEnodes?: string[],
  enodes?: string,
  genesis?: string,
  ipcPath?: string
  keyStoreDir?: string,
  logFile?: string,
  logFileLogLevel?: number,
  maxPeers?: number,
  networkID?: number,
  noDiscovery?: boolean,
  nodeDir?: string,
  syncMode?: number
  useLightweightKDF?: boolean,
}

