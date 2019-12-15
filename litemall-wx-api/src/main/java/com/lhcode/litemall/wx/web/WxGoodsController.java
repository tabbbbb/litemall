package com.lhcode.litemall.wx.web;

import com.lhcode.litemall.core.util.ResponseUtil;
import com.lhcode.litemall.core.validator.Order;
import com.lhcode.litemall.core.validator.Sort;
import com.lhcode.litemall.db.domain.*;
import com.lhcode.litemall.db.service.*;
import com.lhcode.litemall.wx.annotation.LoginUser;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.constraints.NotNull;
import javax.xml.ws.Response;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 商品服务
 */
@RestController
@RequestMapping("/wx/goods")
@Validated
@Api(value = "/wx/goods",description = "商品")
public class WxGoodsController {
	private final Log logger = LogFactory.getLog(WxGoodsController.class);

	@Autowired
	private LitemallGoodsService goodsService;

	@Autowired
	private LitemallRegionService regionService;

	@Autowired
	private LitemallIssueService goodsIssueService;

	@Autowired
	private LitemallGoodsAttributeService goodsAttributeService;

	@Autowired
	private LitemallBrandService brandService;

	@Autowired
	private LitemallUserService userService;

	@Autowired
	private LitemallCollectService collectService;

	@Autowired
	private LitemallFootprintService footprintService;

	@Autowired
	private LitemallCategoryService categoryService;

	@Autowired
	private LitemallSearchHistoryService searchHistoryService;

	@Autowired
	private LitemallGoodsSpecificationService goodsSpecificationService;

	@Autowired
	private LitemallGrouponRulesService rulesService;

	private final static ArrayBlockingQueue<Runnable> WORK_QUEUE = new ArrayBlockingQueue<>(9);

	private final static RejectedExecutionHandler HANDLER = new ThreadPoolExecutor.CallerRunsPolicy();

	private static ThreadPoolExecutor executorService = new ThreadPoolExecutor(16, 16, 1000, TimeUnit.MILLISECONDS, WORK_QUEUE, HANDLER);

