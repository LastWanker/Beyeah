package SCG.beyeah1211.entity;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public class MallUser {
    private Long userId;
    private String nickname;
    private String username;
    private String passwordMd5;
    private String introduceSign;
    private String address;
    private Byte isDeleted;
    private Byte lockedFlag;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getNickName() {
        return nickname;
    }

    public void setNickName(String nickname) {
        this.nickname = nickname == null ? null : nickname.trim();
    }

    public String getusername() {
        return username;
    }

    public void setusername(String username) {
        this.username = username == null ? null : username.trim();
    }

    public String getPasswordMd5() {
        return passwordMd5;
    }

    public void setPasswordMd5(String passwordMd5) {
        this.passwordMd5 = passwordMd5 == null ? null : passwordMd5.trim();
    }

    public String getIntroduceSign() {
        return introduceSign;
    }

    public void setIntroduceSign(String introduceSign) {
        this.introduceSign = introduceSign == null ? null : introduceSign.trim();
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Byte getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Byte isDeleted) {
        this.isDeleted = isDeleted;
    }

    public Byte getLockedFlag() {
        return lockedFlag;
    }

    public void setLockedFlag(Byte lockedFlag) {
        this.lockedFlag = lockedFlag;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", userId=").append(userId);
        sb.append(", nickname=").append(nickname);
        sb.append(", username=").append(username);
        sb.append(", passwordMd5=").append(passwordMd5);
        sb.append(", introduceSign=").append(introduceSign);
        sb.append(", address=").append(address);
        sb.append(", isDeleted=").append(isDeleted);
        sb.append(", lockedFlag=").append(lockedFlag);
        sb.append(", createTime=").append(createTime);
        sb.append("]");
        return sb.toString();
    }
}
