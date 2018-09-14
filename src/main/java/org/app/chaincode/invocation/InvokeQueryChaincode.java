package org.app.chaincode.invocation;

import java.util.Collection;

import org.app.config.FabricClient;
import org.app.util.Util;
import org.hyperledger.fabric.sdk.BlockEvent.TransactionEvent;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;

public class InvokeQueryChaincode {

	public static void main(String args[]) {
		try {
			Util.cleanUp();
            FabricClient client = new FabricClient("/Users/huyinsong/workspace/java/ChaincodeTest/resources/connection.yaml");
			client.initClient();
			client.enrollUser("huyinsong","org1");
			client.initChannel("mychannel");
			HFClient c = client.getInstance();
			c.getChannel("mychannel").getPeers().forEach(peer -> {
				System.out.println(peer.getName());
			});
			c.getChannel("mychannel").getOrderers().forEach(peer -> {
				System.out.println(peer.getName());
			});
			TransactionEvent event = client.invokeChaincode("mychannel", "put", "say", "hello kitty", "test", "1").get();
			System.out.println("Transaction ID"+event.getTransactionID()+";Block height:"+event.getBlockEvent().getBlockNumber());
		
			Collection<ProposalResponse> responses = client.queryByChainCode("mychannel", "test", "get", "say");
			responses.forEach(response ->{
				try {
					System.out.println(new String(response.getChaincodeActionResponsePayload()));
				} catch (InvalidArgumentException e) {
					e.printStackTrace();
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
