package org.app.chaincode.invocation;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.security.Security;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.app.client.ChannelClient;
import org.app.client.FabricClient;
import org.app.config.Config;
import org.app.user.UserContext;
import org.app.util.Util;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.hyperledger.fabric.sdk.BlockEvent.TransactionEvent;
import org.hyperledger.fabric.sdk.ChaincodeID;
import org.hyperledger.fabric.sdk.Channel.PeerOptions;
import org.hyperledger.fabric.sdk.Peer.PeerRole;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.Orderer;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.TransactionProposalRequest;

public class InvokeChaincode {

	private static final byte[] EXPECTED_EVENT_DATA = "!".getBytes(UTF_8);
	private static final String EXPECTED_EVENT_NAME = "event";

	public static void main(String args[]) {
		try {
            Util.cleanUp();
            Security.addProvider(new BouncyCastleProvider());
			
			UserContext org1Admin = new UserContext();
			File pkFolder1 = new File(Config.ORG1_USR_ADMIN_PK);
			File[] pkFiles1 = pkFolder1.listFiles();
			File certFolder = new File(Config.ORG1_USR_ADMIN_CERT);
			File[] certFiles = certFolder.listFiles();
			Enrollment enrollOrg1Admin = Util.getEnrollment(Config.ORG1_USR_ADMIN_PK, pkFiles1[0].getName(),Config.ORG1_USR_ADMIN_CERT, certFiles[0].getName());
			org1Admin.setEnrollment(enrollOrg1Admin);
			org1Admin.setMspId("Org1MSP");
			org1Admin.setName("admin");
			
			FabricClient fabClient = new FabricClient(org1Admin);
			
			ChannelClient channelClient = fabClient.createChannelClient(Config.CHANNEL_NAME);
			Channel channel = channelClient.getChannel();
			
			Properties peer0_org1_properties = new Properties();
			peer0_org1_properties.setProperty("pemFile", Config.PEER0_ORG1_TLS_CERT_PATH+File.separator+"server.crt");
			peer0_org1_properties.setProperty("hostnameOverride", Config.ORG1_PEER_0);
			peer0_org1_properties.setProperty("sshProvider", "openSSL");
			peer0_org1_properties.setProperty("negotiationType", "TLS");
			Peer peer0_org1 = fabClient.getInstance().newPeer(Config.ORG1_PEER_0, Config.ORG1_PEER_0_URL, peer0_org1_properties);

			Properties peer1_org1_properties = new Properties();
			peer1_org1_properties.setProperty("pemFile", Config.PEER1_ORG1_TLS_CERT_PATH+File.separator+"server.crt");
			peer1_org1_properties.setProperty("hostnameOverride", Config.ORG1_PEER_1);
			peer1_org1_properties.setProperty("sshProvider", "openSSL");
			peer1_org1_properties.setProperty("negotiationType", "TLS");
			Peer peer1_org1 = fabClient.getInstance().newPeer(Config.ORG1_PEER_1, Config.ORG1_PEER_1_URL, peer1_org1_properties);
			
			Properties peer0_org2_properties = new Properties();
			peer0_org2_properties.setProperty("pemFile", Config.PEER0_ORG2_TLS_CERT_PATH+File.separator+"server.crt");
			peer0_org2_properties.setProperty("hostnameOverride", Config.ORG2_PEER_0);
			peer0_org2_properties.setProperty("sshProvider", "openSSL");
			peer0_org2_properties.setProperty("negotiationType", "TLS");
			Peer peer0_org2 = fabClient.getInstance().newPeer(Config.ORG2_PEER_0, Config.ORG2_PEER_0_URL, peer0_org2_properties);

			Properties peer1_org2_properties = new Properties();
			peer1_org2_properties.setProperty("pemFile", Config.PEER1_ORG2_TLS_CERT_PATH+File.separator+"server.crt");
			peer1_org2_properties.setProperty("hostnameOverride", Config.ORG2_PEER_1);
			peer1_org2_properties.setProperty("sshProvider", "openSSL");
			peer1_org2_properties.setProperty("negotiationType", "TLS");
			Peer peer1_org2 = fabClient.getInstance().newPeer(Config.ORG2_PEER_1, Config.ORG2_PEER_1_URL, peer1_org2_properties);
			
			Properties orderer_properties = new Properties();
			orderer_properties.setProperty("pemFile", Config.ORDERER_TLS_CERT_PATH+File.separator+"server.crt");
			orderer_properties.setProperty("hostnameOverride", Config.ORDERER_NAME);
			orderer_properties.setProperty("sshProvider", "openSSL");
			orderer_properties.setProperty("negotiationType", "TLS");
			orderer_properties.setProperty("clientKeyFile", Config.ORDERER_TLS_CERT_PATH+File.separator+"client.pem");
			orderer_properties.setProperty("clientCertFile", Config.ORDERER_TLS_CERT_PATH+File.separator+"client.crt");
			Orderer orderer = fabClient.getInstance().newOrderer(Config.ORDERER_NAME, Config.ORDERER_URL,orderer_properties);
					
			final PeerOptions peerEventingOptions =  PeerOptions.createPeerOptions().registerEventsForBlocks().registerEventsForFilteredBlocks().setPeerRoles(EnumSet.of(PeerRole.ENDORSING_PEER, PeerRole.LEDGER_QUERY, PeerRole.CHAINCODE_QUERY, PeerRole.EVENT_SOURCE));
			channel.addOrderer(orderer);
			channel.addPeer(peer0_org1,peerEventingOptions);
			channel.addPeer(peer1_org1,peerEventingOptions);
			channel.addPeer(peer0_org2,peerEventingOptions);
			channel.addPeer(peer1_org2,peerEventingOptions);
			
			channel.initialize();
			          
			TransactionProposalRequest request = fabClient.getInstance().newTransactionProposalRequest();
			ChaincodeID ccid = ChaincodeID.newBuilder().setName(Config.CHAINCODE_1_NAME).setVersion("1").build();
			request.setChaincodeID(ccid);
			request.setFcn("put");
			String[] arguments = { "say","hello world" };
			request.setArgs(arguments);
			request.setProposalWaitTime(1000);

			Map<String, byte[]> tm2 = new HashMap<>();
			tm2.put("HyperLedgerFabric", "TransactionProposalRequest:JavaSDK".getBytes(UTF_8)); 																								
			tm2.put("method", "TransactionProposalRequest".getBytes(UTF_8)); 
			tm2.put("result", ":)".getBytes(UTF_8));
			tm2.put(EXPECTED_EVENT_NAME, EXPECTED_EVENT_DATA); 
			request.setTransientMap(tm2);
			TransactionEvent event = channelClient.sendTransaction(request).get();
			Logger.getLogger(ChannelClient.class.getName()).log(Level.INFO,"TransactionID: " + event.getTransactionID() + " on channel " + event.getChannelId());
									
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
