package org.app.config;

import org.hyperledger.fabric.sdk.BlockEvent;
import org.hyperledger.fabric.sdk.ChaincodeEvent;

public class ChaincodeEventCapture { //A test class to capture chaincode events
    final String handle;
    final BlockEvent blockEvent;
    final ChaincodeEvent chaincodeEvent;

    public ChaincodeEventCapture(String handle, BlockEvent blockEvent, ChaincodeEvent chaincodeEvent) {
        this.handle = handle;
        this.blockEvent = blockEvent;
        this.chaincodeEvent = chaincodeEvent;
    }
}
