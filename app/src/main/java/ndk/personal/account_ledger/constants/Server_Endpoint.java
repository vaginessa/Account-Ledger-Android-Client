package ndk.personal.account_ledger.constants;

public class Server_Endpoint {

    public static String SERVER_IP_ADDRESS = "http://192.168.43.89/php/Account_Ledger_Server";

    public static String BUILD_FOLDER = "builds";
    public static String MODULE_NAME = "app";
    public static String APK_PREFIX = "-debug.apk";
    public static String UPDATE_URL = SERVER_IP_ADDRESS + "/" + BUILD_FOLDER + "/" + MODULE_NAME + APK_PREFIX;
    public static String HTTP_API_FOLDER = "http_API";
    public static String FILE_EXTENSION = ".php";
}

