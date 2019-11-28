package com.lhcode.litemall.db.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@ApiModel(value = "LitemallDirect",description = "主页的直通车")
public class LitemallDirect {

    @ApiModelProperty(value = "主键")
    private Integer id;

    @ApiModelProperty(value = "标题")
    private String title;

    @ApiModelProperty(value = "url")
    private String url;

    @ApiModelProperty(value = "跳转链接")
    private String link;

    @ApiModelProperty(value = "位置")
    private Integer position;

    @ApiModelProperty(value = "是否启用")
    private Integer isStart;


    public Map<String,Object> toVo(Integer pid){
        Map<String,Object> map = new HashMap<>();
        map.put("id",this.id);
        map.put("title",this.title);
        map.put("url",this.url);
        map.put("link",this.link);
        map.put("position",this.position);
        map.put("isStart",this.isStart);
        map.put("pid",pid);
        return map;
    }


    private Integer deleted;

    private Date addTime;

    private Date deleteTime;

    private Date updateTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title == null ? null : title.trim();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url == null ? null : url.trim();
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link == null ? null : link.trim();
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public Integer getIsStart() {
        return isStart;
    }

    public void setIsStart(Integer isStart) {
        this.isStart = isStart;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }

    public Date getAddTime() {
        return addTime;
    }

    public void setAddTime(Date addTime) {
        this.addTime = addTime;
    }

    public Date getDeleteTime() {
        return deleteTime;
    }

    public void setDeleteTime(Date deleteTime) {
        this.deleteTime = deleteTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}