package com.xhz.yuncang.service;

import com.xhz.yuncang.entity.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AgvPathPlanningService {

    private final AgvCarService agvCarService;
    private final InboundOrderService inboundOrderService;
    private final InboundOrderDetailService inboundOrderDetailService;
    private final StorageShelfService storageShelfService;
    private final ShelfInventoryService shelfInventoryService;
    private final ProductService productService;

    // 仓库尺寸（网格）
    private static final int WAREHOUSE_WIDTH = 48;
    private static final int WAREHOUSE_HEIGHT = 34;

    // 区域边界（网格）
    private static final int INBOUND_MIN_X = 1, INBOUND_MAX_X = 10, INBOUND_MIN_Y = 25, INBOUND_MAX_Y = 34;
    private static final int PARKING_MIN_X = 1, PARKING_MAX_X = 10, PARKING_MIN_Y = 13, PARKING_MAX_Y = 22;
    private static final int OUTBOUND_MIN_X = 1, OUTBOUND_MAX_X = 10, OUTBOUND_MIN_Y = 1, OUTBOUND_MAX_Y = 10;

    // 每个AGV的最大处理数量
    private static final int MAX_QUANTITY_PER_AGV = 100;

    // 图
    private Integer[][] g = new Integer[WAREHOUSE_WIDTH + 1][WAREHOUSE_HEIGHT + 1];

    // 实例初始化块
    {
        for (int x = 1; x <= WAREHOUSE_WIDTH; x++) {
            for (int y = 1; y <= WAREHOUSE_HEIGHT; y++) {
                if (y != 11 && y != 12 && y != 23 && y != 24) {
                    if (x == 11 || x == 14 || x == 15 || x == 18 || x == 19 || x == 22 || x == 23
                            || x == 26 || x == 27 || x == 30 || x == 31 || x == 34 || x == 35 || x == 38
                            || x == 39 || x == 42 || x == 43 || x == 46 || x == 47) {
                        g[x][y] = 1;
                    } else {
                        g[x][y] = 0; // 其他元素初始化为 0
                    }
                } else {
                    g[x][y] = 0; // 排除的行初始化为 0
                }
            }
        }
    }

    // A* 算法的节点类
    private static class Node {
        int x, y;
        int gCost, hCost;
        Node parent;

        Node(int x, int y) {
            this.x = x;
            this.y = y;
            this.gCost = 0;
            this.hCost = 0;
            this.parent = null;
        }

        int fCost() {
            return gCost + hCost;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Node node = (Node) o;
            return x == node.x && y == node.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }

    public AgvPathPlanningService(
            AgvCarService agvCarService,
            InboundOrderService inboundOrderService,
            InboundOrderDetailService inboundOrderDetailService,
            StorageShelfService storageShelfService,
            ShelfInventoryService shelfInventoryService,
            ProductService productService) {
        this.agvCarService = agvCarService;
        this.inboundOrderService = inboundOrderService;
        this.inboundOrderDetailService = inboundOrderDetailService;
        this.storageShelfService = storageShelfService;
        this.shelfInventoryService = shelfInventoryService;
        this.productService = productService;
    }

    public Map<String, List<int[]>> planPathsForOrder(String orderNumber) {
        Map<String, List<int[]>> agvPaths = new HashMap<>();
        Set<String> assignedAgvs = new HashSet<>();
        System.out.println("处理订单: " + orderNumber);

        if (orderNumber == null || orderNumber.isEmpty()) {
            System.out.println("错误: orderNumber 为空");
            return agvPaths;
        }

        InboundOrder order = inboundOrderService.getByOrderNumber(orderNumber);
        if (order == null) {
            System.out.println("错误: 订单不存在 - " + orderNumber);
            return agvPaths;
        }

        List<InboundOrderDetail> details = inboundOrderDetailService.listByOrderNumber(orderNumber);
        if (details == null || details.isEmpty()) {
            System.out.println("错误: 订单无明细 - " + orderNumber);
            return agvPaths;
        }
        System.out.println("订单明细数量: " + details.size());

        int[] inboundPos = new int[]{INBOUND_MIN_X, INBOUND_MIN_Y}; // 使用入库区边界
        System.out.println("入库扫描区: (" + inboundPos[0] + ", " + inboundPos[1] + ")");

        for (InboundOrderDetail detail : details) {
            String sku = detail.getSku();
            long remainingQuantity = detail.getQuantity();
            System.out.println("处理明细: SKU=" + sku + ", 总数量=" + remainingQuantity);

            Product product = productService.findBySku(sku);
            if (product == null) {
                System.out.println("警告: SKU不存在 - " + sku);
                continue;
            }
            double productLength = product.getLength() != null ? product.getLength() : 0.0;
            double productWidth = product.getWidth() != null ? product.getWidth() : 0.0;
            double productHeight = product.getHeight() != null ? product.getHeight() : 0.0;
            double productWeight = product.getWeight() != null ? product.getWeight() : 0.0;
            System.out.println("产品: " + sku + ", 尺寸=" + productLength + "x" + productWidth + "x" + productHeight + ", 重量=" + productWeight);

            String lastUsedShelfCode = null;
            long shelfAllocationIndex = 0;

            while (remainingQuantity > 0) {
                AgvCar agv = assignAvailableAgv(assignedAgvs);
                if (agv == null) {
                    System.out.println("错误: 无可用AGV，剩余数量=" + remainingQuantity);
                    break;
                }
                System.out.println("分配AGV: " + agv.getCarNumber());
                assignedAgvs.add(agv.getCarNumber());

                int[] startPos = getAgvStartingPosition(agv.getCarNumber());
                System.out.println("AGV起始位置: (" + startPos[0] + ", " + startPos[1] + ")");
                List<int[]> path = new ArrayList<>();
                int[] currentPos = startPos;

                long agvQuantity = Math.min(remainingQuantity, MAX_QUANTITY_PER_AGV);
                System.out.println("AGV " + agv.getCarNumber() + " 处理数量: " + agvQuantity);

                boolean validAllocation = true;
                long allocatedQuantity = 0;

                while (allocatedQuantity < agvQuantity && validAllocation) {
                    StorageShelf shelf = findSuitableShelf(sku, productLength, productWidth, productHeight, productWeight, agvQuantity - allocatedQuantity, shelfAllocationIndex++, lastUsedShelfCode);
                    if (shelf == null) {
                        System.out.println("警告: 无合适货架 - SKU=" + sku);
                        validAllocation = false;
                        break;
                    }
                    lastUsedShelfCode = shelf.getShelfCode();
                    int[] shelfPos = new int[]{
                            shelf.getLocationX() != null ? shelf.getLocationX().intValue() : 0,
                            shelf.getLocationY() != null ? shelf.getLocationY().intValue() : 0
                    };
                    System.out.println("分配货架: " + shelf.getShelfCode() + ", 位置=(" + shelfPos[0] + ", " + shelfPos[1] + ")");

                    // 计算实际分配件数
                    double currentWeight = shelfInventoryService.getCurrentTotalWeightByShelfCode(shelf.getShelfCode());
                    long maxWeightItems = productWeight > 0 ? (long) Math.floor((shelf.getMaxWeight() - currentWeight) / productWeight) : 0;
                    long maxLengthItems = productLength > 0 ? (long) Math.floor(shelf.getLength() / productLength) : Long.MAX_VALUE;
                    long maxWidthItems = productWidth > 0 ? (long) Math.floor(shelf.getWidth() / productWidth) : Long.MAX_VALUE;
                    long maxHeightItems = productHeight > 0 ? (long) Math.floor(shelf.getHeight() / productHeight) : 1;
                    long maxSizeItems = maxLengthItems * maxWidthItems * maxHeightItems;
                    long maxAdditionalItems = Math.min(maxWeightItems, maxSizeItems);
                    long itemsToAllocate = Math.min(agvQuantity - allocatedQuantity, maxAdditionalItems);
                    if (itemsToAllocate <= 0) {
                        System.out.println("警告: 货架 " + shelf.getShelfCode() + " 已满（重量=" + currentWeight +
                                ", 容量=" + maxSizeItems + "件, 需=" + (agvQuantity - allocatedQuantity) + "）");
                        continue;
                    }

                    List<int[]> pathToInbound = findShortestPath(currentPos, inboundPos);
                    System.out.println("到入库区路径长度: " + pathToInbound.size());
                    List<int[]> pathToShelf = findShortestPath(inboundPos, shelfPos);
                    System.out.println("到货架路径长度: " + pathToShelf.size());

                    if (pathToInbound.isEmpty() || pathToShelf.isEmpty()) {
                        System.out.println("警告: 路径规划失败 - 从(" + currentPos[0] + "," + currentPos[1] + ")到入库区或货架(" + shelfPos[0] + "," + shelfPos[1] + ")");
                        validAllocation = false;
                        break;
                    }

                    path.addAll(pathToInbound);
                    path.addAll(pathToShelf.subList(1, pathToShelf.size()));
                    updateShelfInventory(shelf.getShelfCode(), sku, itemsToAllocate, productLength, productWidth, productHeight, productWeight);
                    allocatedQuantity += itemsToAllocate;
                    currentPos = shelfPos;
                }

                if (validAllocation) {
                    int[] parkingPos = startPos;
                    List<int[]> pathBackToParking = findShortestPath(currentPos, parkingPos);
                    System.out.println("返回停车区路径长度: " + pathBackToParking.size());
                    if (!pathBackToParking.isEmpty()) {
                        path.addAll(pathBackToParking.subList(1, pathBackToParking.size()));
                    } else {
                        System.out.println("警告: 返回停车区路径为空 - 从(" + currentPos[0] + "," + currentPos[1] + ")到(" + parkingPos[0] + "," + parkingPos[1] + ")");
                        validAllocation = false;
                    }
                }

                if (validAllocation && !path.isEmpty() && allocatedQuantity > 0) {
                    agvPaths.compute(agv.getCarNumber(), (k, v) -> {
                        if (v == null) return path;
                        v.addAll(path.subList(1, path.size()));
                        return v;
                    });
                    System.out.println("AGV " + agv.getCarNumber() + " 路径点数: " + path.size());
                    remainingQuantity -= allocatedQuantity;
                } else {
                    System.out.println("警告: AGV " + agv.getCarNumber() + " 分配失败，路径或货架无效");
                    assignedAgvs.remove(agv.getCarNumber());
                }
            }
        }

        System.out.println("总分配AGV数量: " + agvPaths.size());
        return agvPaths;
    }

    private AgvCar assignAvailableAgv(Set<String> assignedAgvs) {
        List<AgvCar> agvs = agvCarService.findAll();
        if (agvs == null || agvs.isEmpty()) {
            System.out.println("警告: 无可用AGV - 数据库无记录");
            return null;
        }
        AgvCar agv = agvs.stream()
                .filter(a -> "空闲".equals(a.getStatus()) && !assignedAgvs.contains(a.getCarNumber()))
                .findFirst()
                .orElse(null);
        if (agv != null) {
            System.out.println("分配AGV: " + agv.getCarNumber());
        } else {
            System.out.println("警告: 无可用AGV");
        }
        return agv;
    }

    private int[] getAgvStartingPosition(String carNumber) {
        AgvCar agv = agvCarService.findByCarNumber(carNumber);
        if (agv != null && agv.getLocationX() != null && agv.getLocationY() != null) {
            int x = agv.getLocationX().intValue();
            int y = agv.getLocationY().intValue();
            if (x >= PARKING_MIN_X && x <= PARKING_MAX_X && y >= PARKING_MIN_Y && y <= PARKING_MAX_Y) {
                return new int[]{x, y};
            }
        }
        try {
            int index = Integer.parseInt(carNumber.replaceAll("[^0-9]", ""));
            int x = PARKING_MIN_X + ((index - 1) / 2) * 20;
            int y = PARKING_MIN_Y + ((index - 1) % 2) * 50;
            System.out.println("使用备用AGV位置: (" + x + ", " + y + ") for " + carNumber);
            return new int[]{x, y};
        } catch (NumberFormatException e) {
            System.out.println("错误: 无效的AGV编号 - " + carNumber);
            return new int[]{PARKING_MIN_X, PARKING_MIN_Y};
        }
    }

    private StorageShelf findSuitableShelf(String sku, double length, double width, double height, double weight, long quantity, long index, String lastUsedShelfCode) {
        List<StorageShelf> shelves = storageShelfService.findAllShelves();
        if (shelves == null || shelves.isEmpty()) {
            System.out.println("错误: 无可用货架 - 数据库无记录");
            return null;
        }

        shelves.sort(Comparator.comparing(StorageShelf::getShelfCode));

        for (StorageShelf shelf : shelves) {
            // 检查货架是否为空或只含当前 SKU
            List<ShelfInventory> inventories = shelfInventoryService.findByShelfCode(shelf.getShelfCode());
            boolean isShelfValid = inventories.isEmpty() ||
                    inventories.stream().allMatch(inv -> sku.equals(inv.getSku()));
            if (!isShelfValid) {
                System.out.println("警告: 货架 " + shelf.getShelfCode() + " 含其他 SKU，跳过");
                continue;
            }

            // 验证单件尺寸
            if (shelf.getLength() != null && shelf.getLength() >= length &&
                    shelf.getWidth() != null && shelf.getWidth() >= width &&
                    shelf.getHeight() != null && shelf.getHeight() >= height) {
                double currentWeight = shelfInventoryService.getCurrentTotalWeightByShelfCode(shelf.getShelfCode());
                // 验证载重
                double totalWeightNeeded = weight * quantity;
                if (currentWeight + totalWeightNeeded <= shelf.getMaxWeight()) {
                    // 验证容量（多层堆叠）
                    long maxLengthItems = length > 0 ? (long) Math.floor(shelf.getLength() / length) : Long.MAX_VALUE;
                    long maxWidthItems = width > 0 ? (long) Math.floor(shelf.getWidth() / width) : Long.MAX_VALUE;
                    long maxHeightItems = height > 0 ? (long) Math.floor(shelf.getHeight() / height) : 1;
                    long maxSizeItems = maxLengthItems * maxWidthItems * maxHeightItems;
                    if (maxSizeItems >= quantity) {
                        System.out.println("分配货架: " + shelf.getShelfCode() + ", 位置=(" +
                                (shelf.getLocationX() != null ? shelf.getLocationX() : 0) + "," +
                                (shelf.getLocationY() != null ? shelf.getLocationY() : 0) + "), 当前重量=" +
                                currentWeight + ", 新增重量=" + totalWeightNeeded + ", 最大重量=" + shelf.getMaxWeight() +
                                ", 容量=" + maxSizeItems + "件 (" + maxLengthItems + "x" + maxWidthItems + "x" + maxHeightItems + "), 需=" + quantity);
                        return shelf;
                    } else {
                        System.out.println("警告: 货架 " + shelf.getShelfCode() + " 容量不足，货架=" +
                                shelf.getLength() + "x" + shelf.getWidth() + "x" + shelf.getHeight() +
                                ", 可容纳=" + maxSizeItems + "件 (" + maxLengthItems + "x" + maxWidthItems + "x" + maxHeightItems + "), 需=" + quantity);
                    }
                } else {
                    System.out.println("警告: 货架 " + shelf.getShelfCode() + " 重量超限，当前=" +
                            currentWeight + ", 新增=" + totalWeightNeeded + ", 最大=" + shelf.getMaxWeight());
                }
            } else {
                System.out.println("警告: 货架 " + shelf.getShelfCode() + " 尺寸不足，货架=" +
                        (shelf.getLength() != null ? shelf.getLength() : "null") + "x" +
                        (shelf.getWidth() != null ? shelf.getWidth() : "null") + "x" +
                        (shelf.getHeight() != null ? shelf.getHeight() : "null") + ", 产品=" +
                        length + "x" + width + "x" + height);
            }
        }

        System.out.println("错误: 无可用货架 - SKU=" + sku);
        return null;
    }

    private void updateShelfInventory(String shelfCode, String sku, long quantity, double length, double width, double height, double weight) {
        try {
            ShelfInventory inventory = shelfInventoryService.findByShelfCodeAndSku(shelfCode, sku);
            if (inventory == null) {
                inventory = new ShelfInventory();
                inventory.setShelfCode(shelfCode);
                inventory.setSku(sku);
                inventory.setQuantity(quantity);
                shelfInventoryService.addShelfInventory(inventory);
                System.out.println("新增库存记录: 货架=" + shelfCode + ", SKU=" + sku + ", 数量=" + quantity);
            } else {
                inventory.setQuantity(inventory.getQuantity() + quantity);
                shelfInventoryService.updateShelfInventory(inventory);
                System.out.println("更新库存记录: 货架=" + shelfCode + ", SKU=" + sku + ", 新数量=" + inventory.getQuantity());
            }
        } catch (Exception e) {
            System.out.println("错误: 更新库存失败 - 货架=" + shelfCode + ", SKU=" + sku + ", 原因=" + e.getMessage());
        }
    }

    private List<int[]> findShortestPath(int[] start, int[] goal) {
        System.out.println("A* 输入: 起点=(" + start[0] + "," + start[1] + "), 目标货架=(" + goal[0] + "," + goal[1] + ")");
        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingInt(Node::fCost));
        Set<Node> closedSet = new HashSet<>();
        Node startNode = new Node(start[0], start[1]);
        Node goalNode = new Node(goal[0], goal[1]);
        openSet.add(startNode);

        // 检查起点和目标点是否有效
        if (!isValidPosition(start[0], start[1])) {
            System.out.println("错误: 起点无效或为障碍 - 起点=(" + start[0] + "," + start[1] + ")");
            return new ArrayList<>();
        }
        if (!isValidPosition(goal[0], goal[1])) {
            System.out.println("错误: 目标点无效或为障碍 - 目标=(" + goal[0] + "," + goal[1] + ")");
            return new ArrayList<>();
        }

        // 查找目标点旁1格的可通行点作为实际目标
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}}; // 上下左右
        List<Node> adjacentGoals = new ArrayList<>();
        for (int[] dir : directions) {
            int adjX = goal[0] + dir[0];
            int adjY = goal[1] + dir[1];
            if (isValidPosition(adjX, adjY)) {
                adjacentGoals.add(new Node(adjX, adjY));
            }
        }
        if (adjacentGoals.isEmpty()) {
            System.out.println("错误: 目标点旁无可用格子 - 目标=(" + goal[0] + "," + goal[1] + ")");
            return new ArrayList<>();
        }

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();

            // 检查是否到达任一邻近目标点
            for (Node adjGoal : adjacentGoals) {
                if (current.x == adjGoal.x && current.y == adjGoal.y) {
                    List<int[]> path = reconstructPath(current);
                    System.out.println("A* 输出: 路径点数=" + path.size() + ", 终点=(" + current.x + "," + current.y + ")");
                    return path;
                }
            }

            closedSet.add(current);

            for (int[] dir : directions) {
                int newX = current.x + dir[0];
                int newY = current.y + dir[1];

                // 检查边界和障碍
                if (!isValidPosition(newX, newY)) {
                    continue;
                }

                Node neighbor = new Node(newX, newY);
                if (closedSet.contains(neighbor)) {
                    continue;
                }

                int tentativeGCost = current.gCost + 10; // 每步成本固定为10
                neighbor.gCost = tentativeGCost;
                // 使用离最近邻近目标点的曼哈顿距离作为启发式
                int minHCost = Integer.MAX_VALUE;
                for (Node adjGoal : adjacentGoals) {
                    int hCost = manhattanDistance(newX, newY, adjGoal.x, adjGoal.y);
                    minHCost = Math.min(minHCost, hCost);
                }
                neighbor.hCost = minHCost;
                neighbor.parent = current;

                // 更新openSet中的节点
                Optional<Node> existingNode = openSet.stream()
                        .filter(node -> node.equals(neighbor))
                        .findFirst();
                if (existingNode.isEmpty()) {
                    openSet.add(neighbor);
                } else if (existingNode.get().gCost > tentativeGCost) {
                    existingNode.get().gCost = tentativeGCost;
                    existingNode.get().hCost = minHCost;
                    existingNode.get().parent = current;
                }
            }
        }

        System.out.println("警告: 未找到到达目标点旁1格的路径 - 从(" + start[0] + "," + start[1] + ")到目标=(" + goal[0] + "," + goal[1] + ")");
        return new ArrayList<>();
    }

    private boolean isValidPosition(int x, int y) {
        // 检查是否在仓库边界内
        if (x < 1 || x > WAREHOUSE_WIDTH || y < 1 || y > WAREHOUSE_HEIGHT) {
            return false;
        }
        // 检查是否为障碍（g[x][y] == 1）
        return g[x][y] != null && g[x][y] == 0;
    }

    private int manhattanDistance(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    private List<int[]> reconstructPath(Node goal) {
        List<int[]> path = new ArrayList<>();
        Node current = goal;
        while (current != null) {
            path.add(new int[]{current.x, current.y});
            current = current.parent;
        }
        Collections.reverse(path);
        return path;
    }
}