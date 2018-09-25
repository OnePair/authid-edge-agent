package ca.onepair.edgeagent.api.service;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import org.bitcoinj.store.BlockStoreException;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import ca.onepair.authid.common.certs.AuthIDCert;
import ca.onepair.authid.common.certs.DHChallengeCert;
import ca.onepair.authid.common.certs.GenericAuthIDCert;
import ca.onepair.authid.common.certs.SignedChallengeCert;
import ca.onepair.authid.common.drivers.MasterAuthIDDriver;
import ca.onepair.authid.common.exceptions.AuthIDDriverException;
import ca.onepair.authid.common.exceptions.InvalidCertException;
import ca.onepair.authid.common.exceptions.MissingKeyException;
import ca.onepair.authid.common.exceptions.NoDriverException;
import ca.onepair.authid.common.model.AuthIDControllerDoc;
import ca.onepair.edgeagent.AuthIDEdgeAgent;
import ca.onepair.edgeagent.loaders.AuthIDDriverLoader;
import ca.onepair.edgeagent.utils.Utils;

/*
 * TODO: 1) Create specific exceptions and use those for HTTP status
 * 2) Check if two AuthID's have matching protocols
*/
@Service
public class EdgeAgentService {

	@Autowired
	private MasterAuthIDDriver authIDDriver;

	/*
	 * The permissionless methods
	 */

	public ResponseEntity<String> getID(String id) throws JSONException {
		JSONObject response = new JSONObject();
		HttpStatus status = HttpStatus.OK;

		String protocol = Utils.getProtocolFromID(id);

		if (protocol == null) {
			status = HttpStatus.SERVICE_UNAVAILABLE;
			response.put("message", "Protocol unavailable.");
		} else {
			try {
				AuthIDControllerDoc idDoc = this.authIDDriver.getAuthIDDriver(protocol)
						.retrieveID(Utils.removeProtocolFromID(id));
				response = idDoc.toJSON();
			} catch (Exception e) {
				status = HttpStatus.EXPECTATION_FAILED;
				response.put("message", e.getMessage());
			}
		}

		return new ResponseEntity<String>(response.toString(), status);
	}

	public ResponseEntity<String> createChallenge(String challengerID, String receiverID) throws JSONException {
		JSONObject response = new JSONObject();
		HttpStatus status = HttpStatus.CREATED;

		String protocol = Utils.getProtocolFromID(challengerID);

		if (protocol == null) {
			status = HttpStatus.SERVICE_UNAVAILABLE;
			response.put("message", "Protocol unavailable.");
		} else {
			DHChallengeCert challengeCert;
			try {
				challengeCert = this.authIDDriver.getAuthIDDriver(protocol).createChallenge(
						Utils.removeProtocolFromID(challengerID), Utils.removeProtocolFromID(receiverID));
				response = challengeCert.toJson();
			} catch (SQLException | NoSuchAlgorithmException | AuthIDDriverException e) {
				status = HttpStatus.INTERNAL_SERVER_ERROR;
			} catch (MissingKeyException e) {
				status = HttpStatus.FORBIDDEN;
				response.put("message", e.getMessage());
			} catch (NoDriverException e) {
				status = HttpStatus.SERVICE_UNAVAILABLE;
				response.put("message", e.getMessage());
			}

		}

		return new ResponseEntity<String>(response.toString(), status);
	}

	public ResponseEntity<String> verifySignedChallenge(String signedChallenge) throws JSONException {
		JSONObject response = new JSONObject();
		HttpStatus status = HttpStatus.OK;

		try {
			JSONObject signedChallengeJson = new JSONObject(signedChallenge);
			SignedChallengeCert signedChallengeCert = SignedChallengeCert.fromJson(signedChallengeJson);

			boolean verified = this.authIDDriver.getAuthIDDriver(signedChallengeCert.getIDDoc().getProtocol())
					.verifyChallenge(signedChallengeCert);
			response.put("verified", verified);
		} catch (JSONException | InvalidCertException e) {
			status = HttpStatus.BAD_REQUEST;
			response.put("message", e.getMessage());
		} catch (NoDriverException e) {
			status = HttpStatus.SERVICE_UNAVAILABLE;
			response.put("message", e.getMessage());
		} catch (Exception e) {
			status = HttpStatus.EXPECTATION_FAILED;
			response.put("message", e.getMessage());
		}

		return new ResponseEntity<String>(response.toString(), status);
	}

	public ResponseEntity<String> verifyCert(String cert) throws JSONException {
		JSONObject response = new JSONObject();
		HttpStatus status = HttpStatus.OK;

		try {
			JSONObject certJson = new JSONObject(cert);
			String protocol = certJson.getJSONObject("id_doc").getString("protocol");
			boolean verificationResult = this.authIDDriver.getAuthIDDriver(protocol).verifyCert(certJson);
			response.put("verified", verificationResult);
		} catch (JSONException e) {
			status = HttpStatus.BAD_REQUEST;
			response.put("message", e.getMessage());
		} catch (NoDriverException e) {
			status = HttpStatus.SERVICE_UNAVAILABLE;
			response.put("message", e.getMessage());
		} catch (Exception e) {
			status = HttpStatus.EXPECTATION_FAILED;
			response.put("message", e.getMessage());
		}

		return new ResponseEntity<String>(response.toString(), status);
	}

