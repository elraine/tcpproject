package tomateCuiteTorrent;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

public class Constants {
    public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");
    public static final CharsetDecoder NETWORK_STRING_DECODER = DEFAULT_CHARSET
            .newDecoder();

    public static final int FILE_ARGS_NUMBER = 4;
    public static final int MIN_PORT_NUMBER = 1025;
    public static final int MAX_PORT_NUMBER = 10250;

	/* Configuration file keys */
    public static final String TRACKER_HOST_KEY = "trackerHost";
    public static final String TRACKER_PORT_KEY = "trackerPort";
    public static final String PEER_HOST_KEY = "peerHost";
    public static final String PEER_PORT_KEY = "peerPort";
    public static final String LOCAL_STORAGE_KEY = "repertoryStorage";
    public static final String PIECE_SIZE_KEY = "pieceSize";
    public static final String MAXIMUM_CONNECTED_PEERS_KEY = "maxConnectedPeers";
    public static final String MAXIMUM_MESSAGE_SIZE_KEY = "maxMessageSize";
    public static final String UPDATE_FREQUENCY_KEY = "updateFrequency";

}