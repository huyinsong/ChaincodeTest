package org.app.chaincode.invocation;

import java.io.File;
import java.security.Security;
import java.util.Collection;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.app.client.ChannelClient;
import org.app.client.FabricClient;
import org.app.config.Config;
import org.app.user.UserContext;
import org.app.util.Util;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.EventHub;
import org.hyperledger.fabric.sdk.Orderer;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.ProposalResponse;

public class QueryChaincode {


	public static void main(String args[]) {
		try {
            //Util.cleanUp();
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
			
			Properties orderer_properties = new Properties();
			orderer_properties.setProperty("pemFile", Config.ORDERER_TLS_CERT_PATH+File.separator+"server.crt");
			orderer_properties.setProperty("hostnameOverride", Config.ORDERER_NAME);
			orderer_properties.setProperty("sshProvider", "openSSL");
			orderer_properties.setProperty("negotiationType", "TLS");
			Orderer orderer = fabClient.getInstance().newOrderer(Config.ORDERER_NAME, Config.ORDERER_URL,orderer_properties);
			
			channel.addPeer(peer0_org1);
			channel.addOrderer(orderer);
			channel.initialize();

			
			String[] args1 = {"say"};
			Logger.getLogger(QueryChaincode.class.getName()).log(Level.INFO, "Querying for a car - " + args1[0]);
			
			Collection<ProposalResponse>  responses1Query = channelClient.queryByChainCode("test", "get", args1);
			for (ProposalResponse pres : responses1Query) {
				String stringResponse = new String(pres.getChaincodeActionResponsePayload());
				Logger.getLogger(QueryChaincode.class.getName()).log(Level.INFO, stringResponse);
			}		
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
