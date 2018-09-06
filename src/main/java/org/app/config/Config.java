package org.app.config;

import java.io.File;

public class Config {
	
	public static final String ORG1_MSP = "Org1MSP";

	public static final String ORG1 = "org1";

	public static final String ORG2_MSP = "Org2MSP";

	public static final String ORG2 = "org2";

	public static final String ADMIN = "admin";

	public static final String ADMIN_PASSWORD = "adminpw";
	
	public static final String CHANNEL_CONFIG_PATH = "/Users/huyinsong/workspace/java/blockchain-application-using-fabric-java-sdk/network_resources/config/channel.tx";
	
	public static final String ORG1_USR_BASE_PATH = "/Users/huyinsong/workspace/java/blockchain-application-using-fabric-java-sdk/network_resources/crypto-config/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp";
	
	public static final String ORG2_USR_BASE_PATH = "/Users/huyinsong/workspace/java/blockchain-application-using-fabric-java-sdk/network_resources/crypto-config/peerOrganizations/org2.example.com/users/Admin@org2.example.com/msp";
	
	public static final String ORG1_USR_ADMIN_PK = ORG1_USR_BASE_PATH + "/keystore";
	public static final String ORG1_USR_ADMIN_CERT = ORG1_USR_BASE_PATH + "/admincerts";

	public static final String ORG2_USR_ADMIN_PK = ORG2_USR_BASE_PATH + "/keystore";
	public static final String ORG2_USR_ADMIN_CERT = ORG2_USR_BASE_PATH + "/admincerts";
	
	public static final String CA_ORG1_URL = "http://localhost:7054";
	
	public static final String CA_ORG2_URL = "http://localhost:8054";
	
	public static final String ORDERER_URL = "grpcs://localhost:7050";
	
	public static final String ORDERER_NAME = "orderer.example.com";
	
	public static final String CHANNEL_NAME = "mychannel";
	
	public static final String ORG1_PEER_0 = "peer0.org1.example.com";
	
	public static final String ORG1_PEER_0_URL = "grpcs://localhost:7051";
	
	public static final String ORG1_PEER_0_EVENT = "grpcs://localhost:7053";
	
	public static final String ORG1_PEER_1 = "peer1.org1.example.com";
	
	public static final String ORG1_PEER_1_URL = "grpcs://localhost:8051";
	
	public static final String ORG1_PEER_1_EVENT = "grpcs://localhost:8053";
	
    public static final String ORG2_PEER_0 = "peer0.org2.example.com";
	
	public static final String ORG2_PEER_0_URL = "grpcs://localhost:9051";
	
	public static final String ORG2_PEER_0_EVENT = "grpcs://localhost:9053";

	public static final String ORG2_PEER_1 = "peer1.org2.example.com";
	
	public static final String ORG2_PEER_1_URL = "grpcs://localhost:10051";
	
	public static final String ORG2_PEER_1_EVENT = "grpcs://localhost:10053";
	
	public static final String CHAINCODE_ROOT_DIR = "/Users/huyinsong/workspace/java/blockchain-application-using-fabric-java-sdk/network_resources/chaincode";
	
	public static final String CHAINCODE_1_NAME = "test";
	
	public static final String CHAINCODE_1_PATH = "github.com/test";
	
	public static final String CHAINCODE_1_VERSION = "1";
	
	public static final String ORDERER_TLS_CERT_PATH = "/Users/huyinsong/workspace/java/blockchain-application-using-fabric-java-sdk/network_resources/crypto-config/ordererOrganizations/example.com/orderers/orderer.example.com/tls";

	public static final String PEER0_ORG1_TLS_CERT_PATH = "/Users/huyinsong/workspace/java/blockchain-application-using-fabric-java-sdk/network_resources/crypto-config/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls";

	public static final String PEER1_ORG1_TLS_CERT_PATH = "/Users/huyinsong/workspace/java/blockchain-application-using-fabric-java-sdk/network_resources/crypto-config/peerOrganizations/org1.example.com/peers/peer1.org1.example.com/tls";
	
	public static final String PEER0_ORG2_TLS_CERT_PATH = "/Users/huyinsong/workspace/java/blockchain-application-using-fabric-java-sdk/network_resources/crypto-config/peerOrganizations/org2.example.com/peers/peer0.org2.example.com/tls";
	
	public static final String PEER1_ORG2_TLS_CERT_PATH = "/Users/huyinsong/workspace/java/blockchain-application-using-fabric-java-sdk/network_resources/crypto-config/peerOrganizations/org2.example.com/peers/peer1.org2.example.com/tls";

}
