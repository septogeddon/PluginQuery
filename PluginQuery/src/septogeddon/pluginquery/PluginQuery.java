package septogeddon.pluginquery;

import septogeddon.pluginquery.api.QueryMessenger;
import septogeddon.pluginquery.utils.QueryUtil;

public class PluginQuery {

	private static QueryMessenger messengerInstance;
	
	@SuppressWarnings("all")
	public static <T extends QueryMessenger> T getMessenger() {
		return (T) messengerInstance;
	}
	
	public static void setMessenger(QueryMessenger messenger) {
		QueryUtil.illegalState(messengerInstance != null, "instance already set");
		messengerInstance = messenger;
	}
	
	public static void initializeDefaultMessenger() {
		setMessenger(new QueryMessengerImpl());
	}
	
}