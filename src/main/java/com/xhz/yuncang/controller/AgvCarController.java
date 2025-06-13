package com.xhz.yuncang.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xhz.yuncang.dto.AgvCarAddDTO;
import com.xhz.yuncang.dto.AgvCarInfoDTO;
import com.xhz.yuncang.entity.AgvCar;
import com.xhz.yuncang.entity.Remind;
import com.xhz.yuncang.entity.User;
import com.xhz.yuncang.service.AgvCarService;
import com.xhz.yuncang.service.ProductService;
import com.xhz.yuncang.service.RemindService;
import com.xhz.yuncang.service.UserService;
import com.xhz.yuncang.utils.AjaxResult;
import com.xhz.yuncang.utils.Constants;
import com.xhz.yuncang.utils.UserHolder;
import com.xhz.yuncang.vo.AgvCarInfoVo;
import com.xhz.yuncang.vo.UserVo;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
/**
 * AGV小车管理控制器
 *
 * <p>提供AGV小车的全生命周期管理功能，包括查询、添加、修改和删除操作。
 * 所有操作都需要管理员权限，返回结果统一使用{@link AjaxResult}封装响应数据。
 *
 * <p>主要功能包括：
 * <ul>
 *   <li>分页查询AGV小车列表</li>
 *   <li>添加新的AGV小车记录</li>
 *   <li>更新现有AGV小车信息</li>
 *   <li>删除AGV小车记录</li>
 * </ul>
 *
 * @author skc
 * @version 1.0
 * @since 2025-06
 * @see AgvCar AGV小车实体类
 * @see AgvCarService AGV小车服务接口
 */
@RestController
@PreAuthorize("hasRole('管理员') or hasRole('操作员')")
public class AgvCarController {
    /**
     * AGV小车服务接口
     */
    @Autowired
    private AgvCarService agvCarService;

    /**
     * 用户服务接口
     */
    @Autowired
    private UserService userService;

    /**
     * Redis模板，用于缓存操作
     */
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RemindService remindService;

