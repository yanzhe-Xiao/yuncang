package com.xhz.yuncang.utils;

import com.xhz.yuncang.entity.ShelfInventory;
import com.xhz.yuncang.vo.path.Point;

import java.util.*;

public class BfsFindPath {

    private static final int maxBordY =34;

    private static final int maxBordX= 48;

    public static int[][] InMap(){
        int[][] map = new int[maxBordY+1][maxBordX+1]; // 地图高度是34（Y轴），宽度是48（X轴）
// 默认初始化为可通行区域
        for (int x=0;x<=maxBordX;x++){
            map[0][x]=Constants.PATH_CANNOT_GO;
        }
        for (int y=0;y<=maxBordY;y++){
            map[y][0]=Constants.PATH_CANNOT_GO;
        }
        System.out.println("======================");
        for (int y = 1; y <= maxBordY; y++) {
            for (int x = 1; x <= maxBordX; x++) {
                map[y][x] = Constants.PATH_CAN_GO; // 可通行 0
            }
        }

        // 黑色墙体
        for (int y = 1; y <= maxBordY; y++) {
//                map[y][11] = Constants.PATH_CANNOT_GO; // 墙体，不可通行1
//                map[y][10] = Constants.PATH_CANNOT_GO; // 墙体，不可通行1
            for (int x=0;x<=11;x++){
                map[y][x]=Constants.PATH_CANNOT_GO;
            }
        }
        // 设置货架区域（绿色区域）→ 不可通行
        int[] shelfX = {14, 18, 22, 26, 30, 34, 38, 42, 46}; // 每列货架起点
        for (int x : shelfX) {
            for (int dx = 0; dx <= 1; dx++) { // 每列宽 2 格
                for (int y = 1; y <= 34; y++) { // 垂直布满（上下各留1行）
                    map[y][x + dx] = Constants.PATH_CANNOT_GO; // 货架格子，不可通行
                }
            }
        }
        for (int x=1;x<=maxBordX;x++){
            map[11][x]=Constants.PATH_CAN_GO;
            map[12][x]=Constants.PATH_CAN_GO;
            map[23][x]=Constants.PATH_CAN_GO;
            map[24][x]=Constants.PATH_CAN_GO;
        }
        for (int y = 34; y >= 1; y--) {
            for (int x = 1; x <= 48; x++) {
                System.out.print(map[y][x]);
            }
            System.out.println();
        }
        return map;
    }

    public static int[][] InMapToProduct(int beginX,int beginY,int stopX,int stopY){
        int[][] map = new int[maxBordY+1][maxBordX+1]; // 地图高度是34（Y轴），宽度是48（X轴）
// 默认初始化为可通行区域
        for (int x=0;x<=maxBordX;x++){
            map[0][x]=Constants.PATH_CANNOT_GO;
        }
        for (int y=0;y<=maxBordY;y++){
            map[y][0]=Constants.PATH_CANNOT_GO;
        }
        System.out.println("======================");
        for (int y = 1; y <= maxBordY; y++) {
            for (int x = 1; x <= maxBordX; x++) {
                map[y][x] = Constants.PATH_CAN_GO; // 可通行 0
            }
        }
        for (int y=25;y<=32;y++){
            for (int x=3;x<=8;x++){
                map[y][x]=Constants.PATH_CANNOT_GO;
            }
        }

        for (int y=3;y<=10;y++){
            for (int x=3;x<=8;x++){
                map[y][x]=Constants.PATH_CANNOT_GO;
            }
        }
        for (int y=1;y<=10;y++){
            map[y][1]=Constants.PATH_CANNOT_GO;
            map[y][10]=Constants.PATH_CANNOT_GO;
        }
        for (int y=25;y<=34;y++){
            map[y][1]=Constants.PATH_CANNOT_GO;
            map[y][10]=Constants.PATH_CANNOT_GO;
        }
        for (int x=1;x<=10;x++){
            map[1][x]=Constants.PATH_CANNOT_GO;
            map[34][x]=Constants.PATH_CANNOT_GO;
        }
        for (int x=2;x<=8;x+=2){
            map[14][x]=Constants.PATH_CANNOT_GO;
            map[15][x]=Constants.PATH_CANNOT_GO;
            map[17][x]=Constants.PATH_CANNOT_GO;
            map[18][x]=Constants.PATH_CANNOT_GO;
            map[20][x]=Constants.PATH_CANNOT_GO;
            map[21][x]=Constants.PATH_CANNOT_GO;
        }

        // 黑色墙体
        for (int y = 1; y <= maxBordY; y++) {
                map[y][11] = Constants.PATH_CANNOT_GO; // 墙体，不可通行1
        }
        // 设置货架区域（绿色区域）→ 不可通行
        int[] shelfX = {14, 18, 22, 26, 30, 34, 38, 42, 46}; // 每列货架起点
        for (int x : shelfX) {
            for (int dx = 0; dx <= 1; dx++) { // 每列宽 2 格
                for (int y = 1; y <= 34; y++) { // 垂直布满（上下各留1行）
                    map[y][x + dx] = Constants.PATH_CANNOT_GO; // 货架格子，不可通行
                }
            }
        }
        for (int x=1;x<=maxBordX;x++){
            map[11][x]=Constants.PATH_CAN_GO;
            map[12][x]=Constants.PATH_CAN_GO;
            map[23][x]=Constants.PATH_CAN_GO;
            map[24][x]=Constants.PATH_CAN_GO;
        }
        map[beginY][beginX]=Constants.PATH_CAN_GO;
        map[stopY][stopX]=Constants.PATH_CANNOT_GO;
        for (int y = 34; y >= 1; y--) {
            for (int x = 1; x <= 48; x++) {
                System.out.print(map[y][x]);
            }
            System.out.println();
        }
        return map;
    }
    public static List<Point> bfsFindPath(int[][] map, int startX, int startY, int endX, int endY, int startTime) {
        int height = map.length - 1;
        int width = map[0].length - 1;
        boolean[][] visited = new boolean[height + 1][width + 1];
        Map<String, Point> cameFrom = new HashMap<>();

        Queue<Point> queue = new LinkedList<>();
        queue.add(new Point(startX, startY, startTime));
        visited[startY][startX] = true;

        int[][] dirs = {{0,1},{1,0},{0,-1},{-1,0}};

        while (!queue.isEmpty()) {
            Point cur = queue.poll();

            if (cur.getX() == endX && cur.getY() == endY) {
                // 回溯路径
                List<Point> path = new LinkedList<>();
                Point p = cur;
                while (p != null) {
                    path.add(0, p);
                    p = cameFrom.get(p.getX() + "," + p.getY());
                }
                return path;
            }

            for (int[] d : dirs) {
                int nx = cur.getX() + d[0];
                int ny = cur.getY() + d[1];
                if (nx >= 1 && nx <= width && ny >= 1 && ny <= height
                        && !visited[ny][nx] && map[ny][nx] == 0) {
                    visited[ny][nx] = true;
                    Point next = new Point(nx, ny, cur.getTime() + 1);
                    queue.add(next);
                    cameFrom.put(nx + "," + ny, cur);
                }
            }
        }

        throw new RuntimeException("无法从 (" + startX + "," + startY + ") 到 (" + endX + "," + endY + ")");
    }


}
