package srvrotator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.UUID;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

public class SRVRotator {
	static File domains = new File("domains.txt");
	static File config = new File("config.json");
	static String xauthemail;
	static String xauthkey;
	static String zoneid;
	static String recordid;
	public static void main(String[] args) throws IOException, UnirestException {
		initalize();
		System.out.println("Started. This could take a while...");
		String unblacklisted;
		try {
		unblacklisted = getUnblacklisted();
		}
		catch(IndexOutOfBoundsException e) {
			System.out.println("Error: domains.txt does not contain any or enough domains");
			return;
		}
		if(unblacklisted.equals(null)) {
			System.out.println("No domains found in file");
			return;
		}
		if(unblacklisted.equals(getCurrentTarget())) {
			System.out.println("Current record is unblacklisted, no changes needed");
			return;
		}
		JsonObject jsonobject = new JsonObject();
		jsonobject.addProperty("service", "_minecraft");
		jsonobject.addProperty("proto", "_tcp");
		jsonobject.addProperty("name", getNameTwo());
		jsonobject.addProperty("priority","0");
		jsonobject.addProperty("weight", "5");
		jsonobject.addProperty("port", "25565");
		jsonobject.addProperty("target", unblacklisted);
		JsonObject newobject = new JsonObject();
		newobject.addProperty("type","SRV");
		newobject.addProperty("name",getNameOne());
		newobject.addProperty("content", "5 25565 "+unblacklisted);
		newobject.addProperty("proxiable","false");
		newobject.addProperty("proxied",false);
		newobject.addProperty("ttl", "1");
		newobject.addProperty("data", jsonobject.toString().replace("\\", ""));
		String bodyinput = newobject.toString().replace("\\", "").replace("\"{\"", "{").replace("\"}\"", "\"}").replace("service\"", "\"service\"");
		Unirest.put("https://api.cloudflare.com/client/v4/zones/"+zoneid+"/dns_records/"+recordid)
				.header("X-Auth-Email",xauthemail)
				.header("X-Auth-Key",xauthkey)
				.header("Content-Type", "application/json")
				.body(bodyinput)
				.asJson();
		System.out.println("New domain: "+unblacklisted);
	}
	public static void initalize() throws IOException {
		if(!domains.exists()) {
			domains.createNewFile();
		}
		if(!config.exists()) {
			config.createNewFile();
			FileWriter filewriter = new FileWriter(config);
			filewriter.write("{\n\"X-Auth-Email\":\"\",\n\"X-Auth-Key\":\"\",\n\"zone_id\":\"\",\n\"record_id\":\"\"\n}");
			filewriter.close();
		}
		setup();

	}
	public static void setup() throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(config)));
		String inputLine;
		StringBuilder response = new StringBuilder();

		while ((inputLine = br.readLine()) != null) {
			response.append(inputLine);
		}
		br.close();
		String json = response.toString();
		JsonElement pageelement = new JsonParser().parse(json);
		JsonObject pageobject = pageelement.getAsJsonObject();
		xauthemail = pageobject.get("X-Auth-Email").getAsString();
		if(xauthemail.equals("")) {
			System.out.println("Error in config.json");
			System.exit(0);
			return;
		}
		xauthkey=pageobject.get("X-Auth-Key").getAsString();
		if(xauthkey.equals("")) {
			System.out.println("Error in config.json");
			System.exit(0);
			return;
		}
		zoneid = pageobject.get("zone_id").getAsString();
		if(zoneid.equals("")) {
			System.out.println("Error in config.json");
			System.exit(0);
			return;
		}
		recordid = pageobject.get("record_id").getAsString();
		if(recordid.equals("")) {
			System.out.println("Error in config.json");
			System.exit(0);
			return;
		}
	}
	public static String getNameOne() throws UnirestException {
		HttpResponse<JsonNode> res = Unirest.get("https://api.cloudflare.com/client/v4/zones/"+zoneid+"/dns_records/"+recordid)
		.header("X-Auth-Email",xauthemail)
		.header("X-Auth-Key",xauthkey)
		.asJson();
		String json = res.getBody().toString();
		JsonElement pageelement = new JsonParser().parse(json);
		JsonObject pageobject = pageelement.getAsJsonObject();
		return pageobject.get("result").getAsJsonObject().get("name").getAsString();
	}
	public static String getNameTwo() throws UnirestException {
		HttpResponse<JsonNode> res = Unirest.get("https://api.cloudflare.com/client/v4/zones/"+zoneid+"/dns_records/"+recordid)
		.header("X-Auth-Email",xauthemail)
		.header("X-Auth-Key",xauthkey)
		.asJson();
		String json = res.getBody().toString();
		JsonElement pageelement = new JsonParser().parse(json);
		JsonObject pageobject = pageelement.getAsJsonObject();
		return pageobject.get("result").getAsJsonObject().get("data").getAsJsonObject().get("name").getAsString();
	}
	public static String getCurrentTarget() throws UnirestException {
		HttpResponse<JsonNode> res = Unirest.get("https://api.cloudflare.com/client/v4/zones/"+zoneid+"/dns_records/"+recordid)
				.header("X-Auth-Email",xauthemail)
				.header("X-Auth-Key",xauthkey)
				.asJson();
				String json = res.getBody().toString();
				JsonElement pageelement = new JsonParser().parse(json);
				JsonObject pageobject = pageelement.getAsJsonObject();
				return pageobject.get("result").getAsJsonObject().get("data").getAsJsonObject().get("target").getAsString();
	}
	public static String getUnblacklisted() throws IOException {
		int place = 0;
		boolean cont=true;
		ArrayList<String> list = getFromFile(domains);
		while(cont) {
		boolean worked=true;
		String json;
		try {
		json = getJson("https://use.gameapis.net/mc/extra/blockedservers/check/" +list.get(place));
		}
		catch(ArrayIndexOutOfBoundsException e) {
			System.out.println("No unblacklisted domains found");
			break;
		}
		//System.out.println(list.get(place));
		JsonElement pageelement = new JsonParser().parse(json);
		JsonObject pageobject = pageelement.getAsJsonObject();
		JsonArray domains = pageobject.getAsJsonArray(list.get(place));
		for(int i=0;i<domains.size();i++) {
			//System.out.println(domains.get(i));
			if(domains.get(i).getAsJsonObject().get("blocked").getAsString().equals("true")) {
				place++;
				worked=false;
				break;
			}
		}
		if(worked==false) {
			continue;
		}
		else {
		return list.get(place);
		}
	  }
		return null;	
	}
	public static String getJson(String urlinput) throws IOException {
		URL url = new URL(urlinput);
		URLConnection conn = url.openConnection();
		BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String inputLine;
		StringBuilder response = new StringBuilder();

		while ((inputLine = br.readLine()) != null) {
			response.append(inputLine);
		}
		br.close();
		return response.toString();
	}
	public static ArrayList<String> getFromFile(File file) throws FileNotFoundException {
		Scanner scanner = new Scanner(file);
        ArrayList<String> list = new ArrayList<String>();
        while(scanner.hasNextLine()) {
        	list.add(scanner.nextLine());
        }
        scanner.close();
        return list;
	}

}
