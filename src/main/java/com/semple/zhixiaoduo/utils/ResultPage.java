package com.semple.zhixiaoduo.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @filename ResultPage
 * @description 分页数据返回
 * @autor aofaming
 * @date 2023/11/29 9:42
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ResultPage<T> implements Serializable {

	private List<T> list;
	private Long pageNum;
	private Long pageSize;
	private Long total;

}
