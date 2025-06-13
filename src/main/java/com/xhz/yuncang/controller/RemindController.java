package com.xhz.yuncang.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xhz.yuncang.dto.WarningDTO;
import com.xhz.yuncang.entity.Product;
import com.xhz.yuncang.entity.Remind;
import com.xhz.yuncang.service.RemindService;
import com.xhz.yuncang.utils.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * 预警提醒管理控制器
 *
 * <p>负责处理系统预警和提醒信息的管理，提供以下核心功能：
 * <ul>
 *   <li>预警信息的分页查询</li>
 *   <li>提醒信息的获取</li>
 *   <li>支持按内容模糊查询</li>
 * </ul>
 *
 * <p>数据特性：
 * <ul>
 *   <li>预警信息按时间倒序排列</li>
 *   <li>分页查询需验证页码有效性</li>
 * </ul>
 * @author xhz
 * @see RemindService 提醒服务接口
 */
@RestController
@PreAuthorize("hasRole('管理员')")
public class RemindController {
    /**
     * 提醒服务实例
     */
    @Autowired
    private RemindService remindService;
    /**
     * 获取预警列表
     *
     * <p>获取所有预警信息（未分页），包含以下处理：
     * <ol>
     *   <li>获取按时间倒序排列的预警列表</li>
     *   <li>构建固定格式的响应结构</li>
     * </ol>
     *
     * <p>响应结构说明：
     * <ul>
     *   <li>prev/next - 固定为-1（未实现分页）</li>
     *   <li>total - 固定为1（需优化）</li>
     *   <li>list - 实际预警列表</li>
     * </ul>
     *
     * @param current 当前页码（参数未使用）
     * @param pageSize 每页记录数（参数未使用）
     * @return 响应实体，包含：
     *         <ul>
     *           <li>成功：状态码200+预警列表</li>
     *         </ul>
     * @deprecated 该方法的分页参数未实际使用，需优化实现
     */
    @GetMapping("/warning")
    public ResponseEntity<AjaxResult> getWarningList(
            @RequestParam(defaultValue = "1")int current,
            @RequestParam(defaultValue = "10")int pageSize
            ){
        List<Remind> reminds = remindService.listRemindsDesc();
        List<Remind> remindList = remindService.listAllReminds();
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("prev", -1);//Error
        responseMap.put("next", -1);//  已处理
        responseMap.put("total", 1);//Warming的数量
        responseMap.put("list", remindList);
        return ResponseEntity.ok().body(AjaxResult.success(responseMap));
    }

    @PostMapping("/warning")
    public ResponseEntity<AjaxResult> updateProcessed(@RequestBody WarningDTO warningDTO){
        Long id = warningDTO.getId();
        Remind remind = remindService.getById(id);
        remind.setProcessed("1");
        remindService.updateById(remind);
        System.out.println(remind);
        return ResponseEntity.ok(AjaxResult.success());
    }

    @GetMapping("/remind")
    public ResponseEntity<AjaxResult> getRemindList(
            @RequestParam(defaultValue = "1")int current,
            @RequestParam(defaultValue = "10")int pageSize,
            @RequestParam(defaultValue = "")String context
    ){

        Page<Remind> page = new Page<>(current, pageSize);
        QueryWrapper<Remind> queryWrapper = new QueryWrapper<>();

        if (!context.isEmpty()) {
            queryWrapper.like("context", context);
        }
        Page<Remind> result = remindService.page(page, queryWrapper);
        if (result.getPages() == 0) {
            return ResponseEntity.ok().body(AjaxResult.success("当前预警信息为空", ""));
        }
        if (current>result.getPages()){
            //大于最大页数
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AjaxResult.error(400,"页数大于最大页数"));
        }
        int nowPage = (int) result.getCurrent();
        int beforePage = nowPage == 1 ? -1 : nowPage - 1;
        int nextPage = nowPage == (int) result.getPages() ? -1 : nowPage + 1;


        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("prev", beforePage);//Error
        responseMap.put("next", nextPage);//  已处理
        responseMap.put("total", result.getTotal());//Warming的数量
        responseMap.put("list", result.getRecords());
        System.out.println(responseMap);
        return ResponseEntity.ok().body(AjaxResult.success(responseMap));
    }
}
