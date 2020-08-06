export declare type NodeConfig = {
    bootnodeEnodes?: string[];
    networkID?: number;
    maxPeers?: number;
    genesis?: string;
    nodeDir?: string;
    keyStoreDir?: string;
    enodes?: string;
    noDiscovery?: boolean;
    syncMode?: number;
};
export declare type Account = {
    address: string;
    account: number;
};
