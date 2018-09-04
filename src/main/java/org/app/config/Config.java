package org.app.config;

import java.io.File;

public class Config {
	
	public static final String ORG1_MSP = "Org1MSP";

	public static final String ORG1 = "org1";

	public static final String ORG2_MSP = "Org2MSP";

	public static final String ORG2 = "org2";

	public static final String ADMIN = "admin";

	public static final String ADMIN_PASSWORD = "adminpw";
	
	public static final String CHANNEL_CONFIG_PATH = "/Users/huyinsong/workspace/go/src/github.com/hyperledger/fabric-samples/fabric-ca/data/channel.tx";
	
	public static final String ORG1_USR_BASE_PATH = "/Users/huyinsong/workspace/go/src/github.com/hyperledger/fabric-samples/fabric-ca/data/orgs/org1/admin/msp";
	
	public static final String ORG2_USR_BASE_PATH = "/Users/huyinsong/workspace/go/src/github.com/hyperledger/fabric-samples/fabric-ca/data/orgs/org2/admin/msp";
	
	public static final String ORG1_USR_ADMIN_PK = ORG1_USR_BASE_PATH + File.separator + "keystore";
	public static final String ORG1_USR_ADMIN_CERT = ORG1_USR_BASE_PATH + File.separator + "admincerts";

	public static final String ORG2_USR_ADMIN_PK = ORG2_USR_BASE_PATH + File.separator + "keystore";
	public static final String ORG2_USR_ADMIN_CERT = ORG2_USR_BASE_PATH + File.separator + "admincerts";
	
	public static final String CA_ORG1_URL = "http://ica-org1:7054";
	
	public static final String CA_ORG2_URL = "http://ica-org2:7054";
	
	public static final String ORDERER_URL = "grpc://orderer1-org0:7050";
	
	public static final String ORDERER_NAME = "orderer1-org0";
	
	public static final String CHANNEL_NAME = "mychannel";
	
	public static final String ORG1_PEER_0 = "peer1.org1";
	
	public static final String ORG1_PEER_0_URL = "grpc://peer1-org1:7051";
	
	public static final String ORG1_PEER_1 = "peer2.org1";
	
	public static final String ORG1_PEER_1_URL = "grpc://peer2-org1:7051";
	
    public static final String ORG2_PEER_0 = "peer1.org2";
	
	public static final String ORG2_PEER_0_URL = "grpc://peer1.org2:7051";
	
	public static final String ORG2_PEER_1 = "peer2.org2";
	
	public static final String ORG2_PEER_1_URL = "grpc://peer2.org2:7051";
	
	public static final String CHAINCODE_ROOT_DIR = "/Users/huyinsong/workspace/java/ChaincodeTest/resources/chaincode";
	
	public static final String CHAINCODE_1_NAME = "test";
	
	public static final String CHAINCODE_1_PATH = "test";
	
	public static final String CHAINCODE_1_VERSION = "1";


}
