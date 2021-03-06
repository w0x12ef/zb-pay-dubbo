/*
 * ====================================================================
 * 【个人网站】：http://www.2b2b92b.com
 * 【网站源码】：http://git.oschina.net/zhoubang85/zb
 * 【技术论坛】：http://www.2b2b92b.cn
 * 【开源中国】：https://gitee.com/zhoubang85
 *
 * 【支付-微信_支付宝_银联】技术QQ群：470414533
 * 【联系QQ】：842324724
 * 【联系Email】：842324724@qq.com
 * ====================================================================
 */
package pers.zb.pay.app.message;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import pers.zb.pay.app.message.scheduled.MessageScheduled;


/**
 * 消息处理定时器<br/>
 * 主要分为两步： MessageStatusEnum <br/>
 * 1.处理状态为“待确认”但已超时的消息 <br/>
 * 2.处理状态为“发送中”但超时没有被成功消费确认的消息 <br/>
 */
public class MessageTask_Main {

	private static final Log log = LogFactory.getLog(MessageTask_Main.class);

	private MessageTask_Main() {

	}

	public static void main(String[] args) {

		try {

			@SuppressWarnings("resource")
			final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[] { "spring-context.xml" });
			context.start();
			log.info("定时任务开始执行>>>");
			final MessageScheduled settScheduled = (MessageScheduled) context.getBean("messageScheduled");
			ThreadPoolTaskExecutor threadPool = (ThreadPoolTaskExecutor) context.getBean("threadPool");

			// 开一个子线程处理状态为“待确认”但已超时的消息.
			threadPool.execute(new Runnable() {
				@Override
				public void run() {
					while (true) {

						log.info("执行(处理[waiting_confirm]状态的消息)任务开始");
						settScheduled.handleWaitingConfirmTimeOutMessages();
						log.info("执行(处理[waiting_confirm]状态的消息)任务结束");
						
						try {
							log.info("[waiting_confirm]睡眠60秒");
							Thread.sleep(60000);
						} catch (InterruptedException e) {
						}
					}
				}
			});

			// 开一个子线程处理状态为“发送中”但超时没有被成功消费确认的消息
			threadPool.execute(new Runnable() {
				@Override
				public void run() {
					while (true) {
						log.info("执行(处理[SENDING]的消息)任务开始");
						settScheduled.handleSendingTimeOutMessage();
						log.info("执行(处理[SENDING]的消息)任务结束");
						try {
							log.info("[SENDING]睡眠60秒");
							Thread.sleep(60000);
						} catch (InterruptedException e) {
						}
					}
				}
			});

		} catch (Exception e) {
			log.error("[zb-pay-app-message] ===>DubboReference context start error:", e);
		}
	}
}
