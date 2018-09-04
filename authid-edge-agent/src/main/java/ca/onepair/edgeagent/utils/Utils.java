package ca.onepair.edgeagent.utils;

public class Utils {

	public static String getProtocolFromID(String id) {
		if (!id.contains("."))
			return null;

		return id.substring(id.indexOf(".")).replace(".", "").toUpperCase();
	}

	public static String removeProtocolFromID(String id) {
		return id.substring(0, id.lastIndexOf("."));
	}

}