	/**
	 * 商品详情
	 * <p>
	 * 用户可以不登录。
	 * 如果用户登录，则记录用户足迹以及返回用户收藏信息。
	 *
	 * @param userId 用户ID
	 * @param id     商品ID
	 * @return 商品详情
	 */
	@GetMapping("detail")
	@ApiOperation(value = "商品详细",response = ResponseUtil.class,notes = "",nickname = "商品详细")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(paramType="query",name="id",value="商品id",dataTypeClass = Integer.class)
	})
	public Object detail(@ApiIgnore @LoginUser Integer userId, @NotNull Integer id) {
		Map<String,Object> map = new HashMap<>();
		LitemallGoods goods = goodsService.findById(id,userId);
		Callable<List> specCallable = () -> goodsSpecificationService.queryByGid(userId,id);
		Callable<List> attrCallable = () -> goodsAttributeService.queryByGid(id);
		FutureTask<List> specFutureTask = new FutureTask<>(specCallable);
		FutureTask<List> attrFutureTask = new FutureTask<>(attrCallable);
		executorService.submit(specFutureTask);
		executorService.submit(attrFutureTask);
		try {
			if(userId != null){
				Callable<Boolean> collectCallable = new Callable<Boolean>() {
					@Override
					public Boolean call() throws Exception {
						if (collectService.queryByTypeAndValue(userId,goods.getId()) == null){
							return false;
						}
						return true;
					}
				};

				Callable addFootprintCallable = new Callable() {
					@Override
					public Object call() throws Exception {
						footprintService.deleteFootprint(goods.getId(), LocalDateTime.now());
						LitemallFootprint footprint = new LitemallFootprint();
						footprint.setGoodsId(goods.getId());
						footprint.setUserId(userId);
						footprintService.add(footprint);
						return null;
					}
				};
				FutureTask<Boolean> collectFutureTask = new FutureTask<>(collectCallable);
				FutureTask addFootprintFutureTask = new FutureTask(addFootprintCallable);
				executorService.submit(collectFutureTask);
				executorService.submit(addFootprintFutureTask);
				map.put("isCollect",collectFutureTask.get(3,TimeUnit.SECONDS));
			}
			map.put("goods",goods);
			map.put("attr",attrFutureTask.get(3,TimeUnit.SECONDS));
			map.put("spec",specFutureTask.get(3,TimeUnit.SECONDS));
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			e.printStackTrace();
		}
		return ResponseUtil.ok(map);
	}

	/*public Object detail(@ApiIgnore @LoginUser Integer userId, @NotNull Integer id) {
		Map<String,Object> map = new HashMap<>();
		LitemallGoods goods = goodsService.findById(id,userId);
		List<LitemallGoodsSpecification> spec = goodsSpecificationService.queryByGid(userId,id);
		LitemallRegion region = null;
		if (goods.getAreaId() != null && goods.getAreaId() != 0){
			region = regionService.findById(goods.getAreaId());
		}else if (goods.getCityId() != null && goods.getCityId() != 0){
			region = regionService.findById(goods.getCityId());
		}else if (goods.getProvinceId() != null && goods.getProvinceId() != 0){
			region = regionService.findById(goods.getProvinceId());
		}
		map.put("goods",goods);
		map.put("spec",spec);
		map.put("region",region);
		return ResponseUtil.ok(map);
	}*/

	/**
	 * 根据条件搜素商品
	 * <p>
	 * 2. 用户是可选登录，如果登录，则记录用户的搜索关键字
	 *
	 * @param categoryId 分类类目ID，可选
	 * @param keyword    关键字，可选
	 * @param userId     用户ID
	 * @param page       分页页数
	 * @param size       分页大小
	 * @param sort       排序方式，支持"add_time", "retail_price"或"name"
	 * @param order      排序类型，顺序或者降序
	 * @return 根据条件搜素的商品详情
	 */
	@GetMapping("list")
	@ApiOperation(value = "根据条件搜索商品",response = LitemallGoods.class,notes = "data:{goodsList:LitemallGoodsList,count:商品数量}",nickname = "根据条件搜素商品")
	@ApiImplicitParams(value = {
			@ApiImplicitParam(paramType = "query",name = "type",value = "规格选择",dataTypeClass = String.class),
			@ApiImplicitParam(paramType="query",name="address",value="地址 省级id,市级id,省区id(没有选择可以不拼)",dataTypeClass = String.class),
			@ApiImplicitParam(paramType="query",name="categoryId",value="类别id",dataTypeClass = Integer.class),
			@ApiImplicitParam(paramType="query",name="keyword",value="搜索关键字",dataTypeClass = String.class),
			@ApiImplicitParam(paramType="query",name="typeId",value="1:热卖,2:特价,3:新品",dataTypeClass = String.class),
			@ApiImplicitParam(paramType="query",name="page",value="页码",dataTypeClass = Integer.class,defaultValue = "1"),
			@ApiImplicitParam(paramType="query",name="size",value="本页条数",dataTypeClass = Integer.class,defaultValue = "10"),
			@ApiImplicitParam(paramType="query",name="sort",value="排序字段",dataTypeClass = String.class,defaultValue = "add_time"),
			@ApiImplicitParam(paramType="query",name="order",value="排序顺序 price:排序价格,sales:排序销量",dataTypeClass = String.class,defaultValue = "desc")
	})
	public Object list(
		 String type,
		 String address,
		 Integer categoryId,
		 String keyword,
		 Integer typeId,
		 @ApiIgnore @LoginUser Integer userId,
		 @RequestParam(defaultValue = "1")Integer page,
		 @RequestParam(defaultValue = "10")Integer size,
		 @RequestParam(defaultValue = "add_time") String sort,
		 @Order @RequestParam(defaultValue = "desc") String order) {


		//添加到搜索历史
		if (userId != null && !StringUtils.isEmpty(keyword)) {
			LitemallSearchHistory searchHistoryVo = new LitemallSearchHistory();
			searchHistoryVo.setKeyword(keyword);
			searchHistoryVo.setUserId(userId);
			searchHistoryVo.setFrom("wx");
			searchHistoryService.save(searchHistoryVo);
		}
		List<LitemallRegion> regionList = regionService.getAll();
		//查询列表数据
		List<LitemallGoods> goodsList = goodsService.querySelective(type,address,categoryId,userId, keyword, typeId, page, size, sort, order);
		List response = new ArrayList();
		for (LitemallGoods goods : goodsList) {
			Map<String,Object> map = new HashMap<>();
			map.put("attr",goodsAttributeService.queryByGid(goods.getId()));
			map.put("goods",goods);
			map.put("region",filterRegion(regionList,goods));
			response.add(map);
		}
		Map<String, Object> data = new HashMap<>();
		data.put("goodsList", response);
		data.put("count", PageInfo.of(goodsList).getTotal());
		return ResponseUtil.ok(data);
	}

	private LitemallRegion filterRegion(List<LitemallRegion> regionList,LitemallGoods goods){
		LitemallRegion region1 = null;
		if (goods.getAreaId() != null && goods.getAreaId() != 0){
			region1 = regionList.stream().filter(region -> region.getId().equals(goods.getAreaId())).findAny().orElse(null);
		}else if (goods.getCityId() != null && goods.getCityId() != 0){
			region1 = regionList.stream().filter(region -> region.getId().equals(goods.getCityId())).findAny().orElse(null);
		}else if (goods.getProvinceId() != null && goods.getProvinceId() != 0){
			region1 = regionList.stream().filter(region -> region.getId().equals(goods.getProvinceId())).findAny().orElse(null);
		}
		return region1;
	}




}