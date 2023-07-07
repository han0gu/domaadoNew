package com.domaado.mobileapp.data;

import com.domaado.mobileapp.Common;
import com.domaado.mobileapp.widget.JsonUtils;

import org.json.JSONArray;

import java.util.HashMap;

/**
 * Created by JamesHong on 2023/07/07
 * Project domaadoNew
 *
 * {
 *     "fileName": "d138bef3-fed8-4cff-b073-1af0f5dcf69c_/data/user/0/com.domaado.mobileapp/cache/tmp_1688689125864.jpg",
 *     "fileSize": 594655,
 *     "msgNm": "등록되었습니다.",
 *     "originFileName": "/data/user/0/com.domaado.mobileapp/cache/tmp_1688689125864.jpg",
 *     "msgCd": "0000",
 *     "thumbnailImagePath": "https://s3.ap-northeast-2.amazonaws.com/market-img/profileImages/d138bef3-fed8-4cff-b073-1af0f5dcf69c_/data/user/0/com.domaado.mobileapp/cache/tmp_1688689125864.jpg",
 *     "delegateThumbnailYn": "N",
 *     "imageFileNumber": 1756,
 *     "imageCfcd": "03"
 * }
 *
 */
public class DomaadoImageResponse extends ResponseBase {
    public String[] fields = { "fileName", "fileSize", "msgNm", "originFileName", "msgCd", "thumbnailImagePath", "delegateThumbnailYn", "imageFileNumber", "imageCfcd" };

    String fileName;
    String fileSize;
    String msgNm;
    String originFileName;
    String msgCd;
    String thumbnailImagePath;
    String delegateThumbnailYn;
    String imageFileNumber;
    String imageCfcd;

    public DomaadoImageResponse() {
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public String getMsgNm() {
        return msgNm;
    }

    public void setMsgNm(String msgNm) {
        setMessage(msgNm);
        this.msgNm = msgNm;
    }

    public String getOriginFileName() {
        return originFileName;
    }

    public void setOriginFileName(String originFileName) {
        this.originFileName = originFileName;
    }

    public String getMsgCd() {
        return msgCd;
    }

    public void setMsgCd(String msgCd) {
        if("0000".equals(msgCd)) {
            setResponseYn("Y");
        }
        this.msgCd = msgCd;
    }

    public String getThumbnailImagePath() {
        return thumbnailImagePath;
    }

    public void setThumbnailImagePath(String thumbnailImagePath) {
        this.thumbnailImagePath = thumbnailImagePath;
    }

    public String getDelegateThumbnailYn() {
        return delegateThumbnailYn;
    }

    public void setDelegateThumbnailYn(String delegateThumbnailYn) {
        this.delegateThumbnailYn = delegateThumbnailYn;
    }

    public String getImageFileNumber() {
        return imageFileNumber;
    }

    public void setImageFileNumber(String imageFileNumber) {
        this.imageFileNumber = imageFileNumber;
    }

    public String getImageCfcd() {
        return imageCfcd;
    }

    public void setImageCfcd(String imageCfcd) {
        this.imageCfcd = imageCfcd;
    }

    public void set(String key, Object value) {
        if(fields[0].equalsIgnoreCase(key)) setFileName(Common.valueOf(value));
        else if(fields[1].equalsIgnoreCase(key)) setFileSize(Common.valueOf(value));
        else if(fields[2].equalsIgnoreCase(key)) setMsgNm(Common.valueOf(value));
        else if(fields[3].equalsIgnoreCase(key)) setOriginFileName(Common.valueOf(value));
        else if(fields[4].equalsIgnoreCase(key)) setMsgCd(Common.valueOf(value));
        else if(fields[5].equalsIgnoreCase(key)) setThumbnailImagePath(Common.valueOf(value));
        else if(fields[6].equalsIgnoreCase(key)) setDelegateThumbnailYn(Common.valueOf(value));
        else if(fields[7].equalsIgnoreCase(key)) setImageFileNumber(Common.valueOf(value));
        else if(fields[8].equalsIgnoreCase(key)) setImageCfcd(Common.valueOf(value));
    }

    public HashMap<String, Object> getRequestParameterMap() {
        HashMap<String, Object> map = getBaseParameter();

        map.put(fields[0], getFileName());
        map.put(fields[1], getFileSize());
        map.put(fields[2], getMsgNm());
        map.put(fields[3], getOriginFileName());
        map.put(fields[4], getMsgCd());
        map.put(fields[5], getThumbnailImagePath());
        map.put(fields[6], getDelegateThumbnailYn());
        map.put(fields[7], getImageFileNumber());
        map.put(fields[8], getImageCfcd());

        return map;
    }

    @Override
    public String toString() {
        return "DomaadoImageResponse{" +
                "fileName='" + fileName + '\'' +
                ", fileSize='" + fileSize + '\'' +
                ", msgNm='" + msgNm + '\'' +
                ", originFileName='" + originFileName + '\'' +
                ", msgCd='" + msgCd + '\'' +
                ", thumbnailImagePath='" + thumbnailImagePath + '\'' +
                ", delegateThumbnailYn='" + delegateThumbnailYn + '\'' +
                ", imageFileNumber='" + imageFileNumber + '\'' +
                ", imageCfcd='" + imageCfcd + '\'' +
                '}';
    }
}
