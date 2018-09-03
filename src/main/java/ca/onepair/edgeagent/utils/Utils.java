package ca.onepair.edgeagent.utils;

public class Utils {

	public static String getProtocolFromID(String id) {
		System.out.println("getting protocol from id: " + id);
		System.out.println("Does id contain dot ??? " + id.contains("."));

		if (!id.contains("."))
			return null;

		return id.substring(id.indexOf(".")).replace(".", "").toUpperCase();
	}

	public static String removeProtocolFromID(String id) {
		return id.substring(0, id.lastIndexOf("."));
	}

}
