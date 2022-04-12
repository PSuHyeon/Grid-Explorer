package test_motor;

import java.util.*;

import lejos.robotics.Color;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;
import lejos.hardware.BrickFinder;
import lejos.hardware.Keys;
import lejos.hardware.ev3.EV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.Motor;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3IRSensor;

public class test_motor {
   private static EV3IRSensor distance_sensor;
    static EV3ColorSensor color_sensor_left;
    static EV3ColorSensor color_sensor_right;
    static RegulatedMotor leftMotor = Motor.D;
    static RegulatedMotor rightMotor = Motor.A;
    public static float maxsp = leftMotor.getMaxSpeed();
    static int speed = 500;
    static int speed2 = 500;
    static int delay = (int) (915200/500);
    static int delay2 = (int) (915200/500);
    static int turnDelay = (int) (435300/500);
    static int turnDelayL = (int) (435300/500);
    static int alignCount = 0;
    static EV3 ev3 = (EV3) BrickFinder.getLocal();
    static Keys keys = ev3.getKeys();
    
    static class Pair {
        int x, y;

        Pair(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object object) {
            Pair pair = (Pair) object;
            // name은 상관없이, id만 같으면 true를 리턴합니다.
            if (pair.x == this.x && pair.y == this.y) {
                return true;
            }
            return false;
        }

    }

    static int curX = 0;
    static int curY = 0;
    static int forwardCnt = 0;
    static int turnCnt = 0;
    static char curDir = 'E';
    static ArrayList<Pair> unVisitedSet = new ArrayList<Pair>();
    static ArrayList<Pair> visitedSet = new ArrayList<Pair>();
    static ArrayList<Pair> redSet = new ArrayList<Pair>();// 아직 안만듦
    static ArrayList<Pair> blockSet = new ArrayList<Pair>();
    static ArrayList<Pair> boxes = new ArrayList<Pair>();
    static ArrayList<Pair> redCells = new ArrayList<Pair>();
    static ArrayList<Pair> initialPairs = new ArrayList<Pair>();

    public static ArrayList<Pair> pickRandoms() {
        ArrayList<Pair> pairs = new ArrayList<Pair>();
        int red1x = (int) (Math.random()*6);
        int red1y = (int) (Math.random()*4);
        int red2x = (int) (Math.random()*6);
        int red2y = (int) (Math.random()*4);
        int box1x = (int) (Math.random()*6);
        int box1y = (int) (Math.random()*4);
        int box2x = (int) (Math.random()*6);
        int box2y = (int) (Math.random()*4); 
        while (red1x == red2x && red2y == red1y){
            red1y = (int) (Math.random()*6);
            red2y = (int) (Math.random()*4);
        }
        while ((red1x == box1x && red1y == box1y) || (red2x == box1x && red2y == box1y) || (box1x == 0 && box1y == 0)){
            box1x = (int) (Math.random()*6);
            box1y = (int) (Math.random()*4); 
        }

        while ((red1x == box2x && red1y == box2y) || (red2x == box2x && red2y == box2y) || (box1x == box2x && box1y == box2y) || (box2x == 0 && box2y == 0)){
            box2x = (int) (Math.random()*6);
            box2y = (int) (Math.random()*4); 
        }
        

        Pair box1 = new Pair(box1x,box1y);
        Pair box2 = new Pair(box2x,box2y);
        Pair red1 = new Pair(red1x, red1y);
        Pair red2 = new Pair(red2x, red2y);
        pairs.add(box1);
        pairs.add(box2);
        pairs.add(red1);
        pairs.add(red2);
        
        return pairs;
    }
    
