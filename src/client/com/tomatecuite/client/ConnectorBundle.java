package com.tomatecuite.client;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Romain on 09/05/2016.
 */
public class ConnectorBundle {
    private static ConnectorBundle INSTANCE;

    private Map<ConnectorBundle, Peer> connectors;

    private ConnectorBundle() {
        connectors = new HashMap<ConnectorBundle, Peer>();
    }

    public static ConnectorBundle getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ConnectorBundle();
        }
        return INSTANCE;
    }

    public Map<ConnectorBundle, Peer> getConnectors() {
        return connectors;
    }

    public void addPeerClient(ConnectorBundle connector, Peer peer) {
        connectors.put(connector, peer);
    }

}
