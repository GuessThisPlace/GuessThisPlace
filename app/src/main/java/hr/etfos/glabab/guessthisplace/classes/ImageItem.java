package hr.etfos.glabab.guessthisplace.classes;

public class ImageItem {

    public static final String URL_HEAD = "http://res.cloudinary.com/guessthisplace/image/upload/";
    public static final String URL_THUMB = "t_thumbnail/";
    public static final String URL_TAIL = ".jpg";

    String imageUrl;
    String imageUrlThumb;
    String imageCode;
    int approved;
    int denyReason;

    public ImageItem(String imageCode, int approved, int denyReason){
        this.imageCode = imageCode;
        this.imageUrl = URL_HEAD + imageCode + URL_TAIL;
        this.imageUrlThumb = URL_HEAD + URL_THUMB + imageCode + URL_TAIL;
        this.approved = approved;
        this.denyReason = denyReason;
    }

    public ImageItem(){
        this.imageUrl = "";
        this.imageUrlThumb = "";
        this.denyReason = 0;
        this.approved = 0;
    }

    public String getImageUrl() {
        return imageUrl;
    }
    public String getImageCode(){ return imageCode; }

    public String getImageUrlThumb() { return imageUrlThumb; }

    public int getApproved() { return approved; }

    public int getDenyReason() { return denyReason; }

    public void setImageUrl(String imageUrl){
        this.imageUrl = imageUrl;
    }

    public void setImageUrlThumb(String imageUrlThumb){
        this.imageUrlThumb = imageUrlThumb;
    }

    public void setApproved(int approved) { this.approved = approved; }

    public void setDenyReason(int denyReason){
        this.denyReason = denyReason;
    }

}

