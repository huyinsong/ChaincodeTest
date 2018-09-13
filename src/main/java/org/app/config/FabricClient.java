package org.app.config;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.security.Security;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.app.user.UserContext;
import org.app.util.Util;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.hyperledger.fabric.sdk.ChaincodeID;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.Orderer;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.hyperledger.fabric.sdk.QueryByChaincodeRequest;
import org.hyperledger.fabric.sdk.TransactionProposalRequest;
import org.hyperledger.fabric.sdk.BlockEvent.TransactionEvent;
import org.hyperledger.fabric.sdk.Channel.PeerOptions;
import org.hyperledger.fabric.sdk.Peer.PeerRole;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.json.JSONObject;

public class FabricClient {
	private HFClient instance;
	private JSONObject connection;
	private String mspid;
	private String orgname;
	private UserContext context;
	private String crypto_config_path;
	private static final byte[] EXPECTED_EVENT_DATA = "!".getBytes(UTF_8);
	private static final String EXPECTED_EVENT_NAME = "event";
	public void initClient(String connection_file) {
		try {
			Security.addProvider(new BouncyCastleProvider());
			
			Iterable<Object> configuration = Util.loadConfig(connection_file);
			Map<String,Object> map= (HashMap<String,Object>)configuration.iterator().next();
			connection = new JSONObject(map);
			orgname=connection.getJSONObject("client").getString("organization");
			mspid=connection.getJSONObject("organizations").getJSONObject(orgname).getString("mspid");
			crypto_config_path=connection.getJSONObject("client").getJSONObject("cryptoconfig").getString("path");
			String PATH_PK_ADMIN=connection.getJSONObject("organizations").getJSONObject(orgname).getJSONObject("adminPrivateKey").getString("pem");
			String PATH_CERT_ADMIN=connection.getJSONObject("organizations").getJSONObject(orgname).getJSONObject("signedCert").getString("path");
			
			context = new UserContext();
			context.setMspId(mspid);
			context.setName("admin");
			context.setEnrollment(Util.getEnrollment(PATH_PK_ADMIN, crypto_config_path+"/"+PATH_CERT_ADMIN));
			
			CryptoSuite cryptoSuite = CryptoSuite.Factory.getCryptoSuite();
			instance = HFClient.createNewInstance();
			instance.setCryptoSuite(cryptoSuite);
			instance.setUserContext(context);

		}catch (Exception e) {
			e.printStackTrace();
		}


	}

	public HFClient getInstance() {
		return this.instance;
	}
	
