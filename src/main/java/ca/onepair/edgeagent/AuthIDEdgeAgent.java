package ca.onepair.edgeagent;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import ca.onepair.authid.common.certs.DHChallengeCert;
import ca.onepair.authid.common.certs.SignedChallengeCert;
import ca.onepair.authid.common.drivers.AuthIDDriver;
import ca.onepair.authid.common.exceptions.AuthIDDriverException;
import ca.onepair.authid.common.exceptions.DoesNotExistException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

@SpringBootApplication
public class AuthIDEdgeAgent extends Application {
	private static final String DIALOG_TITLE = "AuthID Agent";
	private static final String REQUEST_STATUS = "status";
	private static final String WAITING = "waiting";
	private static final String DONE = "confirmed";
	private static final String REJECTED = "rejected";

	private ConfigurableApplicationContext springContext;
	private Map<String, JSONObject> requests; // TODO: delete abandonned

	private static AuthIDEdgeAgent instance;

	public static void main(String args[]) {
		Application.launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		Platform.setImplicitExit(false);

		this.springContext = SpringApplication.run(AuthIDEdgeAgent.class);
		this.requests = new HashMap<String, JSONObject>();

		AuthIDEdgeAgent.instance = this;
	}

	public String newAddress(AuthIDDriver authIDDriver, String protocol) throws JSONException {
		JSONObject request = AuthIDEdgeAgent.createRequestObject();
		String requestID = AuthIDEdgeAgent.getUniqueID();

		this.requests.put(requestID, request);

		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				// Create the dialog
				Alert dialog = createConfirmationDialog("ID Address",
						"Do you want to create a new " + protocol + " address?");
				// Show the dialog
				Optional<ButtonType> result = dialog.showAndWait();

				if (result.get() == ButtonType.OK) {
					try {
						String address = authIDDriver.newAddress();

						// Update the request
						requests.get(requestID).put(REQUEST_STATUS, DONE);
						requests.get(requestID).put("address", address);
					} catch (IOException | JSONException e) {
						try {
							requests.get(requestID).put(REQUEST_STATUS, REJECTED);
							requests.get(requestID).put("message", e.getMessage());
						} catch (JSONException ignored) {
						}
					}
				} else {
					// Update the request
					try {
						requests.get(requestID).put(REQUEST_STATUS, REJECTED);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

		});

		return requestID;
	}

	public String registerID(AuthIDDriver authIDDriver, String id, String address, String protocol)
			throws JSONException {
		JSONObject request = AuthIDEdgeAgent.createRequestObject();
		String requestID = AuthIDEdgeAgent.getUniqueID();

		this.requests.put(requestID, request);

		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				// Create the dialog
				Alert dialog = createConfirmationDialog("Register ID",
						"Do you want to register " + id + "." + protocol);
				// Show the dialog
				Optional<ButtonType> result = dialog.showAndWait();

				if (result.get() == ButtonType.OK) {
					try {
						String txReference = authIDDriver.registerId(id, address, 0);

						// Update the request
						requests.get(requestID).put(REQUEST_STATUS, DONE);
						requests.get(requestID).put("txReference", txReference);
					} catch (Exception e) {
						try {
							requests.get(requestID).put(REQUEST_STATUS, REJECTED);
							requests.get(requestID).put("message", e.getMessage());
						} catch (JSONException ignored) {
						}
					}
				} else {
					// Update the request
					try {
						requests.get(requestID).put(REQUEST_STATUS, REJECTED);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

		});

		return requestID;
	}

