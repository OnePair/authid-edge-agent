package ca.onepair.edgeagent.api.controllers;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import ca.onepair.edgeagent.api.service.EdgeAgentService;

@RestController
@RequestMapping("/api/v0.0.1")
public class EdgeAgentController {
	/*
	 * Permissionless methods
	 */

	@Autowired
	private EdgeAgentService edgeAgentService;

	@RequestMapping(path = "ids/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<String> getID(@PathVariable("id") String id) throws JSONException {
		return this.edgeAgentService.getID(id);
	}

	@RequestMapping(value = "challenges", method = RequestMethod.POST, consumes = { MediaType.MULTIPART_FORM_DATA_VALUE,
			MediaType.APPLICATION_JSON_VALUE }, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<String> createChallenge(@RequestParam("challengerID") String challengerID,
			@RequestParam("receiverID") String receiverID) throws JSONException {
		return this.edgeAgentService.createChallenge(challengerID, receiverID);
	}

	@RequestMapping(value = "challenges:verify", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<String> verifyChallenge(@RequestBody String signedChallenge) throws JSONException {
		return this.edgeAgentService.verifySignedChallenge(signedChallenge);
	}

	@RequestMapping(value = "certs:verify", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<String> verifyCerts(@RequestBody String cert) throws JSONException {
		return this.edgeAgentService.verifyCert(cert);
	}

	@RequestMapping(path = "requests/{requestID}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<String> getRequest(@PathVariable("requestID") String requestID) {
		return this.edgeAgentService.getRequest(requestID);
	}

	/*
	 * Permissioned methods
	 */

	@RequestMapping(path = "addresses/{protocol}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<String> createAddress(@PathVariable("protocol") String protocol) throws JSONException {
		return this.edgeAgentService.createAddress(protocol);
	}

	@RequestMapping(path = "ids/{id}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<String> registerID(@PathVariable("id") String id, @RequestParam("protocol") String protocol,
			@RequestParam("address") String address,
			@RequestParam(value = "fee", required = false, defaultValue = "0") int fee) throws JSONException {
		return this.edgeAgentService.registerID(id, protocol, address, fee);
	}

	@RequestMapping(value = "ids:transfer", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<String> transferID(@RequestParam("id") String id, @RequestParam("address") String address,
			@RequestParam("protocol") String protocol) throws JSONException {
		return this.edgeAgentService.transferID(id, address, protocol);
	}

	@RequestMapping(value = "challenges:sign", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<String> signChallenge(@RequestBody String challenge) throws JSONException {
		return this.edgeAgentService.signChallenge(challenge);
	}

	/*
	 * Generation and signing should be done separately
	 */
	@RequestMapping(path = "processorKeys/{id}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<String> generateProcessorKeys(@PathVariable String id) throws JSONException {
		return this.edgeAgentService.generateProcessorKeys(id);
	}

}
