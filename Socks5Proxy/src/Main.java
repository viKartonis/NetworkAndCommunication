import com.socksProxy.Server;

public class Main
{
	public static void main(String[] args)
	{
		Thread proxyServer = new Thread(new Server(60088));

		proxyServer.setName("PROXY");
		proxyServer.start();
	}
}
