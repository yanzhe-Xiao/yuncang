package com.xhz.yuncang.controller;

import com.xhz.yuncang.service.AgvPathPlanningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
/**
 * AGV路径规划控制器
 *
 * <p>负责处理AGV小车的路径规划请求，根据订单信息计算最优运输路径。
 * 提供基于订单的AGV路径规划功能，返回包含路径点坐标的详细规划结果。
 *
 * <p>主要功能：
 * <ul>
 *   <li>为指定订单规划AGV运输路径</li>
 *   <li>返回包含状态、消息和路径点坐标的响应</li>
 * </ul>
 *
 * @author skc
 * @version 1.0
 * @since 2025-06
 * @see AgvPathPlanningService AGV路径规划服务接口
 */
@RestController
@RequestMapping("/api/agv")
@PreAuthorize("hasRole('管理员') or hasRole('操作员')")
public class AgvPathPlanningController {
    /**
     * AGV路径规划服务实例
     */
    private final AgvPathPlanningService agvPathPlanningService;

    /**
     * 构造函数，依赖注入AGV路径规划服务
     *
     * @param agvPathPlanningService AGV路径规划服务实现
     */
    @Autowired
    public AgvPathPlanningController(AgvPathPlanningService agvPathPlanningService) {
        this.agvPathPlanningService = agvPathPlanningService;
    }

    /**
     * 为订单规划AGV运输路径
     *
     * <p>根据订单编号计算最优AGV运输路径，返回包含以下信息的响应：
     * <ul>
     *   <li>路径规划状态(成功/失败/错误)</li>
     *   <li>详细消息</li>
     *   <li>路径点坐标列表，格式为Map&lt;小车编号,路径点数组&gt;</li>
     * </ul>
     *
     * <p>可能的错误情况：
     * <ul>
     *   <li>订单不存在</li>
     *   <li>无可用AGV</li>
     *   <li>无有效路径</li>
     *   <li>系统内部错误</li>
     * </ul>
     *
     * @param request 包含订单编号的请求参数，格式为：
     *                {
     *                    "orderNumber": "订单编号"
     *                }
     * @return 路径规划响应实体，包含：
     *         <ul>
     *           <li>status - 状态("成功"/"失败"/"错误")</li>
     *           <li>message - 详细消息</li>
     *           <li>paths - 路径数据(Map格式，key为小车编号，value为路径点数组)</li>
     *         </ul>
     * @see AgvPathResponse 路径规划响应数据结构
     */
    @PostMapping("/plan-path")
    public ResponseEntity<AgvPathResponse> planPath(@RequestBody Map<String, String> request) {
        try {
            Map<String, List<int[]>> agvPaths = agvPathPlanningService.planPathsForOrder(request.get("orderNumber"));
            if (agvPaths.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new AgvPathResponse("失败", "订单不存在、无可用AGV或无有效路径", Collections.emptyMap()));
            }
            return ResponseEntity.ok(new AgvPathResponse("成功", "路径规划完成，分配AGV数: " + agvPaths.size(), agvPaths));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AgvPathResponse("错误", "路径规划失败: " + e.getMessage(), Collections.emptyMap()));
        }
    }

    /**
     * AGV路径规划响应数据结构
     *
     * <p>包含路径规划的结果状态、消息和具体路径信息
     *
     * <p>路径点坐标格式说明：
     * <ul>
     *   <li>每个路径点是一个int数组，通常包含[x,y]坐标</li>
     *   <li>路径点列表按顺序表示AGV的移动路径</li>
     * </ul>
     */
    public static class AgvPathResponse {
        /**
         * 操作状态(成功/失败/错误)
         */
        private String status;
        /**
         * 详细消息
         */
        private String message;
        /**
         * 路径数据，key为小车编号，value为路径点数组
         */
        private Map<String, List<int[]>> paths;
        /**
         * 构造函数
         *
         * @param status 操作状态
         * @param message 详细消息
         * @param paths 路径数据
         */
        public AgvPathResponse(String status, String message, Map<String, List<int[]>> paths) {
            this.status = status;
            this.message = message;
            this.paths = paths;
        }
        /**
         * 获取操作状态
         *
         * @return 操作状态字符串
         */
        public String getStatus() {
            return status;
        }

        /**
         * 设置操作状态
         *
         * @param status 新的操作状态
         */
        public void setStatus(String status) {
            this.status = status;
        }

        /**
         * 获取详细消息
         *
         * @return 消息内容
         */
        public String getMessage() {
            return message;
        }

        /**
         * 设置详细消息
         *
         * @param message 新的消息内容
         */
        public void setMessage(String message) {
            this.message = message;
        }

        /**
         * 获取路径数据
         *
         * @return 路径数据Map，key为小车编号，value为路径点数组
         */
        public Map<String, List<int[]>> getPaths() {
            return paths;
        }

        /**
         * 设置路径数据
         *
         * @param paths 新的路径数据
         */
        public void setPaths(Map<String, List<int[]>> paths) {
            this.paths = paths;
        }
    }
}