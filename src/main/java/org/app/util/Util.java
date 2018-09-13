package org.app.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.io.IOUtils;
import org.app.user.CAEnrollment;
import org.app.user.UserContext;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric.sdk.exception.CryptoException;
import org.yaml.snakeyaml.Yaml;

public class Util {

	/**
	 * Serialize user
	 * 
	 * @param userContext
	 * @throws Exception
	 */
	public static void writeUserContext(UserContext userContext) throws Exception {
		String directoryPath = "users/" + userContext.getAffiliation();
		String filePath = directoryPath + "/" + userContext.getName() + ".ser";
		File directory = new File(directoryPath);
		if (!directory.exists())
			directory.mkdirs();

		FileOutputStream file = new FileOutputStream(filePath);
		ObjectOutputStream out = new ObjectOutputStream(file);

		// Method for serialization of object
		out.writeObject(userContext);

		out.close();
		file.close();
	}

	/**
	 * Deserialize user
	 * 
	 * @param affiliation
	 * @param username
	 * @return
	 * @throws Exception
	 */
	public static UserContext readUserContext(String affiliation, String username) throws Exception {
		String filePath = "users/" + affiliation + "/" + username + ".ser";
		File file = new File(filePath);
		if (file.exists()) {
			// Reading the object from a file
			FileInputStream fileStream = new FileInputStream(filePath);
			ObjectInputStream in = new ObjectInputStream(fileStream);

			// Method for deserialization of object
			UserContext uContext = (UserContext) in.readObject();

			in.close();
			fileStream.close();
			return uContext;
		}

		return null;
	}

	/**
	 * Create enrollment from key and certificate files.
	 * 
	 * @param folderPath
	 * @param keyFileName
	 * @param certFileName
	 * @return
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 * @throws CryptoException
	 */
	public static CAEnrollment getEnrollment(String keyFolderPath,  String keyFileName,  String certFolderPath, String certFileName)
			throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, CryptoException {
		PrivateKey key = null;
		String certificate = null;
		InputStream isKey = null;
		BufferedReader brKey = null;

		try {

			isKey = new FileInputStream(keyFolderPath + File.separator + keyFileName);
			brKey = new BufferedReader(new InputStreamReader(isKey));
			StringBuilder keyBuilder = new StringBuilder();

			for (String line = brKey.readLine(); line != null; line = brKey.readLine()) {
				if (line.indexOf("PRIVATE") == -1) {
					keyBuilder.append(line);
				}
			}

			certificate = new String(Files.readAllBytes(Paths.get(certFolderPath, certFileName)));

			byte[] encoded = DatatypeConverter.parseBase64Binary(keyBuilder.toString());
			PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
			KeyFactory kf = KeyFactory.getInstance("ECDSA");
			key = kf.generatePrivate(keySpec);
		} finally {
			isKey.close();
			brKey.close();
		}

		CAEnrollment enrollment = new CAEnrollment(key, certificate);
		return enrollment;
	}

	public static CAEnrollment getEnrollment(String keyStr,  String certfile)throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, CryptoException {
		PrivateKey key = null;
		String certificate = null;
		InputStream isKey = null;
		BufferedReader brKey = null;
		InputStream isCert = null;
		BufferedReader brCert = null;

		try {
			
			isKey = new ByteArrayInputStream(keyStr.getBytes());
			brKey = new BufferedReader(new InputStreamReader(isKey));
			StringBuilder keyBuilder = new StringBuilder();

			for (String line = brKey.readLine(); line != null; line = brKey.readLine()) {
				if (line.indexOf("PRIVATE") == -1) {
					keyBuilder.append(line);
				}
			}
			
			byte[] encoded = DatatypeConverter.parseBase64Binary(keyBuilder.toString());
			PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
			KeyFactory kf = KeyFactory.getInstance("ECDSA");
			key = kf.generatePrivate(keySpec);
			
			
			isCert = new FileInputStream(certfile);
			brCert = new BufferedReader(new InputStreamReader(isCert));
			StringBuilder certBuilder = new StringBuilder();
			for (String line = brCert.readLine(); line != null; line = brCert.readLine()) {
				certBuilder.append(line);
			}
			certificate = new String(certBuilder.toString());

			
		} finally {
			isKey.close();
			brKey.close();
			isCert.close();
			brCert.close();
		}

		CAEnrollment enrollment = new CAEnrollment(key, certificate);
		return enrollment;
	}
	
	public static void cleanUp() {
		String directoryPath = "users";
		File directory = new File(directoryPath);
		deleteDirectory(directory);
	}

	public static boolean deleteDirectory(File dir) {
		if (dir.isDirectory()) {
			File[] children = dir.listFiles();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDirectory(children[i]);
				if (!success) {
					return false;
				}
			}
		}

		// either file or an empty directory
		Logger.getLogger(Util.class.getName()).log(Level.INFO, "Deleting - " + dir.getName());
		return dir.delete();
	}

	public static User getUser(final String name, final String mspId, File privateKeyFile, File certificateFile)
			throws IOException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException {
		try {
			final String certificate = new String(IOUtils.toByteArray(new FileInputStream(certificateFile)), "UTF-8");
			final PrivateKey privateKey = getPrivateKeyFromBytes(
					IOUtils.toByteArray(new FileInputStream(privateKeyFile)));
			User user = new User() {
				public String getName() {
					return name;
				}

				public Set<String> getRoles() {
					return null;
				}

				public String getAccount() {
					return null;
				}

				public String getAffiliation() {
					return null;
				}

				public Enrollment getEnrollment() {
					return new Enrollment() {
						public PrivateKey getKey() {
							return privateKey;
						}

						public String getCert() {
							return certificate;
						}
					};
				}

				public String getMspId() {
					return mspId;
				}
			};
			return user;
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			throw e;
		} catch (NoSuchProviderException e) {
			e.printStackTrace();
			throw e;
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
			throw e;
		} catch (ClassCastException e) {
			e.printStackTrace();
			throw e;
		}
	}

	public static PrivateKey getPrivateKeyFromBytes(byte[] data)
			throws IOException, NoSuchProviderException, NoSuchAlgorithmException, InvalidKeySpecException {
		final Reader pemReader = new StringReader(new String(data));
		final PrivateKeyInfo pemPair;
		try (PEMParser pemParser = new PEMParser(pemReader)) {
			pemPair = (PrivateKeyInfo) pemParser.readObject();
		}
		PrivateKey privateKey = new JcaPEMKeyConverter().setProvider(BouncyCastleProvider.PROVIDER_NAME)
				.getPrivateKey(pemPair);
		return privateKey;
	}
	
	public static Iterable<Object>  loadConfig(String config_file) {
		Iterable<Object> configuration = null;
		try {
			Yaml yaml = new Yaml();
			configuration = yaml.loadAll(new FileInputStream(new File(config_file)));
			return configuration;
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
		
	}
	
	public static void main(String[] args) {
		Iterable<Object> configuration = Util.loadConfig("/home/huyinsong/Documents/connection.yaml");
		for (Object obj: configuration) {
			
			Map<String,Object> map= (HashMap<String,Object>)obj;
			System.out.println(map.get("client"));
			
		}
	}
}