	public void initChannel(String channelName) {
		try {
			Channel channel = instance.getChannel(channelName);
			if(null == channel) {
				channel = instance.newChannel(channelName);
			}
			this.addOrderer(channelName);
			this.addPeers(channelName);
			channel.initialize();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	// Add orderer to channel.
	public void addOrderer(String channelName) {
		connection.getJSONObject("channels").getJSONObject(channelName).getJSONArray("orderers").forEach(key -> {
			try {
				String orderer_name= (String)key;
				JSONObject node = connection.getJSONObject("orderers").getJSONObject(orderer_name);
				Properties properties = new Properties();
				properties.put("pemBytes", node.optJSONObject("tlsCACerts").optString("pem").getBytes());
				properties.put("hostnameOverride", node.optJSONObject("grpcOptions").optString("ssl-target-name-override"));
				properties.put("sshProvider", "openSSL");
				properties.put("negotiationType", "TLS");
				Orderer orderer = instance.newOrderer(orderer_name, node.getString("url"), properties);
				instance.getChannel(channelName).addOrderer(orderer);
			}catch (Exception e) {
				e.printStackTrace();
			}
		});	
	}

	public void addPeers(String channelName) {
		connection.getJSONObject("channels").getJSONObject(channelName).getJSONObject("peers").toMap().forEach((key,value)->{
			String peer_name = (String)key;
			Map<String,Boolean> peer_values = (HashMap<String,Boolean>)value;
			try {
				JSONObject node = connection.getJSONObject("peers").getJSONObject(peer_name);
				Properties properties = new Properties();
				properties.put("pemFile", this.crypto_config_path+"/"+node.optJSONObject("tlsCACerts").optString("path"));
				properties.put("hostnameOverride", node.optJSONObject("grpcOptions").optString("ssl-target-name-override"));
				properties.put("sshProvider", "openSSL");
				properties.put("negotiationType", "TLS");
				Peer peer = instance.newPeer(peer_name, node.getString("url"), properties);
				if(null != instance.getChannel(channelName)) {

					final PeerOptions peerOptions =  PeerOptions.createPeerOptions();
					if(peer_values.containsKey("endorsingPeer")&&peer_values.get("endorsingPeer")) {
						peerOptions.addPeerRole(PeerRole.ENDORSING_PEER);
					}
					if(peer_values.containsKey("chaincodeQuery")&&peer_values.get("chaincodeQuery")) {
						peerOptions.addPeerRole(PeerRole.CHAINCODE_QUERY);
					}

					if(peer_values.containsKey("ledgerQuery")&&peer_values.get("ledgerQuery")) {
						peerOptions.addPeerRole(PeerRole.LEDGER_QUERY);
					}

					if(peer_values.containsKey("eventSource")&&peer_values.get("eventSource")) {
						peerOptions.registerEventsForBlocks().registerEventsForFilteredBlocks().addPeerRole(PeerRole.EVENT_SOURCE);
					}
					instance.getChannel(channelName).addPeer(peer, peerOptions);
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
		});
	}

	public CompletableFuture<TransactionEvent> invokeChaincode(String channel_name,String function,String key, String value,String chaincode_id,String version)throws ProposalException, InvalidArgumentException, InterruptedException, ExecutionException, TimeoutException {
		//Package transaction proposal request.
		TransactionProposalRequest request = instance.newTransactionProposalRequest();
		ChaincodeID ccid = ChaincodeID.newBuilder().setName(chaincode_id).setVersion(version).build();
		request.setChaincodeID(ccid);
		request.setFcn(function);
		String[] arguments = { key,value };
		request.setArgs(arguments);
		request.setProposalWaitTime(1000);

		Map<String, byte[]> tm2 = new HashMap<>();
		tm2.put("HyperLedgerFabric", "TransactionProposalRequest:JavaSDK".getBytes(UTF_8)); 																								
		tm2.put("method", "TransactionProposalRequest".getBytes(UTF_8)); 
		tm2.put("result", ":)".getBytes(UTF_8));
		tm2.put(EXPECTED_EVENT_NAME, EXPECTED_EVENT_DATA); 
		request.setTransientMap(tm2);

		Channel channel = instance.getChannel(channel_name);
		Collection<ProposalResponse> successful = new LinkedList<>();
		Collection<ProposalResponse> failed = new LinkedList<>();
		if(null!= channel) {
			Collection<ProposalResponse> response = channel.sendTransactionProposal(request, channel.getPeers());
			for (ProposalResponse pres : response) {
				if (pres.getStatus() == ProposalResponse.Status.SUCCESS) {
					successful.add(pres);
				}else {
					failed.add(pres);
				}
			}

			Channel.NOfEvents nOfEvents = Channel.NOfEvents.createNofEvents();
			if (!channel.getPeers(EnumSet.of(PeerRole.EVENT_SOURCE)).isEmpty()) {
				nOfEvents.addPeers(channel.getPeers(EnumSet.of(PeerRole.EVENT_SOURCE)));
			}
			Collection<Orderer> orderers = channel.getOrderers();
			if(successful.size()== channel.getPeers().size()) {
				CompletableFuture<TransactionEvent> result = channel.sendTransaction(successful, Channel.TransactionOptions.createTransactionOptions().orderers(orderers).shuffleOrders(false).nOfEvents(nOfEvents));
				return result;
			}else {
				return null;
			}
		}else {
			return null;
		}
	}
	
	
	public Collection<ProposalResponse> queryByChainCode(String channelName,String chaincodeName, String functionName, String key) throws InvalidArgumentException, ProposalException {
		QueryByChaincodeRequest request = instance.newQueryProposalRequest();
		ChaincodeID ccid = ChaincodeID.newBuilder().setName(chaincodeName).build();
		request.setChaincodeID(ccid);
		request.setFcn(functionName);
		if (key != null) {
			String[] args = { key };
			request.setArgs(args);
		}
		Channel channel = instance.getChannel(channelName);
		if(null!=channel) {
			Collection<ProposalResponse> response = instance.getChannel(channelName).queryByChaincode(request);
			return response;
		}else {
			return null;
		}
		
	}
}