    public static void goForward(RegulatedMotor x, RegulatedMotor y) {
        TextLCD lcd = ev3.getTextLCD();
       forwardCnt+=1;
        leftMotor.setSpeed(speed);
        rightMotor.setSpeed(speed);
        int removeIndex = unVisitedSet.indexOf(new Pair(curX, curY));
        if (removeIndex != -1) {
            unVisitedSet.remove(removeIndex);
            visitedSet.add(new Pair(curX, curY));
        }
        checkColor();
        // 방향에 따른 좌표 변화
        switch (curDir) {
            case 'E':
                curX++;
                break;
            case 'W':
                curX--;
                break;
            case 'S':
                curY--;
                break;
            case 'N':
                curY++;
                break;
        }

        y.backward();
        x.backward();
        Delay.msDelay(delay);
        x.stop(true);
        y.stop(true);
        Delay.msDelay(500);
        alignCount = alignCount + 1;
    
        if (alignCount == 4) {
            int leftColor = color_sensor_left.getColorID();
            int rightColor = color_sensor_right.getColorID();
        	while (leftColor != Color.BLACK) {
        		leftMotor.setSpeed(10);
                x.backward();
                Delay.msDelay(10);
                leftColor = color_sensor_left.getColorID();
        	}
            lcd.drawString("left black", 1, 4);
            x.stop(true);
        	while (rightColor != Color.BLACK) {
        		rightMotor.setSpeed(10);
                y.backward();
                Delay.msDelay(10);
                rightColor = color_sensor_right.getColorID();
        	}
            lcd.drawString("right black", 1, 4);
            y.stop(true);
            alignCount = 0;
        }

    } 

    public static void checkColor() {
        EV3 ev3 = (EV3) BrickFinder.getLocal();
        TextLCD lcd = ev3.getTextLCD();
        lcd.clear();
        int id = color_sensor_left.getColorID();
        if (id == Color.RED) {
            // lcd.drawString("red", 1, 4);
            Pair nowPos = new Pair(curX, curY);
            if (redCells.contains(nowPos)) {
                redSet.add(nowPos);
            }
            System.out.printf("(%d,%d,R)\n", curX, curY);
        } else {
            // lcd.drawString("hello world", 1, 4);
        }
//       Pair nowPos = new Pair(curX, curY);
//        if (redCells.contains(nowPos)) {
//            redSet.add(nowPos);
//        }
    }

    public static void returnHome() {
       initializePairs();
        ArrayList<Pair> newVisited = new ArrayList<Pair>();
        visitedSet= newVisited;

        int restrictCnt = 0;
        while(curX!=0 || curY!=0){
            if(restrictCnt >= 30){
                System.out.printf("return BREAK!!!!!!!!!!!!!!!!!!!\n");
                System.exit(1);
            }
            if(!strictCheck()){
                looseCheck();
            }
            restrictCnt+=1;
        }
        return;
    }

    public static void turnRight(RegulatedMotor x, RegulatedMotor y) {
        switch (curDir) {
            case 'E':
                curDir = 'S';
                break;
            case 'W':
                curDir = 'N';
                break;
            case 'S':
                curDir = 'W';
                break;
            case 'N':
                curDir = 'E';
                break;
        }
        // x.rotate(+1170);
        // y.rotate(-1170);

        y.backward();
        x.backward();
        Delay.msDelay(500);
        y.stop(true);
        x.stop(true);
        Delay.msDelay(500);
        
        x.forward();
        y.backward();
        Delay.msDelay(turnDelayL);
        x.stop(true);
        y.stop(true);
        Delay.msDelay(500);

        x.forward();
        y.forward();
        Delay.msDelay(500);
        x.stop(true);
        y.stop(true);
        Delay.msDelay(500);
//        
//      System.out.printf("%f\n", maxsp);
//      Delay.msDelay(1000);

    }

    //it turns a little bit more, so need to change if needed.
    public static void turnLeft(RegulatedMotor x, RegulatedMotor y) {
        switch (curDir) {
            case 'E':
                curDir = 'N';
                break;
            case 'W':
                curDir = 'S';
                break;
            case 'S':
                curDir = 'E';
                break;
            case 'N':
                curDir = 'W';
                break;
        }
        
        y.backward();
        x.backward();
        Delay.msDelay(500);
        y.stop(true);
        x.stop(true);
        Delay.msDelay(500);
                
        y.forward();
        x.backward();
        Delay.msDelay(turnDelayL);
        y.stop(true);
        x.stop(true);
        Delay.msDelay(500);
        
        x.forward();
        y.forward();
        Delay.msDelay(500);
        x.stop(true);
        y.stop(true);
        Delay.msDelay(500);
        
//        System.out.printf("%f", maxsp);
//        Delay.msDelay(1000);
    }

