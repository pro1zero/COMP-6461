
import java.io.*;
import java.util.*;
import java.net.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

//coded by Neelofer Shama (40121559)
//coded by Jenish Soni (40132415)
public class client {
	//have to implemenmt -t or -d not togerhter. 
	public static void main(String[] args) throws Exception {
		List<String> input = new ArrayList<>();
		System.out.println("Hello, welcome to HTTPC!");
		Scanner scan = new Scanner(System.in);
		String getStringFromCommand = "";
		while(!getStringFromCommand.toLowerCase().equals("stop")) {
			System.out.println("====================================================");
			System.out.print(">>> ");
			getStringFromCommand = scan.nextLine().toLowerCase();
			if(getStringFromCommand.toLowerCase().equals("stop")) break;
			//System.out.println("====================================================");
			input = Arrays.asList(getStringFromCommand.split("\\s+"));
			args = getStringFromCommand.split("\\s+");
			if(!input.get(0).equals("httpc")) {
				System.out.println("Enter a valid httpc command!");
			}
			else {
			if(getStringFromCommand.contains("help")) getStaticResponse(args);
			else if((getStringFromCommand.contains("-d") || getStringFromCommand.contains("--d")) && (getStringFromCommand.contains("-f") || getStringFromCommand.contains("--f")))System.out.println("Either [-d] or [-f] can be used but not both.");
			else {
				boolean executeVerbose = false;
				boolean writeToFile =false ;
				String data = null;
				String inputFile ="";
				String outputFile =null;
				List<String> headers = new ArrayList<>();
				if (input.contains("-v") || input.contains("--v")) {
					executeVerbose= true;
				}
				if (input.contains("-d") || input.contains("--d")){
					if(input.contains("--d")) input.set(input.indexOf("--d"),"-d");
					data = input.get(input.indexOf("-d")+1).replaceAll("'","");
				}
				else if (input.contains("-f") || input.contains("--f")){
					if(input.contains("--f")) input.set(input.indexOf("--f"),"-f");
					inputFile = input.get(input.indexOf("-f")+1);
					File file = new File(inputFile);
					Scanner sc = new Scanner(file);
					data =  "";
					while(sc.hasNextLine()) {
						data += sc.nextLine();
					}
				}
				if (input.contains("-h") ||input.contains("--h")){
					if(input.contains("--h")) input.set(input.indexOf("--h"),"-h");

					List<String> finalInput = input;
					int[] headers_indx =  IntStream.range(0, input.size())
							.filter(i -> {
								return finalInput.get(i).equals("-h");
							}) // Only keep those indices
							.toArray();
					for (int i: headers_indx
						 ) {
						headers.add(input.get(i+1));
					}

				}
				if (input.contains("-o")) {
					writeToFile=true;
					outputFile = input.get(input.indexOf("-o")+1);
					String url = input.get(input.indexOf("-o")-1);
					executeFileOperation(url,executeVerbose,outputFile,headers,data);
				}
				if (input.contains("get") && !input.contains("-o")) {
					System.out.println(getGETResponse(args[args.length - 1], executeVerbose, writeToFile, outputFile, 1,headers,""));
				}
				else if (input.contains("post") && !input.contains("-o")) {
					System.out.println(getPOSTResponse(args[args.length - 1],executeVerbose, writeToFile, outputFile, 1,headers,data,""));
				}
			 }
		}}
		scan.close();
	}
	
	public static void executeFileOperation(String url,boolean v, String filename,List<String> headers, String data) throws Exception {
		String urlToPass = url;
		//System.out.println(urlToPass);
		String responseToGoInFile = "";
		if(urlToPass.contains("get".toLowerCase())) responseToGoInFile = getGETResponse(urlToPass, v, true, filename, 1,headers, "");
		else if(urlToPass.contains("post".toLowerCase())) responseToGoInFile = getPOSTResponse(urlToPass, v, true, filename, 1,headers,data,"");
		try {
			PrintWriter out = new PrintWriter(filename);
			out.println(responseToGoInFile);
			out.close();
		}catch(Exception e) {
			System.out.println(e);
		}


	}
	
