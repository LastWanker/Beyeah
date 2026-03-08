package SCG.beyeah1211.controller.vo;

import java.io.Serializable;

public class beyeahUserVO implements Serializable {

    private Long userid;
    private String nickname;
    private String username;
    private String address;
    private String introduceSign;
    private int shopCartItemCount;

    public Long getUserId() {
        return userid;
    }

    public void setUserId(Long userid) {
        this.userid = userid;
    }

    public String getNickName() {
        return nickname;
    }

    public void setNickName(String nickname) {
        this.nickname = nickname;
    }

    public String getusername() {
        return username;
    }

    public void setusername(String username) {
        this.username = username;
    }

    public String getIntroduceSign() {
        return introduceSign;
    }

    public void setIntroduceSign(String introduceSign) {
        this.introduceSign = introduceSign;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getShopCartItemCount() {
        return shopCartItemCount;
    }

    public void setShopCartItemCount(int shopCartItemCount) {
        this.shopCartItemCount = shopCartItemCount;
    }
}

