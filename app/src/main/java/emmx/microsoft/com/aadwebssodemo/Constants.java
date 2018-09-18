package emmx.microsoft.com.aadwebssodemo;

/**
 * Created by hayan on 18-9-18.
 */

public class Constants {
    /*
 *  The AAD authority url
 */
    public static final String AAD_AUTHORITY_URL = "https://login.microsoftonline.com/common";

    /*
     *  The AAD client id
     */
    public static final String AAD_CLIENT_ID = "f44b1140-bc5e-48c6-8dc0-5cf5a53c0e34";

    /*
     *  The AAD redirect url
     */
    public static final String AAD_REDIRECT_URL = "microsoft-edge://com.microsoft.emmx";

    /*
     *  The AAD graph resource name
     */
    public static final String AAD_GRAPH_RESOURCE_NAME = "https://graph.microsoft.com/";

    /** The resource to obtain a device claims token. */
    public static final String DEVICE_CLAIMS_RESOURCE = "https://graph.windows.net";

    /**
     * The device claims token string.
     * It is a url-encoded version of the string {"access_token":{"deviceid":{"essential":true}}}}
     */
    public static final String DEVICE_CLAIMS_DEVICEID_ESSENTIAL_STRING
            = "%7B%22access_token%22%3A%7B%22deviceid%22%3A%7B%22essential%22%3Atrue%7D%7D%7D";

    /** The resource to obtain a managed browser purpose token. */
    static final String PURPOSE_TOKEN_RESOURCE = "urn:microsoft:purpose:ManBro";

    /** The host name of the AAD endpoint. */
    static final String AAD_HOSTNAME = "login.microsoftonline.com";
}