	public ResponseEntity<String> getRequest(String requestID) {
		JSONObject response = new JSONObject();
		HttpStatus status = HttpStatus.OK;

		try {
			JSONObject request = AuthIDEdgeAgent.getInstance().getRequest(requestID);

			if (request == null)
				status = HttpStatus.NO_CONTENT;
			else
				response = request;
		} catch (JSONException e) {
			status = HttpStatus.EXPECTATION_FAILED;
		}

		return new ResponseEntity<String>(response.toString(), status);
	}

	/*
	 * Permissioned methods
	 */

	public ResponseEntity<String> createAddress(String protocol) throws JSONException {
		JSONObject response = new JSONObject();
		HttpStatus status = HttpStatus.OK;

		try {
			String requestID = AuthIDEdgeAgent.getInstance().newAddress(this.authIDDriver.getAuthIDDriver(protocol),
					protocol);
			response.put("requestID", requestID);
		} catch (JSONException e) {
			status = HttpStatus.BAD_REQUEST;
			response.put("message", e.getMessage());
		} catch (NoDriverException e) {
			status = HttpStatus.SERVICE_UNAVAILABLE;
			response.put("message", e.getMessage());
		}

		return new ResponseEntity<String>(response.toString(), status);
	}

	public ResponseEntity<String> registerID(String id, String protocol, String address, int fee) throws JSONException {
		JSONObject response = new JSONObject();
		HttpStatus status = HttpStatus.OK;

		try {
			String requestID = AuthIDEdgeAgent.getInstance().registerID(this.authIDDriver.getAuthIDDriver(protocol), id,
					address, protocol, fee);
			response.put("requestID", requestID);
		} catch (JSONException e) {
			status = HttpStatus.BAD_REQUEST;
			response.put("message", e.getMessage());
		} catch (NoDriverException e) {
			status = HttpStatus.SERVICE_UNAVAILABLE;
			response.put("message", e.getMessage());
		}

		return new ResponseEntity<String>(response.toString(), status);
	}

	public ResponseEntity<String> transferID(String id, String address, String protocol) throws JSONException {
		JSONObject response = new JSONObject();
		HttpStatus status = HttpStatus.OK;

		String requestID;
		try {
			requestID = AuthIDEdgeAgent.getInstance().transferID(this.authIDDriver.getAuthIDDriver(protocol), id,
					address, protocol);
			response.put("requestID", requestID);
		} catch (JSONException e) {
			status = HttpStatus.EXPECTATION_FAILED;
			response.put("message", e.getMessage());
		} catch (NoDriverException e) {
			status = HttpStatus.SERVICE_UNAVAILABLE;
			response.put("message", e.getMessage());
		}

		return new ResponseEntity<String>(response.toString(), status);
	}

	public ResponseEntity<String> signChallenge(String challenge) throws JSONException {
		JSONObject response = new JSONObject();
		HttpStatus status = HttpStatus.OK;

		try {
			DHChallengeCert challengeCert = DHChallengeCert.fromJson(new JSONObject(challenge));
			challengeCert.getIDDoc().getProtocol();
			String requestID = AuthIDEdgeAgent.getInstance().signChallenge(
					this.authIDDriver.getAuthIDDriver(challengeCert.getIDDoc().getProtocol()), challengeCert);
			response.put("requestID", requestID);
		} catch (JSONException e) {
			status = HttpStatus.BAD_REQUEST;
			response.put("message", e.getMessage());
		} catch (NoDriverException e) {
			status = HttpStatus.SERVICE_UNAVAILABLE;
			response.put("message", e.getMessage());
		}

		return new ResponseEntity<String>(response.toString(), status);
	}

	public ResponseEntity<String> signCert(String claims, String id) throws JSONException {
		JSONObject response = new JSONObject();
		HttpStatus status = HttpStatus.OK;

		String protocol = Utils.getProtocolFromID(id);

		if (protocol == null) {
			status = HttpStatus.SERVICE_UNAVAILABLE;
			response.put("message", "Protocol unavailable.");
		} else {
			try {
				JSONObject claimsJson = new JSONObject(claims);
				AuthIDCert authIDCert = GenericAuthIDCert.fromClaimsJson(claimsJson);
				String requestID = AuthIDEdgeAgent.getInstance().signCert(this.authIDDriver.getAuthIDDriver(protocol),
						authIDCert, Utils.removeProtocolFromID(id));
				response.put("requestID", requestID);
			} catch (NoDriverException e) {
				status = HttpStatus.SERVICE_UNAVAILABLE;
				response.put("message", e.getMessage());
			}
		}

		return new ResponseEntity<String>(response.toString(), status);
	}

	public ResponseEntity<String> generateProcessorKeys(String id) throws JSONException {
		JSONObject response = new JSONObject();
		HttpStatus status = HttpStatus.OK;

		String protocol = Utils.getProtocolFromID(id);

		if (protocol == null) {
			status = HttpStatus.SERVICE_UNAVAILABLE;
		} else {
			try {
				String requestID = AuthIDEdgeAgent.getInstance().generateProcessorKeys(
						this.authIDDriver.getAuthIDDriver(protocol), Utils.removeProtocolFromID(id), protocol);
				response.put("requestID", requestID);
			} catch (JSONException e) {
				status = HttpStatus.SERVICE_UNAVAILABLE;
				response.put("message", e.getMessage());
			} catch (NoDriverException e) {
				status = HttpStatus.SERVICE_UNAVAILABLE;
				response.put("message", e.getMessage());
			}

		}

		return new ResponseEntity<String>(response.toString(), status);
	}

	@Bean
	public MasterAuthIDDriver authIDDriver()
			throws ClassNotFoundException, SQLException, UnreadableWalletException, IOException, BlockStoreException {
		return AuthIDDriverLoader.loadDriver();
	}
}