	public static String getGETResponse(String urlToPass, boolean executeVerbose, boolean writeToFile, String fileName, int count,List<String> headers, String response) throws Exception {
		if(count > 5) return "Infinite redirect detected!";
		if(count > 1 ) System.out.println(response);
		String getResponse = "";
		try{
		///String urlToPass = args.get(args.size() - 1);
		urlToPass = urlToPass.replaceAll("'", "");
		int portHTTP = 80;
		if(!urlToPass.startsWith("http") && !urlToPass.startsWith("https"))
			urlToPass = "https://"+urlToPass;
		URL url = new URL(urlToPass);
		if(urlToPass.startsWith("https"))
			portHTTP =443;
		// System.out.println(urlToPass);
		Socket socket = new Socket(InetAddress.getByName(url.getHost().trim()), portHTTP);
		PrintWriter out = new PrintWriter(socket.getOutputStream());
		out.println("GET " + url.getFile() + " HTTP/1.0");
		out.println("Host: " + url.getHost());
		String inputHeader =  "";
		if(headers!= null){
			for (String s : headers) {
				inputHeader += s+"\r\n";
			}
			out.println(inputHeader);
		}

		out.println("");
		out.flush();
		BufferedReader bufferReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		String responseLine = null;
		String header = "";
		boolean receivedHeader = false;
		boolean doRedirect=false;
		while((responseLine = bufferReader.readLine()) != null) {
			if(responseLine.startsWith("HTTP") && responseLine.contains("300") ||responseLine.contains("301") ||responseLine.contains("302")||responseLine.contains("304") ||responseLine.contains("307") ) doRedirect=true;
			else {
				if (doRedirect && responseLine.contains("Location:")) {
					String newURL = responseLine.split("Location:")[1].replaceAll(" ", "");
					System.out.println(getGETResponse(newURL, executeVerbose, writeToFile, fileName, count + 1, headers, getResponse));
				}

			}
			if(!responseLine.isEmpty() && !receivedHeader)
			{
				header += responseLine.toString() +"\n";
			}
			else receivedHeader = true;
			if(receivedHeader) {
				getResponse += responseLine + "\n";
			}
		}
//		System.out.println(getResponse);
	    socket.close();
		if(executeVerbose || getResponse.equals(""))
			getResponse = header.toString() +getResponse;}
		catch (Exception e)
		{
			getResponse = e.toString();
		}
	    return getResponse;
	}
	
	public static String getPOSTResponse(String urlToPass, boolean executeVerbose, boolean writeToFile, String fileName, int count, List<String> headers,String data,String response) throws Exception {
		if(count > 5) return "Infinite redirect detected!";
		if(count >1 ) System.out.println(response);
		String getResponse = "";
		try{
		//String urlToPass = args.get(args.size() - 1);
		urlToPass = urlToPass.replaceAll("'", "");
		URL url = new URL(urlToPass);
		int portHTTP = 80;
		String inputHeader =  "";
		String header = "";

		//System.out.println(Arrays.toString(splitHeaders));
		Socket socket = new Socket(InetAddress.getByName(url.getHost().trim()), portHTTP);
		PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
		out.println("POST " + url.getFile() + " HTTP/1.0");
		out.println("Host: " + url.getHost());
		if(headers!= null){
			for (String s : headers) {
				inputHeader += s+"\r\n";
			}
			if(data!=null){
				data.replaceAll("\\W","");
				out.println("Content-Length: " + data.length());
			}
			out.println(inputHeader);
			out.println(data);
		}

		out.println("");
		//out.flush();
		BufferedReader bufferReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		String responseLine = null;
		boolean receivedHeader = false;
		while((responseLine = bufferReader.readLine()) != null) {
			if(responseLine.contains("300") ||responseLine.contains("301") ||responseLine.contains("302") ||responseLine.contains("304")) {
				String newURL = responseLine.split("Location:")[1];
				getPOSTResponse(newURL, executeVerbose, writeToFile, fileName, count + 1,headers,data,getResponse);
			}
		//System.out.println(getResponse);
			if(!responseLine.isEmpty() && !receivedHeader)
			{
				header += responseLine.toString() +"\n";
			}
			else receivedHeader = true;
			if(receivedHeader) {
				getResponse += responseLine + "\n";
			}
	}
	    socket.close();
		if(executeVerbose || getResponse.equals(""))
				getResponse = header.toString() +getResponse;
		}
		catch (Exception e){ getResponse += e.toString();}
	    return getResponse;
	}
	
	public static void getStaticResponse(String[] args) {
		if(args.length == 2 &&args[1].equals("help")) {
			System.out.println("httpc is a curl-like application but supports HTTP protocol only.\n"
					+ "Usage:\n"
					+ " httpc command [arguments]\n"
					+ "The commands are:\n"
					+ " get executes a HTTP GET request and prints the response.\n"
					+ " post executes a HTTP POST request and prints the response.\n"
					+ " help prints this screen.\n"
					+ "Use \"httpc help [command]\" for more information about a command.");
		}else if( args.length == 3 && args[2].equals("get")) {
			System.out.println("httpc help get\n"
					+ "usage: httpc get [-v] [-h key:value] URL\n"
					+ "Get executes a HTTP GET request for a given URL.\n"
					+ " -v Prints the detail of the response such as protocol, status,\n"
					+ "and headers.\n"
					+ " -h key:value Associates headers to HTTP Request with the format\n"
					+ "'key:value'.");
		}else if( args.length == 3 && args[2].equals("post")) {
			System.out.println("httpc help post\n"
					+ "Comp 6461 – Fall 2021 - Lab Assignment # 1 Page 7\n"
					+ "usage: httpc post [-v] [-h key:value] [-d inline-data] [-f file] URL\n"
					+ "Post executes a HTTP POST request for a given URL with inline data or from\n"
					+ "file.\n"
					+ " -v Prints the detail of the response such as protocol, status,\n"
					+ "and headers.\n"
					+ " -h key:value Associates headers to HTTP Request with the format\n"
					+ "'key:value'.\n"
					+ " -d string Associates an inline data to the body HTTP POST request.\n"
					+ " -f file Associates the content of a file to the body HTTP POST\n"
					+ "request.\n"
					+ "Either [-d] or [-f] can be used but not both.");
		}else {
			System.out.println("Enter a valid command!");
		}
	}
}
