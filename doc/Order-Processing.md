# Order Processing

> **Relevant source files**
> * [src/main/java/com/xhz/yuncang/controller/InboundOrderController.java](https://github.com/yanzhe-Xiao/yuncang/blob/a4a28616/src/main/java/com/xhz/yuncang/controller/InboundOrderController.java)
> * [src/main/java/com/xhz/yuncang/controller/InboundOrderDetailController.java](https://github.com/yanzhe-Xiao/yuncang/blob/a4a28616/src/main/java/com/xhz/yuncang/controller/InboundOrderDetailController.java)
> * [src/main/java/com/xhz/yuncang/controller/OutboundOrderController.java](https://github.com/yanzhe-Xiao/yuncang/blob/a4a28616/src/main/java/com/xhz/yuncang/controller/OutboundOrderController.java)

## Purpose and Scope

This document covers the order processing subsystem of the yuncang warehouse management system, which handles both inbound and outbound order workflows. Order processing encompasses order creation, validation, execution, and integration with the AGV automation system for physical warehouse operations.

For inventory tracking and storage management details, see [Inventory Management](/yanzhe-Xiao/yuncang/4.1-inventory-management). For AGV automation specifics, see [AGV Automation](/yanzhe-Xiao/yuncang/4.3-agv-automation). For complete API documentation, see [Order Management APIs](/yanzhe-Xiao/yuncang/7.2-order-management-apis).

## System Overview

The order processing system consists of three main controllers that handle the complete order lifecycle from creation to fulfillment:

```mermaid
flowchart TD

IOC["InboundOrderController"]
IODC["InboundOrderDetailController"]
OOC["OutboundOrderController"]
IOS["InboundOrderService"]
IODS["InboundOrderDetailService"]
OOS["OutboundOrderService"]
SOS["SalesOrderService"]
SIS["ShelfInventoryService"]
IS["InventoryService"]
ACS["AgvCarService"]
BFP["BfsFindPath"]
CSTS["CarStorageToShelf"]
CO["CarOut"]
IB_ORDER["InboundOrder"]
IB_DETAIL["InboundOrderDetail"]
OUT_ORDER["OutboundOrder"]
SALES_ORDER["SalesOrder"]
SHELF_INV["ShelfInventory"]
INVENTORY["Inventory"]
AGV_CAR["AgvCar"]

IOC --> IOS
IOC --> IODS
IOC --> SIS
IOC --> ACS
IOC --> BFP
IODC --> IODS
IODC --> IOS
OOC --> OOS
OOC --> SOS
OOC --> SIS
OOC --> IS
OOC --> ACS
OOC --> BFP
IOS --> IB_ORDER
IODS --> IB_DETAIL
OOS --> OUT_ORDER
SOS --> SALES_ORDER
SIS --> SHELF_INV
IS --> INVENTORY
ACS --> AGV_CAR

subgraph subGraph3 ["Data Layer"]
    IB_ORDER
    IB_DETAIL
    OUT_ORDER
    SALES_ORDER
    SHELF_INV
    INVENTORY
    AGV_CAR
end

subgraph subGraph2 ["Path Planning & AGV"]
    BFP
    CSTS
    CO
    BFP --> CSTS
    BFP --> CO
end

subgraph subGraph1 ["Core Services"]
    IOS
    IODS
    OOS
    SOS
    SIS
    IS
    ACS
end

subgraph subGraph0 ["Order Processing Controllers"]
    IOC
    IODC
    OOC
end
```

Sources: [src/main/java/com/xhz/yuncang/controller/InboundOrderController.java L31-L53](https://github.com/yanzhe-Xiao/yuncang/blob/a4a28616/src/main/java/com/xhz/yuncang/controller/InboundOrderController.java#L31-L53)

 [src/main/java/com/xhz/yuncang/controller/InboundOrderDetailController.java L48-L65](https://github.com/yanzhe-Xiao/yuncang/blob/a4a28616/src/main/java/com/xhz/yuncang/controller/InboundOrderDetailController.java#L48-L65)

 [src/main/java/com/xhz/yuncang/controller/OutboundOrderController.java L45-L81](https://github.com/yanzhe-Xiao/yuncang/blob/a4a28616/src/main/java/com/xhz/yuncang/controller/OutboundOrderController.java#L45-L81)

## Inbound Order Processing

### Order Creation and Validation

The `InboundOrderController` manages the complete inbound order lifecycle through several key endpoints:

```mermaid
flowchart TD

START["POST /inbound"]
VALIDATE["Validate Order Data"]
CHECK_NAME["Check Order Name Uniqueness"]
CALC_CAPACITY["Calculate Shelf Capacity"]
VALIDATE_PRODUCTS["Validate Product SKUs"]
CHECK_WEIGHT["Check Total Weight vs Available Capacity"]
CREATE_ORDER["Create InboundOrder with Snowflake ID"]
CREATE_DETAILS["Create InboundOrderDetail Records"]
SAVE_REMIND["Save Success Reminder"]
SUCCESS["Return Success Response"]
ERROR1["Return 400: Order Name Duplicate"]
ERROR2["Return 400: Product Not Found"]
ERROR3["Return 400: Shelf Capacity Insufficient"]

START --> VALIDATE
VALIDATE --> CHECK_NAME
CHECK_NAME --> CALC_CAPACITY
CALC_CAPACITY --> VALIDATE_PRODUCTS
VALIDATE_PRODUCTS --> CHECK_WEIGHT
CHECK_WEIGHT --> CREATE_ORDER
CREATE_ORDER --> CREATE_DETAILS
CREATE_DETAILS --> SAVE_REMIND
SAVE_REMIND --> SUCCESS
CHECK_NAME --> ERROR1
VALIDATE_PRODUCTS --> ERROR2
CHECK_WEIGHT --> ERROR3
```

**Key validation steps implemented in `addInboundOrder`:**

| Validation | Implementation | Error Response |
| --- | --- | --- |
| Order Name Uniqueness | `inboundOrderService.getByOrderName()` | "订单名称重复" |
| Product Existence | `productService.findByName()` | "没有该物品" |
| Shelf Capacity | Weight calculation vs `maxWeight` | "货架容量不足" |
| Data Completeness | Null checks on DTO fields | "请填入正确信息" |

Sources: [src/main/java/com/xhz/yuncang/controller/InboundOrderController.java L160-L237](https://github.com/yanzhe-Xiao/yuncang/blob/a4a28616/src/main/java/com/xhz/yuncang/controller/InboundOrderController.java#L160-L237)

### Order Execution and Path Planning

The order execution process involves complex algorithms for shelf allocation and AGV path planning:

```mermaid
sequenceDiagram
  participant Client
  participant InboundOrderController
  participant BfsFindPath
  participant AgvCarService
  participant ShelfInventoryService
  participant InventoryService

  Client->>InboundOrderController: GET /path/{id}
  InboundOrderController->>InboundOrderController: Validate Order Status (TOSTART)
  InboundOrderController->>ShelfInventoryService: Get Available Shelves
  InboundOrderController->>InboundOrderController: Sort Shelves by Distance
  InboundOrderController->>InboundOrderController: Allocate Products to Shelves
  InboundOrderController->>AgvCarService: Get Available AGV Cars
  InboundOrderController->>InboundOrderController: Distribute Loads Among Cars
  InboundOrderController->>BfsFindPath: Calculate Paths for Each Car
  InboundOrderController->>InboundOrderController: Handle Path Conflicts
  InboundOrderController->>InventoryService: Update Inventory
  InboundOrderController->>ShelfInventoryService: Update Shelf Inventory
  InboundOrderController->>InboundOrderController: Update Order Status to FINISHED
  InboundOrderController-->>Client: Return CarStorageToShelf List
```

**Distance Calculation Formula:**

```
distance = |x - beginX| * MOVE_DIFFER_X + |y - beginY| * MOVE_DIFFER_Y + |z - beginZ + 1| * MOVE_DIFFER_Z
```

Sources: [src/main/java/com/xhz/yuncang/controller/InboundOrderController.java L377-L907](https://github.com/yanzhe-Xiao/yuncang/blob/a4a28616/src/main/java/com/xhz/yuncang/controller/InboundOrderController.java#L377-L907)

 [src/main/java/com/xhz/yuncang/controller/InboundOrderController.java L920-L1410](https://github.com/yanzhe-Xiao/yuncang/blob/a4a28616/src/main/java/com/xhz/yuncang/controller/InboundOrderController.java#L920-L1410)

## Outbound Order Processing

### Order Retrieval and Status Management

The `OutboundOrderController` provides paginated access to outbound orders and manages their execution:

```mermaid
flowchart TD

A["GET /outboundorder/{pageNo}"]
B["Paginated Query"]
C["Convert to OutboundOrderInfoVo"]
D["Return PageVo Response"]
E["PUT /out/{id}"]
F["Validate Order Status"]
G["Execute Outbound Algorithm"]
H["Update Database"]
I["Return Path Results"]

subgraph subGraph0 ["Outbound Order Flow"]
    A
    B
    C
    D
    E
    F
    G
    H
    I
    A --> B
    B --> C
    C --> D
    E --> F
    F --> G
    G --> H
    H --> I
end
```

**Outbound Order States:**

* `STATUS_ORDER_TOSTART`: Ready for execution
* `STATUS_ORDER_FINISHED`: Completed processing

Sources: [src/main/java/com/xhz/yuncang/controller/OutboundOrderController.java L102-L146](https://github.com/yanzhe-Xiao/yuncang/blob/a4a28616/src/main/java/com/xhz/yuncang/controller/OutboundOrderController.java#L102-L146)

 [src/main/java/com/xhz/yuncang/controller/OutboundOrderController.java L182-L795](https://github.com/yanzhe-Xiao/yuncang/blob/a4a28616/src/main/java/com/xhz/yuncang/controller/OutboundOrderController.java#L182-L795)

### Picking Strategy and Optimization

The outbound algorithm implements multiple optimization strategies configurable through `FactoryConfig`:

| Strategy | Alpha (Path Weight) | Beta (Quantity Weight) | Use Case |
| --- | --- | --- | --- |
| `system-judged` | Dynamic (0.8/2.0) | Dynamic (3000/500) | Adaptive based on order characteristics |
| `short-path` | 2.0 | 500.0 | Minimize travel distance |
| `more-stock` | 0.5 | 3000.0 | Prefer shelves with more inventory |
| `balanced` | 1.0 | 1000.0 | Balance between path and quantity |

**Optimization Score Formula:**

```
score = (pathScore * alpha) + (beta / availableQuantity)
```

Sources: [src/main/java/com/xhz/yuncang/controller/OutboundOrderController.java L210-L254](https://github.com/yanzhe-Xiao/yuncang/blob/a4a28616/src/main/java/com/xhz/yuncang/controller/OutboundOrderController.java#L210-L254)

 [src/main/java/com/xhz/yuncang/controller/OutboundOrderController.java L298-L333](https://github.com/yanzhe-Xiao/yuncang/blob/a4a28616/src/main/java/com/xhz/yuncang/controller/OutboundOrderController.java#L298-L333)

## Order Detail Management

The `InboundOrderDetailController` provides comprehensive CRUD operations for order line items:

```mermaid
flowchart TD

A["POST /inboundOrderDetail/{pageNo}"]
A1["Paginated Detail Query"]
B["POST /inboundOrderDetail/add"]
B1["Add New Detail"]
C["POST /inboundOrderDetail/update"]
C1["Update Existing Detail"]
D["POST /inboundOrderDetail/remove"]
D1["Delete Detail"]
E["POST /inboundOrderDetail/findByOrderNumbers"]
E1["Batch Query Details"]
V1["Order Number Validation"]
V2["SKU Validation"]
V3["Quantity Validation"]
V4["Uniqueness Check"]

B1 --> V1
B1 --> V2
B1 --> V3
B1 --> V4
C1 --> V1
C1 --> V2
C1 --> V3
D1 --> V1
D1 --> V2

subgraph subGraph1 ["Validation Layer"]
    V1
    V2
    V3
    V4
end

subgraph subGraph0 ["Order Detail Operations"]
    A
    A1
    B
    B1
    C
    C1
    D
    D1
    E
    E1
    A --> A1
    B --> B1
    C --> C1
    D --> D1
    E --> E1
end
```

**Validation Rules:**

* Order Number: Must exist in `InboundOrder` table
* SKU: Must exist in `Product` table
* Quantity: Must be non-negative integer matching regex `^[0-9]+$`
* Uniqueness: No duplicate `orderNumber` + `sku` combinations

Sources: [src/main/java/com/xhz/yuncang/controller/InboundOrderDetailController.java L172-L209](https://github.com/yanzhe-Xiao/yuncang/blob/a4a28616/src/main/java/com/xhz/yuncang/controller/InboundOrderDetailController.java#L172-L209)

 [src/main/java/com/xhz/yuncang/controller/InboundOrderDetailController.java L239-L276](https://github.com/yanzhe-Xiao/yuncang/blob/a4a28616/src/main/java/com/xhz/yuncang/controller/InboundOrderDetailController.java#L239-L276)

## AGV Integration and Path Planning

### Path Generation Algorithm

The system uses BFS (Breadth-First Search) pathfinding with multiple map configurations:

```mermaid
flowchart TD

P1["Parking → Loading Area"]
P2["Loading Area → Warehouse Entry"]
P3["Warehouse Entry → Shelf Position"]
P4["Shelf Position → Warehouse Exit"]
P5["Warehouse Exit → Parking"]
M1["InMapToProduct(startX, startY, excludeX, excludeY)"]
M2["InMap() - Full Warehouse Map"]
T1["Movement Time: 1 second per grid"]
T2["Z-axis Time: 2 seconds per level"]
T3["Loading Time: INTERACTION constant"]

P1 --> M1
P2 --> M1
P3 --> M2
P4 --> M2
P5 --> M1
P3 --> T2
P2 --> T3

subgraph subGraph2 ["Time Calculations"]
    T1
    T2
    T3
end

subgraph subGraph1 ["Map Types"]
    M1
    M2
end

subgraph subGraph0 ["Path Planning Phases"]
    P1
    P2
    P3
    P4
    P5
    P1 --> P2
    P2 --> P3
    P3 --> P4
    P4 --> P5
end
```

**Key Path Planning Constants:**

* `beginX = 11, beginY = 24, beginZ = 1`: Entry point coordinates
* `endX = 11, endY = 12, endZ = 1`: Exit point coordinates
* `maxBordX = 48, maxBordY = 34`: Warehouse boundaries
* `INTERACTION`: Loading/unloading time constant

Sources: [src/main/java/com/xhz/yuncang/controller/InboundOrderController.java L361-L369](https://github.com/yanzhe-Xiao/yuncang/blob/a4a28616/src/main/java/com/xhz/yuncang/controller/InboundOrderController.java#L361-L369)

 [src/main/java/com/xhz/yuncang/controller/InboundOrderController.java L642-L729](https://github.com/yanzhe-Xiao/yuncang/blob/a4a28616/src/main/java/com/xhz/yuncang/controller/InboundOrderController.java#L642-L729)

### Conflict Resolution

The system implements configurable conflict resolution for multiple AGV cars:

```mermaid
stateDiagram-v2
    [*] --> CheckConflictSetting
    CheckConflictSetting --> ProcessConflicts : "dealing="是"
    CheckConflictSetting --> IgnoreConflicts : "dealing="否"
    ProcessConflicts --> CheckTimeSlot : "dealing="是"
    CheckTimeSlot --> ConflictFound : "Position occupied"
    CheckTimeSlot --> AssignPath : "Position free"
    ConflictFound --> DelayStart : "startTime++"
    DelayStart --> CheckTimeSlot : "Position occupied"
    AssignPath --> UpdateOccupancy : "Position free"
    UpdateOccupancy --> [*]
    IgnoreConflicts --> SequentialAssignment
    SequentialAssignment --> [*]
```

**Conflict Resolution Data Structures:**

* `Map<String, Integer> carLastEndTime`: Tracks when each car finishes
* `Map<Integer, Set<String>> occupied`: Maps time slots to occupied positions
* Position encoding: `"x,y"` string format

Sources: [src/main/java/com/xhz/yuncang/controller/InboundOrderController.java L758-L832](https://github.com/yanzhe-Xiao/yuncang/blob/a4a28616/src/main/java/com/xhz/yuncang/controller/InboundOrderController.java#L758-L832)

 [src/main/java/com/xhz/yuncang/controller/OutboundOrderController.java L646-L714](https://github.com/yanzhe-Xiao/yuncang/blob/a4a28616/src/main/java/com/xhz/yuncang/controller/OutboundOrderController.java#L646-L714)

## Data Flow and State Management

### Order Processing Data Flow

```mermaid
flowchart TD

DTO1["InboundOrderDTO"]
DTO2["OutDTO"]
DTO3["InboundOrderDetailAddDTO"]
CTRL1["InboundOrderController"]
CTRL2["OutboundOrderController"]
CTRL3["InboundOrderDetailController"]
ALLOC["Shelf Allocation Algorithm"]
PATH["Path Planning Algorithm"]
OPT["Picking Optimization"]
UPD1["Update InboundOrder.status"]
UPD2["Update ShelfInventory.quantity"]
UPD3["Update Inventory.quantity"]
UPD4["Update OutboundOrder.status"]

DTO1 --> CTRL1
DTO2 --> CTRL2
DTO3 --> CTRL3
CTRL1 --> ALLOC
CTRL2 --> OPT
PATH --> UPD1
PATH --> UPD2
PATH --> UPD3
CTRL2 --> UPD4

subgraph subGraph3 ["Data Updates"]
    UPD1
    UPD2
    UPD3
    UPD4
end

subgraph subGraph2 ["Business Logic"]
    ALLOC
    PATH
    OPT
    ALLOC --> PATH
    OPT --> PATH
end

subgraph subGraph1 ["Processing Layer"]
    CTRL1
    CTRL2
    CTRL3
end

subgraph subGraph0 ["Input Layer"]
    DTO1
    DTO2
    DTO3
end
```

**Status Constants:**

* `Constants.STATUS_ORDER_TOSTART`: "未开始"
* `Constants.STATUS_ORDER_FINISHED`: "已完成"

Sources: [src/main/java/com/xhz/yuncang/controller/InboundOrderController.java L844-L900](https://github.com/yanzhe-Xiao/yuncang/blob/a4a28616/src/main/java/com/xhz/yuncang/controller/InboundOrderController.java#L844-L900)

 [src/main/java/com/xhz/yuncang/controller/OutboundOrderController.java L725-L747](https://github.com/yanzhe-Xiao/yuncang/blob/a4a28616/src/main/java/com/xhz/yuncang/controller/OutboundOrderController.java#L725-L747)

## API Endpoints Summary

### Inbound Order Endpoints

| Method | Endpoint | Purpose | Key Response Objects |
| --- | --- | --- | --- |
| POST | `/inbound` | Create inbound order | `AjaxResult` |
| DELETE | `/inbound/{id}` | Delete inbound order | `AjaxResult` |
| GET | `/inbound` | List orders (paginated) | `PageVo<InboundOrderVo>` |
| GET | `/path/{id}` | Execute inbound with DB updates | `List<CarStorageToShelf>` |
| GET | `/in/{id}` | Preview inbound paths | `List<CarStorageToShelf>` |

### Outbound Order Endpoints

| Method | Endpoint | Purpose | Key Response Objects |
| --- | --- | --- | --- |
| GET | `/outboundorder/{pageNo}` | List outbound orders | `PageVo<OutboundOrderInfoVo>` |
| PUT | `/out/{id}` | Execute outbound with DB updates | `List<CarOut>` |
| GET | `/out/{id}` | Preview outbound paths | `List<CarOut>` |

### Order Detail Endpoints

| Method | Endpoint | Purpose | Key Response Objects |
| --- | --- | --- | --- |
| POST | `/inboundOrderDetail/{pageNo}` | List order details | `PageVo<InboundOrderDetailInfoVo>` |
| POST | `/inboundOrderDetail/add` | Add order detail | `AjaxResult` |
| POST | `/inboundOrderDetail/update` | Update order detail | `AjaxResult` |
| POST | `/inboundOrderDetail/remove` | Remove order detail | `AjaxResult` |
| POST | `/inboundOrderDetail/findByOrderNumbers` | Batch query details | `List<InboundOrderDetailInfoVo>` |

Sources: [src/main/java/com/xhz/yuncang/controller/InboundOrderController.java L160-L274](https://github.com/yanzhe-Xiao/yuncang/blob/a4a28616/src/main/java/com/xhz/yuncang/controller/InboundOrderController.java#L160-L274)

 [src/main/java/com/xhz/yuncang/controller/OutboundOrderController.java L102-L795](https://github.com/yanzhe-Xiao/yuncang/blob/a4a28616/src/main/java/com/xhz/yuncang/controller/OutboundOrderController.java#L102-L795)

 [src/main/java/com/xhz/yuncang/controller/InboundOrderDetailController.java L98-L354](https://github.com/yanzhe-Xiao/yuncang/blob/a4a28616/src/main/java/com/xhz/yuncang/controller/InboundOrderDetailController.java#L98-L354)