    /**
     * 分页查询AGV小车列表
     *
     * <p>返回AGV小车的分页数据，支持按小车编号过滤查询。响应数据会转换为VO对象返回前端。
     *
     * <p>分页参数说明：
     * <ul>
     *   <li>当前页码小于1时会返回400错误</li>
     *   <li>当前页码超过最大页数时会返回400错误</li>
     *   <li>响应数据包含分页导航信息(上一页/下一页)</li>
     * </ul>
     *
     * @param current 当前页码，从1开始，默认为1
     * @param pageSize 每页记录数，默认为10
     * @param carNumber 小车编号过滤条件，模糊匹配，可选参数
     * @return 包含分页数据的响应实体，数据格式为：
     *         {
     *             "prev": 上一页页码,
     *             "next": 下一页页码,
     *             "total": 总记录数,
     *             "list": [AgvCarInfoVo列表]
     *         }
     * @see AgvCarInfoVo AGV小车信息视图对象
     */
    @GetMapping("/car")
    public ResponseEntity<AjaxResult> showAgvCarsByPage(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "") String carNumber
    ) {
        if (current<=0){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error("页数小于 1"));
        }
        Page<AgvCar> page = new Page<>(current, pageSize);
        QueryWrapper<AgvCar> queryWrapper = new QueryWrapper<>();

        if (!carNumber.isEmpty()) {
            queryWrapper.like("car_number", carNumber);
        }
        Page<AgvCar> result = agvCarService.page(page, queryWrapper);

        if (result.getPages()==0){
            remindService.saveRemind(new Remind(null, Constants.REMIND_INFO, "AVG小车为空", "于" + LocalDateTime.now() + "时刻，" +
                    UserHolder.getUser().getUserType() + UserHolder.getUser().getUsername() + "发现没有小车，建议添加3辆小车",
                    LocalDateTime.now(),"0"));
            return ResponseEntity.ok().body(AjaxResult.success("当前AGV小车为空",""));
        }
        if (current > result.getPages()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error(400,"页数大于最大页数"));
        }

        int nowPage = (int) result.getCurrent();
        int beforePage = nowPage == 1 ? -1 : nowPage - 1;
        int nextPage = nowPage == (int) result.getPages() ? -1 : nowPage + 1;

        List<AgvCar> agvCars = result.getRecords();
        List<AgvCarInfoVo> agvCarInfoVoList = agvCars.stream().map(car -> new AgvCarInfoVo(
                car.getId().toString(),
                car.getCarNumber(),
                car.getStatus(),
                car.getUserId(),
                car.getBatteryLevel(),
                car.getMaxWeight(),
                car.getLocationX(),
                car.getLocationY(),
                car.getStartX(),
                car.getStartY(),
                car.getEndX(),
                car.getEndY(),
                car.getEndZ(),
                car.getSku(),
                car.getQuantity(),
                car.getCreateTime(),
                car.getUpdateTime()
        )).toList();

        // 构建 ListResponse<T> 风格的响应数据
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("prev", beforePage);
        responseMap.put("next", nextPage);
        responseMap.put("total", result.getTotal());//page
        responseMap.put("list", agvCarInfoVoList);
        System.out.println("responseMap: "+responseMap);
        return ResponseEntity.ok().body(AjaxResult.success(responseMap));
    }

    /**
     * 创建新的AGV小车记录
     *
     * <p>添加一个新的AGV小车到系统，会自动设置默认值：
     * <ul>
     *   <li>状态默认为空闲({@link Constants#CAR_STATUS_FREE})</li>
     *   <li>电量默认为100%</li>
     *   <li>终点坐标默认为(0,0)</li>
     *   <li>载货SKU默认为空字符串</li>
     *   <li>载货数量默认为0</li>
     * </ul>
     *
     * @param agvCarAddDTO 包含AGV小车信息的DTO对象，必须包含：
     *                     <ul>
     *                       <li>carNumber - 小车编号(必填)</li>
     *                       <li>locationX - X坐标(必填)</li>
     *                       <li>locationY - Y坐标(必填)</li>
     *                       <li>maxWeight - 最大载重(可选，默认为0)</li>
     *                     </ul>
     * @return 操作结果响应实体，成功返回200状态码，失败返回相应错误码
     * @throws IllegalArgumentException 当：
     *         <ul>
     *           <li>小车编号为空</li>
     *           <li>小车编号已存在</li>
     *           <li>当前用户不存在</li>
     *         </ul>
     * @see AgvCarAddDTO AGV小车添加数据传输对象
     */
    @PostMapping("/car")
    public ResponseEntity<AjaxResult> addAgvCar(@RequestBody AgvCarAddDTO agvCarAddDTO) {
        // 验证 carNumber 是否为空
        if (agvCarAddDTO.getCarNumber() == null || agvCarAddDTO.getCarNumber().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error(400,"小车编号不能为空"));
        }

        UserVo userVo = UserHolder.getUser();
        User user = userService.findByUname(userVo.getUsername());
        if (user == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error(400,"用户ID不存在"));
        }
        // 验证 carNumber 是否存在
        AgvCar agvCar = agvCarService.findByCarNumber(agvCarAddDTO.getCarNumber());
        if (agvCar != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error(400,"AGV 小车已存在"));
        }

        // 创建新 AGV 小车，使用默认值填充缺失字段
        AgvCar newAgvCar = new AgvCar(
                null, // id 由数据库自增
                agvCarAddDTO.getCarNumber(),
                Constants.CAR_STATUS_FREE, // 默认状态
                100, // 默认电量 100，满足 0-100 范围
                "", // 默认空 SKU
                0L, // 默认数量 0
                agvCarAddDTO.getLocationX(),
                agvCarAddDTO.getLocationY(),
                0.0, // 默认终点 X 坐标
                0.0, // 默认终点 Y 坐标
                user.getUserId(),
                agvCarAddDTO.getMaxWeight() != null ? agvCarAddDTO.getMaxWeight() : 0.0, // 默认 0.0
                0.0,          //默认0.0
                agvCarAddDTO.getLocationX(),
                agvCarAddDTO.getLocationY()
        );
        Boolean success = agvCarService.addOneAgvCar(newAgvCar);
        if (success) {
            remindService.saveRemind(new Remind(null,Constants.REMIND_SUCCESS,"成功添加小车","于"+LocalDateTime.now()+",时刻,"+
                    user.getUserType()+user.getUsername()+"添加了编号为"+newAgvCar.getCarNumber()+"的小车",
                    LocalDateTime.now(),"1"));
            return ResponseEntity.ok().body(AjaxResult.success());
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(AjaxResult.error(500,"添加失败"));
        }
    }

    /**
     * 更新AGV小车信息
     *
     * <p>修改指定ID的AGV小车信息，保留原有值如果DTO未提供。
     * 小车编号不能修改，只能更新其他字段。
     *
     * @param id 要更新的AGV小车ID，路径变量
     * @param agvCarInfoDTO 包含更新信息的DTO对象，可以包含：
     *                      <ul>
     *                        <li>carNumber - 小车编号(必填，但实际不会修改)</li>
     *                        <li>locationX - 新X坐标</li>
     *                        <li>locationY - 新Y坐标</li>
     *                        <li>maxWeight - 新最大载重</li>
     *                      </ul>
     * @return 操作结果响应实体，成功返回200状态码，失败返回相应错误码
     * @throws IllegalArgumentException 当：
     *         <ul>
     *           <li>小车编号为空</li>
     *           <li>AGV小车不存在</li>
     *           <li>新小车编号与其他小车冲突</li>
     *         </ul>
     * @see AgvCarInfoDTO AGV小车信息数据传输对象
     */
    @PutMapping("/car/{id}")
    public ResponseEntity<AjaxResult> updateAgvCar(
            @PathVariable Long id,
            @RequestBody AgvCarInfoDTO agvCarInfoDTO) {
        // 验证 carNumber 是否为空
        if (agvCarInfoDTO.getCarNumber() == null || agvCarInfoDTO.getCarNumber().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error(400,"小车编号不能为空"));
        }

        // 验证 carNumber 是否存在
        AgvCar agvCar = agvCarService.findById(id);
        if (agvCar == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error(400,"AGV 小车不存在"));
        }
        AgvCar number = agvCarService.findByCarNumber(agvCarInfoDTO.getCarNumber());
        if (number!=null&& !Objects.equals(agvCarInfoDTO.getCarNumber(), agvCar.getCarNumber())){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error(400,"编号重复"));
        }

        // 更新 AGV 小车，保留原有值如果 DTO 未提供
        AgvCar updatedAgvCar = new AgvCar(
                id,
                agvCarInfoDTO.getCarNumber(),
                agvCar.getStatus(),
                agvCar.getBatteryLevel(),
                agvCar.getSku(),
                agvCar.getQuantity(),
                agvCar.getStartX(),
                agvCar.getStartY(),
                agvCar.getEndX(),
                agvCar.getEndY(),
                agvCar.getUserId(),
                agvCarInfoDTO.getMaxWeight() != null ? agvCarInfoDTO.getMaxWeight() : agvCar.getMaxWeight(),
                agvCar.getEndZ(),
                agvCarInfoDTO.getLocationX(),
                agvCarInfoDTO.getLocationY()
        );
        boolean success = agvCarService.updateById(updatedAgvCar);
        UserVo user = UserHolder.getUser();
        if (success) {
            remindService.saveRemind(new Remind(null,Constants.REMIND_SUCCESS,"成功修改小车","于"+LocalDateTime.now()+",时刻,"+
                    user.getUserType()+user.getUsername()+"修改了小车信息，现编号为"+updatedAgvCar.getCarNumber(),
                    LocalDateTime.now(),"1"));
            return ResponseEntity.ok().body(AjaxResult.success());
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(AjaxResult.error(500,"修改失败"));
        }
    }

    /**
     * 删除AGV小车记录
     *
     * <p>从系统中删除指定ID的AGV小车，但有以下限制：
     * <ul>
     *   <li>不能删除状态为运行中({@link Constants#CAR_STATUS_ONGOING})的小车</li>
     *   <li>不能删除状态为维修中({@link Constants#CAR_STATUS_REPAIR})的小车</li>
     * </ul>
     *
     * @param id 要删除的AGV小车ID，路径变量
     * @return 操作结果响应实体，成功返回200状态码和成功消息，失败返回相应错误码
     * @throws IllegalStateException 当小车状态不允许删除时抛出
     */
    @DeleteMapping("/car/{id}")
    public ResponseEntity<AjaxResult> removeAgvCar(@PathVariable Long id) {
        AgvCar agvCar = agvCarService.findById(id);
        if (agvCar == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error("没有该 AGV 小车"));
        } else if(agvCar.getStatus().equals(Constants.CAR_STATUS_ONGOING)||agvCar.getStatus().equals(Constants.CAR_STATUS_REPAIR)){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error("AGV 小车目前无法删除"));
        } else {
            String carNumber = agvCar.getCarNumber();
//            Boolean success = agvCarService.deleteByCarNumber(agvCarRemoveDTO.getCarNumber());
            Boolean success = agvCarService.deleteById(id);
            if (success) {
                UserVo user = UserHolder.getUser();
                remindService.saveRemind(new Remind(null,Constants.REMIND_SUCCESS,"成功删除小车","于"+LocalDateTime.now()+",时刻,"+
                        user.getUserType()+user.getUsername()+"删除了编号为"+carNumber+"的小车",
                        LocalDateTime.now(),"1"));
                return ResponseEntity.ok().body(AjaxResult.success("删除成功"));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(AjaxResult.error(500,"删除失败"));
            }
        }
    }
}