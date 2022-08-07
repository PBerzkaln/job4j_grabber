package ru.job4j.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;
import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;

public class AlertRabbit {
    /**Создает объект Connection (поодключение к БД).
     *Создает объект Scheduler (планировщик) управляющий всеми работами.
     *Создает объект Rabbit с типом org.quartz.Job (задачу)
     *с параметрами data, в них передается ссылка на Connection.
     *Создает объект SimpleScheduleBuilder (расписание)
     *с периодичным и бесконечным повтором.
     *Создает объект Trigger, в который передается расписание
     *и указывается когда начинать запуск.
     *Загружает задачу и тригер в планировщик.
     *Метод работает 10 сек.
     * @param args
     */
    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        Properties properties = getProperties();
        int interval = Integer.parseInt(properties.getProperty("rabbit.interval"));
        Class.forName(getProperties().getProperty("driver-class-name"));
        try (Connection cn = DriverManager.getConnection(
                properties.getProperty("url"),
                properties.getProperty("username"),
                properties.getProperty("password")
        )) {
            try {
                Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
                scheduler.start();
                JobDataMap data = new JobDataMap();
                data.put("cn", cn);
                JobDetail job = newJob(Rabbit.class)
                        .usingJobData(data)
                        .build();
                SimpleScheduleBuilder times = simpleSchedule()
                        .withIntervalInSeconds(interval)
                        .repeatForever();
                Trigger trigger = newTrigger()
                        .startNow()
                        .withSchedule(times)
                        .build();
                scheduler.scheduleJob(job, trigger);
                Thread.sleep(10000);
                scheduler.shutdown();
            } catch (SchedulerException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**Выполняет работу (выводит на консоль String).
     *По ключу из контекста использует подключение к БД.
     *Добавляет время выполнения работу в БД.
     */
    public static class Rabbit implements Job {
        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            System.out.println("Rabbit runs here ...");
            Connection cn = (Connection) context.getJobDetail().getJobDataMap().get("cn");
            try (PreparedStatement statement = cn.prepareStatement(
                    "INSERT INTO rabbit(created_date) VALUES (?)")) {
                statement.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
                statement.execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**Читает файл конфигурации rabbit.properties.
     * @return параметры для connection и периодичность для job
     */
    public static Properties getProperties() {
        Properties properties = new Properties();
        try (InputStream fis = AlertRabbit.class.getClassLoader()
                .getResourceAsStream("rabbit.properties")) {
            properties.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }
}