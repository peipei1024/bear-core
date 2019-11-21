package com.bear.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.NetworkInterface;

/**
 * @author shomop
 * @date 2019/7/1 11:44
 */
public class IdWorker {
    private static Logger logger = LoggerFactory.getLogger(IdWorker.class);

    /**
     * 开始时间戳 (2017-07-01 00:00:00)
     */
    private final long startTime = 1498838400000L;

    /**
     * 机器id所占的位数
     */
    private final long machineIdBits = 8L;

    /**
     * 服务id所占的位数
     */
    private final long serviceIdBits = 4L;

    /**
     * 支持的最大机器id，结果是255
     */
    private final long maxMachineId = ~(-1L << machineIdBits);

    /**
     * 支持的最大服务标识id，结果是15
     */
    private final long maxServiceId = ~(-1L << serviceIdBits);

    /**
     * 序列在id中占的位数
     */
    private final long sequenceBits = 10L;

    /**
     * 机器ID向左移10位
     */
    private final long machineIdShift = sequenceBits;

    /**
     * 服务id向左移18位(10+8)
     */
    private final long serviceIdShift = sequenceBits + machineIdBits;

    /**
     * 时间截向左移22位(4+8+10)
     */
    private final long timestampLeftShift = sequenceBits + machineIdBits + serviceIdBits;

    /**
     * 生成序列的掩码，这里为1023
     */
    private final long sequenceMask = ~(-1L << sequenceBits);

    /**
     * 工作机器ID(0~255)
     */
    private long machineId;

    /**
     * 服务ID(0~15)
     */
    @Value("${idWorker.serviceId}")
    private Long serviceId;

    /**
     * 毫秒内序列(0~1023)
     */
    private long sequence = 0L;

    /**
     * 上次生成ID的时间戳
     */
    private long lastTimestamp = -1L;


    @PostConstruct
    public void init() {
        //初始化机器ID
        try {
            machineId = 1;
        } catch (Exception e) {
            logger.error("======机器ID获取失败!=======", e);
            throw new RuntimeException("机器ID获取失败!");
        }
        if (serviceId == null) {
            throw new RuntimeException("服务ID获取失败!");
        }
        if (machineId > maxMachineId || machineId < 0) {
            throw new IllegalArgumentException(String.format("机器ID不能超过%d或小于0", maxMachineId));
        }
        if (serviceId > maxServiceId || serviceId < 0) {
            throw new IllegalArgumentException(String.format("服务ID不能超过%d或小于0", maxServiceId));
        }

        logger.info("======ID生成器初始化开始=======");
        logger.info("machineId:" + machineId);
        logger.info("serviceId:" + serviceId);
        logger.info("======ID生成器初始化结束=======");
    }

    /**
     * 获得下一个ID (该方法是线程安全的)
     *
     * @return SnowflakeId
     */
    public synchronized long nextId() {
        long timestamp = timeGen();
        //如果当前时间小于上一次ID生成的时间戳，等待当前时间大于上次的时间戳
        while (timestamp < lastTimestamp) {
            try {
                logger.warn("系统时间出现回退,等待" + (lastTimestamp - timestamp) + "毫秒后继续");
                Thread.sleep(lastTimestamp - timestamp);
                timestamp = timeGen();
            } catch (InterruptedException e) {
                logger.error("系统时间被回退,等待时出现异常", e);
                throw new RuntimeException("系统时间被回退,等待时出现异常");
            }
        }

        //如果是同一时间生成的，则进行毫秒内序列
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;
            //毫秒内序列溢出
            if (sequence == 0) {
                //阻塞到下一个毫秒,获得新的时间戳
                timestamp = tilNextMillis(lastTimestamp);
            }
        }
        //时间戳改变，毫秒内序列重置
        else {
            sequence = 0L;
        }

        //上次生成ID的时间戳
        lastTimestamp = timestamp;

        //移位并通过或运算拼到一起组成64位的ID
        return ((timestamp - startTime) << timestampLeftShift)
                | (serviceId << serviceIdShift)
                | (machineId << machineIdShift)
                | sequence;

    }

    /**
     * 阻塞到下一个毫秒，直到获得新的时间戳
     *
     * @param lastTimestamp 上次生成ID的时间戳
     * @return 当前时间戳
     */
    private long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    /**
     * 返回以毫秒为单位的当前时间
     *
     * @return 当前时间(毫秒)
     */
    private long timeGen() {
        return System.currentTimeMillis();
    }


    private static String getMachineMac() throws Exception {
        InetAddress ia = InetAddress.getLocalHost();
        //获取网卡，获取地址
        byte[] mac = NetworkInterface.getByInetAddress(ia).getHardwareAddress();
        StringBuilder sb = new StringBuilder("");
        for (int i = 0; i < mac.length; i++) {
            if (i != 0) {
                sb.append("-");
            }
            //字节转换为整数
            int temp = mac[i] & 0xff;
            String str = Integer.toHexString(temp);
            if (str.length() == 1) {
                sb.append("0").append(str);
            } else {
                sb.append(str);
            }
        }
        return sb.toString().toUpperCase();
    }

    private Long getMachineId() {
        return 6L;
    }

}