    public static void initializePairs() {
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 4; j++) {
                Pair newCoor = new Pair(i, j);
                unVisitedSet.add(newCoor);
            }
        }
        return;
    }

    public static boolean distanceCheck() {
        // 앞에 박스가 없으면 true, 있으면 false
        SampleProvider distanceMode = distance_sensor.getMode("Distance");
        float value[] = new float[distanceMode.sampleSize()];

        distanceMode.fetchSample(value, 0);
        float centimeter = value[0];
        if(centimeter < 53.0){
            return false;
        }
        else{
            return true;
        }
    }

    public static Pair getNextPos() {
        int nextPosX = curX;
        int nextPosY = curY;
        switch (curDir) {
            case 'E':
                nextPosX = curX + 1;
                break;
            case 'W':
                nextPosX = curX - 1;
                break;
            case 'S':
                nextPosY = curY - 1;
                break;
            case 'N':
                nextPosY = curY + 1;
                break;
        }
        Pair nextPos = new Pair(nextPosX, nextPosY);
        return nextPos;
    }
    public static Pair getLeftPos() {
        int leftPosX = curX;
        int leftPosY = curY;
        switch (curDir) {
            case 'E':
                leftPosY = curY + 1;
                break;
            case 'W':
                leftPosY = curY - 1;
                break;
            case 'S':
                leftPosX = curX + 1;
                break;
            case 'N':
                leftPosX = curX - 1;
                break;
        }
        Pair leftPos = new Pair(leftPosX, leftPosY);
        return leftPos;
    }
    public static Pair getRightPos() {
        int rightPosX = curX;
        int rightPosY = curY;
        switch (curDir) {
            case 'E':
                rightPosY = curY - 1;
                break;
            case 'W':
                rightPosY = curY + 1;
                break;
            case 'S':
                rightPosX = curX - 1;
                break;
            case 'N':
                rightPosX = curX + 1;
                break;
        }
        Pair rightPos = new Pair(rightPosX, rightPosY);
        return rightPos;
    }


    public static boolean isAvailableStrict(Pair nextPos) {
        if (nextPos.x > 5 || nextPos.x < 0) {
            return false;
        }
        if (nextPos.y > 3 || nextPos.y < 0) {
            return false;
        }
        if (visitedSet.contains(new Pair(nextPos.x, nextPos.y)) || blockSet.contains(new Pair(nextPos.x, nextPos.y))) {
            return false;
        }

        return true;
    }

    public static boolean isAvailableLoose(Pair nextPos) {
        if (nextPos.x > 5 || nextPos.x < 0) {
            return false;
        }
        if (nextPos.y > 3 || nextPos.y < 0) {
            return false;
        }
        if (blockSet.contains(new Pair(nextPos.x, nextPos.y))) {
            return false;
        }

        return true;
    }

    public static boolean strictCheck() {
        // strictCheck에 통과(갈 수 있는 칸이 존재)면 true
        // 정면
        Pair nextPos = getNextPos();
        Pair leftPos = getLeftPos();
        Pair rightPos = getRightPos();
        if (isAvailableStrict(nextPos)) {
            if (!distanceCheck()) {
                // box pos 추가
                if(!blockSet.contains(nextPos)){
                    System.out.printf("(%d,%d,B)\n",nextPos.x,nextPos.y);
                    blockSet.add(nextPos);
                }
            }
            else{
                goForward(leftMotor, rightMotor);
                return true;
            }
        }
        // 왼쪽
        if (isAvailableStrict(leftPos)) {
            turnLeft(leftMotor, rightMotor);
            if (!distanceCheck()) {
                // box pos 추가
                if(!(blockSet.contains(leftPos))){
                    System.out.printf("(%d,%d,B)\n",leftPos.x,leftPos.y);
                    blockSet.add(leftPos);
                }
                turnRight(leftMotor, rightMotor);
            }
            else{
                goForward(leftMotor, rightMotor);
                return true;
            }
        }
        // 오른쪽
        if (isAvailableStrict(rightPos)) {
            // System.out.println("strict right");
            turnRight(leftMotor, rightMotor);
            if (!distanceCheck()) {
                // box pos 추가
                if(!(blockSet.contains(rightPos))){
                    System.out.printf("(%d,%d,B)\n",rightPos.x,rightPos.y);
                    blockSet.add(rightPos);
                }
                turnLeft(leftMotor, rightMotor);
            }
            else{
                goForward(leftMotor, rightMotor);
                return true;
            }
        }
        return false;
    }

    public static boolean looseCheck() {
        // looseCheck에 통과(갈 수 있는 칸이 존재)면 true
        // 정면
        Pair nextPos = getNextPos();
        Pair leftPos = getLeftPos();
        Pair rightPos = getRightPos();
        if (isAvailableLoose(nextPos)) {
            if (!distanceCheck()) {
                // box pos 추가
                if(!blockSet.contains(nextPos)){
                    System.out.printf("(%d,%d,B)\n",nextPos.x,nextPos.y);
                    blockSet.add(nextPos);
                }
            }
            else{
                goForward(leftMotor, rightMotor);
                return true;
            }
        }

        if(curDir=='N' || curDir=='W'){
            if(isAvailableLoose(leftPos)){
                turnLeft(leftMotor, rightMotor);
                if (!distanceCheck()) {
                    // box pos 추가
                    if(!(blockSet.contains(leftPos))){
                        System.out.printf("(%d,%d,B)\n",leftPos.x,leftPos.y);
                        blockSet.add(leftPos);
                    }
                    turnRight(leftMotor, rightMotor);
                }
                else{
                    goForward(leftMotor, rightMotor);
                    return true;
                }
            }
            if(isAvailableLoose(rightPos)){
                turnRight(leftMotor, rightMotor);
                if (!distanceCheck()) {
                    // box pos 추가
                    if(!(blockSet.contains(rightPos))){
                        System.out.printf("(%d,%d,B)\n",rightPos.x,rightPos.y);
                        blockSet.add(rightPos);
                    }
                    turnLeft(leftMotor, rightMotor);
                }
                else{
                    goForward(leftMotor, rightMotor);
                    return true;
                }
            }
            turnRight(leftMotor, rightMotor);
            turnRight(leftMotor, rightMotor);
            goForward(leftMotor, rightMotor);
        }
        else{
            if(isAvailableLoose(rightPos)){
                turnRight(leftMotor, rightMotor);
                if (!distanceCheck()) {
                    // box pos 추가
                    if(!(blockSet.contains(rightPos))){
                        System.out.printf("(%d,%d,B)\n",rightPos.x,rightPos.y);
                        blockSet.add(rightPos);
                    }
                    turnLeft(leftMotor, rightMotor);
                }
                else{
                    goForward(leftMotor, rightMotor);
                    return true;
                }
            }
            if(isAvailableLoose(leftPos)){
                turnLeft(leftMotor, rightMotor);
                if (!distanceCheck()) {
                    // box pos 추가
                    if(!(blockSet.contains(leftPos))){
                        System.out.printf("(%d,%d,B)\n",leftPos.x,leftPos.y);
                        blockSet.add(leftPos);
                    }
                    turnRight(leftMotor, rightMotor);
                }
                else{
                    goForward(leftMotor, rightMotor);
                    return true;
                }
            }
            turnRight(leftMotor, rightMotor);
            turnRight(leftMotor, rightMotor);
            goForward(leftMotor, rightMotor);
        }
        return true;
    }

    public static void main(String[] args) {
       
       distance_sensor = new EV3IRSensor(SensorPort.S1);
       color_sensor_right = new EV3ColorSensor(SensorPort.S2);
       color_sensor_left = new EV3ColorSensor(SensorPort.S3);

       initializePairs();
        while (!unVisitedSet.isEmpty() && !(redSet.size()>=2 && blockSet.size()>=2)) {
            try {
                Thread.sleep(50); // 1초 대기
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (!strictCheck()) {
                looseCheck();
            }
        }
        returnHome();
    }
}