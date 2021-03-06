package org.app.network;

import java.io.File;
import java.security.Security;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.app.client.FabricClient;
import org.app.config.Config;
import org.app.user.UserContext;
import org.app.util.Util;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.ChannelConfiguration;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.Orderer;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.security.CryptoSuite;


public class CreateChannel {

	public static void main(String[] args) {
		try {
			Security.addProvider(new BouncyCastleProvider());
			CryptoSuite.Factory.getCryptoSuite();
			Util.cleanUp();
			// Construct Channel
			UserContext org1Admin = new UserContext();
			File pkFolder1 = new File(Config.ORG1_USR_ADMIN_PK);
			File[] pkFiles1 = pkFolder1.listFiles();
			File certFolder1 = new File(Config.ORG1_USR_ADMIN_CERT);
			File[] certFiles1 = certFolder1.listFiles();
			Enrollment enrollOrg1Admin = Util.getEnrollment(Config.ORG1_USR_ADMIN_PK, pkFiles1[0].getName(),Config.ORG1_USR_ADMIN_CERT, certFiles1[0].getName());
			org1Admin.setEnrollment(enrollOrg1Admin);
			org1Admin.setMspId(Config.ORG1_MSP);
			org1Admin.setName(Config.ADMIN);

			UserContext org2Admin = new UserContext();
			File pkFolder2 = new File(Config.ORG2_USR_ADMIN_PK);
			File[] pkFiles2 = pkFolder2.listFiles();
			File certFolder2 = new File(Config.ORG2_USR_ADMIN_CERT);
			File[] certFiles2 = certFolder2.listFiles();
			Enrollment enrollOrg2Admin = Util.getEnrollment(Config.ORG2_USR_ADMIN_PK, pkFiles2[0].getName(),Config.ORG2_USR_ADMIN_CERT, certFiles2[0].getName());
			org2Admin.setEnrollment(enrollOrg2Admin);
			org2Admin.setMspId(Config.ORG2_MSP);
			org2Admin.setName(Config.ADMIN);

			FabricClient fabClient = new FabricClient(org1Admin);

			// Create a new channel
			
			Properties orderer_properties = new Properties();
			orderer_properties.setProperty("pemFile", Config.ORDERER_TLS_CERT_PATH+File.separator+"server.crt");
			orderer_properties.setProperty("hostnameOverride", Config.ORDERER_NAME);
			orderer_properties.setProperty("sshProvider", "openSSL");
			orderer_properties.setProperty("negotiationType", "TLS");
			
			Orderer orderer = fabClient.getInstance().newOrderer(Config.ORDERER_NAME, Config.ORDERER_URL,orderer_properties);
			ChannelConfiguration channelConfiguration = new ChannelConfiguration(new File(Config.CHANNEL_CONFIG_PATH));

			byte[] channelConfigurationSignatures = fabClient.getInstance().getChannelConfigurationSignature(channelConfiguration, org1Admin);

			Channel mychannel = fabClient.getInstance().newChannel(Config.CHANNEL_NAME, orderer, channelConfiguration,channelConfigurationSignatures);

			/*Peer peer0_org1 = fabClient.getInstance().newPeer(Config.ORG1_PEER_0, Config.ORG1_PEER_0_URL);
			Peer peer1_org1 = fabClient.getInstance().newPeer(Config.ORG1_PEER_1, Config.ORG1_PEER_1_URL);
			Peer peer0_org2 = fabClient.getInstance().newPeer(Config.ORG2_PEER_0, Config.ORG2_PEER_0_URL);
			Peer peer1_org2 = fabClient.getInstance().newPeer(Config.ORG2_PEER_1, Config.ORG2_PEER_1_URL);*/
			
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

			mychannel.joinPeer(peer0_org1);
			mychannel.joinPeer(peer1_org1);
			
			mychannel.addOrderer(orderer);

			mychannel.initialize();
			
			fabClient.getInstance().setUserContext(org2Admin);
			mychannel = fabClient.getInstance().getChannel("mychannel");
			mychannel.joinPeer(peer0_org2);
			mychannel.joinPeer(peer1_org2);
			
			Logger.getLogger(CreateChannel.class.getName()).log(Level.INFO, "Channel created "+mychannel.getName());
            Collection peers = mychannel.getPeers();
            Iterator peerIter = peers.iterator();
            while (peerIter.hasNext())
            {
            	  Peer pr = (Peer) peerIter.next();
            	  Logger.getLogger(CreateChannel.class.getName()).log(Level.INFO,pr.getName()+ " at " + pr.getUrl());
            }
            
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