	public String transferID(AuthIDDriver authIDDriver, String id, String address, String protocol)
			throws JSONException {
		JSONObject request = AuthIDEdgeAgent.createRequestObject();
		String requestID = AuthIDEdgeAgent.getUniqueID();

		this.requests.put(requestID, request);

		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				// Create the dialog
				Alert dialog = createConfirmationDialog("Transfer ID",
						"Do you want to transfer " + id + "." + protocol);
				// Show the dialog
				Optional<ButtonType> result = dialog.showAndWait();

				if (result.get() == ButtonType.OK) {
					try {
						String txReference = authIDDriver.transferID(id, address);

						// Update the request
						requests.get(requestID).put(REQUEST_STATUS, DONE);
						requests.get(requestID).put("txReference", txReference);
					} catch (Exception e) {
						try {
							requests.get(requestID).put(REQUEST_STATUS, REJECTED);
							requests.get(requestID).put("message", e.getMessage());
						} catch (JSONException ignored) {
						}
					}
				} else {
					// Update the request
					try {
						requests.get(requestID).put(REQUEST_STATUS, REJECTED);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

		});

		return requestID;
	}

	public String signChallenge(AuthIDDriver authIDDriver, DHChallengeCert challenge) throws JSONException {
		JSONObject request = AuthIDEdgeAgent.createRequestObject();
		String requestID = AuthIDEdgeAgent.getUniqueID();

		this.requests.put(requestID, request);

		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				// Create the dialog
				Alert dialog = createConfirmationDialog("Incoming verification request",
						"Do you want to pursue with a response?");
				// Show the dialog
				Optional<ButtonType> result = dialog.showAndWait();

				if (result.get() == ButtonType.OK) {
					try {
						SignedChallengeCert signedChallenge = authIDDriver.signChallenge(challenge);

						// Update the request
						requests.get(requestID).put(REQUEST_STATUS, DONE);
						requests.get(requestID).put("signedChallenge", signedChallenge.toJson());
					} catch (Exception e) {
						try {
							requests.get(requestID).put(REQUEST_STATUS, REJECTED);
							requests.get(requestID).put("message", e.getMessage());
						} catch (JSONException ignored) {
						}
					}
				} else {
					// Update the request
					try {
						requests.get(requestID).put(REQUEST_STATUS, REJECTED);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

		});

		return requestID;
	}

	public String generateProcessorKeys(AuthIDDriver authIDDriver, String id, String protocol) throws JSONException {
		JSONObject request = AuthIDEdgeAgent.createRequestObject();
		String requestID = AuthIDEdgeAgent.getUniqueID();

		this.requests.put(requestID, request);

		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				Alert dialog = createConfirmationDialog("Generate Processor Keys",
						"Do you want to create some more keys?");
				// Show the dialog
				Optional<ButtonType> result = dialog.showAndWait();

				if (result.get() == ButtonType.OK) {
					try {
						// Generate the processor keys
						authIDDriver.generateProcessorKeys(id, 10);
						// Sign the processor keys
						authIDDriver.signProcessorKeys(id);

						requests.get(requestID).put(REQUEST_STATUS, DONE);
					} catch (DoesNotExistException | SQLException | AuthIDDriverException | JSONException e) {
						try {
							requests.get(requestID).put(REQUEST_STATUS, REJECTED);
							requests.get(requestID).put("message", e.getMessage());
						} catch (JSONException e1) {
							e1.printStackTrace();
						}
					}
				} else {
					try {
						requests.get(requestID).put(REQUEST_STATUS, REJECTED);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}

			}
		});

		return requestID;
	}

	public JSONObject getRequest(String requestID) throws JSONException {
		JSONObject request = this.requests.get(requestID);

		if (request == null)
			return null;

		if (!request.getString(REQUEST_STATUS).equals(WAITING)) {
			this.requests.remove(requestID);
		}

		return request;
	}

	private Alert createConfirmationDialog(String header, String contentText) {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle(DIALOG_TITLE);
		alert.setHeaderText(header);
		alert.setContentText(contentText);

		return alert;
	}

	public static AuthIDEdgeAgent getInstance() {
		return AuthIDEdgeAgent.instance;
	}

	private static String getUniqueID() {
		return UUID.randomUUID().toString();
	}

	private static JSONObject createRequestObject() throws JSONException {
		JSONObject request = new JSONObject();
		request.put(REQUEST_STATUS, WAITING);

		return request;
	}

}
