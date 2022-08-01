package ru.job4j.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;

public class AlertRabbit {
    /**Создает объект Scheduler (планировщик) управляющий всеми работами.
     *Создает объект Rabbit с типом org.quartz.Job (задачу).
     *Создает объект SimpleScheduleBuilder (расписание)
     *с периодичным и бесконечным повтором.
     *Создает объект Trigger, в который передается расписание
     *и указывается когда начинать запуск.
     *Загружает задачу и тригер в планировщик.
     * @param args
     */
    public static void main(String[] args) {
        try {
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            JobDetail job = newJob(Rabbit.class).build();
            SimpleScheduleBuilder times = simpleSchedule()
                    .withIntervalInSeconds(getProperties())
                    .repeatForever();
            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(times)
                    .build();
            scheduler.scheduleJob(job, trigger);
        } catch (SchedulerException se) {
            se.printStackTrace();
        }
    }

    /**В классе описываются требуемые действия задачи.
     */
    public static class Rabbit implements Job {
        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            System.out.println("Rabbit runs here ...");
        }
    }

    /**Читает файл конфигурации rabbit.properties.
     * @return integer значение периодичности повтора действия
     */
    public static Integer getProperties() {
        Properties properties = new Properties();
        try (InputStream fis = new FileInputStream("./src/main/resources/rabbit.properties")) {
            properties.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Integer.parseInt(properties.getProperty("rabbit.interval"));
    }
}