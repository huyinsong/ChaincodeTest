package org.app.config;

import static java.nio.charset.StandardCharsets.UTF_8;
import java.io.File;
import java.security.Security;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.app.user.UserContext;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.hyperledger.fabric.sdk.ChaincodeID;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.NetworkConfig;
import org.hyperledger.fabric.sdk.Orderer;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.hyperledger.fabric.sdk.QueryByChaincodeRequest;
import org.hyperledger.fabric.sdk.TransactionProposalRequest;
import org.hyperledger.fabric.sdk.BlockEvent.TransactionEvent;
import org.hyperledger.fabric.sdk.NetworkConfig.CAInfo;
import org.hyperledger.fabric.sdk.NetworkConfig.UserInfo;
import org.hyperledger.fabric.sdk.Peer.PeerRole;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.NetworkConfigurationException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric.sdk.exception.TransactionException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.hyperledger.fabric_ca.sdk.HFCAInfo;
import org.hyperledger.fabric_ca.sdk.RegistrationRequest;

public class FabricClient {
	private NetworkConfig networkConfig;
	private HFClient instance;
	private static final byte[] EXPECTED_EVENT_DATA = "!".getBytes(UTF_8);
	private static final String EXPECTED_EVENT_NAME = "event";

	public FabricClient(String networkyaml) {
		try {
			Security.addProvider(new BouncyCastleProvider());
			networkConfig = NetworkConfig.fromYamlFile(new File(networkyaml));	

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void initClient() {
		try {

			CryptoSuite cryptoSuite = CryptoSuite.Factory.getCryptoSuite();
			instance = HFClient.createNewInstance();
			instance.setCryptoSuite(cryptoSuite);
			instance.setUserContext(networkConfig.getPeerAdmin());

		}catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public HFClient getInstance() {
		return this.instance;
	}

	public Channel initChannel(String channelName) throws InvalidArgumentException, NetworkConfigurationException, TransactionException {
		Channel newChannel = instance.loadChannelFromConfig(channelName, networkConfig);
		if (newChannel == null) {
			throw new RuntimeException("Channel " + channelName + " is not defined in the config file!");
		}

		return newChannel.initialize();

	}

	public UserContext enrollUser(String username,String affiliation) throws Exception {
		NetworkConfig.OrgInfo org = networkConfig.getOrganizationInfo("Org1");
        CAInfo caInfo = org.getCertificateAuthorities().get(0);

        HFCAClient hfcaClient = HFCAClient.createNewInstance(caInfo);
        HFCAInfo info = hfcaClient.info();
        System.out.println(info.getCACertificateChain());

        Collection<UserInfo> registrars = caInfo.getRegistrars();
        UserInfo registrar = registrars.iterator().next();
        registrar.setEnrollment(hfcaClient.enroll(registrar.getName(), registrar.getEnrollSecret()));
        UserContext context = new UserContext();
        context.setName(username);
        context.setAffiliation(affiliation);
        RegistrationRequest rr = new RegistrationRequest(context.getName(), context.getAffiliation());
        context.setEnrollment(hfcaClient.enroll(context.getName(), hfcaClient.register(rr, registrar)));
        return context;
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
