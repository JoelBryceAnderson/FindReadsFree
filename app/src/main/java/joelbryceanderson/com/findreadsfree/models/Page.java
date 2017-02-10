package joelbryceanderson.com.findreadsfree.models;

/**
 * Created by JAnderson on 2/8/17.
 *
 * Model class that corresponds with backend json objects.
 */

public class Page {

    private String titleText;
    private String mainText;
    private String redirectionUrl;
    private String imageUrl;
    private String purchaseUrl;
    private String bname;
    private String appName;

    public String getTitleText() {
        return titleText;
    }

    public String getMainText() {
        return mainText;
    }

    public String getRedirectionUrl() {
        return redirectionUrl;
    }

    public String getPurchaseUrl() {
        return purchaseUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getBname() {
        return bname;
    }

    public String getAppName() {
        return appName;
    }
}
